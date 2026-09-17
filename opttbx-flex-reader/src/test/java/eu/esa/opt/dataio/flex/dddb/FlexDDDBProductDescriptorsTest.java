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
        assertEquals("Measurement_data", descriptor.getDimensionGroupPath());
        assertEquals("number_of_across_track_samples", descriptor.getWidthDimensionName());
        assertEquals("number_of_along_track_samples", descriptor.getHeightDimensionName());
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
    public void testLoadL1bProductDescriptor_versionedResources() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        assertDddbResourceExists("FLX_L1B_OBS/FLX_L1B_OBS_03.02.json");
        final FlexProductDescriptor descriptor = dddb.getProductDescriptor("FLX_L1B_OBS", "03.02");
        assertEquals("FLX_L1B_OBS", descriptor.getProductType());
        assertEquals("number_of_across_track_samples", descriptor.getWidthDimensionName());
        for (String dataFile : descriptor.getDataFiles()) {
            assertDddbResourceExists("FLX_L1B_OBS/variables_03.02/" + dataFile + "_03.02.json");
        }

        final FlexVariableDescriptor[] descriptors =
                dddb.getVariableDescriptors("measurement_data_hre1", "FLX_L1B_OBS", "03.02");
        assertEquals(280, descriptors.length);
        assertEquals("FLORIS_HR1B_1_radiance", descriptors[0].getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L1C_FLXSYN");

        assertEquals("FLX_L1C_FLXSYN", descriptor.getProductType());
        assertEquals("Measurement_data", descriptor.getDimensionGroupPath());
        assertEquals("number_of_across_track_samples", descriptor.getWidthDimensionName());
        assertEquals("number_of_along_track_samples", descriptor.getHeightDimensionName());
        assertEquals(7, descriptor.getDataFiles().length);
        assertTrue(descriptor.getFlagMasks().length > 0);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2ProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L2_FLXSYN");

        assertEquals("FLX_L2_FLXSYN", descriptor.getProductType());
        assertEquals("L2_Atmosphere", descriptor.getDimensionGroupPath());
        assertEquals("number_of_easting_pixels", descriptor.getWidthDimensionName());
        assertEquals("number_of_northing_pixels", descriptor.getHeightDimensionName());
        assertEquals(5, descriptor.getDataFiles().length);
        assertCurrentL2FlagMasks(descriptor);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cProductDescriptor_versionedResources() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor oldDescriptor = dddb.getProductDescriptor("FLX_L1C_FLXSYN", "03.02");
        assertEquals("Measurement_data", oldDescriptor.getDimensionGroupPath());
        assertEquals("number_of_across_track_samples", oldDescriptor.getWidthDimensionName());
        assertEquals("number_of_along_track_samples", oldDescriptor.getHeightDimensionName());
        assertFalse(oldDescriptor.getBandGroupingPattern().contains("floris_toa_radiance_coregis_uncertainty_ch_*"));

        final FlexProductDescriptor newDescriptor = dddb.getProductDescriptor("FLX_L1C_FLXSYN", "04.02");
        assertEquals("Measurement_data", newDescriptor.getDimensionGroupPath());
        assertTrue(newDescriptor.getBandGroupingPattern().contains("floris_toa_radiance_coregis_uncertainty_ch_*"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2ProductDescriptor_versionedResources() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor oldDescriptor = dddb.getProductDescriptor("FLX_L2_FLXSYN", "03.02");
        assertEquals("L2_Atmosphere", oldDescriptor.getDimensionGroupPath());
        assertEquals("number_of_across_track_samples", oldDescriptor.getWidthDimensionName());
        assertEquals("number_of_along_track_samples", oldDescriptor.getHeightDimensionName());
        assertTrue(oldDescriptor.getFlagMasks().length > 0);

        final FlexProductDescriptor newDescriptor = dddb.getProductDescriptor("FLX_L2_FLXSYN", "04.01");
        assertEquals("L2_Atmosphere", newDescriptor.getDimensionGroupPath());
        assertEquals("number_of_easting_pixels", newDescriptor.getWidthDimensionName());
        assertEquals("number_of_northing_pixels", newDescriptor.getHeightDimensionName());
        assertCurrentL2FlagMasks(newDescriptor);
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
        assertEquals("float32", vars[0].getDataType());
        assertEquals(-999.0, vars[0].getFillValue(), 1.0e-12);
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
        assertEquals("float32", vars[0].getDataType());

        final FlexVariableDescriptor olciRadiance = findDescriptor(vars, "olci_toa_radiance");
        assertEquals("uint16", olciRadiance.getDataType());
        assertEquals(0.009155552843, olciRadiance.getScaleFactor(), 1.0e-15);
        assertEquals(65535.0, olciRadiance.getFillValue(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cMeasurementData_oldVersion() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors(
                "measurement_data", "FLX_L1C_FLXSYN", "03.02");

        assertEquals(11, vars.length);
        assertDescriptorMissing(vars, "floris_toa_radiance_coregis_uncertainty");

        final FlexVariableDescriptor florisRadiance = findDescriptor(vars, "floris_toa_radiance");
        assertEquals("float64", florisRadiance.getDataType());

        final FlexVariableDescriptor tirRadiance = findDescriptor(vars, "slstr_nadir_tir_toa_radiance");
        assertEquals("float64", tirRadiance.getDataType());
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
    public void testLoadL2Quality_oldVersion() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors(
                "quality", "FLX_L2_FLXSYN", "03.02");

        assertEquals(1, vars.length);

        final FlexVariableDescriptor qualityFlags = findDescriptor(vars, "quality_flags");
        assertEquals("L2_Quality", qualityFlags.getNcGroupPath());
        assertEquals("int16", qualityFlags.getDataType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cAndL2VariableDescriptors_includeCurrentScaleOffsetFillValues() throws IOException {
        assertScaleOffsetFillValueCounts("FLX_L1C_FLXSYN", 10, 0, 38, 38);
        assertScaleOffsetFillValueCounts("FLX_L2_FLXSYN", 16, 1, 61, 61);
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

    private static void assertDescriptorMissing(FlexVariableDescriptor[] descriptors, String name) {
        for (final FlexVariableDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(name)) {
                fail("Descriptor should not be present: " + name);
            }
        }
    }

    private static void assertCurrentL2FlagMasks(FlexProductDescriptor descriptor) {
        assertEquals(103, descriptor.getFlagMasks().length);

        assertFlagMask(descriptor, "quality_flags_atmosphere", "wsa", 1);
        assertFlagMask(descriptor, "quality_flags_atmosphere", "wde", 2);
        assertFlagMask(descriptor, "quality_flags_floris_app_reflectance", "ext", 64);
        assertFlagMask(descriptor, "quality_flags_sif", "rat", 16);
        assertFlagMask(descriptor, "quality_flags_lcc", "fai", 16);
        assertFlagMask(descriptor, "quality_flags_fqe", "fai", 8);
        assertFlagMask(descriptor, "quality_flags_photo", "nda", 8);
        assertFlagMask(descriptor, "quality_flags_s3_reflectance", "saturated_sample_o21", 0x80000000);
        assertFlagMask(descriptor, "pixel_classification", "dense_vegetation", 128);
    }

    private static void assertFlagMask(FlexProductDescriptor descriptor, String bandName, String name, int value) {
        for (final FlexFlagMask flagMask : descriptor.getFlagMasks()) {
            if (bandName.equals(flagMask.getBandName()) && name.equals(flagMask.getName())) {
                assertEquals(value, flagMask.getValue());
                assertTrue(flagMask.isBitmask());
                return;
            }
        }
        fail("Flag mask not found: " + bandName + "." + name);
    }

    private static void assertDddbResourceExists(String resourceName) {
        assertNotNull("Missing DDDB resource: " + resourceName,
                FlexDDDBProductDescriptorsTest.class.getClassLoader()
                        .getResource("eu/esa/opt/dataio/flex/dddb/" + resourceName));
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
