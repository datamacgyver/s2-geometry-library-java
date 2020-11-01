package org.proagrica.wallys2.gateways;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2Polygon;
import com.vividsolutions.jts.geom.Geometry;
import org.proagrica.wallys2.utils.s2RegionTermIndexer;

import java.util.ArrayList;
import java.util.List;

import static org.proagrica.wallys2.converters.wktConverter.*;
import static org.proagrica.wallys2.utils.CellIdTools.getCentroidWKT;
import static org.proagrica.wallys2.utils.GeometryTools.getAreaWKT;
import static org.proagrica.wallys2.utils.GeometryTools.readWKT;
import static org.proagrica.wallys2.utils.S2CellAreaTools.getAreaS2;

public class CellProcessor {

    private S2CellUnion union;
    Geometry geom;
    String WKT;
    ArrayList<S2CellId> UnionCellIDs;
    S2Polygon poly;
    s2RegionTermIndexer indexer;

    public boolean addWKT(String wkt_in, int max_res, int min_res, int max_cells, int level_mod) throws Exception {
        WKT = wkt_in;
        geom = readWKT(WKT);

        boolean SensibleFaceNo = WktToS2Ids(WKT, 0, 0).size() < 5;
        boolean SensibleArea = geom.getArea() > 0.0;

        if (geom.isValid() & SensibleFaceNo & SensibleArea){
            if (max_cells != 0){
                poly = WktToS2Polygon(WKT);
                union = convertToUnion(poly, max_cells, max_res, min_res, level_mod);
            } else {
                union = WktToS2(WKT, max_res, min_res);
            }
            UnionCellIDs = union.cellIds();
            indexer = new s2RegionTermIndexer(min_res, max_res, level_mod);
            return true;
        }
        return false;
    }

    public List<String>  getQueryTerms(){
        //Set maxCells to 0 to use default (at time of writing, 25)
        return indexer.GetQueryTermsForCanonicalCovering(union);
    }

    public List<String>  getIndexTerms(){
        //Set maxCells to 0 to use default (at time of writing, 100)
        return indexer.GetIndexTermsForCanonicalCovering(union);
    }


    public double getPolygonArea(){
        return poly.getArea();
    };


    public List<Long> getS2CellNos(){
        List<Long> unionList = new ArrayList<>();
        for (S2CellId c : UnionCellIDs) {
            unionList.add(c.id());
        };
        return unionList;
    };

    public List<String> getS2CellHexes(){
        List<String> unionList = new ArrayList<>();
        for (S2CellId c : UnionCellIDs) {
            unionList.add(c.toToken());
        };
        return unionList;
    };

    public List<String> getS2CellIds(){
        List<String> unionList = new ArrayList<>();
        for (S2CellId c : UnionCellIDs) {
            unionList.add(String.valueOf(c.id()));
        };
        return unionList;
    };

    public static String hexToStr(String hex){
        S2CellId cell = S2CellId.fromToken(hex);
        long id_number = cell.id();
        return String.valueOf(id_number);
    }

    public List<Double> getS2CellAreas(){
        List<Double> areaList = new ArrayList<>();
        for (S2CellId c : UnionCellIDs) {
            areaList.add(getAreaS2(c));
        }
        return areaList;
    };

    public List<Double> getRawS2CellAreas(){
        List<Double> areaList = new ArrayList<>();
        for (S2CellId c : UnionCellIDs) {
            S2Cell cell = new S2Cell(c);
            areaList.add(cell.exactArea());
        }
        return areaList;
    };

    public List<Integer> getS2Resolutions(){
        List<Integer> resList = new ArrayList<>();
        for (S2CellId c : UnionCellIDs) {
            resList.add(c.level());
        }
        return resList;
    };

    public List<String> getS2Centroids(){
        List<String> centroidList = new ArrayList<>();
        for (S2CellId c : UnionCellIDs) {
            centroidList.add(getCentroidWKT(c));
        }
        return centroidList;
    };

    public double getWktArea() throws Exception {
        return getAreaWKT(WKT);
    };

    public String getS2WKT(){
        return s2ToWkt(union);
    };

    public List<Long> getSingleResCellList(int res){
        ArrayList<S2CellId> singleResIds = new ArrayList<>();
        List<Long> unionList = new ArrayList<>();
        union.denormalize(res, 0, singleResIds);
        for (S2CellId c : singleResIds) {
//            if c.level() != res) System.out.println(c.level());
            unionList.add(c.id());
        };
        return unionList;
    };

    public List<String> getSingleResCellListHex(int res){
        ArrayList<S2CellId> singleResIds = new ArrayList<>();
        List<String> unionList = new ArrayList<>();
        union.denormalize(res, 0, singleResIds);
        for (S2CellId c : singleResIds) {
//            if c.level() != res) System.out.println(c.level());
            unionList.add(c.toToken());
        };
        return unionList;
    };

    public static void main(String[] args) throws Exception {
        String a_wkt = "POLYGON ((-92.88112331857228 44.86480346533368, -92.88112331857228 44.86479433882711, -92.88111357300382 44.86479458405908, -92.88111357300382 44.86480371056565, -92.88112331857228 44.86480346533368))";
        CellProcessor x = new CellProcessor();
//        x.addWKT(a_wkt, 26, 26, 100, 1);
//        System.out.println(x.getS2Resolutions());
//        System.out.println(x.getRawS2CellAreas());
        x.addWKT(a_wkt, 21, 21, 100000, 1);
        x.getSingleResCellList(21);
//        System.out.println(x.getPolygonArea());
//        System.out.println(x.getS2CellAreas());
//        System.out.println(x.getIndexTerms());
    }
}
