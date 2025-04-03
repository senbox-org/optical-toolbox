package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.Context;

public class OlciContext implements Context {

    private final static String OLCI_USE_PIXELGEOCODING = "opttbx.reader.olci.pixelGeoCoding";
    private final static String SYSPROP_OLCI_PIXEL_CODING_INVERSE = "opttbx.reader.olci.pixelGeoCoding.inverse";
    private final static String SYSPROP_OLCI_TIE_POINT_CODING_FORWARD = "opttbx.reader.olci.tiePointGeoCoding.forward";
    private final static String OLCI_L1_CUSTOM_CALIBRATION = "opttbx.reader.olcil1.applyCustomCalibration";
    private final static String OLCI_L1_CALIBRATION_PATTERN = "opttbx.reader.olcil1.ID.calibration.TYPE";

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
}
