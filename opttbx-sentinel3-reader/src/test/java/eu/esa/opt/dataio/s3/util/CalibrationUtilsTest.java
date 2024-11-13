package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;
import java.util.Map;

import static org.junit.Assert.*;

public class CalibrationUtilsTest {

    @Test
    @STTM("SNAP-3701")
    public void getBandName() {
        String key = "Oa%02d_radiance";
        String bandName = CalibrationUtils.getBandName(key, 2);
        String key2 = "S%d_radiance_bo";
        String bandName2 = CalibrationUtils.getBandName(key2, 21);

        assertEquals("Oa02_radiance", bandName);
        assertEquals("S21_radiance_bo", bandName2);
    }

    @Test
    @STTM("SNAP-3701")
    public void getCalibrationKey() {
        String bandName = "Oa04_radiance";
        String param = CalibrationUtils.FACTOR_PARAM;
        String productType = "olcil1";

        String key = CalibrationUtils.getCalibrationKey(bandName, param, productType);
        assertEquals("opttbx.reader.olcil1.oa04_radiance.calibration.factor", key);

        String bandName2 = "S4_radiance_an";
        String param2 = CalibrationUtils.OFFSET_PARAM;
        String param3 = CalibrationUtils.ADJUSTMENT_FACTOR_PARAM;
        String productType2 = "slstrl1b";

        String key2 = CalibrationUtils.getCalibrationKey(bandName2, param2, productType2);
        String key3 = CalibrationUtils.getCalibrationKey(bandName2, param3, productType2);

        assertEquals("opttbx.reader.slstrl1b.s4_radiance_an.calibration.offset", key2);
        assertEquals("opttbx.reader.slstrl1b.s4_radiance_an.calibration.adjustment_factor", key3);
    }

    @Test
    @STTM("SNAP-3701")
    public void getBandRanges() {
        Map<String, int[]> olciMap = CalibrationUtils.getBandRanges("olcil1");
        Map<String, int[]> slstrMap = CalibrationUtils.getBandRanges("slstrl1b");

        assertEquals(3, olciMap.size());
        assertEquals(21, olciMap.get("Oa%02d_radiance")[1]);

        assertEquals(6, slstrMap.size());
        assertEquals(4, slstrMap.get("S%d_radiance_co")[0]);
    }
}