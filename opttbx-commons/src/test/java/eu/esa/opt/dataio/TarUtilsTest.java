package eu.esa.opt.dataio;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TarUtilsTest {

    @Test
    @STTM("SNAP-3627,SNAP-3666")
    public void testIsTar() {
        assertTrue(TarUtils.isTar(Paths.get("matokaman.tar.gz")));
        assertTrue(TarUtils.isTar(Paths.get("matokaman.tgz")));
        assertTrue(TarUtils.isTar(Paths.get("matokaman.tar")));

        assertFalse(TarUtils.isTar(Paths.get("firlefanz.zip")));
        assertFalse(TarUtils.isTar(Paths.get("firlefanz.json")));
    }
}
