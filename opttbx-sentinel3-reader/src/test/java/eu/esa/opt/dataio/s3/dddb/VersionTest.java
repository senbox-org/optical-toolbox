package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionTest {

    @Test
    @STTM("SNAP-4253")
    public void testConstructionAndGet() {
        final Version version = new Version("baseCo", "proBas");
        assertEquals("baseCo", version.baselineCollection());
        assertEquals("proBas", version.processingBaseline());
    }
}
