package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.Context;
import eu.esa.opt.dataio.s3.util.GeoLocationNames;

public class OlciContext implements Context {

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
}
