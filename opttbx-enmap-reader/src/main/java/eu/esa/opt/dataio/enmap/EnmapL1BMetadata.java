package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.datamodel.ProductData;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class EnmapL1BMetadata extends EnmapMetadata {

    EnmapL1BMetadata(Document doc, XPath xPath) {
        super(doc, xPath);
    }

    @Override
    public Dimension getSceneDimension() throws IOException {
        int width = Integer.parseInt(getNodeContent("/level_X/specific/widthOfScene"));
        int height = Integer.parseInt(getNodeContent("/level_X/specific/heightOfScene"));
        return new Dimension(width, height);
    }

    @Override
    public double getPixelSize() throws IOException {
        return Integer.parseInt(getNodeContent("/level_X/specific/pixelSize"));
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

    @Override
    public Map<String, String> getFileNameMap() throws IOException {
        NodeList nodeSet = getNodeSet("/level_X/product/productFileInformation/*/name");

        HashMap<String, String> map = new HashMap<>();
        map.put(EnmapFileUtils.METADATA_KEY, getFileName(EnmapFileUtils.METADATA_KEY, nodeSet));
        map.put(EnmapFileUtils.SPECTRAL_IMAGE_VNIR_KEY, getFileName(EnmapFileUtils.SPECTRAL_IMAGE_VNIR_KEY, nodeSet));
        map.put(EnmapFileUtils.SPECTRAL_IMAGE_SWIR_KEY, getFileName(EnmapFileUtils.SPECTRAL_IMAGE_SWIR_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CLASSES_KEY, getFileName(EnmapFileUtils.QUALITY_CLASSES_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CLOUD_KEY, getFileName(EnmapFileUtils.QUALITY_CLOUD_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CLOUDSHADOW_KEY, getFileName(EnmapFileUtils.QUALITY_CLOUDSHADOW_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_HAZE_KEY, getFileName(EnmapFileUtils.QUALITY_HAZE_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CIRRUS_KEY, getFileName(EnmapFileUtils.QUALITY_CIRRUS_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_SNOW_KEY, getFileName(EnmapFileUtils.QUALITY_SNOW_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_TESTFLAGS_SWIR_KEY, getFileName(EnmapFileUtils.QUALITY_TESTFLAGS_SWIR_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_TESTFLAGS_VNIR_KEY, getFileName(EnmapFileUtils.QUALITY_TESTFLAGS_VNIR_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_PIXELMASK_SWIR_KEY, getFileName(EnmapFileUtils.QUALITY_PIXELMASK_SWIR_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_PIXELMASK_VNIR_KEY, getFileName(EnmapFileUtils.QUALITY_PIXELMASK_VNIR_KEY, nodeSet));
        return map;
    }

    @Override
    public int getNumSpectralBands() throws IOException {
        return getNumVnirBands() + getNumSwirBands();
    }
}
