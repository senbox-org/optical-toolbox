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

    @Test
    @STTM("SNAP-3627")
    public void testIsTar() {
        assertTrue(EnmapFileUtils.isTar(Paths.get("matokaman.tar.gz")));
        assertTrue(EnmapFileUtils.isTar(Paths.get("matokaman.tgz")));
        assertTrue(EnmapFileUtils.isTar(Paths.get("matokaman.tar")));

        assertFalse(EnmapFileUtils.isTar(Paths.get("firlefanz.zip")));
        assertFalse(EnmapFileUtils.isTar(Paths.get("firlefanz.json")));
    }
}
