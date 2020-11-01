package org.proagrica.wallys2.utils;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2Point;

public class CellIdTools {

    public static Boolean checkIntersect(S2CellId region1, S2CellId region2){
        return region1.intersects(region2);
    }

    public static Boolean checkIntersect(Long regionIds1, Long regionIds2){
        S2CellId r1 = new S2CellId(regionIds1);
        S2CellId r2 = new S2CellId(regionIds2);
        return checkIntersect(r1, r2);
    }

    public static S2CellId getIntersect(S2CellId region1, S2CellId region2){
        S2Cell region1Cell = new S2Cell(region1);
        S2Cell region2Cell = new S2Cell(region2);
        if (checkIntersect(region1, region2)){
            if (region1Cell.level() > region2Cell.level()){
                return region1;
            } else {
                return region2;
            }
        } else {
            return new S2CellId();
        }
    }

    public static Long getIntersect(Long regionIds1, Long regionIds2){
        S2CellId r1 = new S2CellId(regionIds1);
        S2CellId r2 = new S2CellId(regionIds2);
        S2CellId intersection = getIntersect(r1, r2);
        return intersection.id();
    }
    public static Long getThreeWayIntersect(Long regionIds1, Long regionIds2, Long regionIds3){
        S2CellId r1 = new S2CellId(regionIds1);
        S2CellId r2 = new S2CellId(regionIds2);
        S2CellId r3 = new S2CellId(regionIds3);
        S2CellId intersection1 = getIntersect(r1, r2);
        S2CellId intersection = getIntersect(intersection1, r3);
        return intersection.id();
    }

    public static String getCentroidWKT(S2CellId cellId) {
        S2Cell cell = new S2Cell(cellId);
        S2Point s2Centre = cell.getCenter();
        return s2Centre.toDegreesWKT();
    }

    // These are just tests really. Well, more like experimments
    public static void main(String[] args){
        S2CellId r1 = S2CellId.fromToken("89c2594");
        S2CellId r2 = S2CellId.fromToken("89c25905");

        System.out.println("cells as Ids:");
        System.out.println(r1.id());
        System.out.println(r2.id());

        System.out.println("Check intersect:");
        System.out.println(checkIntersect(r1, r2));

        System.out.println("Do intersect:");
        S2CellId intersection = getIntersect(r1, r2);
        System.out.println(intersection);

        System.out.println("ExactArea");
        System.out.println(S2CellAreaTools.getAreaS2(intersection));

        System.out.println("ExactArea poly_1");
        System.out.println(S2CellAreaTools.getAreaS2(r1));
    }
}
