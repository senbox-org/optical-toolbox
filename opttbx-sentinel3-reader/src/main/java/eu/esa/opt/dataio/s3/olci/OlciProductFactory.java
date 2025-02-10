package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.AbstractProductFactory;
import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.opt.dataio.s3.SentinelTimeCoding;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import eu.esa.opt.dataio.s3.util.S3NetcdfReaderFactory;
import eu.esa.opt.dataio.s3.util.S3Util;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.dataio.geocoding.forward.TiePointBilinearForward;
import org.esa.snap.core.dataio.geocoding.inverse.TiePointInverse;
import org.esa.snap.core.dataio.geocoding.util.RasterUtils;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.runtime.Config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tonio Fincke
 */
public abstract class OlciProductFactory extends AbstractProductFactory {

    public final static String OLCI_USE_PIXELGEOCODING = "opttbx.reader.olci.pixelGeoCoding";

    final static String SYSPROP_OLCI_TIE_POINT_CODING_FORWARD = "opttbx.reader.olci.tiePointGeoCoding.forward";
    private final static String[] excludedIDs = {"removedPixelsData"};
    private static final String UNCERTAINTY_REGEX = ".*_unc";
    private static final String LOG10_REGEX = "lg(.*)";
    private static final Pattern uncertaintyRegEx = Pattern.compile(UNCERTAINTY_REGEX);
    private static final Pattern log10RegEx = Pattern.compile(LOG10_REGEX);
    private static final String[] LOG_SCALED_GEO_VARIABLE_NAMES = {"anw_443", "acdm_443", "aphy_443", "acdom_443", "bbp_443", "kd_490", "bbp_slope", "OWC",
            "ADG443_NN", "CHL_NN", "CHL_OC4ME", "KD490_M07", "TSM_NN"};

    private final Map<String, Float> nameToWavelengthMap;
    private final Map<String, Float> nameToBandwidthMap;
    private final Map<String, Integer> nameToIndexMap;
    private int subSamplingX;
    private int subSamplingY;

    OlciProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
        nameToWavelengthMap = new HashMap<>();
        nameToBandwidthMap = new HashMap<>();
        nameToIndexMap = new HashMap<>();
    }

    // @todo 2 tb/tb move to product-type specific class, evt. read from manifest 2025-02-07
    static double getResolutionInKm(String productType) {
        switch (productType) {
            case "OL_1_EFR":
            case "OL_2_LFR":
            case "OL_2_WFR":
                return 0.3;

            case "OL_1_ERR":
            case "OL_2_LRR":
            case "OL_2_WRR":
                return 1.2;

            default:
                throw new IllegalArgumentException("unsupported product of type: " + productType);
        }
    }

    static int getMaxLineTimeDelta(String productType) {
        switch (productType) {
            case "OL_1_EFR":
            case "OL_2_LFR":
            case "OL_2_WFR":
                return 66057; // nominal time delta + 50%

            case "OL_1_ERR":
            case "OL_2_LRR":
            case "OL_2_WRR":
                return 264054; // nominal time delta + 50%

            default:
                throw new IllegalArgumentException("unsupported product of type: " + productType);
        }
    }

    static String[] getForwardAndInverseKeys_tiePointCoding() {
        final String[] codingNames = new String[2];

        final Preferences preferences = Config.instance("opttbx").preferences();
        codingNames[0] = preferences.get(SYSPROP_OLCI_TIE_POINT_CODING_FORWARD, TiePointBilinearForward.KEY);
        codingNames[1] = TiePointInverse.KEY;

        return codingNames;
    }

    public static boolean isUncertaintyBand(String bandName) {
        final Matcher matcher = uncertaintyRegEx.matcher(bandName);

        return matcher.matches();
    }

    public static boolean isLogScaledUnit(String units) {
        if (StringUtils.isNullOrEmpty(units)) {
            return false;
        }
        final Matcher matcher = log10RegEx.matcher(units);
        return matcher.matches();
    }

    static boolean isLogScaledGeophysicalData(String bandName) {
        for (final String logScaledBandName : LOG_SCALED_GEO_VARIABLE_NAMES) {
            if (bandName.startsWith(logScaledBandName)) {
                return true;
            }
        }
        return false;
    }

    static String stripLogFromUnit(String unitString) {
        if (StringUtils.isNullOrEmpty(unitString)) {
            return "";
        }

        final String trimmedUnit = unitString.trim();
        int logIdx = trimmedUnit.indexOf("lg(");
        if (logIdx >= 0) {
            final String logRemoved = trimmedUnit.substring(logIdx + 3, trimmedUnit.length() - 1);
            if (logRemoved.startsWith("re")) {
                return logRemoved.substring(3);
            }

            return logRemoved;
        }

        if (trimmedUnit.startsWith("lg")) {
            return trimmedUnit.substring(2);
        }
        return unitString;
    }

    static String stripLogFromDescription(String description) {
        if (StringUtils.isNullOrEmpty(description)) {
            return "";
        }

        final String trimmed = description.trim();
        return trimmed.replace("log10 scaled ", "");
    }

    public static File getFileFromVirtualDir(String fileName, VirtualDir virtualDir) throws IOException {
        final String[] allFiles = virtualDir.listAllFiles();
        for (String dirFileName : allFiles) {
            final String filenameFromVirtualDir = FileUtils.getFilenameFromPath(dirFileName);
            if (filenameFromVirtualDir.equalsIgnoreCase(fileName)) {
                return virtualDir.getFile(dirFileName);
            }
        }
        return null;
    }

    @Override
    protected List<String> getFileNames(Manifest manifest) {
        return manifest.getFileNames(excludedIDs);
    }

    @Override
    protected void processProductSpecificMetadata(MetadataElement metadataElement) {
        final MetadataElement olciInformationElement = metadataElement.getElement("olciProductInformation");
        final MetadataElement samplingParametersElement = olciInformationElement.getElement("samplingParameters");
        subSamplingY = Integer.parseInt(samplingParametersElement.getAttribute("rowsPerTiePoint").getData().toString());
        subSamplingX = Integer.parseInt(samplingParametersElement.getAttribute("columnsPerTiePoint").getData().toString());
        final MetadataElement bandDescriptionsElement = olciInformationElement.getElement("bandDescriptions");
        if (bandDescriptionsElement != null) {
            for (int i = 0; i < bandDescriptionsElement.getNumElements(); i++) {
                final MetadataElement bandDescriptionElement = bandDescriptionsElement.getElementAt(i);
                final String bandName = bandDescriptionElement.getAttribute("name").getData().getElemString();
                final float wavelength =
                        Float.parseFloat(bandDescriptionElement.getAttribute("centralWavelength").getData().getElemString());
                final float bandWidth =
                        Float.parseFloat(bandDescriptionElement.getAttribute("bandWidth").getData().getElemString());
                nameToWavelengthMap.put(bandName, wavelength);
                nameToBandwidthMap.put(bandName, bandWidth);
                nameToIndexMap.put(bandName, i);
            }
        }
    }

    private float getWavelength(String name) {
        return nameToWavelengthMap.get(name);
    }

    private float getBandwidth(String name) {
        return nameToBandwidthMap.get(name);
    }

    private int getBandindex(String name) {
        return nameToIndexMap.get(name);
    }

    @Override
    protected RasterDataNode addSpecialNode(Product masterProduct, Band sourceBand, Product targetProduct) {
        final String sourceBandName = sourceBand.getName();
        if (targetProduct.containsBand(sourceBandName)) {
            sourceBand.setName("TP_" + sourceBandName);
        }
        return copyBandAsTiePointGrid(sourceBand, targetProduct, subSamplingX, subSamplingY, 0.0f, 0.0f);
    }

    @Override
    protected void setGeoCoding(Product targetProduct) throws IOException {
        if (Config.instance("opttbx").load().preferences().getBoolean(OLCI_USE_PIXELGEOCODING, true)) {
            setPixelGeoCoding(targetProduct);
        } else {
            setTiePointGeoCoding(targetProduct);
        }
    }

    private void setPixelGeoCoding(Product targetProduct) throws IOException {
        final String lonVariableName = "longitude";
        final String latVariableName = "latitude";
        final Band lonBand = targetProduct.getBand(lonVariableName);
        final Band latBand = targetProduct.getBand(latVariableName);
        if (lonBand == null || latBand == null) {
            return;
        }

        final double[] longitudes = RasterUtils.loadGeoData(lonBand);
        final double[] latitudes = RasterUtils.loadGeoData(latBand);

        final double resolutionInKilometers = getResolutionInKm(targetProduct.getProductType());
        final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes, lonVariableName, latVariableName,
                lonBand.getRasterWidth(), lonBand.getRasterHeight(), resolutionInKilometers);

        final String[] codingKeys = S3Util.getForwardAndInverseKeys_pixelCoding(S3Util.SYSPROP_OLCI_PIXEL_CODING_INVERSE);
        final ForwardCoding forward = ComponentFactory.getForward(codingKeys[0]);
        final InverseCoding inverse = ComponentFactory.getInverse(codingKeys[1]);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();

        targetProduct.setSceneGeoCoding(geoCoding);
    }

    private void setTiePointGeoCoding(Product targetProduct) {
        String lonVarName = "longitude";
        String latVarName = "latitude";
        TiePointGrid lonGrid = targetProduct.getTiePointGrid(lonVarName);
        TiePointGrid latGrid = targetProduct.getTiePointGrid(latVarName);

        if (latGrid == null || lonGrid == null) {
            lonVarName = "TP_longitude";
            latVarName = "TP_latitude";
            lonGrid = targetProduct.getTiePointGrid(lonVarName);
            latGrid = targetProduct.getTiePointGrid(latVarName);
            if (latGrid == null || lonGrid == null) {
                return;
            }
        }

        final double[] longitudes = loadTiePointData(lonVarName);
        final double[] latitudes = loadTiePointData(latVarName);
        final double resolutionInKilometers = getResolutionInKm(targetProduct.getProductType());

        final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes, lonVarName, latVarName,
                lonGrid.getGridWidth(), lonGrid.getGridHeight(),
                targetProduct.getSceneRasterWidth(), targetProduct.getSceneRasterHeight(), resolutionInKilometers,
                lonGrid.getOffsetX(), lonGrid.getOffsetY(),
                lonGrid.getSubSamplingX(), lonGrid.getSubSamplingY());

        final String[] codingKeys = getForwardAndInverseKeys_tiePointCoding();
        final ForwardCoding forward = ComponentFactory.getForward(codingKeys[0]);
        final InverseCoding inverse = ComponentFactory.getInverse(codingKeys[1]);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();

        targetProduct.setSceneGeoCoding(geoCoding);
    }

    @Override
    protected void configureTargetNode(Band sourceBand, RasterDataNode targetNode) {
        final String targetNodeName = targetNode.getName();
        if (targetNodeName.matches("Oa[0-2][0-9].*")) {
            if (targetNode instanceof Band targetBand) {
                String cutName = targetBand.getName().substring(0, 4);
                targetBand.setSpectralBandIndex(getBandindex(cutName));
                targetBand.setSpectralWavelength(getWavelength(cutName));
                targetBand.setSpectralBandwidth(getBandwidth(cutName));
                applyCustomCalibration(targetBand);
            }
        }

        if (isUncertaintyBand(targetNodeName) || isLogScaledGeophysicalData(targetNodeName)) {
            final String unit = sourceBand.getUnit();
            if (isLogScaledUnit(unit)) {
                targetNode.setLog10Scaled(true);
                targetNode.setUnit(stripLogFromUnit(unit));

                final String description = sourceBand.getDescription();
                targetNode.setDescription(stripLogFromDescription(description));
            }

            targetNode.setValidPixelExpression(getValidExpression());
        }
    }

    protected void applyCustomCalibration(Band targetBand) {
        //empty implementation
    }

    @Override
    protected void setTimeCoding(Product targetProduct, VirtualDir virtualDir) throws IOException {
        setTimeCoding(targetProduct, virtualDir, "time_coordinates.nc", "time_stamp");

        final SentinelTimeCoding sceneTimeCoding = (SentinelTimeCoding) targetProduct.getSceneTimeCoding();
        final int maxDelta = sceneTimeCoding.getMaxDelta();
        final int maxLineTimeDelta = getMaxLineTimeDelta(targetProduct.getProductType());
        if (maxDelta > maxLineTimeDelta) {
            throw new IOException("Data gap detected - product rejected");
        }
    }

    protected abstract String getValidExpression();

    @Override
    protected Product readProduct(String fileName, Manifest manifest, VirtualDir virtualDir) throws IOException {
        final File file = getFileFromVirtualDir(fileName, virtualDir);

        final S3NetcdfReader reader = S3NetcdfReaderFactory.createS3NetcdfProduct(file);
        addSeparatingDimensions(reader.getSuffixesForSeparatingDimensions());
        return reader.readProductNodes(file, null);
    }
}
