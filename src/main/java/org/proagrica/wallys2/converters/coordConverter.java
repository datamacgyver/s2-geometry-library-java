package org.proagrica.wallys2.converters;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


// TODO: We shouldn't have to go from WKT, can we just go straight from an S2, or simply hack the S2 from WKT?
public class coordConverter {
    /** The CellIds that form the Union */
    CoordinateReferenceSystem sourceCRS;
    CoordinateReferenceSystem targetCRS;
    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    boolean lenient;

    public coordConverter(String fromCRS, String toCRS, boolean leniency) throws FactoryException {
//        "EPSG:4326"
//        "EPSG:2163"
        sourceCRS = CRS.decode(fromCRS);
        targetCRS = CRS.decode(toCRS);
        lenient = leniency;
    }

    public String doConvert(String inWKT) throws ParseException, FactoryException, TransformException {
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geom = reader.read(inWKT);
        Geometry geomConverted = convertCoords(geom);
        return geomConverted.toString();
    }

    public double getConvertedArea(String inWKT) throws ParseException, FactoryException, TransformException {
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geom = reader.read(inWKT);
        return getConvertedArea(geom);
    }

    public double getConvertedArea(Geometry geom) throws ParseException, FactoryException, TransformException {
        Geometry geomConverted = convertCoords(geom);
        return geomConverted.getArea();
    }

    public double getConvertedPerimeter(String inWKT) throws ParseException, FactoryException, TransformException {
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geom = reader.read(inWKT);
        Geometry geomConverted = convertCoords(geom);
        return geomConverted.getLength();
    }

    public double getAreaToPerimeter(String inWKT) throws ParseException, FactoryException, TransformException {
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geom = reader.read(inWKT);
        Geometry geomConverted = convertCoords(geom);
        return geomConverted.getArea() / geomConverted.getLength();
    }

    Geometry convertCoords(Geometry geom) throws FactoryException, TransformException {
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, lenient);
        return JTS.transform(geom, transform);
    }

    public static void main(String[] args) throws FactoryException, ParseException, TransformException {
        coordConverter c = new coordConverter("EPSG:4326", "EPSG:2163", true);
        String out = c.doConvert("POINT (1 1)");
        System.out.println(out);

    }
}