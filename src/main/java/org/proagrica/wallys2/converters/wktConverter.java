package org.proagrica.wallys2.converters;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.proagrica.wallys2.utils.CellUnionTools.S2IdsToUnion;

public class wktConverter {

    public static S2CellUnion WktToS2(String wkt, int maxResolution, int minResolution) {
        S2Polygon poly = makePolygon(wkt);
        return convertToUnion(poly, 0, maxResolution, minResolution, 0);
    }

    public static S2CellUnion WktToS2(String wkt, int maxResolution, int minResolution, int max_cells) {
        S2Polygon poly = makePolygon(wkt);
        return convertToUnion(poly, max_cells, maxResolution, minResolution, 0);
    }

    public static S2Polygon makePolygon(String wkt){
        Pattern p = Pattern.compile("\\(([0-9 ,-.]+)\\)");
        Matcher m = p.matcher(wkt);
        List<S2Loop> loops = Lists.newArrayList();

        while (m.find()) {
            String lp = m.group(1);
            S2Loop loop = makeLoop(lp);
            loop.normalize();
            loops.add(loop);
        }
        return new S2Polygon(loops);
    }

    public static S2Polygon WktToS2Polygon(String wkt) {
        Pattern p = Pattern.compile("\\(([0-9 ,-.]+)\\)");
        Matcher m = p.matcher(wkt);
        List<S2Loop> loops = Lists.newArrayList();

        while (m.find()) {
            String lp = m.group(1);
            S2Loop loop = makeLoop(lp);
            loop.normalize();
            loops.add(loop);
        }
        S2Polygon poly = new S2Polygon(loops);
        return poly;
    }


    public static S2CellUnion WktToS2(String wkt, int maxPossCells) {
        Pattern p = Pattern.compile("\\(([0-9 ,-.]+)\\)");
        Matcher m = p.matcher(wkt);
        List<S2Loop> loops = Lists.newArrayList();

        while (m.find()) {
            String lp = m.group(1);
            S2Loop loop = makeLoop(lp);
            loop.normalize();
            loops.add(loop);
        }
        S2Polygon poly = new S2Polygon(loops);
        return convertToUnion(poly, maxPossCells, 0, 30, 0);
    }

    /**
     * Wraps up the call to parseVertices by creating the vertiex list and
     * passing the wkt coordinates. It then converts the populated list to
     * an S2 Loop. See parseVertices for details.
     */
    static S2Loop makeLoop(String wkt_coords) {
        List<S2Point> vertices = Lists.newArrayList();
        parseVertices(wkt_coords, vertices);
        return new S2Loop(vertices);
    }

    /**
     * Will take the coordinate part of a wkt polygon and convert it to S2
     * vertices. It then appends them to the given vertices list.
     * <p>
     * If the original WKT is POLYGON((1 2, 3 4, 5 6)) then the input for
     * wkt_coords would be 1 2, 3 4, 5 6.
     *
     * @param wkt_coords Coordinate part of a wkt
     * @param vertices   List of S2 points that will become vertices
     */
    static void parseVertices(String wkt_coords, List<S2Point> vertices) {
        if (wkt_coords == null) {
            return;
        }
        wkt_coords = wkt_coords.replaceAll(", ", ",");
        for (String token : Splitter.on(",").split(wkt_coords)) {
            int space = token.indexOf(' ');
            if (space == -1) {
                throw new IllegalArgumentException(
                        "Illegal string:" + token + ". Should look like '35 20'");
            }
            double lng = Double.parseDouble(token.substring(0, space));
            double lat = Double.parseDouble(token.substring(space + 1));
            vertices.add(S2LatLng.fromDegrees(lat, lng).toPoint());
        }
    }


    public static S2CellUnion convertToUnion(S2Polygon poly, int maxNoCells, int maxResolution, int minResolution, int levelMod) {
        S2RegionCoverer coverer = new S2RegionCoverer();

        if (maxNoCells != 0){
            coverer.setMaxCells(maxNoCells);
        }

        if (levelMod != 0){
            coverer.setLevelMod(levelMod);
        }
        coverer.setMaxLevel(maxResolution);
        coverer.setMinLevel(minResolution);

        S2CellUnion polyCells = coverer.getCovering(poly);
        polyCells.pack();

        return polyCells;
    }



