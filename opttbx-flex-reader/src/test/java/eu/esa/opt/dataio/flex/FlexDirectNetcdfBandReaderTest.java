package eu.esa.opt.dataio.flex;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FlexDirectNetcdfBandReaderTest {

    private static final int WIDTH = 4;
    private static final int HEIGHT = 3;
    private static final int CHANNELS = 4;

    private static File tempFile;
    private static NetcdfFile netcdfFile;

    @BeforeClass
    public static void createTestFile() throws IOException {
        tempFile = File.createTempFile("FlexDirectNetcdfBandReaderTest", ".nc");
        final NetcdfFileWriter writer = NetcdfFileWriter.createNew(
                NetcdfFileWriter.Version.netcdf3, tempFile.getAbsolutePath());

        writer.addDimension("number_of_along_track_samples", HEIGHT);
        writer.addDimension("number_of_across_track_samples", WIDTH);
        writer.addDimension("number_of_floris_spectral_channels", CHANNELS);
        writer.addDimension("two", 2);
        writer.addDimension("one", 1);

        final Variable data2d = writer.addVariable("data2d", DataType.INT,
                "number_of_along_track_samples number_of_across_track_samples");
        final Variable data2dTransposed = writer.addVariable("data2d_transposed", DataType.INT,
                "number_of_across_track_samples number_of_along_track_samples");
        final Variable data3d = writer.addVariable("data3d", DataType.INT,
                "number_of_along_track_samples number_of_across_track_samples number_of_floris_spectral_channels");
        final Variable doubleData = writer.addVariable("double_data", DataType.DOUBLE, "one two");
        final Variable floatData = writer.addVariable("float_data", DataType.FLOAT, "one two");
        final Variable shortData = writer.addVariable("short_data", DataType.SHORT, "one two");

        writer.create();

        try {
            writer.write(data2d, Array.factory(DataType.INT, new int[]{HEIGHT, WIDTH}, create2dData()));
            writer.write(data2dTransposed, Array.factory(DataType.INT, new int[]{WIDTH, HEIGHT}, createTransposed2dData()));
            writer.write(data3d, Array.factory(DataType.INT, new int[]{HEIGHT, WIDTH, CHANNELS}, create3dData()));
            writer.write(doubleData, Array.factory(DataType.DOUBLE, new int[]{1, 2}, new double[]{1.5, 2.5}));
            writer.write(floatData, Array.factory(DataType.FLOAT, new int[]{1, 2}, new float[]{3.5f, 4.5f}));
            writer.write(shortData, Array.factory(DataType.SHORT, new int[]{1, 2}, new short[]{-1, 7}));
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        } finally {
            writer.close();
        }

        netcdfFile = NetcdfFile.open(tempFile.getAbsolutePath());
    }

    @AfterClass
    public static void deleteTestFile() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
        }
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    @Test
    public void testRead2dFullBlock() throws IOException {
        final ProductData dest = ProductData.createInstance(ProductData.TYPE_INT32, 4);

        new FlexDirectNetcdfBandReader().read(netcdfFile.findVariable("data2d"), ProductData.TYPE_INT32,
                1, 1, 2, 2,
                1, 1, 2, 2, dest);

        assertArrayEquals(new int[]{11, 12, 21, 22}, (int[]) dest.getElems());
    }

    @Test
    public void testRead2dWithSubsamplingUsesNetcdfStride() throws IOException {
        final ProductData dest = ProductData.createInstance(ProductData.TYPE_INT32, 4);

        new FlexDirectNetcdfBandReader().read(netcdfFile.findVariable("data2d"), ProductData.TYPE_INT32,
                0, 0, 4, 3,
                2, 2, 2, 2, dest);

        assertArrayEquals(new int[]{0, 2, 20, 22}, (int[]) dest.getElems());
    }

    @Test
    public void testRead2dNormalizesTransposedDimensionsToRowMajorYX() throws IOException {
        final ProductData dest = ProductData.createInstance(ProductData.TYPE_INT32, 4);

        new FlexDirectNetcdfBandReader().read(netcdfFile.findVariable("data2d_transposed"), ProductData.TYPE_INT32,
                1, 1, 2, 2,
                1, 1, 2, 2, dest);

        assertArrayEquals(new int[]{11, 12, 21, 22}, (int[]) dest.getElems());
    }

    @Test
    public void testRead3dLayerFullBlockForYXChannelLayout() throws IOException {
        final ProductData dest = ProductData.createInstance(ProductData.TYPE_INT32, 6);

        new FlexDirectNetcdfBandReader().readLayer(netcdfFile.findVariable("data3d"), 2, ProductData.TYPE_INT32,
                0, 0, 3, 2,
                1, 1, 3, 2, dest);

        assertArrayEquals(new int[]{2, 12, 22, 102, 112, 122}, (int[]) dest.getElems());
    }

    @Test
    public void testRead3dLayerWithSubsamplingUsesNetcdfStride() throws IOException {
        final ProductData dest = ProductData.createInstance(ProductData.TYPE_INT32, 4);

        new FlexDirectNetcdfBandReader().readLayer(netcdfFile.findVariable("data3d"), 3, ProductData.TYPE_INT32,
                0, 0, 3, 2,
                2, 1, 2, 2, dest);

        assertArrayEquals(new int[]{3, 23, 103, 123}, (int[]) dest.getElems());
    }

    @Test
    public void testReadConvertsSupportedProductDataTypes() throws IOException {
        final FlexDirectNetcdfBandReader reader = new FlexDirectNetcdfBandReader();
        final ProductData doubleDest = ProductData.createInstance(ProductData.TYPE_FLOAT64, 2);
        final ProductData floatDest = ProductData.createInstance(ProductData.TYPE_FLOAT32, 2);
        final ProductData shortDest = ProductData.createInstance(ProductData.TYPE_INT16, 2);
        final ProductData ushortDest = ProductData.createInstance(ProductData.TYPE_UINT16, 2);

        reader.read(netcdfFile.findVariable("double_data"), ProductData.TYPE_FLOAT64,
                0, 0, 2, 1, 1, 1, 2, 1, doubleDest);
        reader.read(netcdfFile.findVariable("float_data"), ProductData.TYPE_FLOAT32,
                0, 0, 2, 1, 1, 1, 2, 1, floatDest);
        reader.read(netcdfFile.findVariable("short_data"), ProductData.TYPE_INT16,
                0, 0, 2, 1, 1, 1, 2, 1, shortDest);
        reader.read(netcdfFile.findVariable("short_data"), ProductData.TYPE_UINT16,
                0, 0, 2, 1, 1, 1, 2, 1, ushortDest);

        assertEquals(1.5, doubleDest.getElemDoubleAt(0), 1.0e-8);
        assertEquals(2.5, doubleDest.getElemDoubleAt(1), 1.0e-8);
        assertEquals(3.5f, floatDest.getElemFloatAt(0), 1.0e-6f);
        assertEquals(4.5f, floatDest.getElemFloatAt(1), 1.0e-6f);
        assertEquals(-1, shortDest.getElemIntAt(0));
        assertEquals(7, shortDest.getElemIntAt(1));
        assertEquals(65535L, ushortDest.getElemUIntAt(0));
        assertEquals(7L, ushortDest.getElemUIntAt(1));
    }

    private static int[] create2dData() {
        final int[] data = new int[HEIGHT * WIDTH];
        int index = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                data[index++] = y * 10 + x;
            }
        }
        return data;
    }

    private static int[] createTransposed2dData() {
        final int[] data = new int[WIDTH * HEIGHT];
        int index = 0;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                data[index++] = y * 10 + x;
            }
        }
        return data;
    }

    private static int[] create3dData() {
        final int[] data = new int[HEIGHT * WIDTH * CHANNELS];
        int index = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                for (int channel = 0; channel < CHANNELS; channel++) {
                    data[index++] = y * 100 + x * 10 + channel;
                }
            }
        }
        return data;
    }
}
