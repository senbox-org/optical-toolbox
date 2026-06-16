package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.ProgressMonitor;
import eu.esa.snap.core.dataio.cache.CacheManager;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.ProductCache;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.netcdf.cache.NetcdfCacheDataProvider;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tonio Fincke
 */
public class S3NetcdfReaderTest {

    private static final int SYNTH_WIDTH  = 8;
    private static final int SYNTH_HEIGHT = 10;

    private static File synthFile;
    private static File layeredSynthFile;

    private S3NetcdfReader reader;


    @BeforeClass
    public static void createSyntheticFile() throws IOException {
        synthFile = File.createTempFile("S3NetcdfReaderTest_synth", ".nc");

        NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, synthFile.getAbsolutePath());
        writer.addDimension("rows",    SYNTH_HEIGHT);
        writer.addDimension("columns", SYNTH_WIDTH);
        Variable varInt = writer.addVariable("data_int", DataType.INT, "rows columns");
        writer.create();

        int[] flat = new int[SYNTH_HEIGHT * SYNTH_WIDTH];
        for (int ii = 0; ii < flat.length; ii++) {
            flat[ii] = ii;
        }

        try {
            writer.write(varInt, Array.factory(DataType.INT, new int[]{SYNTH_HEIGHT, SYNTH_WIDTH}, flat));
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
        writer.close();

        layeredSynthFile = File.createTempFile("S3NetcdfReaderTest_layered", ".nc");

        writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, layeredSynthFile.getAbsolutePath());
        writer.addDimension("time", 2);
        writer.addDimension("channel", 3);
        writer.addDimension("single_channel", 1);
        writer.addDimension("rows", SYNTH_HEIGHT);
        writer.addDimension("columns", SYNTH_WIDTH);
        Variable varChannel = writer.addVariable("cube", DataType.INT, "channel rows columns");
        Variable varRank4 = writer.addVariable("rank4cube", DataType.INT, "time channel rows columns");
        Variable varSingleLayer = writer.addVariable("single_layer_cube", DataType.INT, "single_channel rows columns");
        writer.create();

        int[] channelData = new int[3 * SYNTH_HEIGHT * SYNTH_WIDTH];
        for (int channel = 0; channel < 3; channel++) {
            for (int y = 0; y < SYNTH_HEIGHT; y++) {
                for (int x = 0; x < SYNTH_WIDTH; x++) {
                    channelData[channel * SYNTH_HEIGHT * SYNTH_WIDTH + y * SYNTH_WIDTH + x] =
                            1000 * channel + y * SYNTH_WIDTH + x;
                }
            }
        }

        int[] rank4Data = new int[2 * 3 * SYNTH_HEIGHT * SYNTH_WIDTH];
        for (int time = 0; time < 2; time++) {
            for (int channel = 0; channel < 3; channel++) {
                for (int y = 0; y < SYNTH_HEIGHT; y++) {
                    for (int x = 0; x < SYNTH_WIDTH; x++) {
                        rank4Data[((time * 3 + channel) * SYNTH_HEIGHT + y) * SYNTH_WIDTH + x] =
                                10000 * time + 1000 * channel + y * SYNTH_WIDTH + x;
                    }
                }
            }
        }

        int[] singleLayerData = new int[SYNTH_HEIGHT * SYNTH_WIDTH];
        for (int y = 0; y < SYNTH_HEIGHT; y++) {
            for (int x = 0; x < SYNTH_WIDTH; x++) {
                singleLayerData[y * SYNTH_WIDTH + x] = 5000 + y * SYNTH_WIDTH + x;
            }
        }

        try {
            writer.write(varChannel, Array.factory(DataType.INT, new int[]{3, SYNTH_HEIGHT, SYNTH_WIDTH}, channelData));
            writer.write(varRank4, Array.factory(DataType.INT, new int[]{2, 3, SYNTH_HEIGHT, SYNTH_WIDTH}, rank4Data));
            writer.write(varSingleLayer, Array.factory(DataType.INT, new int[]{1, SYNTH_HEIGHT, SYNTH_WIDTH}, singleLayerData));
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
        writer.close();
    }

    @AfterClass
    public static void cleanUpClass() {
        CacheManager.dispose();
        if (synthFile != null) {
            synthFile.delete();
        }
        if (layeredSynthFile != null) {
            layeredSynthFile.delete();
        }
    }

    @Before
    public void setUp() {
        reader = new S3NetcdfReader();
    }

    @Test
    public void testReadProduct() throws Exception {
        final Product product = reader.readProductNodes(getTestFilePath("../../s3/FRP_in.nc"), null);
        assertNotNull(product);
        assertEquals("FRP_in", product.getName());
        assertEquals("NetCDF", product.getProductType());
        assertEquals(1568, product.getSceneRasterWidth());
        assertEquals(266, product.getSceneRasterHeight());
    }

