package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnmapFileUtilsTest {

    @Test
    public void testIsZip() {
        assertTrue(EnmapFileUtils.isZip(Paths.get("matokaman.zip")));
        assertFalse(EnmapFileUtils.isZip(Paths.get("firlefanz.xml")));
    }
}
