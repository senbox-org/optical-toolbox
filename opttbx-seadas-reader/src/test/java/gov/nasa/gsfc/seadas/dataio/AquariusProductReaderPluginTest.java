package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.dataio.ProductReader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AquariusProductReaderPluginTest {

    private AquariusProductReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new AquariusProductReaderPlugIn();
    }

    @Test
    public void testGetInputTypes() {
        Class[] inputTypes = plugIn.getInputTypes();
        assertEquals(2, inputTypes.length);
        assertEquals(String.class, inputTypes[0]);
        assertEquals(File.class, inputTypes[1]);
    }

    @Test
    public void testGetDefaultFileExtensions() {
        final String[] defaultFileExtensions = plugIn.getDefaultFileExtensions();
        assertEquals(3, defaultFileExtensions.length);
        assertEquals(".h5",  defaultFileExtensions[0]);
        assertEquals(".L2_SCI_V1.3",  defaultFileExtensions[1]);
        assertEquals(".main",  defaultFileExtensions[2]);
    }

    @Test
    public void testCreateReaderInstance() {
        final ProductReader readerInstance = plugIn.createReaderInstance();
        assertTrue(readerInstance instanceof SeadasProductReader);
    }
}