    @Test
    @STTM("SNAP-4200")
    public void testProductCache_isNull_beforeOpen() {
        assertNull("ProductCache must be null before any file is opened", reader.getProductCache());
    }

    @Test
    @STTM("SNAP-4200")
    public void testProductCache_isNotNull_afterOpen() throws Exception {
        reader.readProductNodes(getTestFilePath("../../s3/FRP_in.nc"), null);
        Assert.assertNotNull("ProductCache must be initialized after opening a file", reader.getProductCache());
    }

    @Test
    @STTM("SNAP-4200")
    public void testProductCache_isNull_afterClose() throws Exception {
        reader.readProductNodes(getTestFilePath("../../s3/FRP_in.nc"), null);
        reader.close();
        assertNull("ProductCache must be null after close()", reader.getProductCache());
    }

    @Test
    @STTM("SNAP-4200")
    public void testCacheManager_registersProductCache_afterOpen() throws Exception {
        final int before = CacheManager.getInstance().getNumProductCaches();
        reader.readProductNodes(getTestFilePath("../../s3/FRP_in.nc"), null);
        Assert.assertEquals("CacheManager must contain one additional ProductCache after open", before + 1, CacheManager.getInstance().getNumProductCaches());
    }

    @Test
    @STTM("SNAP-4200")
    public void testCacheManager_removesProductCache_afterClose() throws Exception {
        reader.readProductNodes(getTestFilePath("../../s3/FRP_in.nc"), null);
        final int before = CacheManager.getInstance().getNumProductCaches();
        reader.close();
        Assert.assertEquals("CacheManager must contain one fewer ProductCache after close", before - 1, CacheManager.getInstance().getNumProductCaches());
    }



    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_syntheticFile_fullBand_correctValues() throws Exception {
        final Product product = reader.readProductNodes(synthFile.getAbsolutePath(), null);
        final Band band = product.getBand("data_int");
        Assert.assertNotNull("Synthetic band 'data_int' must be present in product", band);

        final int w = SYNTH_WIDTH;
        final int h = SYNTH_HEIGHT;
        final ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_INT32, w * h);

        reader.readBandRasterDataImpl(0, 0, w, h, 1, 1,
                band, 0, 0, w, h, destBuffer, ProgressMonitor.NULL);

        final int[] expected = new int[w * h];

        for (int ii = 0; ii < expected.length; ii++) {
            expected[ii] = ii;
        }
        assertArrayEquals("Full-band read must return sequential values 0..(W*H-1)", expected, (int[]) destBuffer.getElems());
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_syntheticFile_subRegion_correctValues() throws Exception {
        final Product product = reader.readProductNodes(synthFile.getAbsolutePath(), null);
        final Band band = product.getBand("data_int");

        final int offX = 1;
        final int offY = 2;
        final int w = 4;
        final int h = 3;

        final ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_INT32, w * h);
        reader.readBandRasterDataImpl(offX, offY, w, h, 1, 1,
                band, offX, offY, w, h, destBuffer, ProgressMonitor.NULL);

        assertArrayEquals("Sub-region read must return correct values",
                new int[]{17, 18, 19, 20, 25, 26, 27, 28, 33, 34, 35, 36}, (int[]) destBuffer.getElems());
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_syntheticFile_withSubsampling_correctValues() throws Exception {
        final Product product = reader.readProductNodes(synthFile.getAbsolutePath(), null);
        final Band band = product.getBand("data_int");

        final int srcW = 6;
        final int srcH = 4;
        final int stepX = 2;
        final int stepY = 2;

        final int destW = srcW / stepX, destH = srcH / stepY;
        final ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_INT32, destW * destH);

        reader.readBandRasterDataImpl(0, 0, srcW, srcH, stepX, stepY,
                band, 0, 0, destW, destH, destBuffer, ProgressMonitor.NULL);

        assertArrayEquals("Subsampled (step=2) read must return correct downsampled values",
                new int[]{0, 2, 4, 16, 18, 20}, (int[]) destBuffer.getElems());
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_syntheticFile_lastTile_correctValues() throws Exception {
        final Product product = reader.readProductNodes(synthFile.getAbsolutePath(), null);
        final Band band = product.getBand("data_int");

        final int offX = SYNTH_WIDTH - 2;
        final int offY = SYNTH_HEIGHT - 2;
        final int w = 2;
        final int h = 2;

        final ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_INT32, w * h);
        reader.readBandRasterDataImpl(offX, offY, w, h, 1, 1,
                band, offX, offY, w, h, destBuffer, ProgressMonitor.NULL);

