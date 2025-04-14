package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.olci.OlciContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class SensorContextFactoryTest {

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetOlci_L1() {
        SensorContext olciL1 = SensorContextFactory.get("OL_1_EFR");
        assertNotNull(olciL1);
        assertTrue(olciL1 instanceof OlciContext);

        SensorContext secondCtx= SensorContextFactory.get("OL_1_EFR");
        assertSame(olciL1, secondCtx);

        olciL1 = SensorContextFactory.get("OL_1_ERR");
        assertNotNull(olciL1);
        assertTrue(olciL1 instanceof OlciContext);

        secondCtx= SensorContextFactory.get("OL_1_ERR");
        assertSame(olciL1, secondCtx);
    }
}
