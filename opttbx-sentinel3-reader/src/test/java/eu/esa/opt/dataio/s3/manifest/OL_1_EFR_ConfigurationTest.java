package eu.esa.opt.dataio.s3.manifest;

import com.bc.ceres.annotation.STTM;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OL_1_EFR_ConfigurationTest {

    private OL_1_EFR_Configuration testConfig;

    @Before
    public void setUp() {
        testConfig = new OL_1_EFR_Configuration();
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetRasterWidth() {
        assertEquals(4865, testConfig.getRasterWidth());
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetRasterHeight() {
        assertEquals(4100, testConfig.getRasterHeight());
    }
}
