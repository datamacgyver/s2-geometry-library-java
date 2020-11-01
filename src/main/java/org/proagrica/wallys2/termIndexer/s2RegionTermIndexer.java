// Copyright 2017 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS-IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// Author: ericv@google.com (Eric Veach)
//
// Indexing Strategy
// -----------------
//
// Given a query region, we want to find all of the document regions that
// intersect it.  The first step is to represent all the regions as S2Cell
// coverings (see S2RegionCoverer).  We then split the problem into two parts,
// namely finding the document regions that are "smaller" than the query
// region and those that are "larger" than the query region.
//
// We do this by defining two terms for each S2CellId: a "covering term" and
// an "ancestor term".  (In the implementation below, covering terms are
// distinguished by prefixing a '$' to them.)  For each document region, we
// insert a covering term for every cell in the region's covering, and we
// insert an ancestor term for these cells *and* all of their ancestors.
//
// Then given a query region, we can look up all the document regions that
// intersect its covering by querying the union of the following terms:
//
// 1. An "ancestor term" for each cell in the query region.  These terms
//    ensure that we find all document regions that are "smaller" than the
//    query region, i.e. where the query region contains a cell that is either
//    a cell of a document region or one of its ancestors.
//
// 2. A "covering term" for every ancestor of the cells in the query region.
//    These terms ensure that we find all the document regions that are
//    "larger" than the query region, i.e. where document region contains a
//    cell that is a (proper) ancestor of a cell in the query region.
//
// Together, these terms find all of the document regions that intersect the
// query region.  Furthermore, the number of terms to be indexed and queried
// are both fairly small, and can be bounded in terms of max_cells() and the
// number of cell levels used.
//
// Optimizations
// -------------
//
// + Cells at the maximum level being indexed (max_level()) have the special
//   property that they will never be an ancestor of a cell in the query
//   region.  Therefore we can safely skip generating "covering terms" for
//   these cells (see query step 2 above).
//
// + If the index will contain only points (rather than general regions), then
//   we can skip all the covering terms mentioned above because there will
//   never be any document regions larger than the query region.  This can
//   significantly reduce the size of queries.
//
// + If it is more important to optimize index size rather than query speed,
//   the number of index terms can be reduced by creating ancestor terms only
//   for the *proper* ancestors of the cells in a document region, and
//   compensating for this by including covering terms for all cells in the
//   query region (in addition to their ancestors).
//
//   Effectively, when the query region and a document region contain exactly
//   the same cell, we have a choice about whether to treat this match as a
//   "covering term" or an "ancestor term".  One choice minimizes query size
//   while the other minimizes index size.
////
//#include "s2/s2region_term_indexer.h"
//        #include <cctype>
//
//#include "s2/base/logging.h"
//        #include "s2/s1angle.h"
//        #include "s2/s2cap.h"
//        #include "s2/s2cell_id.h"
//        #include "s2/s2region.h"
//        #include "s2/third_party/absl/strings/str_cat.h"

package org.proagrica.wallys2.utils;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2RegionCoverer;

import java.util.ArrayList;

import static org.proagrica.wallys2.converters.wktConverter.WktToS2;
import static org.proagrica.wallys2.converters.wktConverter.makePolygon;


public class s2RegionTermIndexer {
    //See long docstring at the bottom for which of these settings are useful and why
    int minLevel = 18;
    int maxLevel = 25;
    int levelMod = 2; //TODO: This can be set to two if you have very small regions (ie points...or perhaps asapp?)

    //Setting index high and query low will result in fast (yet less accurate) search against a highly accurate index
    //These defaults are here more to give you an idea of what settings work, they can be overwritten when you generate
    //an entry.
    int defaultMaxCellsIdx = 100;
    int defaultMaxCellsQuery = 25;
    int trueMaxLevel;

    S2RegionCoverer coverer = new S2RegionCoverer();

    public s2RegionTermIndexer(){
        initIndexers();
    }

    public s2RegionTermIndexer(int minL, int maxL, int levelM) {
        maxLevel = maxL;
        minLevel = minL;
        levelMod = levelM;  //TODO: This can be set to two if you have very small regions (ie points...or perhaps asapp?)
        initIndexers();
    }

    void initIndexers(){
        coverer.setMaxCells(defaultMaxCellsIdx);
        coverer.setMaxLevel(maxLevel);
        coverer.setMinLevel(minLevel);
        coverer.setLevelMod(levelMod);
    }

