package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GdalDirectGeoTiffImageReaderTest {

    @Test
    @STTM("SNAP-4123")
    public void testReadLayerBlockBuildsContiguousOneBasedBandMapAndStrides() throws Exception {
        FakeDatasetRasterReader fakeReader = new FakeDatasetRasterReader();
        fakeReader.mode = FakeDatasetRasterReader.Mode.INT_SEQUENCE;
        GdalDirectGeoTiffImageReader reader = new GdalDirectGeoTiffImageReader(
                fakeReader, 224, new Dimension(256, 3), 5, 4);

        ProductData targetData = ProductData.createInstance(ProductData.TYPE_INT32, 24);
        reader.readLayerBlock(5, 2, 10, 20, 4, 3, targetData);

        assertArrayEquals(new int[]{6, 7}, fakeReader.lastBandMap);
        assertEquals(4, fakeReader.lastPixelSpace);
        assertEquals(16, fakeReader.lastLineSpace);
        assertEquals(48, fakeReader.lastBandSpace);
        assertEquals(5, fakeReader.lastBufferType);
        assertEquals(1000, targetData.getElemIntAt(0));
        assertEquals(1023, targetData.getElemIntAt(23));
    }

    @Test
    @STTM("SNAP-4123")
    public void testReadLayerBlockPreservesLayerMajorLayout() throws Exception {
        FakeDatasetRasterReader fakeReader = new FakeDatasetRasterReader();
        fakeReader.mode = FakeDatasetRasterReader.Mode.SHORT_LAYER_MAJOR_PATTERN;
        GdalDirectGeoTiffImageReader reader = new GdalDirectGeoTiffImageReader(
                fakeReader, 10, new Dimension(128, 128), 2, 2);

        ProductData targetData = ProductData.createInstance(ProductData.TYPE_UINT16, 8);
        reader.readLayerBlock(0, 2, 0, 0, 2, 2, targetData);

        int[] expected = new int[]{1, 2, 3, 4, 11, 12, 13, 14};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], targetData.getElemIntAt(i));
        }
        assertArrayEquals(new int[]{1, 2}, fakeReader.lastBandMap);
    }

    @Test
    @STTM("SNAP-4123")
    public void testReadLayerBlockThrowsOnGdalError() throws Exception {
        FakeDatasetRasterReader fakeReader = new FakeDatasetRasterReader();
        fakeReader.returnCode = 7;
        GdalDirectGeoTiffImageReader reader = new GdalDirectGeoTiffImageReader(
                fakeReader, 10, new Dimension(64, 64), 2, 2);

        ProductData targetData = ProductData.createInstance(ProductData.TYPE_UINT16, 4);
        try {
            reader.readLayerBlock(0, 1, 0, 0, 2, 2, targetData);
            fail("IOException expected");
        } catch (IOException expected) {
            if (!expected.getMessage().contains("returnCode=7")) {
                fail("Expected returnCode in exception message, got: " + expected.getMessage());
            }
        }
    }

    private static final class FakeDatasetRasterReader implements GdalDirectGeoTiffImageReader.DatasetRasterReader {
        private enum Mode {
            INT_SEQUENCE,
            SHORT_LAYER_MAJOR_PATTERN
        }

        private int returnCode = 0;
        private Mode mode = Mode.INT_SEQUENCE;
        private int[] lastBandMap;
        private int lastPixelSpace;
        private int lastLineSpace;
        private int lastBandSpace;
        private int lastBufferType;

        @Override
        public int readRasterDirect(int xOffset, int yOffset, int xSize, int ySize, int bufferXSize, int bufferYSize, int bufferType,
                                    ByteBuffer nioBuffer, int[] bandMap, int pixelSpace, int lineSpace, int bandSpace) {
            this.lastBandMap = Arrays.copyOf(bandMap, bandMap.length);
            this.lastPixelSpace = pixelSpace;
            this.lastLineSpace = lineSpace;
            this.lastBandSpace = bandSpace;
            this.lastBufferType = bufferType;
            if (returnCode != 0) {
                return returnCode;
            }

            final int elementCount = bandMap.length * bufferXSize * bufferYSize;
            if (mode == Mode.INT_SEQUENCE) {
                IntBuffer intBuffer = nioBuffer.asIntBuffer();
                for (int i = 0; i < elementCount; i++) {
                    intBuffer.put(i, 1000 + i);
                }
            } else {
                short[] values = new short[]{1, 2, 3, 4, 11, 12, 13, 14};
                ShortBuffer shortBuffer = nioBuffer.asShortBuffer();
                for (int i = 0; i < elementCount; i++) {
                    shortBuffer.put(i, values[i]);
                }
            }
            return 0;
        }
    }
}
