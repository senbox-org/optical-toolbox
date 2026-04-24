package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.enmap.imgReader.EnmapImageReader;
import eu.esa.snap.core.dataio.cache.CacheManager;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.ProductCache;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class EnmapProductReaderTest {


    private EnmapProductReader readerUnderTest;
    private ProductCache originallyRegisteredCache;


    @After
    public void tearDown() {
        if (readerUnderTest != null) {
            try {
                readerUnderTest.close();
            } catch (Throwable ignored) {
            }
        }
        if (originallyRegisteredCache != null) {
            try {
                CacheManager.getInstance().remove(originallyRegisteredCache);
            } catch (Throwable ignored) {
            }
        }
    }



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
    @STTM("SNAP-4123")
    public void test_readBandRasterDataImpl_spectralBand_uses3DOffsetsShapes() throws Exception {
        EnmapMultiCubeCacheProvider cacheProvider = mock(EnmapMultiCubeCacheProvider.class);
        ProductCache productCache = mock(ProductCache.class);

        readerUnderTest = newReaderWithMocks(cacheProvider, productCache);

        Map spectralMap = (Map) getStateField(readerUnderTest, "spectralBandLayerIndexMap");
        spectralMap.put("BAND_X", 5);

        VariableDescriptor d = new VariableDescriptor();
        d.name = EnmapProductReader.SPECTRAL_CACHE_VARIABLE_NAME;
        d.layers = 10;
        when(cacheProvider.getVariableDescriptor(EnmapProductReader.SPECTRAL_CACHE_VARIABLE_NAME)).thenReturn(d);

        Band destBand = mock(Band.class);
        when(destBand.getName()).thenReturn("BAND_X");
        ProductData dest = ProductData.createInstance(ProductData.TYPE_INT16, 13 * 11);

        readerUnderTest.readBandRasterDataImpl(
                0, 0, 0, 0,
                1, 1,
                destBand,
                7, 9,
                11, 13,
                dest,
                ProgressMonitor.NULL
        );

        verify(productCache, times(1)).read(
                eq(EnmapProductReader.SPECTRAL_CACHE_VARIABLE_NAME),
                aryEq(new int[]{5, 9, 7}),
                aryEq(new int[]{1, 13, 11}),
                any(DataBuffer.class)
        );
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readBandRasterDataImpl_singleLayerCachedBand_uses2DOffsetsShapes() throws Exception {
        EnmapMultiCubeCacheProvider cacheProvider = mock(EnmapMultiCubeCacheProvider.class);
        ProductCache productCache = mock(ProductCache.class);
        readerUnderTest = newReaderWithMocks(cacheProvider, productCache);

        Map cachedBindings = (Map) getStateField(readerUnderTest, "cachedBandBindings");
        Object binding = newCachedBandBinding("CUBE_QL", 0);
        cachedBindings.put("QL_BAND", binding);

        VariableDescriptor d = new VariableDescriptor();
        d.name = "CUBE_QL";
        d.layers = 1;
        when(cacheProvider.getVariableDescriptor("CUBE_QL")).thenReturn(d);

        Band destBand = mock(Band.class);
        when(destBand.getName()).thenReturn("QL_BAND");
        ProductData dest = ProductData.createInstance(ProductData.TYPE_UINT8, 13 * 11);

        readerUnderTest.readBandRasterDataImpl(
                0, 0, 0, 0, 1, 1, destBand, 7, 9, 11, 13, dest, ProgressMonitor.NULL
        );

        verify(productCache, times(1)).read(
                eq("CUBE_QL"),
                aryEq(new int[]{9, 7}),
                aryEq(new int[]{13, 11}),
                any(DataBuffer.class)
        );
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readBandRasterDataImpl_multiLayerCachedBand_uses3DOffsetsShapes() throws Exception {
        EnmapMultiCubeCacheProvider cacheProvider = mock(EnmapMultiCubeCacheProvider.class);
        ProductCache productCache = mock(ProductCache.class);
        readerUnderTest = newReaderWithMocks(cacheProvider, productCache);

        Map cachedBindings = (Map) getStateField(readerUnderTest, "cachedBandBindings");
        Object binding = newCachedBandBinding("PIXELMASK_CUBE", 3);
        cachedBindings.put("PM_BAND", binding);

        VariableDescriptor d = new VariableDescriptor();
        d.name = "PIXELMASK_CUBE";
        d.layers = 200;
        when(cacheProvider.getVariableDescriptor("PIXELMASK_CUBE")).thenReturn(d);

        Band destBand = mock(Band.class);
        when(destBand.getName()).thenReturn("PM_BAND");
        ProductData dest = ProductData.createInstance(ProductData.TYPE_UINT8, 13 * 11);

        readerUnderTest.readBandRasterDataImpl(
                0, 0, 0, 0, 1, 1, destBand, 7, 9, 11, 13, dest, ProgressMonitor.NULL
        );

        verify(productCache, times(1)).read(
                eq("PIXELMASK_CUBE"),
                aryEq(new int[]{3, 9, 7}),
                aryEq(new int[]{1, 13, 11}),
                any(DataBuffer.class)
        );
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readBandRasterDataImpl_unknownBand_throws() throws Exception {
        EnmapMultiCubeCacheProvider cacheProvider = mock(EnmapMultiCubeCacheProvider.class);
        ProductCache productCache = mock(ProductCache.class);
        readerUnderTest = newReaderWithMocks(cacheProvider, productCache);

        Band destBand = mock(Band.class);
        when(destBand.getName()).thenReturn("UNKNOWN");

        try {
            readerUnderTest.readBandRasterDataImpl(
                    0, 0, 0, 0, 1, 1, destBand, 0, 0, 2, 2,
                    ProductData.createInstance(ProductData.TYPE_UINT8, 4),
                    ProgressMonitor.NULL
            );
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("No cache binding for band"));
        }

        verifyNoInteractions(productCache);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_ensureCubeRegistered_doesNotRegisterTwice() throws Exception {
        EnmapMultiCubeCacheProvider cacheProvider = mock(EnmapMultiCubeCacheProvider.class);
        ProductCache productCache = mock(ProductCache.class);
        readerUnderTest = newReaderWithMocks(cacheProvider, productCache);

        EnmapImageReader dummyReader = mock(EnmapImageReader.class);

        Method m = EnmapProductReader.class.getDeclaredMethod(
                "ensureCubeRegistered", String.class, EnmapImageReader.class, int.class, int.class, int.class, int.class
        );
        m.setAccessible(true);

        m.invoke(readerUnderTest, "VAR_X", dummyReader, 100, 200, 10, ProductData.TYPE_UINT8);
        m.invoke(readerUnderTest, "VAR_X", dummyReader, 100, 200, 10, ProductData.TYPE_UINT8);

        verify(cacheProvider, times(1)).addCube(
                eq("VAR_X"),
                eq(dummyReader),
                eq(100),
                eq(200),
                eq(10),
                eq(ProductData.TYPE_UINT8)
        );
    }

    @Test
    @STTM("SNAP-4105")
    @SuppressWarnings("unchecked")
    public void test_close_cleansCleanerState() throws Exception {
        readerUnderTest = new EnmapProductReader(null);
        EnmapImageReader imageReader = mock(EnmapImageReader.class);

        VirtualDir dataDir = mock(VirtualDir.class);
        VirtualDir tgzDataDir = mock(VirtualDir.class);

        ((Map<String, Integer>) getStateField(readerUnderTest, "spectralBandLayerIndexMap")).put("band_001", 0);
        ((Map<String, Object>) getStateField(readerUnderTest, "cachedBandBindings")).put("ql_band", newCachedBandBinding("QL_CUBE", 0));
        ((Set<String>) getStateField(readerUnderTest, "registeredCacheVariables")).add("VAR_1");
        ((List<EnmapImageReader>) getStateField(readerUnderTest, "imageReaderList")).add(imageReader);

        setStateField(readerUnderTest, "dataDir", dataDir);
        setStateField(readerUnderTest, "tgzDataDir", tgzDataDir);
        readerUnderTest.close();

        verify(imageReader, times(1)).close();
        verify(dataDir, times(1)).close();
        verify(tgzDataDir, times(1)).close();

        assertTrue(((Map<?, ?>) getStateField(readerUnderTest, "spectralBandLayerIndexMap")).isEmpty());
        assertTrue(((Map<?, ?>) getStateField(readerUnderTest, "cachedBandBindings")).isEmpty());
        assertTrue(((Set<?>) getStateField(readerUnderTest, "registeredCacheVariables")).isEmpty());
        assertTrue(((List<?>) getStateField(readerUnderTest, "imageReaderList")).isEmpty());
        assertNull(getStateField(readerUnderTest, "dataDir"));
        assertNull(getStateField(readerUnderTest, "tgzDataDir"));
    }

    @Test
    @STTM("SNAP-4105")
    @SuppressWarnings("unchecked")
    public void test_close_continuesCleanupWhenResourceCloseFails() throws Exception {
        readerUnderTest = new EnmapProductReader(null);
        EnmapImageReader failingReader = mock(EnmapImageReader.class);
        EnmapImageReader secondReader = mock(EnmapImageReader.class);

        VirtualDir failingDataDir = mock(VirtualDir.class);
        VirtualDir failingTgzDataDir = mock(VirtualDir.class);

        doThrow(new RuntimeException("reader-close-failed")).when(failingReader).close();
        doThrow(new RuntimeException("dataDir-close-failed")).when(failingDataDir).close();
        doThrow(new RuntimeException("tgzDataDir-close-failed")).when(failingTgzDataDir).close();

        ((Map<String, Integer>) getStateField(readerUnderTest, "spectralBandLayerIndexMap")).put("band_001", 0);
        ((Map<String, Object>) getStateField(readerUnderTest, "cachedBandBindings")).put("ql_band", newCachedBandBinding("QL_CUBE", 0));
        ((Set<String>) getStateField(readerUnderTest, "registeredCacheVariables")).add("VAR_1");

        List<EnmapImageReader> imageReaderList = (List<EnmapImageReader>) getStateField(readerUnderTest, "imageReaderList");
        imageReaderList.add(failingReader);
        imageReaderList.add(secondReader);

        setStateField(readerUnderTest, "dataDir", failingDataDir);
        setStateField(readerUnderTest, "tgzDataDir", failingTgzDataDir);

        readerUnderTest.close();

        verify(failingReader, times(1)).close();
        verify(secondReader, times(1)).close();
        verify(failingDataDir, times(1)).close();
        verify(failingTgzDataDir, times(1)).close();

        assertTrue(((Map<?, ?>) getStateField(readerUnderTest, "spectralBandLayerIndexMap")).isEmpty());
        assertTrue(((Map<?, ?>) getStateField(readerUnderTest, "cachedBandBindings")).isEmpty());
        assertTrue(((Set<?>) getStateField(readerUnderTest, "registeredCacheVariables")).isEmpty());
        assertTrue(((List<?>) getStateField(readerUnderTest, "imageReaderList")).isEmpty());
        assertNull(getStateField(readerUnderTest, "dataDir"));
        assertNull(getStateField(readerUnderTest, "tgzDataDir"));
    }


    private EnmapProductReader newReaderWithMocks(EnmapMultiCubeCacheProvider cacheProviderMock,
                                                  ProductCache productCacheMock) throws Exception {
        EnmapProductReader r = new EnmapProductReader(null);
        ProductCache orig = (ProductCache) getStateField(r, "productCache");
        originallyRegisteredCache = orig;
        CacheManager.getInstance().remove(orig);

        setStateField(r, "cacheProvider", cacheProviderMock);
        setStateField(r, "productCache", productCacheMock);
        return r;
    }

    private static Object newCachedBandBinding(String variableName, int layerIndex) throws Exception {
        Class<?> bindingClass = null;
        for (Class<?> c : EnmapProductReader.class.getDeclaredClasses()) {
            if ("CachedBandBinding".equals(c.getSimpleName())) {
                bindingClass = c;
                break;
            }
        }
        assertNotNull("CachedBandBinding inner class not found", bindingClass);

        Constructor<?> ctor = bindingClass.getDeclaredConstructor(String.class, int.class);
        ctor.setAccessible(true);
        return ctor.newInstance(variableName, layerIndex);
    }

    private static Object getStateField(Object reader, String name) throws Exception {
        Object state = getField(reader, "state");
        return getField(state, name);
    }

    private static void setStateField(Object reader, String name, Object value) throws Exception {
        Object state = getField(reader, "state");
        setField(state, name, value);
    }

    private static Object getField(Object o, String name) throws Exception {
        Field f = findField(o.getClass(), name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static void setField(Object o, String name, Object value) throws Exception {
        Field f = findField(o.getClass(), name);
        f.setAccessible(true);
        f.set(o, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignore) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
