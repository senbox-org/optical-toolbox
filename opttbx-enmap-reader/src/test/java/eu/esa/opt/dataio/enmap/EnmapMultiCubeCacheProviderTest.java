package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.enmap.imgReader.EnmapImageReader;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class EnmapMultiCubeCacheProviderTest {


    private EnmapMultiCubeCacheProvider provider;


    @Before
    public void setUp() {
        provider = new EnmapMultiCubeCacheProvider();
    }


    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_createsTargetData_andInvokesReader() throws Exception {
        TestReader r = new TestReader(new Dimension(32, 32));
        provider.addCube("CUBE_R", r, 100, 200, 50, ProductData.TYPE_UINT8);

        int[] offsets = {3, 10, 20};
        int[] shapes  = {1, 5, 4};

        DataBuffer out = provider.readCacheBlock("CUBE_R", offsets, shapes, null);
        assertNotNull(out);

        assertEquals(1, r.calls);
        assertEquals(3, r.lastStartLayer);
        assertEquals(1, r.lastNumLayers);
        assertEquals(20, r.lastX);
        assertEquals(10, r.lastY);
        assertEquals(4, r.lastW);
        assertEquals(5, r.lastH);

        assertNotNull(r.lastTarget);
        assertEquals(ProductData.TYPE_UINT8, r.lastTarget.getType());
        assertEquals(1 * 5 * 4, r.lastTarget.getNumElems());

        ProductData fromBuffer = extractProductData(out);
        assertSame(r.lastTarget, fromBuffer);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_usesProvidedTargetDataInstance() throws Exception {
        TestReader r = new TestReader(new Dimension(32, 32));
        provider.addCube("CUBE_T", r, 100, 200, 50, ProductData.TYPE_INT16);

        int[] offsets = {7, 0, 0};
        int[] shapes  = {2, 3, 4};

        ProductData provided = ProductData.createInstance(ProductData.TYPE_INT16, 2 * 3 * 4);
        DataBuffer out = provider.readCacheBlock("CUBE_T", offsets, shapes, provided);

        assertNotNull(out);
        assertSame(provided, r.lastTarget);

        ProductData fromBuffer = extractProductData(out);
        assertSame(provided, fromBuffer);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_addCube_setsDescriptorFields_defaultTileLayersClampedToNumLayers() throws Exception {
        TestReader r = new TestReader(new Dimension(64, 32));
        provider.addCube("CUBE_A", r, 1000, 2000, 7, ProductData.TYPE_INT16);

        VariableDescriptor d = provider.getVariableDescriptor("CUBE_A");
        assertNotNull(d);
        assertEquals("CUBE_A", d.name);
        assertEquals(ProductData.TYPE_INT16, d.dataType);
        assertEquals(1000, d.width);
        assertEquals(2000, d.height);
        assertEquals(7, d.layers);
        assertEquals(64, d.tileWidth);
        assertEquals(32, d.tileHeight);
        assertEquals(7, d.tileLayers);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_addCube_tileLayersClampedTo1_whenTileLayersNonPositive() throws Exception {
        TestReader r = new TestReader(new Dimension(16, 16));
        provider.addCube("CUBE_B", r, 10, 20, 10, ProductData.TYPE_UINT8, 0);

        VariableDescriptor d = provider.getVariableDescriptor("CUBE_B");
        assertEquals(1, d.tileLayers);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_addCube_tileLayersClampedTo1_whenNumLayersNonPositive() throws Exception {
        TestReader r = new TestReader(new Dimension(8, 8));
        provider.addCube("CUBE_C", r, 10, 20, 0, ProductData.TYPE_UINT8, 32);

        VariableDescriptor d = provider.getVariableDescriptor("CUBE_C");
        assertEquals(0, d.layers);
        assertEquals(1, d.tileLayers);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_addCube_duplicateVariableThrows() throws Exception {
        TestReader r = new TestReader(new Dimension(32, 32));
        provider.addCube("CUBE_DUP", r, 10, 10, 3, ProductData.TYPE_UINT8);

        try {
            provider.addCube("CUBE_DUP", r, 10, 10, 3, ProductData.TYPE_UINT8);
            fail("Expected IOException for duplicate variable");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Duplicate cache variable"));
        }
    }

    @Test
    @STTM("SNAP-4123")
    public void test_getVariableDescriptor_unknownThrows() {
        try {
            provider.getVariableDescriptor("DOES_NOT_EXIST");
            fail("Expected IOException for unknown cache variable");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Unknown cache variable"));
        }
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_invalidOffsetsShapesThrows() throws Exception {
        TestReader r = new TestReader(new Dimension(32, 32));
        provider.addCube("CUBE_S", r, 10, 10, 5, ProductData.TYPE_UINT8);

        assertExpected2Dor3D(provider, "CUBE_S", null, new int[]{1, 2, 3});
        assertExpected2Dor3D(provider, "CUBE_S", new int[]{0, 0, 0}, null);

        assertExpected2Dor3D(provider, "CUBE_S", new int[]{0}, new int[]{1});
        assertExpected2Dor3D(provider, "CUBE_S", new int[]{0, 1}, new int[]{1, 2, 3});
        assertExpected2Dor3D(provider, "CUBE_S", new int[]{0, 1, 2}, new int[]{1, 2});
        assertExpected2Dor3D(provider, "CUBE_S", new int[]{0, 1, 2, 3}, new int[]{1, 2, 3, 4});
    }

    private static void assertExpected2Dor3D(EnmapMultiCubeCacheProvider p, String var, int[] offsets, int[] shapes) {
        try {
            p.readCacheBlock(var, offsets, shapes, null);
            fail("Expected IOException for invalid offsets/shapes");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Expected 2D/3D offsets and shapes"));
        }
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_unknownVariableThrows() throws Exception {
        try {
            provider.readCacheBlock("NOPE", new int[]{0, 0, 0}, new int[]{1, 1, 1}, null);
            fail("Expected IOException for unknown cache variable");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Unknown cache variable"));
        }
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_createsTargetData_andInvokesReader_3D() throws Exception {
        TestReader r = new TestReader(new Dimension(32, 32));
        provider.addCube("CUBE_R", r, 100, 200, 50, ProductData.TYPE_UINT8);

        int[] offsets = {3, 10, 20};
        int[] shapes  = {1, 5, 4};

        DataBuffer out = provider.readCacheBlock("CUBE_R", offsets, shapes, null);
        assertNotNull(out);

        assertEquals(1, r.calls);
        assertEquals(3, r.lastStartLayer);
        assertEquals(1, r.lastNumLayers);
        assertEquals(20, r.lastX);
        assertEquals(10, r.lastY);
        assertEquals(4, r.lastW);
        assertEquals(5, r.lastH);

        assertNotNull(r.lastTarget);
        assertEquals(ProductData.TYPE_UINT8, r.lastTarget.getType());
        assertEquals(1 * 5 * 4, r.lastTarget.getNumElems());

        ProductData fromBuffer = extractProductData(out);
        assertSame(r.lastTarget, fromBuffer);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_usesProvidedTargetDataInstance_3D() throws Exception {
        TestReader r = new TestReader(new Dimension(32, 32));
        provider.addCube("CUBE_T", r, 100, 200, 50, ProductData.TYPE_INT16);

        int[] offsets = {7, 0, 0};
        int[] shapes  = {2, 3, 4};

        ProductData provided = ProductData.createInstance(ProductData.TYPE_INT16, 2 * 3 * 4);
        DataBuffer out = provider.readCacheBlock("CUBE_T", offsets, shapes, provided);

        assertNotNull(out);
        assertSame(provided, r.lastTarget);

        ProductData fromBuffer = extractProductData(out);
        assertSame(provided, fromBuffer);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_createsTargetData_andInvokesReader_2D() throws Exception {
        TestReader r = new TestReader(new Dimension(16, 8));
        provider.addCube("CUBE_2D", r, 100, 200, 1, ProductData.TYPE_UINT8);

        int[] offsets = {10, 20};
        int[] shapes  = {5, 4};

        DataBuffer out = provider.readCacheBlock("CUBE_2D", offsets, shapes, null);
        assertNotNull(out);

        assertEquals(1, r.calls);
        assertEquals(0, r.lastStartLayer);
        assertEquals(1, r.lastNumLayers);
        assertEquals(20, r.lastX);
        assertEquals(10, r.lastY);
        assertEquals(4, r.lastW);
        assertEquals(5, r.lastH);

        assertNotNull(r.lastTarget);
        assertEquals(ProductData.TYPE_UINT8, r.lastTarget.getType());
        assertEquals(5 * 4, r.lastTarget.getNumElems());

        ProductData fromBuffer = extractProductData(out);
        assertSame(r.lastTarget, fromBuffer);
    }

    @Test
    @STTM("SNAP-4123")
    public void test_readCacheBlock_usesProvidedTargetDataInstance_2D() throws Exception {
        TestReader r = new TestReader(new Dimension(16, 8));
        provider.addCube("CUBE_2D_T", r, 100, 200, 1, ProductData.TYPE_INT16);

        int[] offsets = {0, 0};
        int[] shapes  = {3, 4};

        ProductData provided = ProductData.createInstance(ProductData.TYPE_INT16, 3 * 4);
        DataBuffer out = provider.readCacheBlock("CUBE_2D_T", offsets, shapes, provided);

        assertNotNull(out);
        assertSame(provided, r.lastTarget);

        ProductData fromBuffer = extractProductData(out);
        assertSame(provided, fromBuffer);
    }





    private static ProductData extractProductData(DataBuffer buffer) {
        try {
            for (String m : new String[]{"getData", "getProductData"}) {
                try {
                    Object v = buffer.getClass().getMethod(m).invoke(buffer);
                    if (v instanceof ProductData) {
                        return (ProductData) v;
                    }
                } catch (NoSuchMethodException ignored) {}
            }

            List<Field> fields = new ArrayList<>();
            Class<?> c = buffer.getClass();
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    fields.add(f);
                }
                c = c.getSuperclass();
            }
            for (Field f : fields) {
                if (ProductData.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (ProductData) f.get(buffer);
                }
            }
        } catch (Exception e) {
            throw new AssertionError("Failed to extract ProductData from DataBuffer via reflection", e);
        }
        throw new AssertionError("No ProductData found inside DataBuffer");
    }


    private static final class TestReader extends EnmapImageReader {
        private final Dimension tileDim;

        int calls;
        int lastStartLayer, lastNumLayers, lastX, lastY, lastW, lastH;
        ProductData lastTarget;

        TestReader(Dimension tileDim) {
            this.tileDim = tileDim;
        }

        @Override
        public Dimension getTileDimension() {
            return tileDim;
        }

        @Override
        public int getNumImages() {
            return 999;
        }

        @Override
        public RenderedImage getImageAt(int index) {
            return null;
        }

        @Override
        public void readLayerBlock(int startLayer, int numLayers, int x, int y, int width, int height, ProductData targetData) {
            calls++;
            lastStartLayer = startLayer;
            lastNumLayers = numLayers;
            lastX = x;
            lastY = y;
            lastW = width;
            lastH = height;
            lastTarget = targetData;

            int n = Math.min(targetData.getNumElems(), numLayers * width * height);
            for (int i = 0; i < n; i++) {
                targetData.setElemIntAt(i, i);
            }
        }

        @Override
        public void close() {}
    }
}