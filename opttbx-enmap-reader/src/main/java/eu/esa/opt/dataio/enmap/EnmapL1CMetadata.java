package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.datamodel.ProductData;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;

class EnmapL1CMetadata extends EnmapOrthoMetadata {

    EnmapL1CMetadata(Document doc, XPath xPath) {
        super(doc, xPath);
    }

    @Override
    public String getSpectralMeasurementName() {
        return "radiance";
    }

    @Override
    public String getSpectralUnit() {
        return "W/m^2/sr/nm";
    }

    @Override
    public int getSpectralDataType() {
        return ProductData.TYPE_UINT16;
    }

}
