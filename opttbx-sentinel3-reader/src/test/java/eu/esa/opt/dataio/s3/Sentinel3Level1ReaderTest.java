package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.dddb.ProductDescriptor;
import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("AnonymousInnerClass")
public class Sentinel3Level1ReaderTest {

    private static Manifest createManifest() {
        return new Manifest() {
            @Override
            public String getProductName() {
                return "S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3";
            }

            @Override
            public String getProductType() {
                return "OL_1_EFR";
            }

            @Override
            public String getBaselineCollection() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String getDescription() {
                return "SENTINEL-3 OLCI Level 1 Earth Observation Full Resolution Product";
            }

            @Override
            public ProductData.UTC getStartTime() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public ProductData.UTC getStopTime() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public List<String> getFileNames(String schema) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public List<String> getFileNames(String[] excluded) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public MetadataElement getMetadata() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String getXPathString(String xPath) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public int getXPathInt(String xPath) {
                if (xPath.contains("rowsPerTiePoint")) {
                    return 4;
                } else if (xPath.contains("columnsPerTiePoint")) {
                    return 3;
                } else if (xPath.contains("width")) {
                    return 108;
                } else if (xPath.contains("height")) {
                    return 176;
                }

                throw new RuntimeException("unexpected value");
            }
        };
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testEnsureProductWithAndHeight_alreadySet() {
        final ProductDescriptor productDescriptor = new ProductDescriptor();
        productDescriptor.setWidth(27);
        productDescriptor.setHeight(38);

        final Manifest manifest = createManifest();
        Sentinel3Level1Reader.ensureWidthAndHeight(productDescriptor, manifest);

        assertEquals(27, productDescriptor.getWidth());
        assertEquals(38, productDescriptor.getHeight());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testEnsureProductWithAndHeight_fromXPath() {
        final ProductDescriptor productDescriptor = new ProductDescriptor();
        productDescriptor.setWidthXPath("/data/simple/width");
        productDescriptor.setHeightXPath("/data/simple/height");

        final Manifest manifest = createManifest();
        Sentinel3Level1Reader.ensureWidthAndHeight(productDescriptor, manifest);

        assertEquals(108, productDescriptor.getWidth());
        assertEquals(176, productDescriptor.getHeight());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testEnsureTiePointWithAndHeight_alreadySet() {
        VariableDescriptor variableDescriptor = new VariableDescriptor();
        variableDescriptor.setWidth(14);
        variableDescriptor.setHeight(28);
        variableDescriptor.setTpSubsamplingX(3);
        variableDescriptor.setTpSubsamplingY(4);
        variableDescriptor.setType('t');

        final Manifest manifest = createManifest();
        Sentinel3Level1Reader.ensureWidthAndHeight(variableDescriptor, manifest);

        assertEquals(14, variableDescriptor.getWidth());
        assertEquals(28, variableDescriptor.getHeight());
        assertEquals(3, variableDescriptor.getTpSubsamplingX());
        assertEquals(4, variableDescriptor.getTpSubsamplingY());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testEnsureTiePointWithAndHeight_fromManifest() {
        VariableDescriptor variableDescriptor = new VariableDescriptor();
        variableDescriptor.setWidthXPath("/data/simple/width");
        variableDescriptor.setHeightXPath("/data/simple/height");
        variableDescriptor.settpXSubsamplingXPath("/tie_points/geo/columnsPerTiePoint");
        variableDescriptor.settpYSubsamplingXPath("/tie_points/geo/rowsPerTiePoint");
        variableDescriptor.setType('t');

        final Manifest manifest = createManifest();
        Sentinel3Level1Reader.ensureWidthAndHeight(variableDescriptor, manifest);

        assertEquals(36, variableDescriptor.getWidth());
        assertEquals(44, variableDescriptor.getHeight());
        assertEquals(3, variableDescriptor.getTpSubsamplingX());
        assertEquals(4, variableDescriptor.getTpSubsamplingY());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testCreateDescriptorKey() {
        final VariableDescriptor descriptor = new VariableDescriptor();
        descriptor.setName("Elfriede");

        assertEquals("Elfriede", Sentinel3Level1Reader.createDescriptorKey(descriptor));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testClose() throws IOException {
        Sentinel3Level1Reader sentinel3DDDBReader = new Sentinel3Level1Reader(new Sentinel3ProductReaderPlugIn());

        sentinel3DDDBReader.close();
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAssignResultData() throws IOException {
        final int[] rawData = {12, 13, 14, 15};
        final Array rawArray = Array.factory(DataType.INT, new int[]{2, 2}, rawData);

        final ProductData productDataDouble = ProductData.createInstance(ProductData.TYPE_FLOAT64, 4);
        Sentinel3Level1Reader.assignResultData(productDataDouble, rawArray);
        assertEquals(12.0, productDataDouble.getElemDoubleAt(0), 1e-8);
        assertEquals(14.0, productDataDouble.getElemDoubleAt(2), 1e-8);

        final ProductData productDataInt = ProductData.createInstance(ProductData.TYPE_INT32, 4);
        Sentinel3Level1Reader.assignResultData(productDataInt, rawArray);
        assertEquals(13, productDataInt.getElemIntAt(1));
        assertEquals(15, productDataInt.getElemIntAt(3));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetLayerName() throws IOException {
        assertEquals("FWHM_band_11", Sentinel3Level1Reader.getLayerName("FWHM", 11));
        assertEquals("Gandasum_band_6", Sentinel3Level1Reader.getLayerName("Gandasum", 6));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testIsLayerName() throws IOException {
        assertTrue(Sentinel3Level1Reader.isLayerName("FWHM_band_11"));
        assertTrue(Sentinel3Level1Reader.isLayerName("Gandasum_band_6"));

        assertFalse(Sentinel3Level1Reader.isLayerName("OaRadiance"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testVariableNameFromLayerName() throws IOException {
        assertEquals("FWHM", Sentinel3Level1Reader.getVariableNameFromLayerName("FWHM_band_11"));
        assertEquals("Gandasum", Sentinel3Level1Reader.getVariableNameFromLayerName("Gandasum_band_6"));

        assertEquals("Heffalump", Sentinel3Level1Reader.getVariableNameFromLayerName("Heffalump"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testLayerIndexFromLayerName() throws IOException {
        assertEquals(11, Sentinel3Level1Reader.getLayerIndexFromLayerName("FWHM_band_11"));
        assertEquals(6, Sentinel3Level1Reader.getLayerIndexFromLayerName("Gandasum_band_6"));

        assertEquals(-1, Sentinel3Level1Reader.getLayerIndexFromLayerName("Heffalump"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testIsPressureLevelName() throws IOException {
        assertTrue(Sentinel3Level1Reader.isPressureLevelName("atmospheric_temperature_profile_pressure_level_14"));

        assertFalse(Sentinel3Level1Reader.isPressureLevelName("Gandasum_band_6"));
        assertFalse(Sentinel3Level1Reader.isPressureLevelName("OnTheAirTonight"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testVariableNameFromPressureLevelName() throws IOException {
        assertEquals("whatever_measured", Sentinel3Level1Reader.getVariableNameFromPressureLevelName("whatever_measured_pressure_level_11"));

        assertEquals("Gandasum_band_6", Sentinel3Level1Reader.getVariableNameFromPressureLevelName("Gandasum_band_6"));
        assertEquals("Heffalump", Sentinel3Level1Reader.getVariableNameFromPressureLevelName("Heffalump"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testLayerIndexFromPressureLevelName() throws IOException {
        assertEquals(10, Sentinel3Level1Reader.getLayerIndexFromPressureLevelName("whatever_measured_pressure_level_10"));
        assertEquals(6, Sentinel3Level1Reader.getLayerIndexFromPressureLevelName("Gandasum_pressure_level_6"));

        assertEquals(-1, Sentinel3Level1Reader.getLayerIndexFromPressureLevelName("Nasenmann.org"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testLayerIndexFromTiePointName() {
        final VariableDescriptor variableDescriptor = new VariableDescriptor();
        variableDescriptor.setDepthPrefixToken("_pressure_level_");
        assertEquals(10, Sentinel3Level1Reader.getLayerIndexFromTiePointName("whatever_measured_pressure_level_10", variableDescriptor));
        assertEquals(6, Sentinel3Level1Reader.getLayerIndexFromPressureLevelName("Gandasum_pressure_level_6"));

        assertEquals(-1, Sentinel3Level1Reader.getLayerIndexFromPressureLevelName("Nasenmann.org"));
    }
}
