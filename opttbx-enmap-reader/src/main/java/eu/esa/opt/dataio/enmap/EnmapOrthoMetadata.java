package eu.esa.opt.dataio.enmap;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

abstract class EnmapOrthoMetadata extends EnmapMetadata {

    EnmapOrthoMetadata(Document doc, XPath xPath) {
        super(doc, xPath);
    }

    @Override
    public Dimension getSceneDimension() throws IOException {
        int width = Integer.parseInt(getNodeContent("/level_X/specific/widthOfOrthoScene"));
        int height = Integer.parseInt(getNodeContent("/level_X/specific/heightOfOrthoScene"));
        return new Dimension(width, height);
    }

    @Override
    public double getPixelSize() throws IOException {
        return Integer.parseInt(getNodeContent("/level_X/specific/pixelSizeOfOrthoScene"));
    }

    @Override
    public Map<String, String> getFileNameMap() throws IOException {
        NodeList nodeSet = getNodeSet("/level_X/product/productFileInformation/*/name");

        HashMap<String, String> map = new HashMap<>();
        map.put(EnmapFileUtils.METADATA_KEY, getCorrectedFileName(EnmapFileUtils.METADATA_KEY, nodeSet));
        map.put(EnmapFileUtils.SPECTRAL_IMAGE_KEY, getCorrectedFileName(EnmapFileUtils.SPECTRAL_IMAGE_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CLASSES_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_CLASSES_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CLOUD_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_CLOUD_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CLOUDSHADOW_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_CLOUDSHADOW_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_HAZE_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_HAZE_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_CIRRUS_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_CIRRUS_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_SNOW_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_SNOW_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_TESTFLAGS_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_TESTFLAGS_KEY, nodeSet));
        map.put(EnmapFileUtils.QUALITY_PIXELMASK_KEY, getCorrectedFileName(EnmapFileUtils.QUALITY_PIXELMASK_KEY, nodeSet));

        return map;
    }

    @Override
    public int getNumSpectralBands() throws IOException {
        return Integer.parseInt(getNodeContent("/level_X/product/image/merge/channels"));
    }
}
