package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.VirtualDir;
import com.bc.ceres.util.CleanerRegistry;
import eu.esa.opt.dataio.enmap.imgReader.EnmapImageReader;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class EnmapProductReaderTest {

    @Test
    public void testGetEPSGCode() throws Exception {
        assertEquals("EPSG:32608", EnmapProductReader.getEPSGCode("UTM_Zone8_North"));
        assertEquals("EPSG:32708", EnmapProductReader.getEPSGCode("UTM_Zone08_South"));
        assertEquals("EPSG:32632", EnmapProductReader.getEPSGCode("UTM_Zone32_North"));
        assertEquals("EPSG:32714", EnmapProductReader.getEPSGCode("UTM_Zone14_South"));
        assertEquals("EPSG:3035", EnmapProductReader.getEPSGCode("LAEA-ETRS89"));
        assertEquals("EPSG:4326", EnmapProductReader.getEPSGCode("Geographic"));
    }

    @Test
    public void testGetEPSGCode_invalidInput() throws Exception {
        try {
            EnmapProductReader.getEPSGCode("Heffalump");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }


    @Test
    @STTM("SNAP-4105")
    public void test_close_runsCleanerStateAndClearsResources() throws Exception {
        EnmapProductReader reader = new EnmapProductReader(mock(EnmapProductReaderPlugIn.class));
        EnmapImageReader imageReader1 = mock(EnmapImageReader.class);
        EnmapImageReader imageReader2 = mock(EnmapImageReader.class);
        VirtualDir dataDir = mock(VirtualDir.class);
        VirtualDir tgzDataDir = mock(VirtualDir.class);

        getImageReaderList(reader).add(imageReader1);
        getImageReaderList(reader).add(imageReader2);
        getBandImageMap(reader).put("band_001", new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY));
        setStateField(reader, "dataDir", dataDir);
        setStateField(reader, "tgzDataDir", tgzDataDir);

        reader.close();
        verify(imageReader1, times(1)).close();
        verify(imageReader2, times(1)).close();
        verify(dataDir, times(1)).close();
        verify(tgzDataDir, times(1)).close();

        assertTrue(getImageReaderList(reader).isEmpty());
        assertTrue(getBandImageMap(reader).isEmpty());
        assertNull(getStateField(reader, "dataDir"));
        assertNull(getStateField(reader, "tgzDataDir"));
    }

    @Test
    @STTM("SNAP-4105")
    public void test_close_isIdempotent() throws Exception {
        EnmapProductReader reader = new EnmapProductReader(mock(EnmapProductReaderPlugIn.class));
        EnmapImageReader imageReader = mock(EnmapImageReader.class);
        VirtualDir dataDir = mock(VirtualDir.class);
        getImageReaderList(reader).add(imageReader);
        setStateField(reader, "dataDir", dataDir);

        reader.close();
        reader.close();
        verify(imageReader, times(1)).close();
        verify(dataDir, times(1)).close();

        assertTrue(getImageReaderList(reader).isEmpty());
        assertTrue(getBandImageMap(reader).isEmpty());
        assertNull(getStateField(reader, "dataDir"));
    }

    @Test
    @STTM("SNAP-4105")
    public void test_cleanerRegistryCleanup_runsRegisteredState() throws Exception {
        EnmapProductReader reader = new EnmapProductReader(mock(EnmapProductReaderPlugIn.class));
        EnmapImageReader imageReader = mock(EnmapImageReader.class);
        VirtualDir dataDir = mock(VirtualDir.class);
        VirtualDir tgzDataDir = mock(VirtualDir.class);

        getImageReaderList(reader).add(imageReader);
        getBandImageMap(reader).put("quality", mock(RenderedImage.class));
        setStateField(reader, "dataDir", dataDir);
        setStateField(reader, "tgzDataDir", tgzDataDir);

        CleanerRegistry.getInstance().cleanup(reader);

        verify(imageReader, times(1)).close();
        verify(dataDir, times(1)).close();
        verify(tgzDataDir, times(1)).close();

        assertTrue(getImageReaderList(reader).isEmpty());
        assertTrue(getBandImageMap(reader).isEmpty());
        assertNull(getStateField(reader, "dataDir"));
        assertNull(getStateField(reader, "tgzDataDir"));
    }

    @Test
    @STTM("SNAP-4105")
    public void test_cleanup_continuesWhenOneImageReaderFails() throws Exception {
        EnmapProductReader reader = new EnmapProductReader(mock(EnmapProductReaderPlugIn.class));
        EnmapImageReader failingReader = mock(EnmapImageReader.class);
        EnmapImageReader secondReader = mock(EnmapImageReader.class);
        VirtualDir dataDir = mock(VirtualDir.class);

        doThrow(new RuntimeException("boom")).when(failingReader).close();
        getImageReaderList(reader).add(failingReader);
        getImageReaderList(reader).add(secondReader);
        setStateField(reader, "dataDir", dataDir);

        reader.close();
        verify(failingReader, times(1)).close();
        verify(secondReader, times(1)).close();
        verify(dataDir, times(1)).close();

        assertTrue(getImageReaderList(reader).isEmpty());
        assertTrue(getBandImageMap(reader).isEmpty());
        assertNull(getStateField(reader, "dataDir"));
    }

    @SuppressWarnings("unchecked")
    private static List<EnmapImageReader> getImageReaderList(EnmapProductReader reader) throws Exception {
        return (List<EnmapImageReader>) getStateField(reader, "imageReaderList");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, RenderedImage> getBandImageMap(EnmapProductReader reader) throws Exception {
        return (Map<String, RenderedImage>) getStateField(reader, "bandImageMap");
    }

    private static Object getStateField(EnmapProductReader reader, String fieldName) throws Exception {
        Object state = getState(reader);
        Field field = state.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(state);
    }

    private static void setStateField(EnmapProductReader reader, String fieldName, Object value) throws Exception {
        Object state = getState(reader);
        Field field = state.getClass().getDeclaredField(fieldName);
        field.setAccessible(true); field.set(state, value);
    }

    private static Object getState(EnmapProductReader reader) throws Exception {
        Field field = EnmapProductReader.class.getDeclaredField("state");
        field.setAccessible(true);
        return field.get(reader);
    }
}
