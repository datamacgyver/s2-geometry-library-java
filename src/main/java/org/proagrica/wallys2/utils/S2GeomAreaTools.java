package org.proagrica.wallys2.utils;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import org.proagrica.wallys2.converters.coordConverter;
import org.opengis.referencing.FactoryException;

import java.util.ArrayList;
import java.util.Collections;

import static org.proagrica.wallys2.converters.wktConverter.s2ToWkt;

public class S2GeomAreaTools {

    public static double getAreaCellWKT(S2CellId cellId) throws Exception {
        S2CellUnion cellUnion = new S2CellUnion();
        cellUnion.initRawIds(new ArrayList<>(Collections.singletonList(cellId.id())));
        return getAreaCellWKT(cellUnion);
    }

    public static double getAreaCellWKT(Long cellId) throws Exception {
        S2CellUnion cellUnion = new S2CellUnion();
        cellUnion.initRawIds(new ArrayList<>(Collections.singletonList(cellId)));
        return getAreaCellWKT(cellUnion);
    }

    public static double getAreaCellWKT(ArrayList<Long> cellRegion) throws Exception {
        S2CellUnion cellUnion = new S2CellUnion();
        cellUnion.initRawIds(new ArrayList<>(cellRegion));
        return getAreaCellWKT(cellUnion);
    }

    //Main Call!!
    public static double getAreaCellWKT(S2CellUnion cellRegion) throws Exception {
        try {
            String wkt = s2ToWkt(cellRegion);
            coordConverter conv;
            conv = new coordConverter("EPSG:4326", "EPSG:2163", true); //TODO: Generalise
            return conv.getConvertedArea(wkt);
        } catch (FactoryException e) {
            e.printStackTrace();
            throw new Exception("Coordinate Conversion Failed! Check earlier stacktrace.");
        }
    }

}
