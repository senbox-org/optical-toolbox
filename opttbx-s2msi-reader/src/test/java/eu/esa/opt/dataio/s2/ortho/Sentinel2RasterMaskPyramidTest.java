package eu.esa.opt.dataio.s2.ortho;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.multilevel.MultiLevelImage;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import com.bc.ceres.multilevel.support.DefaultMultiLevelSource;
import eu.esa.opt.dataio.s2.ortho.plugins.Sentinel2L1CProduct_Multi_UTM16N_ReaderPlugIn;
import eu.esa.opt.dataio.s2.ortho.plugins.Sentinel2L1CProduct_Multi_UTM34S_ReaderPlugIn;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.runtime.Engine;
import org.esa.snap.engine_utilities.utils.TestUtil;
import org.esa.snap.runtime.LogUtils4Tests;
import org.junit.*;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class Sentinel2RasterMaskPyramidTest {

    private Engine engine;

    private Path sentinel2TestProductsPath;

    private static final String SENTINEL2_DIR = "S2";
    private static final String L2A_PRODUCT_NAME = "L2A" + File.separator
            + "S2C_MSIL2A_20261012T161221_N9999_R140_T16QDF_20261014T131627.SAFE" + File.separator
            + "MTD_MSIL2A.xml";


    @BeforeClass
    public static void setupLogger() throws Exception {
        LogUtils4Tests.initLogger();
    }

    @Before
    public void setup() {
        /*
         * We need a proper Engine start so that the openjpeg activator is started
         */
        engine = Engine.start(false);

        /**
         * Run these tests only if Sentinel 2 products test directory exists and is set
         */
        String productPath = System.getProperty(TestUtil.PROPERTYNAME_DATA_DIR);
        Assume.assumeNotNull(productPath);
        sentinel2TestProductsPath = Paths.get(productPath, SENTINEL2_DIR);
        assumeTrue(Files.exists(sentinel2TestProductsPath));
    }

    @After
    public void teardown() {
        if (engine != null) {
            engine.stop();
        }
    }

    @Test
    @STTM("SNAP-4269")
    public void suppliesMissingMaskLevelsWithMatchingDimensions() {
        assertAlignedMask(96, 5, 96, 4);
    }

    @Test
    @STTM("SNAP-4269")
    public void resamplesCoarserQualityMasksAndSuppliesMissingLevels() {
        assertAlignedMask(96, 5, 32, 4);
    }

    @Test
    @STTM("SNAP-4269")
    public void preservesAlreadyAlignedMask() {
        MultiLevelImage target = createMultiLevelImage(96, 5);
        MultiLevelImage mask = createMultiLevelImage(96, 5);
        assertSame(mask, Sentinel2OrthoProductReader.alignMaskImage(target, mask, 0));
    }

    @Test
    @STTM("SNAP-4269")
    public void readsValidPixelsAtEveryLevelOf20mProduct() throws Exception {
        Path productPath = sentinel2TestProductsPath.resolve(L2A_PRODUCT_NAME);
        S2OrthoProductReaderPlugIn readerPlugIn = new Sentinel2L1CProduct_Multi_UTM34S_ReaderPlugIn();
        ProductReader reader = readerPlugIn.createReaderInstance();

        try (Product product = new Sentinel2L1CProduct_Multi_UTM16N_ReaderPlugIn().createReaderInstance()
                    .readProductNodes(productPath.toFile(), null)){

            Band band = product.getBand("B1");
            assertNotNull(band);
            MultiLevelImage image = band.getSourceImage();
            for (String name : new String[]{"B_nodata_B1", "B_detector_footprint_B1"}) {
                MultiLevelImage mask = product.getBand(name).getSourceImage();
                assertEquals(image.getWidth(), mask.getWidth());
                assertEquals(image.getHeight(), mask.getHeight());
                assertEquals(image.getModel().getLevelCount(), mask.getModel().getLevelCount());
            }
            for (int level = 0; level < image.getModel().getLevelCount(); level++) {
                RenderedImage validMask = ImageManager.getInstance().getValidMaskImage(band, level);
                assertNotNull(validMask.getTile(validMask.getMinTileX(), validMask.getMinTileY()));
            }
            assertNotNull(band.getImageInfo(com.bc.ceres.core.ProgressMonitor.NULL));
        }
    }

    private static void assertAlignedMask(int targetSize, int targetLevels, int maskSize, int maskLevels) {
        MultiLevelImage target = createMultiLevelImage(targetSize, targetLevels);
        MultiLevelImage aligned = Sentinel2OrthoProductReader.alignMaskImage(target, createMultiLevelImage(maskSize, maskLevels), 0);
        assertEquals(targetLevels, aligned.getModel().getLevelCount());
        for (int level = 0; level < targetLevels; level++) {
            RenderedImage expected = target.getImage(level);
            RenderedImage actual = aligned.getImage(level);
            assertEquals(expected.getWidth(), actual.getWidth());
            assertEquals(expected.getHeight(), actual.getHeight());
            assertEquals(1, actual.getData().getSample(actual.getWidth() / 2, actual.getHeight() / 2, 0));
        }
    }

    private static MultiLevelImage createMultiLevelImage(int size, int levels) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                image.getRaster().setSample(x, y, 0, 1);
            }
        }
        return new DefaultMultiLevelImage(new DefaultMultiLevelSource(image, levels));
    }
}
