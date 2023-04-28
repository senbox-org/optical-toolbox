package eu.esa.opt.slstr.pdu.stitching;

import com.bc.ceres.core.ProgressMonitor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Tonio Fincke
 */
public class SlstrPduStitcherLongTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testStitchPDUs_AllSlstrL1BProductFiles() throws Exception {
        File targetDirectory = tempFolder.newFolder("AllSlstrL1BProductFiles");

        final File[] slstrFiles = TestUtils.getSlstrFiles();
        final File stitchedProductFile = SlstrPduStitcher.createStitchedSlstrL1BFile(targetDirectory, slstrFiles,
                null, ProgressMonitor.NULL);
        assertNotNull(stitchedProductFile);
        final File stitchedProductFileParentDirectory = stitchedProductFile.getParentFile();
        assert (new File(stitchedProductFileParentDirectory, "xfdumanifest.xml").exists());
        assert (new File(stitchedProductFileParentDirectory, "F1_BT_io.nc").exists());
        assert (new File(stitchedProductFileParentDirectory, "met_tx.nc").exists());
        assert (new File(stitchedProductFileParentDirectory, "viscal.nc").exists());
        assertEquals(targetDirectory, stitchedProductFileParentDirectory.getParentFile());
    }

}