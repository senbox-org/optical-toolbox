package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.SensorContext;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.util.GeoLocationNames;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

public class OlciContext implements SensorContext {

    private final static String OLCI_USE_PIXELGEOCODING = "opttbx.reader.olci.pixelGeoCoding";
    private final static String SYSPROP_OLCI_PIXEL_CODING_INVERSE = "opttbx.reader.olci.pixelGeoCoding.inverse";
    private final static String SYSPROP_OLCI_TIE_POINT_CODING_FORWARD = "opttbx.reader.olci.tiePointGeoCoding.forward";
    private final static String OLCI_L1_CUSTOM_CALIBRATION = "opttbx.reader.olcil1.applyCustomCalibration";
    private final static String OLCI_L1_CALIBRATION_PATTERN = "opttbx.reader.olcil1.ID.calibration.TYPE";

    private static final String LON_VAR_NAME = "longitude";
    private static final String LAT_VAR_NAME = "latitude";
    private static final String TP_LON_VAR_NAME = "TP_longitude";
    private static final String TP_LAT_VAR_NAME = "TP_latitude";

    @Override
    public String getInversePixelGeoCodingKey() {
        return SYSPROP_OLCI_PIXEL_CODING_INVERSE;
    }

    @Override
    public String getTiePointForwardGeoCodingKey() {
        return SYSPROP_OLCI_TIE_POINT_CODING_FORWARD;
    }

    @Override
    public String getUsePixelGeoCodingKey() {
        return OLCI_USE_PIXELGEOCODING;
    }

    @Override
    public String getCustomCalibrationKey() {
        return OLCI_L1_CUSTOM_CALIBRATION;
    }

    @Override
    public String getCalibrationPatternKey() {
        return OLCI_L1_CALIBRATION_PATTERN;
    }

    @Override
    public GeoLocationNames getGeoLocationNames() {
       return new GeoLocationNames(LON_VAR_NAME, LAT_VAR_NAME, TP_LON_VAR_NAME, TP_LAT_VAR_NAME);
    }

    @Override
    public String bandNameToKey(String bandName) {
        return bandName.substring(0, 4);
    }

    @Override
    public MetadataElement getBandDescriptionsElement(Manifest manifest) {
        final MetadataElement metadata = manifest.getMetadata();
        final MetadataElement metadataSection = metadata.getElement("metadataSection");
        final MetadataElement olciProductInformation = metadataSection.getElement("olciProductInformation");

        return olciProductInformation.getElement("bandDescriptions");
    }

    @Override
    public double getResolutionInKm(String productType) {
        if (productType.contains("RR")) {
            return 1.2;
        } else if (productType.contains("FR")) {
            return 0.3;
        } else {
            throw new RuntimeException("invalid product type: " + productType);
        }
    }
}
