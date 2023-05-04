package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.datamodel.ProductData;
import org.locationtech.jts.geom.Geometry;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import java.io.IOException;

class EnmapL2AMetadata extends EnmapOrthoMetadata {

    EnmapL2AMetadata(Document doc, XPath xPath) {
        super(doc, xPath);
    }

    public Geometry getSpatialCoverage() throws IOException {
        double[] lats = getDoubleValues("/level_X/base/spatialCoverage/boundingPolygon/*/latitude", 5);
        double[] lons = getDoubleValues("/level_X/base/spatialCoverage/boundingPolygon/*/longitude", 5);
        // in L2A metadata the 5th coordinate is not equal to the first. The precision is higher.
        lats[4] = lats[0];
        lons[4] = lons[0];
        return createPolygon(lats, lons);
    }

    @Override
    public String getSpectralMeasurementName() {
        return "surface reflectance";
    }

    @Override
    public String getSpectralUnit() {
        return "";
    }

    @Override
    public int getSpectralDataType() {
        return ProductData.TYPE_UINT16;
    }

    @Override
    public float getSpectralBackgroundValue() {
        // For L2A the background value is not available or wrong in the testdata
        return -32768;
    }

}