        assertArrayEquals("Bottom-right corner read must return correct values",
                new int[]{70, 71, 78, 79}, (int[]) destBuffer.getElems());
    }


    @Test
    @STTM("SNAP-4200")
    public void testAllBands_areReadableWithoutException() throws Exception {
        final Product product = reader.readProductNodes(getTestFilePath("../../s3/FRP_in.nc"), null);
        assertTrue("Product must have at least one band", product.getNumBands() > 0);

        for (final Band band : product.getBands()) {
            final int w = Math.min(16, band.getRasterWidth());
            final int h = Math.min(16, band.getRasterHeight());
            final ProductData buffer = ProductData.createInstance(band.getDataType(), w * h);

            reader.readBandRasterDataImpl(0, 0, w, h, 1, 1,
                    band, 0, 0, w, h, buffer, ProgressMonitor.NULL);
        }
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_matchesDirectNetcdfRead() throws Exception {
        final String filePath = getTestFilePath("../../s3/FRP_in.nc");
        final Product product = reader.readProductNodes(filePath, null);

        Band band = null;
        for (final Band bb : product.getBands()) {
            final String name = bb.getName();

            if (!name.endsWith("_lsb") && !name.endsWith("_msb")) {
                band = bb;
                break;
            }
        }
        Assert.assertNotNull("At least one non-split band must be present in FRP_in.nc", band);

        final int w = 16, h = 8;
        final ProductData cacheResult = ProductData.createInstance(band.getDataType(), w * h);

        reader.readBandRasterDataImpl(0, 0, w, h, 1, 1,
                band, 0, 0, w, h, cacheResult, ProgressMonitor.NULL);

        try (NetcdfFile ncFile = NetcdfFile.open(filePath)) {
            final Variable variable = ncFile.findVariable(band.getName());
            Assert.assertNotNull("NetCDF variable '" + band.getName() + "' must exist in file", variable);

            final int[] origin = {0, 0};
            final int[] shape  = {h, w};
            final Array array  = variable.read(origin, shape);

            for (int i = 0; i < w * h; i++) {
                Assert.assertEquals("Pixel " + i + " of band '" + band.getName() + "' must match direct NetCDF read",
                        array.getDouble(i), cacheResult.getElemDoubleAt(i), 1e-6);}
        }
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_withSubsampling_matchesManuallySampledValues() throws Exception {
        final String filePath = getTestFilePath("../../s3/FRP_in.nc");
        final Product product  = reader.readProductNodes(filePath, null);

        Band band = null;
        for (final Band b : product.getBands()) {
            final String name = b.getName();

            if (!name.endsWith("_lsb") && !name.endsWith("_msb")) {
                band = b;
                break;
            }
        }
        Assert.assertNotNull("At least one non-split band must be present in FRP_in.nc", band);

        final int srcW = 12;
        final int srcH = 8;
        final int stepX = 3;
        final int stepY = 2;
        final int destW = srcW / stepX;
        final int destH = srcH / stepY;

        final ProductData subsampled = ProductData.createInstance(band.getDataType(), destW * destH);
        reader.readBandRasterDataImpl(0, 0, srcW, srcH, stepX, stepY,
                band, 0, 0, destW, destH, subsampled, ProgressMonitor.NULL);

        try (NetcdfFile ncFile = NetcdfFile.open(filePath)) {
            final Variable variable = ncFile.findVariable(band.getName());
            Assert.assertNotNull(variable);

            final Array fullBlock = variable.read(new int[]{0, 0}, new int[]{srcH, srcW});
            int refIdx = 0;

            for (int yy = 0; yy < srcH; yy += stepY) {
                for (int xx = 0; xx < srcW; xx += stepX) {
                    final double expected = fullBlock.getDouble(yy * srcW + xx);

                    Assert.assertEquals("Subsampled pixel (" + xx + "," + yy + ") of band '" + band.getName() + "' must match manual subsample",
                            expected, subsampled.getElemDoubleAt(refIdx), 1e-6);
                    refIdx++;
                }
            }
        }
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_rank3SeparatedBand_readsRequestedLayer() throws Exception {
        reader = new ChannelSeparatingReader();
        final Product product = reader.readProductNodes(layeredSynthFile.getAbsolutePath(), null);
        final Band band = product.getBand("cube_channel_2");
        Assert.assertNotNull("Synthetic channel band must be present in product", band);

        final int w = 4;
        final int h = 3;
        final ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_INT32, w * h);

        reader.readBandRasterDataImpl(1, 2, w, h, 1, 1,
                band, 1, 2, w, h, destBuffer, ProgressMonitor.NULL);

        assertArrayEquals(new int[]{1017, 1018, 1019, 1020, 1025, 1026, 1027, 1028, 1033, 1034, 1035, 1036},
                (int[]) destBuffer.getElems());
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_rank4SeparatedBand_flattensLayerIndexLikeCacheProvider() throws Exception {
        reader = new ChannelSeparatingReader();
        final Product product = reader.readProductNodes(layeredSynthFile.getAbsolutePath(), null);
        final Band band = product.getBand("rank4cube_channel_3");
        Assert.assertNotNull("Synthetic rank-4 channel band must be present in product", band);

        final int sourceWidth = 6;
        final int sourceHeight = 4;
        final int stepX = 2;
        final int stepY = 2;
        final ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_INT32, 6);

        reader.readBandRasterDataImpl(0, 0, sourceWidth, sourceHeight, stepX, stepY,
                band, 0, 0, 3, 2, destBuffer, ProgressMonitor.NULL);

        assertArrayEquals(new int[]{2000, 2002, 2004, 2016, 2018, 2020}, (int[]) destBuffer.getElems());
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_singleLayerRank3SeparatedBand_uses2DCacheRead() throws Exception {
        reader = new SingleChannelSeparatingReader();
        final Product product = reader.readProductNodes(layeredSynthFile.getAbsolutePath(), null);
        final Band band = product.getBand("single_layer_cube_slice_1");
        Assert.assertNotNull("Synthetic single-layer band must be present in product", band);

        final int w = 4;
        final int h = 3;
        final ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_INT32, w * h);

        reader.readBandRasterDataImpl(1, 2, w, h, 1, 1,
                band, 1, 2, w, h, destBuffer, ProgressMonitor.NULL);

        assertArrayEquals(new int[]{5017, 5018, 5019, 5020, 5025, 5026, 5027, 5028, 5033, 5034, 5035, 5036},
                (int[]) destBuffer.getElems());
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadCacheData_singleLayerRank3Mapping_uses2DCacheRead() throws Exception {
        reader = new SingleChannelSeparatingReader();
        final Product product = reader.readProductNodes(layeredSynthFile.getAbsolutePath(), null);
        Assert.assertNotNull(product.getBand("single_layer_cube_slice_1"));

        final int[] offsets = new int[]{2, 1};
        final int[] shapes = new int[]{3, 4};
        final DataBuffer buffer = new DataBuffer(ProductData.TYPE_INT32, offsets, shapes);

        reader.readCacheData("single_layer_cube_slice_1", offsets, shapes, buffer);

        assertArrayEquals(new int[]{5017, 5018, 5019, 5020, 5025, 5026, 5027, 5028, 5033, 5034, 5035, 5036},
                (int[]) buffer.getData().getElems());
    }

    @Test
    @STTM("SNAP-4200")
    public void testReadBandRasterData_singleLayerMappingUses2DOffsets() throws Exception {
        reader = new S3NetcdfReader();
        ProductCache productCache = mock(ProductCache.class);
        NetcdfCacheDataProvider cacheDataProvider = mock(NetcdfCacheDataProvider.class);
        setField(reader, "productCache", productCache);
        setField(reader, "cacheDataProvider", cacheDataProvider);

        eu.esa.snap.core.dataio.cache.VariableDescriptor descriptor =
                new eu.esa.snap.core.dataio.cache.VariableDescriptor();
        descriptor.name = "surface_pressure";
        descriptor.layers = 1;
        when(cacheDataProvider.getVariableDescriptor("surface_pressure")).thenReturn(descriptor);

        @SuppressWarnings("unchecked")
        Map<String, S3CacheLayerMapper.LayerReference> mappings =
                (Map<String, S3CacheLayerMapper.LayerReference>) getField(reader, "cacheLayerMappings");
        mappings.put("surface_pressure_layer_1", new S3CacheLayerMapper.LayerReference("surface_pressure", 0));

        Band band = new Band("surface_pressure_layer_1", ProductData.TYPE_FLOAT32, 8, 10);
        ProductData destBuffer = ProductData.createInstance(ProductData.TYPE_FLOAT32, 6);

        reader.readBandRasterData(5, 4, 3, 2, 1, 1, band, 3, 2, destBuffer);

        verify(productCache).read(eq("surface_pressure"), aryEq(new int[]{4, 5}), aryEq(new int[]{2, 3}), any(DataBuffer.class));
    }

    private String getTestFilePath(String name) throws Exception {
        URL url = getClass().getResource(name);
        URI uri = new URI(url.toString());
        return uri.getPath();
    }

    private static class ChannelSeparatingReader extends S3NetcdfReader {

        @Override
        protected String[] getSeparatingDimensions() {
            return new String[]{"channel"};
        }

        @Override
        public String[] getSuffixesForSeparatingDimensions() {
            return new String[]{"channel"};
        }
    }

    private static class SingleChannelSeparatingReader extends S3NetcdfReader {

        @Override
        protected String[] getSeparatingDimensions() {
            return new String[]{"single_channel"};
        }

        @Override
        public String[] getSuffixesForSeparatingDimensions() {
            return new String[]{"slice"};
        }
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
