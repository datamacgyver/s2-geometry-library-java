package org.proagrica.wallys2.gateways;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2Polygon;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import org.proagrica.wallys2.utils.s2RegionTermIndexer;

import java.util.ArrayList;
import java.util.List;

import static org.proagrica.wallys2.converters.wktConverter.*;
import static org.proagrica.wallys2.utils.CellIdTools.getCentroidWKT;
import static org.proagrica.wallys2.utils.GeometryTools.*;
import static org.proagrica.wallys2.utils.S2CellAreaTools.getAreaS2;

public class GeomProcessor {

    Geometry geom;
    String WKT;

    public boolean addWKT(String wkt_in) throws Exception {
        try {
            WKT = wkt_in;
            geom = readWKT(WKT);
        } catch (Exception e){
            return false;
        }
        return true;
    }

    public double getArea(String targetCRS) throws Exception {
        //"EPSG:2163"?
        if (geom==null){
            return 0;
        }
        return getAreaGeom(geom, targetCRS);
    }

    public boolean intersect(String new_wkt) throws ParseException {
        if (geom==null){
            return false;
        }

        Geometry new_geom = readWKT(new_wkt);
        if (!geom.intersects(new_geom)) {
            geom = null;
            return false;
        }

        if (!geom.contains(geom)){
            geom = geom.intersection(geom);
        } //else we just keep geom

        return true;
    }

    public String getWKT(){
        if (geom==null){
            return "";
        }
        return geom.toText();
    }


    public static void main(String[] args) throws Exception {
        String a_wkt = "POLYGON ((-92.88112331857228 44.86480346533368, -92.88112331857228 44.86479433882711, -92.88111357300382 44.86479458405908, -92.88111357300382 44.86480371056565, -92.88112331857228 44.86480346533368))";
        String another_wkt =  "POLYGON ((-92.8811843490808 44.86479280307094, -92.88118179202503 44.86479286741677, -92.88118179202503 44.86479271891675, -92.8811843490808 44.86479280307094))";
        GeomProcessor g = new GeomProcessor();
        g.addWKT(a_wkt);
        System.out.println(g.getArea("EPSG:2163"));
        System.out.println(g.getWKT());

        g.intersect(another_wkt);
        System.out.println(g.getArea("EPSG:2163"));
        System.out.println(g.getWKT());
    }
}
