package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.enmap.imgReader.EnmapImageReader;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.ProductCache;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class EnmapSpectralCacheProviderTest {

    private static final String VARIABLE_NAME = "ENMAP_SPECTRAL_CUBE";

    @Test
    @STTM("SNAP-4123")
    public void testGetVariableDescriptor_interleavedAndNormalizedTiles() throws IOException {
        FakeEnmapImageReader imageReader = new FakeEnmapImageReader(218, new Dimension(1128, 1212), true);
        EnmapSpectralCacheProvider provider = new EnmapSpectralCacheProvider(VARIABLE_NAME, imageReader,
                                                                             1128, 1212, 218, ProductData.TYPE_INT32);

        VariableDescriptor descriptor = provider.getVariableDescriptor(VARIABLE_NAME);

        assertEquals(1128, descriptor.width);
        assertEquals(1212, descriptor.height);
        assertEquals(218, descriptor.layers);
        assertEquals(256, descriptor.tileWidth);
        assertEquals(256, descriptor.tileHeight);
        assertEquals(16, descriptor.tileLayers);
        assertEquals(ProductData.TYPE_INT32, descriptor.dataType);
    }

    @Test
    @STTM("SNAP-4123")
    public void testGetVariableDescriptor_nonInterleavedUsesSingleLayerTiles() throws IOException {
        FakeEnmapImageReader imageReader = new FakeEnmapImageReader(218, new Dimension(128, 64), false);
        EnmapSpectralCacheProvider provider = new EnmapSpectralCacheProvider(VARIABLE_NAME, imageReader,
                                                                             1128, 1212, 218, ProductData.TYPE_INT32);

        VariableDescriptor descriptor = provider.getVariableDescriptor(VARIABLE_NAME);

        assertEquals(128, descriptor.tileWidth);
        assertEquals(64, descriptor.tileHeight);
        assertEquals(1, descriptor.tileLayers);
    }

    @Test
    @STTM("SNAP-4123")
    public void testReadCacheBlock_returnsLayerMajorDataForPartialCube() throws IOException {
        FakeEnmapImageReader imageReader = new FakeEnmapImageReader(10, new Dimension(64, 64), true);
        EnmapSpectralCacheProvider provider = new EnmapSpectralCacheProvider(VARIABLE_NAME, imageReader,
                                                                             300, 200, 10, ProductData.TYPE_INT32);

        int[] offsets = {2, 3, 4};
        int[] shapes = {2, 2, 3};
        DataBuffer dataBuffer = provider.readCacheBlock(VARIABLE_NAME, offsets, shapes, null);

        ProductData data = dataBuffer.getData();
        assertEquals(12, data.getNumElems());
        assertEquals(expectedValue(2, 3, 4), data.getElemIntAt(0));
        assertEquals(expectedValue(2, 4, 6), data.getElemIntAt(5));
        assertEquals(expectedValue(3, 3, 4), data.getElemIntAt(6));
        assertEquals(expectedValue(3, 4, 6), data.getElemIntAt(11));
    }

    @Test
    @STTM("SNAP-4123")
    public void testReadCacheBlock_reusesProvidedTargetData() throws IOException {
        FakeEnmapImageReader imageReader = new FakeEnmapImageReader(10, new Dimension(64, 64), true);
        EnmapSpectralCacheProvider provider = new EnmapSpectralCacheProvider(VARIABLE_NAME, imageReader,
                                                                             300, 200, 10, ProductData.TYPE_INT32);

        int[] offsets = {1, 5, 6};
        int[] shapes = {1, 2, 2};
        ProductData targetData = ProductData.createInstance(ProductData.TYPE_INT32, 4);
        DataBuffer dataBuffer = provider.readCacheBlock(VARIABLE_NAME, offsets, shapes, targetData);

        assertSame(targetData, dataBuffer.getData());
        assertEquals(expectedValue(1, 5, 6), targetData.getElemIntAt(0));
        assertEquals(expectedValue(1, 6, 7), targetData.getElemIntAt(3));
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4123")
    public void testGetVariableDescriptor_unknownVariableThrows() throws IOException {
        FakeEnmapImageReader imageReader = new FakeEnmapImageReader(10, new Dimension(64, 64), true);
        EnmapSpectralCacheProvider provider = new EnmapSpectralCacheProvider(VARIABLE_NAME, imageReader,
                                                                             300, 200, 10, ProductData.TYPE_INT32);

        provider.getVariableDescriptor("UNKNOWN");
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4123")
    public void testReadCacheBlock_unknownVariableThrows() throws IOException {
        FakeEnmapImageReader imageReader = new FakeEnmapImageReader(10, new Dimension(64, 64), true);
        EnmapSpectralCacheProvider provider = new EnmapSpectralCacheProvider(VARIABLE_NAME, imageReader,
                                                                             300, 200, 10, ProductData.TYPE_INT32);

        provider.readCacheBlock("UNKNOWN", new int[]{0, 0, 0}, new int[]{1, 1, 1}, null);
    }

    @Test
    @STTM("SNAP-4123")
    public void testSpectrumViewPattern_usesCeilLayersPerInterleavedCacheTile() throws IOException {
        int layerCount = 33;
        FakeEnmapImageReader imageReader = new FakeEnmapImageReader(layerCount, new Dimension(128, 128), true);
        EnmapSpectralCacheProvider provider = new EnmapSpectralCacheProvider(VARIABLE_NAME, imageReader,
                                                                             512, 512, layerCount, ProductData.TYPE_INT32);
        ProductCache productCache = new ProductCache(provider);

        for (int layer = 0; layer < layerCount; layer++) {
            ProductData targetData = ProductData.createInstance(ProductData.TYPE_INT32, 1);
            DataBuffer targetBuffer = new DataBuffer(targetData, new int[]{0, 0}, new int[]{1, 1});
            productCache.read(VARIABLE_NAME, new int[]{layer, 10, 20}, new int[]{1, 1, 1}, targetBuffer);
        }

        assertEquals(3, imageReader.getReadLayerBlockCount());
    }

    private static int expectedValue(int layer, int y, int x) {
        return layer * 1_000_000 + y * 1_000 + x;
    }

    private static class FakeEnmapImageReader extends EnmapImageReader {

        private final int numImages;
        private final Dimension tileDimension;
        private final boolean interleavedReadOptimized;
        private int readLayerBlockCount;

        private FakeEnmapImageReader(int numImages, Dimension tileDimension, boolean interleavedReadOptimized) {
            this.numImages = numImages;
            this.tileDimension = tileDimension;
            this.interleavedReadOptimized = interleavedReadOptimized;
        }

        @Override
        public Dimension getTileDimension() {
            return tileDimension;
        }

        @Override
        public int getNumImages() {
            return numImages;
        }

        @Override
        public RenderedImage getImageAt(int index) {
            throw new UnsupportedOperationException("Not required in this test double");
        }

        @Override
        public void readLayerBlock(int startLayer, int numLayers, int x, int y, int width, int height, ProductData targetData) {
            readLayerBlockCount++;

            int index = 0;
            for (int layer = 0; layer < numLayers; layer++) {
                int absoluteLayer = startLayer + layer;
                for (int row = 0; row < height; row++) {
                    int absoluteY = y + row;
                    for (int col = 0; col < width; col++) {
                        int absoluteX = x + col;
                        targetData.setElemIntAt(index++, expectedValue(absoluteLayer, absoluteY, absoluteX));
                    }
                }
            }
        }

        @Override
        public boolean isInterleavedReadOptimized() {
            return interleavedReadOptimized;
        }

        @Override
        public void close() {
        }

        int getReadLayerBlockCount() {
            return readLayerBlockCount;
        }
    }
}