    public String GetTerm(boolean ancestorType, S2CellId id) {
        // There are generally more ancestor terms than covering terms, so we add       
        // the extra "marker" character to the covering terms to distinguish them.
        //TODO: I've added level here, not sure why
        if (ancestorType) {
            return (id.toToken()); //id.level() + "-" +
        } else { //is covering type
            return ("$" + id.toToken()); // + id.level() + "-"
        }
    }

//TODO: You could use this as a cell union is a region
//        vector<string> S2RegionTermIndexer::GetIndexTerms(const S2Region& region,
//        string_view prefix) {
//        // Note that options may have changed since the last call.
//        *coverer_.mutable_options() = options_;
//        S2CellUnion covering = coverer_.GetCovering(region);
//        return GetIndexTermsForCanonicalCovering(covering, prefix);
//        }

        public ArrayList<String> GetIndexTermsForCanonicalCovering(String wkt, int maxCells) {
            S2Polygon poly = makePolygon(wkt);
            return GetIndexTermsForCanonicalCovering(poly, maxCells);
        }

        public ArrayList<String> GetIndexTermsForCanonicalCovering(S2Polygon poly, int maxCells) {
            if (maxCells != 0) {
                coverer.setMaxLevel(maxCells);
            } else {
                coverer.setMaxLevel(defaultMaxCellsIdx);
            }
            S2CellUnion union = coverer.getCovering(poly);
            union.pack();
            return GetIndexTermsForCanonicalCovering(union);
        }

        public ArrayList<String> GetIndexTermsForCanonicalCovering(S2CellUnion covering) {
        // See the top of this file for an overview of the indexing strategy.
        //
        // Cells in the covering are normally indexed as covering terms.  If we are
        // optimizing for query time rather than index space, they are also indexed
        // as ancestor terms (since this lets us reduce the number of terms in the
        // query).  Finally, as an optimization we always index true_max_level()
        // cells as ancestor cells only, since these cells have the special property
        // that query regions will never contain a descendant of these cell
        if (levelMod == 1) {
            trueMaxLevel = maxLevel;
        } else {
            trueMaxLevel = maxLevel - (maxLevel - minLevel) % levelMod;
        }
        ArrayList<String> terms = new ArrayList<String>();
        S2CellId prev_id = null;
        for (S2CellId id : covering) {
            int level = id.level();

            if (level < trueMaxLevel) {
                // Add a covering term for this cell.
                terms.add(GetTerm(false, id));
            }
            // Add an ancestor term for this cell at the constrained level.
            terms.add(GetTerm(true, id.parent(level)));

            // Finally, add ancestor terms for all the ancestors of this cell.
            while ((level -= levelMod) >= minLevel) {
                S2CellId ancestor_id = id.parent(level);
                if (prev_id != null && prev_id.level() > level &&
                        prev_id.parent(level).equals(ancestor_id)) {
                    break;  // We have already processed this cell and its ancestors.
                }
                terms.add(GetTerm(true, ancestor_id));
            }
            prev_id = id;
        }
        return terms;
    }

//TODO: Could use this as a cell union is a regon
//        vector<string> S2RegionTermIndexer::GetQueryTerms(const S2Region& region,
//        string_view prefix) {
//        // Note that options may have changed since the last call.
//        *coverer_.mutable_options() = options_;
//        S2CellUnion covering = coverer_.GetCovering(region);
//        return GetQueryTermsForCanonicalCovering(covering, prefix);
//        }

    public ArrayList<String> GetQueryTermsForCanonicalCovering(String wkt, int maxCells) {
        S2Polygon poly = makePolygon(wkt);
        return GetQueryTermsForCanonicalCovering(poly, maxCells);

    }

    public ArrayList<String> GetQueryTermsForCanonicalCovering(S2Polygon poly, int maxCells) {
        if (maxCells != 0) {
            coverer.setMaxLevel(maxCells);
        } else {
            coverer.setMaxLevel(defaultMaxCellsQuery);
        }
        S2CellUnion union = coverer.getCovering(poly);
        union.pack();
        return GetQueryTermsForCanonicalCovering(union);
    }

