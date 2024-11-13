package eu.esa.opt.dataio.s3.util;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class CalibrationUtils {

    private final static String CALIBRATION_PATTERN = "opttbx.reader.PRODUCT_TYPE.ID.calibration.TYPE";

    public static final String OFFSET_PARAM = "offset";
    public static final String FACTOR_PARAM = "factor";
    public static final String ADJUSTMENT_FACTOR_PARAM = "adjustment_factor";

    public static final Map<String, int[]> SLSTR_BAND_RANGES = new HashMap<>();
    public static final Map<String, int[]> OLCI_BAND_RANGES = new HashMap<>();

    public enum PRODUCT_TYPE {
        SLSTRL1B,
        OLCIL1
    }

    static {
        OLCI_BAND_RANGES.put("Oa%02d_radiance", new int[]{1,21});
        OLCI_BAND_RANGES.put("Oa%02d_radiance_err", new int[]{1,21});
        OLCI_BAND_RANGES.put("Oa%02d_radiance_unc", new int[]{1,21});

        SLSTR_BAND_RANGES.put("F1_BT_fn", new int[]{1, 1});
        SLSTR_BAND_RANGES.put("F1_BT_orphan_fn", new int[]{1, 1});
        SLSTR_BAND_RANGES.put("F1_BT_fo", new int[]{1, 1});
        SLSTR_BAND_RANGES.put("F1_BT_orphan_fo", new int[]{1, 1});
        SLSTR_BAND_RANGES.put("F1_BT_in", new int[]{1, 1});
        SLSTR_BAND_RANGES.put("F1_BT_orphan_in", new int[]{1, 1});
        SLSTR_BAND_RANGES.put("F1_BT_io", new int[]{1, 1});
        SLSTR_BAND_RANGES.put("F1_BT_orphan_io", new int[]{1, 1});

        SLSTR_BAND_RANGES.put("S%d_exception_an", new int[]{1, 6});
        SLSTR_BAND_RANGES.put("S%d_exception_orphan_an", new int[]{1, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_an", new int[]{1, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_orphan_an", new int[]{1, 6});
        SLSTR_BAND_RANGES.put("S%d_exception_ao", new int[]{1, 6});
        SLSTR_BAND_RANGES.put("S%d_exception_orphan_ao", new int[]{1, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_ao", new int[]{1, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_orphan_ao", new int[]{1, 6});

        SLSTR_BAND_RANGES.put("S%d_exception_bn", new int[]{4, 6});
        SLSTR_BAND_RANGES.put("S%d_exception_orphan_bn", new int[]{4, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_bn", new int[]{4, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_orphan_bn", new int[]{4, 6});
        SLSTR_BAND_RANGES.put("S%d_exception_bo", new int[]{4, 6});
        SLSTR_BAND_RANGES.put("S%d_exception_orphan_bo", new int[]{4, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_bo", new int[]{4, 6});
        SLSTR_BAND_RANGES.put("S%d_radiance_orphan_bo", new int[]{4, 6});

        SLSTR_BAND_RANGES.put("S%d_BT_in", new int[]{7, 9});
        SLSTR_BAND_RANGES.put("S%d_BT_orphan_in", new int[]{7, 9});
        SLSTR_BAND_RANGES.put("S%d_exception_in", new int[]{7, 9});
        SLSTR_BAND_RANGES.put("S%d_exception_orphan_in", new int[]{7, 9});
        SLSTR_BAND_RANGES.put("S%d_BT_io", new int[]{7, 9});
        SLSTR_BAND_RANGES.put("S%d_BT_orphan_io", new int[]{7, 9});
        SLSTR_BAND_RANGES.put("S%d_exception_io", new int[]{7, 9});
        SLSTR_BAND_RANGES.put("S%d_exception_orphan_io", new int[]{7, 9});
    }

    public static String getBandName(String key, int bandNumber) {
        if (key.contains("%")) {
            return String.format(key, bandNumber);
        } else {
            return key;
        }
    }

    public static double getParameterValueFromConfig(String bandName, String param, Preferences preferences, String productType) {
        String key = getCalibrationKey(bandName, param, productType);
        return preferences.getDouble( key, Double.NaN);
    }

    public static String getCalibrationKey(String bandName, String param, String productType) {
        return CALIBRATION_PATTERN
                .replace("PRODUCT_TYPE", productType)
                .replace("ID", bandName.toLowerCase())
                .replace("TYPE", param);
    }

    public static Map<String, int[]> getBandRanges(String productType) {
        return productType.equals(PRODUCT_TYPE.SLSTRL1B.name().toLowerCase()) ? SLSTR_BAND_RANGES : OLCI_BAND_RANGES;
    }
}
