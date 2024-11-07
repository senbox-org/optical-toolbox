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
}