    public ArrayList<String> GetQueryTermsForCanonicalCovering(S2CellUnion covering) {
        // See the top of this file for an overview of the indexing strategy.
        if (levelMod == 1) {
            trueMaxLevel = maxLevel;
        } else {
            trueMaxLevel = maxLevel - (maxLevel - minLevel) % levelMod;
        }
        ArrayList<String> terms = new ArrayList<String>();
        S2CellId prev_id = null;
        for (S2CellId id : covering) {
            // IsCanonical() already checks the following conditions, but we repeat
            // them here for documentation purposes.
            int level = id.level();

            // Cells in the covering are always queried as ancestor terms.
            terms.add(GetTerm(true, id));

            // Finally, add covering terms for all the ancestors of this cell.
            while ((level -= levelMod) >= minLevel) {
                S2CellId ancestor_id = id.parent(level);
                if (prev_id != null && prev_id.level() > level &&
                        prev_id.parent(level).equals(ancestor_id)) {
                    break;  // We have already processed this cell and its ancestors.
                }
                terms.add(GetTerm(false, ancestor_id));
            }
            prev_id = id;
        }
        return terms;
    }

    public static void main(String[] args) {
        s2RegionTermIndexer indxer = new s2RegionTermIndexer();
        String a_wkt = "POLYGON ((-92.88112331857228 44.86480346533368, -92.88112331857228 44.86479433882711, -92.88111357300382 44.86479458405908, -92.88111357300382 44.86480371056565, -92.88112331857228 44.86480346533368))";

//        S2CellUnion x = WktToS2(wkt, 1000);
//        System.out.println(x.cellIds());
//        System.out.println(x.cellIds().size());

        ArrayList<String> q_terms = indxer.GetQueryTermsForCanonicalCovering(wkt, 0);
        System.out.println("Query");
        System.out.println(q_terms);
        System.out.println(q_terms.size());

        ArrayList<String> i_terms = indxer.GetIndexTermsForCanonicalCovering(wkt, 0);
        System.out.println("Index");
        System.out.println(i_terms);
        System.out.println(i_terms.size());
    }
}


//    public S2RegionCoverer coverer;
//    int maxCells;
//    String marker_ = "$";

    // The following parameters control the tradeoffs between index size, query
    // size, and accuracy (see s2region_coverer.java for details).
    //
    // IMPORTANT: You must use the same values for min_level(), max_level(), and
    // level_mod() for both indexing and queries, otherwise queries will return
    // incorrect results.  However, max_cells() can be changed as often as
    // desired -- you can even change this parameter for every region.

    ///////////////// Options Inherited From S2RegionCoverer ////////////////

    // max_cells() controls the maximum number of cells when approximating
    // each region.  This parameter value may be changed as often as desired
    // (using mutable_options(), see below), e.g. to approximate some regions
    // more accurately than others.
    //
    // Increasing this value during indexing will make indexes more accurate
    // but larger.  Increasing this value for queries will make queries more
    // accurate but slower. (See s2region_coverer.h for details on how this
    // parameter affects accuracy.)  For example, if you don't mind large
    // indexes but want fast serving, it might be reasonable to set
    // max_cells() == 100 during indexing and max_cells() == 8 for queries.
    //
    // DEFAULT: 8  (coarse approximations)
    // min_level() and max_level() control the minimum and maximum size of the
    // S2Cells used to approximate regions.  Setting these parameters
    // appropriately can reduce the size of the index and speed up queries by
    // reducing the number of terms needed.  For example, if you know that
    // your query regions will rarely be less than 100 meters in width, then
    // you could set max_level() as follows:
    //
    //   options.set_max_level(S2::kAvgEdge.GetClosestLevel(
    //       S2Earth::MetersToRadians(100)));
    //
    // This restricts the index to S2Cells that are approximately 100 meters
    // across or larger.  Similar, if you know that query regions will rarely
    // be larger than 1000km across, then you could set min_level() similarly.
    //
    // If min_level() is set too high, then large regions may generate too
    // many query terms.  If max_level() is set too low, then small query
    // regions will not be able to discriminate which regions they intersect
    // very precisely and may return many more candidates than necessary.
    //
    // If you have no idea about the scale of the regions being queried,
    // it is perfectly fine to set min_level() == 0 and max_level() == 30
    // (== S2::kMaxLevel).  The only drawback is that may result in a larger
    // index and slower queries.
    //
    // The default parameter values are suitable for query regions ranging
    // from about 100 meters to 3000 km across.
    //
    // DEFAULT: 4  (average cell width == 600km)

    // Setting level_mod() to a value greater than 1 increases the effective
    // branching factor of the S2Cell hierarchy by skipping some levels.  For
    // example, if level_mod() == 2 then every second level is skipped (which
    // increases the effective branching factor to 16).  You might want to
    // consider doing this if your query regions are typically very small
    // (e.g., single points) and you don't mind increasing the index size
    // (since skipping levels will reduce the accuracy of cell coverings for a
    // given max_cells() limit).
    //
    // DEFAULT: 1  (don't skip any cell levels)