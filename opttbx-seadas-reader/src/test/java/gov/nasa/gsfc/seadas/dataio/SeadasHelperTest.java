package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.runtime.EngineConfig;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SeadasHelperTest {

    @Test
    @STTM("SNAP-3882")
    public void testCheckInputObject_fileInput() throws IOException {
        DecodeQualification decodeQualification = SeadasHelper.checkInputObject(new File("out/of/reality/and/not/there"));
        assertEquals(DecodeQualification.UNABLE, decodeQualification);

        final File tempFile = File.createTempFile("whatever", "dont_care");
        decodeQualification = SeadasHelper.checkInputObject(tempFile);
        assertEquals(DecodeQualification.SUITABLE, decodeQualification);
    }

    @Test
    @STTM("SNAP-3882")
    public void testCheckInputObject_directoryInput() throws IOException {
        final File directory = EngineConfig.instance().userDir().toFile();
        assertTrue(directory.isDirectory());

        final DecodeQualification decodeQualification = SeadasHelper.checkInputObject(directory);
        assertEquals(DecodeQualification.UNABLE, decodeQualification);
    }

    @Test
    @STTM("SNAP-3882")
    public void testGetInputFile() {
        final File inputFile = new File("nasenmann");

        File fileRetrieved = SeadasHelper.getInputFile(inputFile);
        assertNotNull(fileRetrieved);
        assertTrue(fileRetrieved.getAbsolutePath().contains("nasenmann"));

        fileRetrieved = SeadasHelper.getInputFile("heffalump");
        assertNotNull(fileRetrieved);
        assertTrue(fileRetrieved.getAbsolutePath().contains("heffalump"));

        fileRetrieved = SeadasHelper.getInputFile(265.0987);
        assertNull(fileRetrieved);
    }

    private static final String REMOTE =
            "https://obdaac-tea.earthdatacloud.nasa.gov/ob-cumulus-prod-public/PACE_OCI.20260901T235307.L2.OC_AOP.V3_2.nc";

    @Test
    public void testRemoteLocationIsPassedThroughAsString() {
        assertTrue(SeadasHelper.isRemote(REMOTE));
        assertEquals(REMOTE, SeadasHelper.getNetcdfInput("  " + REMOTE + " "));
        assertNull("a URL is not a local file", SeadasHelper.getInputFile(REMOTE));
    }

    @Test
    public void testGetNetcdfInput_localInputStillResolvesToFile() {
        assertFalse(SeadasHelper.isRemote("/data/PACE_OCI.L2.nc"));
        assertEquals(new File("/data/PACE_OCI.L2.nc"), SeadasHelper.getNetcdfInput("/data/PACE_OCI.L2.nc"));
        assertEquals(new File("a.nc"), SeadasHelper.getNetcdfInput(new File("a.nc")));
        assertNull(SeadasHelper.getNetcdfInput(42));
    }

    @Test
    public void testCheckNetcdfInput() {
        assertEquals(DecodeQualification.SUITABLE, SeadasHelper.checkNetcdfInput(REMOTE));
        assertEquals(DecodeQualification.SUITABLE, SeadasHelper.checkNetcdfInput("https://host/x.h5?sig=abc"));
        assertEquals(DecodeQualification.UNABLE, SeadasHelper.checkNetcdfInput("https://host/browse.png"));
        assertEquals(DecodeQualification.UNABLE, SeadasHelper.checkNetcdfInput(new File("/does/not/exist.nc")));
        assertEquals(DecodeQualification.UNABLE, SeadasHelper.checkNetcdfInput(null));
    }
}