    /**
     * Will take the S2LatLngRect of an S2 cell and convert it to
     * a WKT polygon in the form of (a,b,c,a).
     *
     * @param cell S2Cell of a cell
     * @return WKT polygon sting
     */
    public static String getCellPolygon(S2Cell cell) {

        String aPolygon;
        String FirstPoint = "";
        StringBuilder aPolygonBuilder = new StringBuilder("((");

        for (int i = 0; i < 4; i++) {
            S2Point loc = cell.getVertex(i);
            String loc_str = loc.toDegreesString();
            loc_str = loc_str.substring(1, loc_str.length() - 1); // remove brackets
            int comma = loc_str.indexOf(',');
            if (comma == -1) {
                throw new IllegalArgumentException(
                        "Illegal string:" + loc_str);
            }
            String lat = loc_str.substring(0, comma);
            String lng = loc_str.substring(comma + 1);
            if (aPolygonBuilder.length() != 2) {
                aPolygonBuilder.append(",");
            } else {
                FirstPoint = lat + " " + lng;
            }
            aPolygonBuilder.append(lat).append(" ").append(lng);
        }
        aPolygon = aPolygonBuilder.toString();
        aPolygon = aPolygon + "," + FirstPoint + "))";
        return aPolygon;
    }

    /**
     * Convert an S2 region (ie collection of polygons) to a
     * WKT multipolygon. Note that even if there's one cell it'll
     * still return a multpolygon with one polygon. It was easier.
     *
     * @param region Collection of S2 cells
     * @return WKT multipolygon sting
     */
    public static String s2ToWkt(S2CellUnion region) {
        ArrayList<S2CellId> regionCells = region.cellIds();
        StringBuilder multipoly = new StringBuilder();
        for (S2CellId cell : regionCells) {
            if (multipoly.length() != 0) {
                multipoly.append(",");
            } else {
                multipoly.append("MULTIPOLYGON(");
            }
            S2Cell c = new S2Cell(cell);
            multipoly.append(getCellPolygon(c));
        }
        multipoly.append(")");
        return multipoly.toString();
    }
//
//    public static S2Polygon s2CellToPolygon(S2Cell c) {
//        List<S2Point> verts = Lists.newArrayList();
//        verts.add(c.getVertex(0));
//        verts.add(c.getVertex(1));
//        verts.add(c.getVertex(2));
//        verts.add(c.getVertex(3));
//        verts.add(c.getVertex(0));
//
//        S2Loop loop = new S2Loop(verts);
//        loop.normalize();
//        return new S2Polygon(loop);
//    }

    public static String s2ToWkt(List<Long> regionIds1) {
        S2CellUnion region1 = S2IdsToUnion(regionIds1);
        return s2ToWkt(region1);
    }


    /**
     * Convert WKT (polygon or multipolygon) to an S2 polygon (a collection
     * of S2 cells).
     *
     * @param wkt a string containing a WKT string.
     * @return S2 polygon
     */
    public static S2CellUnion WktToS2(String wkt) {  //todo: this isn't the way to do it. should be a class var

        return WktToS2(wkt, 23, 10);
    }


    public static List<String> WktToSingleResS2Ids(String wkt, int res) {
        S2CellUnion s2Cells = WktToS2(wkt, res, res, 100000);
        ArrayList<S2CellId> singleResIds = new ArrayList<>();
        s2Cells.denormalize(res, 0, singleResIds);

        List<String> s2CellStrings = new ArrayList<>();
        for (S2CellId c : singleResIds) {
            s2CellStrings.add(c.toToken());
        }
        return s2CellStrings;
    }


    public static List<Long> WktToS2Ids(String wkt) {
        S2CellUnion s2Cells = WktToS2(wkt);
        List<Long> s2CellStrings = new ArrayList<>();
        for (S2CellId c : s2Cells) {
            s2CellStrings.add(c.id());
        }
        return s2CellStrings;
    }

    public static List<Long> WktToS2Ids(String wkt, int maxPossCells) {
        S2CellUnion s2Cells = WktToS2(wkt, maxPossCells);
        List<Long> s2CellStrings = new ArrayList<>();
        for (S2CellId c : s2Cells) {
            s2CellStrings.add(c.id());
        }
        return s2CellStrings;
    }

    public static List<Long> WktToS2Ids(String wkt, int maxResolution, int minResolution) {
        S2CellUnion s2Cells = WktToS2(wkt, maxResolution, minResolution);
        List<Long> s2CellStrings = new ArrayList<>();
        for (S2CellId c : s2Cells) {
            s2CellStrings.add(c.id());
        }
        return s2CellStrings;
    }

    public static void main(String[] args) throws Exception {
        String wkt = "POLYGON ((-92.88112331857228 44.86480346533368, -92.88112331857228 44.86479433882711, -92.88111357300382 44.86479458405908, -92.88111357300382 44.86480371056565, -92.88112331857228 44.86480346533368))";

        S2CellUnion x = WktToS2(wkt, 20, 19);
        System.out.println(x.cellIds());
        System.out.println(x.cellIds().size());
        System.out.println(x.cellIds());
        System.out.println(x.cellIds().size());
        for (S2CellId c : x.cellIds()){System.out.println(c.level());};

        String wkt_out = s2ToWkt(x);
        System.out.println(wkt_out);

        ArrayList<String> y = (ArrayList<String>) WktToSingleResS2Ids(wkt, 20);
        System.out.println(y);
    }

}