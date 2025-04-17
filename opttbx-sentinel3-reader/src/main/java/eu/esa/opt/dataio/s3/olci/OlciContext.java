package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.util.AbstractSensorContext;
import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.util.GeoLocationNames;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.runtime.Config;

import java.util.prefs.Preferences;

import static eu.esa.opt.dataio.s3.olci.OlciProductFactory.isLogScaledUnit;
import static eu.esa.opt.dataio.s3.olci.OlciProductFactory.stripLogFromDescription;

public class OlciContext extends AbstractSensorContext {

    private final static String OLCI_USE_PIXELGEOCODING = "opttbx.reader.olci.pixelGeoCoding";
    private final static String SYSPROP_OLCI_PIXEL_CODING_INVERSE = "opttbx.reader.olci.pixelGeoCoding.inverse";
    private final static String SYSPROP_OLCI_TIE_POINT_CODING_FORWARD = "opttbx.reader.olci.tiePointGeoCoding.forward";
    private final static String OLCI_L1_CUSTOM_CALIBRATION = "opttbx.reader.olcil1.applyCustomCalibration";
    private final static String OLCI_L1_CALIBRATION_PATTERN = "opttbx.reader.olcil1.ID.calibration.TYPE";

    private static final String[] LOG_SCALED_GEO_VARIABLE_NAMES = {"anw_443", "acdm_443", "aphy_443", "acdom_443", "bbp_443", "kd_490", "bbp_slope", "OWC",
            "ADG443_NN", "CHL_NN", "CHL_OC4ME", "KD490_M07", "TSM_NN"};

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
    public GeoLocationNames getGeoLocationNames() {
        return new GeoLocationNames(LON_VAR_NAME, LAT_VAR_NAME, TP_LON_VAR_NAME, TP_LAT_VAR_NAME);
    }

    @Override
    public void applyCalibration(Band band) {
        final Preferences preferences = loadPreferences();
        final boolean useCustomCalibration = preferences.getBoolean(OLCI_L1_CUSTOM_CALIBRATION, false);
        if (!useCustomCalibration) {
            return;
        }

        final String bandName = band.getName();
        final String calibrationOffsetPropertyName = getCalibrationProperty(bandName, "offset");
        final double calibrationOffset = preferences.getDouble(calibrationOffsetPropertyName, Double.NaN);
        if (!Double.isNaN(calibrationOffset)) {
            band.setScalingOffset(calibrationOffset);
        }

        final String calibrationFactorPropertyName = getCalibrationProperty(bandName, "factor");
        final double calibrationFactor = preferences.getDouble(calibrationFactorPropertyName, Double.NaN);
        if (!Double.isNaN(calibrationFactor)) {
            band.setScalingFactor(calibrationFactor);
        }
    }

    @Override
    public String bandNameToKey(String bandName) {
        return bandName.substring(0, 4);
    }

    @Override
    public void addDescriptionAndUnit(Band band, VariableDescriptor descriptor) {
        final String bandName = band.getName();
        if (OlciProductFactory.isUncertaintyBand(bandName) || isLogScaledGeophysicalData(bandName)) {
            final String unit = descriptor.getUnits();
            if (isLogScaledUnit(unit)) {
                band.setLog10Scaled(true);
                band.setUnit(OlciProductFactory.stripLogFromUnit(unit));

                final String description = descriptor.getDescription();
                band.setDescription(stripLogFromDescription(description));
            }
        } else {
            super.addDescriptionAndUnit(band, descriptor);
        }
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

    // package access for testing only tb 2025-04-14
    static boolean isLogScaledGeophysicalData(String bandName) {
        for (final String logScaledBandName : LOG_SCALED_GEO_VARIABLE_NAMES) {
            if (bandName.startsWith(logScaledBandName)) {
                return true;
            }
        }
        return false;
    }

    private Preferences loadPreferences() {
        return Config.instance("opttbx").load().preferences();
    }

    // package local for testing only tb 2024-04-14
    static String getCalibrationProperty(String bandName, String type) {
        return OLCI_L1_CALIBRATION_PATTERN.replace("ID", bandName.toLowerCase())
                .replace("TYPE", type);
    }
}
