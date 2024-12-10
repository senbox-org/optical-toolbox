package eu.esa.opt.dataio.s3.manifest;

import com.bc.ceres.annotation.STTM;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SL_1_RBT_ConfigurationTest {
    private SL_1_RBT_Configuration testConfig;

    @Before
    public void setUp() {
        testConfig = new SL_1_RBT_Configuration();
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetRasterWidth() {
        assertEquals(3000, testConfig.getRasterWidth());
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetRasterHeight() {
        assertEquals(2400, testConfig.getRasterHeight());
    }
}
