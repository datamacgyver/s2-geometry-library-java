package org.proagrica.wallys2.utils;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2Point;

import java.util.ArrayList;
import java.util.List;

import static org.proagrica.wallys2.utils.S2CellAreaTools.getAreaS2;

public class CellUnionTools {

    public static Boolean checkIntersect(S2CellUnion region1, S2CellUnion region2){
        return region1.intersects(region2);
    }

    public static Boolean checkIntersect(List<Long> regionIds1, List<Long> regionIds2){
        S2CellUnion r1 = S2IdsToUnion(regionIds1);
        S2CellUnion r2 = S2IdsToUnion(regionIds2);
        return checkIntersect(r1, r2);
    }

    public static S2CellUnion getIntersect(S2CellUnion region1, S2CellUnion region2){
        S2CellUnion intersection = new S2CellUnion();
        intersection.getIntersection(region1, region2);
        intersection.normalize();
        intersection.pack();
        return intersection;
    }

    public static List<Long> getIntersect(List<Long> regionIds1, List<Long> regionIds2){
        S2CellUnion r1 = S2IdsToUnion(regionIds1);
        S2CellUnion r2 = S2IdsToUnion(regionIds2);
        S2CellUnion intersection = getIntersect(r1, r2);
        return UnionToS2Ids(intersection);
    }

    //get area
    /**
     * so S2 assumes the earth is a perfect sphere when it does areas.
     * This means that the areas may not be perfect. It should also be
     * noted that the area returned is a little....abstract. What it's
     * actually returning is the surface area assuming a sphere of radius
     * one. As the earth does not have radius one we need to convert.
     * I use the cell area tools to do this.
     */
    public static double exactArea(S2CellUnion region1){
        return getAreaS2(region1);
    }
    public static double exactArea(List<Long> regionIds1){
        S2CellUnion region1 = S2IdsToUnion(regionIds1);
        return exactArea(region1);
    }
    public static double approxArea(S2CellUnion region1){
        return region1.approxArea() * areaConversionm;
    }
    public static double approxArea(List<Long> regionIds1){
        S2CellUnion region1 = S2IdsToUnion(regionIds1);
        return region1.approxArea() * areaConversionm;
    }

    private static double areaConversionKm = 40589768.42185161576473144721091;
    private static double areaConversionm = 40589768421851.61576473144721091;

    /* *
    * This will return a centroid reference that'll allow you to join together cells
    * within a level grid. I recommend 10 (about 80-100km2) but 11 could also work
    * (25-20). It shouldn't be smaller than your largest polygon so you should wither check
    * this or enforce a min/max resolution (although that will make your indices bigger).
    * the problem with this is that we have to assume the polygon covers a single cell,
    * it doesn't cross a boundary.
    * */
    public static long getHighLevelCell (S2CellUnion region, int level){
        S2Point centroid = region.getCentroid();
        S2CellId centreCell = S2CellId.fromPoint(centroid);
        centreCell = centreCell.parent(level);
        return centreCell.id();
    }

    public static String getCentroid (S2CellUnion region){
        S2Point centroid = region.getCentroid();
        return centroid.toDegreesWKT();
    }

    public static String getCentroid (List<Long> regionIds){
        S2CellUnion region = S2IdsToUnion(regionIds);
        return getCentroid(region);
    }

    public static long getHighLevelCell (List<Long> regionIds1, int level){
        S2CellUnion region1 = S2IdsToUnion(regionIds1);
        return getHighLevelCell (region1, level);
    }

    public static S2CellUnion S2IdsToUnion(List<Long> cellTokensIn){
        S2CellUnion newRegion = new S2CellUnion();
        ArrayList<Long> cellTokens = new ArrayList<>(cellTokensIn);
        newRegion.initFromIds(cellTokens);
        newRegion.pack();
        return newRegion;
    }

    public static List<Long> UnionToS2Ids(S2CellUnion poly){
        List<Long> ids = new ArrayList<>();
        for (S2CellId cell: poly.cellIds()){
            ids.add(cell.id());
        }
        return ids;
    }

    public static List<Long> denormalise(List<Long> cellTokensIn, int level){
        ArrayList<S2CellId> S2ids = new ArrayList<>();
        ArrayList<Long> ids = new ArrayList<>();
        S2CellUnion region1 = S2IdsToUnion(cellTokensIn);
        region1.denormalize(level,1, S2ids);

        for (S2CellId cellId : S2ids){
            ids.add(cellId.id());
        }
        return ids;
    }

    // These are just tests really. Well, more like experimments
    public static void main(String[] args){
//        String raw_wkt = "POLYGON ((-92.88112331857228 44.86480346533368, -92.88112331857228 44.86479433882711, -92.88111357300382 44.86479458405908, -92.88111357300382 44.86480371056565, -92.88112331857228 44.86480346533368))";
//        List<Long> poly_1 = WktToS2Ids(raw_wkt_1);
//        String raw_wkt_2 =  "POLYGON ((-92.8811843490808 44.86479280307094, -92.88118179202503 44.86479286741677, -92.88118179202503 44.86479271891675, -92.8811843490808 44.86479280307094))";
//        List<Long> poly_2 = WktToS2Ids(raw_wkt_2);
//
//        System.out.println("Polygons as Ids:");
//        System.out.println(poly_1);
//        System.out.println(poly_2);
//
//        System.out.println("Check intersect:");
//        System.out.println(checkIntersect(poly_1, poly_2));
//
//        System.out.println("Do intersect:");
//        List<Long> intersection = getIntersect(poly_1, poly_2);
//        System.out.println(intersection);
//
//        System.out.println("ApproxArea");
//        System.out.println(approxArea(intersection));
//        System.out.println("ExactArea");
//        System.out.println(exactArea(intersection));
//
//        System.out.println("ExactArea poly_1");
//        System.out.println(exactArea(poly_1));
    }
}
