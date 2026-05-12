package eu.esa.opt.dataio.flex;

import org.esa.snap.core.datamodel.*;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Integration tests that read real FLEX test products.
 * Skipped unless test data is available at the configured path.
 */
public class FlexProductReaderIntegrationTest {


    // TODO remove this test!!! BL



    private static final String DATA_DIR_PROPERTY = "flex.test.data.dir";
    private static final String DEFAULT_DATA_DIR = "C:/Users/user/Work/FLEX/FLEX_SPECS/data";

    private static Path dataDir;
    private Product product;

    @BeforeClass
    public static void setUp() {
        final String dirPath = System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR);
        dataDir = Paths.get(dirPath);
        Assume.assumeTrue("Test data not available at " + dataDir, Files.isDirectory(dataDir));
    }

    @After
    public void tearDown() {
        if (product != null) {
            product.dispose();
            product = null;
        }
    }

    // ------ L1B ------

    @Test
    public void testReadL1bProduct() throws IOException {
        final Path productDir = findProductDir("FLX_L1B_OBS");
        Assume.assumeNotNull("No L1B product found", productDir);

        product = readProduct(productDir);
        assertNotNull("Product should not be null", product);

        assertEquals("FLX_L1B_OBS", product.getProductType());
        assertEquals(536, product.getSceneRasterWidth());
        assertEquals(3640, product.getSceneRasterHeight());

        assertTrue("L1B should have bands", product.getNumBands() > 0);

        assertNotNull(product.getBand("FLORIS_HR1B_1_radiance"));
        assertNotNull(product.getBand("FLORIS_HR2B_1_radiance"));
        assertNotNull(product.getBand("FLORIS_LRB_1_radiance"));

        assertNull("L1B should not have geocoding", product.getSceneGeoCoding());

        assertNotNull(product.getMetadataRoot().getElement("Header"));
        assertEquals("L1B_OBS___",
                product.getMetadataRoot().getElement("Header").getAttributeString("productType"));
    }

    @Test
    public void testReadL1bPixelValue() throws IOException {
        final Path productDir = findProductDir("FLX_L1B_OBS");
        Assume.assumeNotNull("No L1B product found", productDir);

        product = readProduct(productDir);
        assertNotNull(product);

        final Band band = product.getBand("FLORIS_HR1B_1_radiance");
        assertNotNull(band);
        assertTrue("HR1B_1 should have scale_factor", band.getScalingFactor() != 1.0 || band.getScalingOffset() != 0.0);

        band.loadRasterData();
        // reading succeeds; value depends on test data
    }

    // ------ L1C ------

    @Test
    public void testReadL1cProduct() throws IOException {
        final Path productDir = findProductDir("FLX_L1C_FLXSYN");
        Assume.assumeNotNull("No L1C product found", productDir);

        product = readProduct(productDir);
        assertNotNull("Product should not be null", product);

        assertEquals("FLX_L1C_FLXSYN", product.getProductType());
        assertEquals(536, product.getSceneRasterWidth());
        assertEquals(3640, product.getSceneRasterHeight());

        assertNotNull("L1C should have latitude", product.getBand("latitude"));
        assertNotNull("L1C should have longitude", product.getBand("longitude"));

        assertNotNull("L1C should have floris_toa_radiance_ch_1",
                product.getBand("floris_toa_radiance_ch_1"));

        assertTrue("L1C should have many bands", product.getNumBands() > 20);

        final GeoCoding geoCoding = product.getSceneGeoCoding();
        assertNotNull("L1C should have geocoding", geoCoding);

        final GeoPos geoPos = geoCoding.getGeoPos(new PixelPos(268, 1820), null);
        assertTrue("Latitude should be valid", geoPos.getLat() >= -90 && geoPos.getLat() <= 90);
        assertTrue("Longitude should be valid", geoPos.getLon() >= -180 && geoPos.getLon() <= 180);
    }

    @Test
    public void testReadL1cMetadata() throws IOException {
        final Path productDir = findProductDir("FLX_L1C_FLXSYN");
        Assume.assumeNotNull("No L1C product found", productDir);

        product = readProduct(productDir);
        assertNotNull(product);

        final MetadataElement header = product.getMetadataRoot().getElement("Header");
        assertNotNull(header);
        assertEquals("FLEX", header.getAttributeString("platformName", ""));
        assertFalse(header.getAttributeString("startTime", "").isEmpty());
    }

    @Test
    public void testReadL1cMetadataVariables() throws IOException {
        final Path productDir = findProductDir("FLX_L1C_FLXSYN");
        Assume.assumeNotNull("No L1C product found", productDir);

        product = readProduct(productDir);
        assertNotNull(product);

        final MetadataElement root = product.getMetadataRoot();

        final MetadataElement timeStamp = root.getElement("time_stamp");
        assertNotNull("L1C should have time_stamp metadata", timeStamp);
        assertTrue("time_stamp should have attributes after lazy load",
                timeStamp.getNumAttributes() > 0);

        final MetadataElement tiePressure = root.getElement("tie_pressure_levels");
        assertNotNull("L1C should have tie_pressure_levels metadata", tiePressure);
        assertNotNull("tie_pressure_levels should have 'value' attribute",
                tiePressure.getAttribute("value"));
    }

    // ------ L2 ------

    @Test
    public void testReadL2Product() throws IOException {
        final Path productDir = findProductDir("FLX_L2__FLXSYN");
        Assume.assumeNotNull("No L2 product found", productDir);

        product = readProduct(productDir);
        assertNotNull("Product should not be null", product);

        assertEquals("FLX_L2_FLXSYN", product.getProductType());
        assertEquals(366, product.getSceneRasterWidth());
        assertEquals(366, product.getSceneRasterHeight());

        assertNotNull("L2 should have latitude", product.getBand("latitude"));
        assertNotNull("L2 should have longitude", product.getBand("longitude"));
        assertNotNull("L2 should have leaf_area_index", product.getBand("leaf_area_index"));
        assertNotNull("L2 should have total_integrated_sif", product.getBand("total_integrated_sif"));

        assertTrue("L2 should have many bands", product.getNumBands() > 20);

        final GeoCoding geoCoding = product.getSceneGeoCoding();
        assertNotNull("L2 should have geocoding", geoCoding);

        // L2 tile products have sparse lat/lon - find a pixel with valid data
        GeoPos geoPos = null;
        for (int y = 365; y >= 0 && geoPos == null; y--) {
            for (int x = 365; x >= 0 && geoPos == null; x--) {
                final GeoPos candidate = geoCoding.getGeoPos(new PixelPos(x, y), null);
                if (!Double.isNaN(candidate.getLat()) && !Double.isNaN(candidate.getLon())) {
                    geoPos = candidate;
                }
            }
        }
        assertNotNull("L2 should have at least one valid geo position", geoPos);
        assertTrue("Latitude should be valid", geoPos.getLat() >= -90 && geoPos.getLat() <= 90);
        assertTrue("Longitude should be valid", geoPos.getLon() >= -180 && geoPos.getLon() <= 180);
    }

    @Test
    public void testReadL2SpecialBands() throws IOException {
        final Path productDir = findProductDir("FLX_L2__FLXSYN");
        Assume.assumeNotNull("No L2 product found", productDir);

        product = readProduct(productDir);
        assertNotNull(product);

        assertNotNull("L2 should have sif_emission_spectrum_ch_1",
                product.getBand("sif_emission_spectrum_ch_1"));

        final Band sifBand = product.getBand("sif_emission_spectrum_ch_1");
        sifBand.loadRasterData();
        // reading succeeds; value depends on test data
    }

    @Test
    public void testReadL2FlagMasks() throws IOException {
        final Path productDir = findProductDir("FLX_L2__FLXSYN");
        Assume.assumeNotNull("No L2 product found", productDir);

        product = readProduct(productDir);
        assertNotNull(product);

        final ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
        // mask count depends on whether quality bands exist in this test product
        if (product.getBand("quality_flags") != null || product.getBand("pixel_classification") != null) {
            assertTrue("L2 should have flag masks when quality bands exist", maskGroup.getNodeCount() > 0);
        }
    }

    @Test
    public void testReadL2MetadataVariables() throws IOException {
        final Path productDir = findProductDir("FLX_L2__FLXSYN");
        Assume.assumeNotNull("No L2 product found", productDir);

        product = readProduct(productDir);
        assertNotNull(product);

        final MetadataElement root = product.getMetadataRoot();

        final MetadataElement sifWavelength = root.getElement("sif_wavelength_grid");
        assertNotNull("L2 should have sif_wavelength_grid metadata", sifWavelength);
        assertNotNull("sif_wavelength_grid should have 'value' attribute",
                sifWavelength.getAttribute("value"));

        final MetadataElement reflWavelength = root.getElement("reflectance_wavelength_grid");
        assertNotNull("L2 should have reflectance_wavelength_grid metadata", reflWavelength);
        assertNotNull("reflectance_wavelength_grid should have 'value' attribute",
                reflWavelength.getAttribute("value"));
    }

    // ------ helpers ------

    private Product readProduct(Path productDir) throws IOException {
        final Path xmlHeader = findXmlHeader(productDir);
        final FlexReaderPlugIn plugIn = resolvePlugIn(productDir.getFileName().toString());
        final FlexProductReader reader = (FlexProductReader) plugIn.createReaderInstance();
        return reader.readProductNodes(xmlHeader.toFile(), null);
    }

    private FlexReaderPlugIn resolvePlugIn(String dirName) {
        final String upper = dirName.toUpperCase();
        if (upper.startsWith("FLX_L1B")) return new FlexL1bReaderPlugIn();
        if (upper.startsWith("FLX_L1C")) return new FlexL1cReaderPlugIn();
        return new FlexL2ReaderPlugIn();
    }

    private Path findProductDir(String prefix) {
        final File[] dirs = dataDir.toFile().listFiles(
                f -> f.isDirectory() && f.getName().toUpperCase().startsWith(prefix.toUpperCase()));
        if (dirs == null || dirs.length == 0) {
            return null;
        }
        return dirs[0].toPath();
    }

    private Path findXmlHeader(Path productDir) throws IOException {
        final File[] xmlFiles = productDir.toFile().listFiles(
                f -> f.isFile() && f.getName().toLowerCase().endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            throw new IOException("No XML header found in " + productDir);
        }
        return xmlFiles[0].toPath();
    }
}
