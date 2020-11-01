package org.proagrica.wallys2.utils;

import com.google.common.geometry.*;

public class S2CellAreaTools {
    /**
     * so S2 assumes the earth is a perfect sphere when it does areas.
     * This means that the areas may not be perfect. It should also be
     * noted that the area returned is a little....abstract. What it's
     * actually returning is the surface area assuming a sphere of radius
     * one. As the earth does not have radius one we need to convert
     */
    public static double getAreaS2(S2CellUnion cellRegion){
        S2Point latLng = cellRegion.getCentroid();
        double area = cellRegion.exactArea();
        double radiusAtCell = calculateRadius(latLng.getlatDegrees());
        return getTotalArea(area, radiusAtCell);
    }

    public static double getAreaS2(S2Polygon poly){
        S2Point latLng = poly.getCentroid();
        double area = poly.getArea();
        double radiusAtCell = calculateRadius(latLng.getlatDegrees());
        return getTotalArea(area, radiusAtCell);
    }

    public static double getAreaS2(S2CellId cellId) {
        S2Cell cell = new S2Cell(cellId);
        double area = cell.exactArea();
        double radiusAtCell = calculateRadius(cellId.toLatLng().latDegrees());
        return getTotalArea(area, radiusAtCell);
    }

    static double calculateRadius (double latDeg) {
        // https://en.wikipedia.org/wiki/Earth_radius#Location-dependent_radii
        double polarR = 6356752.3141;  //m
        double equatorR = 6378137;  //m
        double lat = Math.abs(latDeg);

        double a = Math.pow(Math.pow(equatorR, 2) * Math.cos(lat), 2);
        double b = Math.pow(Math.pow(polarR, 2) * Math.sin(lat), 2);
        double c = Math.pow(equatorR * Math.cos(lat), 2);
        double d = Math.pow(polarR * Math.sin(lat), 2);
        double x = ((a+b) / (c+d));

        return Math.sqrt(x);
    }

    static double getTotalArea (double area, double radiusAtCell){
        double totalAreaAtUnit = 4 * Math.PI;
        double cellAreaProp = area / totalAreaAtUnit;
        double totalAreaAtRadius = 4 * Math.PI * Math.pow(radiusAtCell, 2);
        return totalAreaAtRadius * cellAreaProp;
    }
}
