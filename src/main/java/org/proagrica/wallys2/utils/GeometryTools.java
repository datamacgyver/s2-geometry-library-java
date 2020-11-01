package org.proagrica.wallys2.utils;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import org.opengis.referencing.operation.TransformException;
import org.proagrica.wallys2.converters.coordConverter;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.referencing.FactoryException;

import java.util.ArrayList;
import java.util.List;

import static org.proagrica.wallys2.converters.wktConverter.s2ToWkt;

public class GeometryTools {
    public static Geometry readWKT(String inWKT) throws ParseException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);
        return reader.read(inWKT);
    }

    public static String unionWKTsToWKT(List<String> WKTs) {
        return unionWKTs(WKTs).toText();
    }

    public static Geometry unionWKTs(List<String> WKTs) {
        Geometry outGeom = null;
        for (String WKT : WKTs) {
            try {
                if (outGeom == null) {
                    outGeom = readWKT(WKT);
                } else {
                    outGeom = outGeom.union(readWKT(WKT));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return outGeom;
    }

    public static String intersectWKTsToWKT(List<String> WKTs) {
        return intersectWKTs(WKTs).toText();
    }
    public static String intersectWKTsToWKT(String WKT1, String WKT2) {
        List<String> WKTs = new ArrayList<>();
        WKTs.add(WKT1);
        WKTs.add(WKT2);
        return intersectWKTs(WKTs).toText();
    }

    public static Geometry intersectWKTs(List<String> WKTs) {
        Geometry outGeom = null;
        for (String WKT : WKTs) {
            try {
                if (outGeom == null) {
                    outGeom = readWKT(WKT);
                } else {
                    outGeom = outGeom.intersection(readWKT(WKT));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return outGeom;
    }

    public static Geometry intersectGeoms(List<Geometry> geoms) {
        Geometry outGeom = null;
        for (Geometry geom : geoms) {
            if (outGeom == null) {
                outGeom = geom;
            } else {
                //TODO: Not got in intersect check here as I'd have to return an empty geom and I don't know how to do that?
                if (!outGeom.contains(geom)){
                    outGeom = outGeom.intersection(geom);
                }
            }
        }
        return outGeom;
    }

    public static boolean isContained (String wkt, String point) throws ParseException {
        Geometry geom = readWKT(wkt);
        Geometry geom_point = readWKT(point);  //TODO: Techinically this can take a wkt too.
        return geom.contains(geom_point);
    }

    public static boolean isContained (String wkt, Long cellId) throws ParseException {
        Geometry geom = readWKT(wkt);
        S2Cell cell = new S2Cell(new S2CellId(cellId));
        String wktPoint = cell.getCenter().toDegreesWKT();
        Geometry geom_point = readWKT(wktPoint);
        return geom.contains(geom_point);
    }

    public static double getAreaWKT(String wkt) throws Exception {
        try {
            coordConverter conv;
            conv = new coordConverter("EPSG:4326", "EPSG:2163", true); //TODO: Generalise
            return conv.getConvertedArea(wkt);
        } catch (FactoryException e) {
            e.printStackTrace();
            throw new Exception("Coordinate Conversion Failed! Check earlier stacktrace.");
        }
    }

    public static double getAreaWKT(String wkt, String target_crs) throws Exception {
        try {
            coordConverter conv;
            conv = new coordConverter("EPSG:4326", target_crs, true); //TODO: Generalise
            return conv.getConvertedArea(wkt);
        } catch (FactoryException e) {
            e.printStackTrace();
            throw new Exception("Coordinate Conversion Failed! Check earlier stacktrace.");
        }
    }

    public static double getAreaGeom(Geometry geom, String target_crs) throws Exception {
        try {
            coordConverter conv;
            conv = new coordConverter("EPSG:4326", target_crs, true); //TODO: Generalise
            return conv.getConvertedArea(geom);
        } catch (FactoryException e) {
            e.printStackTrace();
            throw new Exception("Coordinate Conversion Failed! Check earlier stacktrace.");
        }
    }

    public static String checkCorrect(String wkt) throws ParseException, FactoryException, TransformException {
        Geometry geom = readWKT(wkt);
        if (geom.isValid()) {
            return wkt;
        }
        double area_old = geom.getArea();
        geom = TopologyPreservingSimplifier.simplify(geom,0.0001);
        double area_new = geom.getArea();

        if (area_new > (area_old*0.9) & (area_new < area_old*1.1)){
            return geom.toString();
        } else {
            return "";
        }
    }

    public static boolean WKTvalid(String wkt) throws ParseException {
        Geometry geom = readWKT(wkt);
        return geom.isValid();
    }
    public static double getPerimeterWKT(String wkt) throws Exception {
        try {
            coordConverter conv;
            conv = new coordConverter("EPSG:4326", "EPSG:2163", true); //TODO: Generalise
            return conv.getConvertedPerimeter(wkt);
        } catch (FactoryException e) {
            e.printStackTrace();
            throw new Exception("Coordinate Conversion Failed! Check earlier stacktrace.");
        }
    }

    public static void main(String[] args) throws Exception {
        String a_wkt = "POLYGON ((-92.88112331857228 44.86480346533368, -92.88112331857228 44.86479433882711, -92.88111357300382 44.86479458405908, -92.88111357300382 44.86480371056565, -92.88112331857228 44.86480346533368))";
        System.out.println(WKTvalid(a_wkt));
        System.out.println(getAreaWKT(a_wkt));
        System.out.println(getPerimeterWKT(a_wkt));
        System.out.println(getAreaWKT(a_wkt)/getPerimeterWKT(a_wkt));
        System.out.println(checkCorrect(a_wkt));
        System.out.println(getAreaWKT(checkCorrect(a_wkt)));
    }
}
