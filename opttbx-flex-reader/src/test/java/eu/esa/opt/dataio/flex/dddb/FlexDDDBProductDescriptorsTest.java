package eu.esa.opt.dataio.flex.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class FlexDDDBProductDescriptorsTest {


    @Test
    @STTM("SNAP-4126")
    public void testLoadL1bProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L1B_OBS");

        assertEquals("FLX_L1B_OBS", descriptor.getProductType());
        assertEquals(536, descriptor.getWidth());
        assertEquals(3640, descriptor.getHeight());
        assertEquals(6, descriptor.getDataFiles().length);
        assertEquals("measurement_data_hre1", descriptor.getDataFiles()[0]);
        assertEquals("measurement_data_hre2", descriptor.getDataFiles()[1]);
        assertEquals("measurement_data_lres", descriptor.getDataFiles()[2]);
        assertEquals("annotation_data_hre1", descriptor.getDataFiles()[3]);
        assertEquals("annotation_data_hre2", descriptor.getDataFiles()[4]);
        assertEquals("annotation_data_lres", descriptor.getDataFiles()[5]);
        assertTrue("L1B should have bitmask flag masks", descriptor.getFlagMasks().length > 0);
        assertTrue("L1B flag masks should be bitmask", descriptor.getFlagMasks()[0].isBitmask());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L1C_FLXSYN");

        assertEquals("FLX_L1C_FLXSYN", descriptor.getProductType());
        assertEquals(536, descriptor.getWidth());
        assertEquals(3640, descriptor.getHeight());
        assertEquals(7, descriptor.getDataFiles().length);
        assertTrue(descriptor.getFlagMasks().length > 0);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2ProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L2_FLXSYN");

        assertEquals("FLX_L2_FLXSYN", descriptor.getProductType());
        assertEquals(366, descriptor.getWidth());
        assertEquals(366, descriptor.getHeight());
        assertEquals(5, descriptor.getDataFiles().length);
        assertTrue(descriptor.getFlagMasks().length > 0);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1bVariableDescriptors() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] hre1 = dddb.getVariableDescriptors("measurement_data_hre1", "FLX_L1B_OBS");
        assertEquals(280, hre1.length);
        assertEquals("FLORIS_HR1B_1_radiance", hre1[0].getName());
        assertEquals("FLORIS_HR1U_140_radiance_unc", hre1[279].getName());
        assertEquals("Measurement_data", hre1[0].getNcGroupPath());

        final FlexVariableDescriptor[] hre2 = dddb.getVariableDescriptors("measurement_data_hre2", "FLX_L1B_OBS");
        assertEquals(538, hre2.length);
        assertEquals("FLORIS_HR2B_1_radiance", hre2[0].getName());

        final FlexVariableDescriptor[] lres = dddb.getVariableDescriptors("measurement_data_lres", "FLX_L1B_OBS");
        assertEquals(470, lres.length);
        assertEquals("FLORIS_LRB_1_radiance", lres[0].getName());
        assertEquals("FLORIS_LRU_235_radiance_unc", lres[469].getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1bVariableDescriptors_scaleOffsetFillValueSamples() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] hre1Measurement =
                dddb.getVariableDescriptors("measurement_data_hre1", "FLX_L1B_OBS");
        final FlexVariableDescriptor radiance = findDescriptor(hre1Measurement, "FLORIS_HR1B_1_radiance");
        assertEquals(0.013384721241891384, radiance.getScaleFactor(), 1.0e-12);
        assertEquals(-0.020648246631026268, radiance.getAddOffset(), 1.0e-12);
        assertNull(radiance.getFillValue());

        final FlexVariableDescriptor radianceUncertainty = findDescriptor(hre1Measurement, "FLORIS_HR1B_1_radiance_unc");
        assertEquals(0.34264886379241943, radianceUncertainty.getScaleFactor(), 1.0e-12);
        assertEquals(0.0, radianceUncertainty.getAddOffset(), 1.0e-12);
        assertEquals(0.0, radianceUncertainty.getFillValue(), 1.0e-12);

        final FlexVariableDescriptor[] hre1Annotation =
                dddb.getVariableDescriptors("annotation_data_hre1", "FLX_L1B_OBS");
        final FlexVariableDescriptor longitude = findDescriptor(hre1Annotation, "HRE1_longitude");
        assertEquals(9.999999974752427e-7, longitude.getScaleFactor(), 1.0e-12);
        assertEquals(0.0, longitude.getAddOffset(), 1.0e-12);
        assertEquals(-2147483648.0, longitude.getFillValue(), 1.0e-12);

        final FlexVariableDescriptor altitude = findDescriptor(hre1Annotation, "HRE1_altitude");
        assertEquals(1.0, altitude.getScaleFactor(), 1.0e-12);
        assertEquals(0.0, altitude.getAddOffset(), 1.0e-12);
        assertEquals(-32768.0, altitude.getFillValue(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1bVariableDescriptors_scaleOffsetFillValueCounts() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();
        final FlexProductDescriptor productDescriptor = dddb.getProductDescriptor("FLX_L1B_OBS");

        int scaleFactorCount = 0;
        int addOffsetCount = 0;
        int fillValueCount = 0;
        int anyValueCount = 0;

        for (final String dataFile : productDescriptor.getDataFiles()) {
            final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors(dataFile, "FLX_L1B_OBS");
            for (final FlexVariableDescriptor descriptor : descriptors) {
                final boolean hasScaleFactor = descriptor.getScaleFactor() != 1.0;
                final boolean hasAddOffset = descriptor.getAddOffset() != 0.0;
                final boolean hasFillValue = descriptor.getFillValue() != null;
                if (hasScaleFactor) {
                    scaleFactorCount++;
                }
                if (hasAddOffset) {
                    addOffsetCount++;
                }
                if (hasFillValue) {
                    fillValueCount++;
                }
                if (hasScaleFactor || hasAddOffset || hasFillValue) {
                    anyValueCount++;
                }
            }
        }

        assertEquals(1306, scaleFactorCount);
        assertEquals(644, addOffsetCount);
        assertEquals(779, fillValueCount);
        assertEquals(1423, anyValueCount);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cVariableDescriptors_allFiles() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();
        final FlexProductDescriptor pd = dddb.getProductDescriptor("FLX_L1C_FLXSYN");

        int totalVars = 0;
        for (String dataFile : pd.getDataFiles()) {
            final FlexVariableDescriptor[] vars = dddb.getVariableDescriptors(dataFile, "FLX_L1C_FLXSYN");
            assertNotNull(vars);
            assertTrue(dataFile + " should have at least 1 variable", vars.length > 0);
            totalVars += vars.length;
        }
        assertTrue("L1C should have many variable descriptors", totalVars > 20);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cGeometry() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("geometry", "FLX_L1C_FLXSYN");

        assertEquals(10, vars.length);
        assertEquals("latitude", vars[0].getName());
        assertEquals("Annotation_data/Geometry", vars[0].getNcGroupPath());
        assertEquals("float64", vars[0].getDataType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cMeasurementData() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("measurement_data", "FLX_L1C_FLXSYN");

        assertEquals(11, vars.length);
        assertEquals("floris_toa_radiance", vars[0].getName());
        assertEquals(580, vars[0].getDepth());
        assertEquals("_ch_", vars[0].getDepthPrefixToken());
        assertEquals('s', vars[0].getType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2VariableDescriptors_allFiles() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();
        final FlexProductDescriptor pd = dddb.getProductDescriptor("FLX_L2_FLXSYN");

        int totalVars = 0;
        for (String dataFile : pd.getDataFiles()) {
            final FlexVariableDescriptor[] vars = dddb.getVariableDescriptors(dataFile, "FLX_L2_FLXSYN");
            assertNotNull(vars);
            assertTrue(dataFile + " should have at least 1 variable", vars.length > 0);
            totalVars += vars.length;
        }
        assertTrue("L2 should have many variable descriptors", totalVars > 40);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2Fluorescence() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("fluorescence", "FLX_L2_FLXSYN");

        assertEquals(14, vars.length);
        assertEquals("sif_emission_spectrum", vars[0].getName());
        assertEquals("L2_Fluorescence", vars[0].getNcGroupPath());
        assertEquals(111, vars[0].getDepth());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2Vegetation() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("vegetation", "FLX_L2_FLXSYN");

        assertEquals(20, vars.length);
        assertEquals("leaf_area_index", vars[0].getName());
        assertEquals("L2_Vegetation", vars[0].getNcGroupPath());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cAndL2VariableDescriptors_keepDefaultScaleOffsetFillValuesImplicit() throws IOException {
        assertScaleOffsetFillValueCounts("FLX_L1C_FLXSYN", 0, 0, 0, 0);
        assertScaleOffsetFillValueCounts("FLX_L2_FLXSYN", 0, 0, 0, 0);
    }

    private static FlexVariableDescriptor findDescriptor(FlexVariableDescriptor[] descriptors, String name) {
        for (final FlexVariableDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(name)) {
                return descriptor;
            }
        }
        fail("Descriptor not found: " + name);
        return null;
    }

    private static void assertScaleOffsetFillValueCounts(String productType, int expectedScaleFactors,
                                                         int expectedAddOffsets, int expectedFillValues,
                                                         int expectedAnyValues) throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();
        final FlexProductDescriptor productDescriptor = dddb.getProductDescriptor(productType);

        int scaleFactorCount = 0;
        int addOffsetCount = 0;
        int fillValueCount = 0;
        int anyValueCount = 0;
        for (String dataFile : productDescriptor.getDataFiles()) {
            for (FlexVariableDescriptor descriptor : dddb.getVariableDescriptors(dataFile, productType)) {
                boolean hasAnyValue = false;
                if (Double.compare(descriptor.getScaleFactor(), 1.0) != 0) {
                    scaleFactorCount++;
                    hasAnyValue = true;
                }
                if (Double.compare(descriptor.getAddOffset(), 0.0) != 0) {
                    addOffsetCount++;
                    hasAnyValue = true;
                }
                if (descriptor.getFillValue() != null) {
                    fillValueCount++;
                    hasAnyValue = true;
                }
                if (hasAnyValue) {
                    anyValueCount++;
                }
            }
        }

        assertEquals(expectedScaleFactors, scaleFactorCount);
        assertEquals(expectedAddOffsets, addOffsetCount);
        assertEquals(expectedFillValues, fillValueCount);
        assertEquals(expectedAnyValues, anyValueCount);
    }
}
