package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.dataio.ProductReader;
import org.junit.Before;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AquariusProductReaderPluginTest {

    private AquariusProductReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new AquariusProductReaderPlugIn();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetInputTypes() {
        final Class[] inputTypes = plugIn.getInputTypes();
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

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testGetTitleAttribute() {
        final Attribute attribute = new Attribute("theTitle", "hiho");
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        when(netcdfFile.findGlobalAttribute("Title")).thenReturn(attribute);
        when(netcdfFile.findGlobalAttribute("title")).thenReturn(null);

        Attribute titleAttribute = AquariusProductReaderPlugIn.getTitleAttribute(netcdfFile);
        assertEquals("hiho", titleAttribute.getStringValue());

        when(netcdfFile.findGlobalAttribute("Title")).thenReturn(null);
        when(netcdfFile.findGlobalAttribute("title")).thenReturn(attribute);
        titleAttribute = AquariusProductReaderPlugIn.getTitleAttribute(netcdfFile);
        assertEquals("hiho", titleAttribute.getStringValue());

        when(netcdfFile.findGlobalAttribute("Title")).thenReturn(null);
        when(netcdfFile.findGlobalAttribute("title")).thenReturn(null);
        titleAttribute = AquariusProductReaderPlugIn.getTitleAttribute(netcdfFile);
        assertNull(titleAttribute);
    }
}
