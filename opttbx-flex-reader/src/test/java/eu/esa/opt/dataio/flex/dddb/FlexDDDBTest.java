package eu.esa.opt.dataio.flex.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class FlexDDDBTest {


    @Test
    @STTM("SNAP-4126")
    public void testGetInstance() {
        final FlexDDDB dddb = FlexDDDB.getInstance();
        assertNotNull(dddb);
        assertSame(dddb, FlexDDDB.getInstance());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetProductDescriptor() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor descriptor = dddb.getProductDescriptor("TEST_PRODUCT");

        assertNotNull(descriptor);
        assertEquals("TEST_PRODUCT", descriptor.getProductType());
        assertEquals("Measurement_data", descriptor.getDimensionGroupPath());
        assertEquals("number_of_across_track_samples", descriptor.getWidthDimensionName());
        assertEquals("number_of_along_track_samples", descriptor.getHeightDimensionName());
        assertEquals(2, descriptor.getDataFiles().length);
        assertEquals("test_variables", descriptor.getDataFiles()[0]);
        assertEquals("test_flags", descriptor.getDataFiles()[1]);
        assertEquals("radiance_ch_*:reflectance_ch_*", descriptor.getBandGroupingPattern());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetProductDescriptor_withVersion() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor descriptor = dddb.getProductDescriptor("TEST_PRODUCT", "1.0");

        assertNotNull(descriptor);
        assertEquals("TEST_PRODUCT", descriptor.getProductType());
        assertEquals("Versioned_data", descriptor.getDimensionGroupPath());
        assertEquals("versioned_width", descriptor.getWidthDimensionName());
        assertEquals("versioned_height", descriptor.getHeightDimensionName());
        assertEquals("versioned_ch_*", descriptor.getBandGroupingPattern());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetProductDescriptor_withMissingVersionFallsBackToDefault() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor descriptor = dddb.getProductDescriptor("TEST_PRODUCT", "9.9");

        assertNotNull(descriptor);
        assertEquals("Measurement_data", descriptor.getDimensionGroupPath());
        assertEquals("radiance_ch_*:reflectance_ch_*", descriptor.getBandGroupingPattern());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetProductDescriptor_flagMasks() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor descriptor = dddb.getProductDescriptor("TEST_PRODUCT");
        final FlexFlagMask[] flagMasks = descriptor.getFlagMasks();

        assertEquals(2, flagMasks.length);
        assertEquals("pixel_classification", flagMasks[0].getBandName());
        assertEquals("not_valid", flagMasks[0].getName());
        assertEquals(0, flagMasks[0].getValue());
        assertEquals("Not-valid pixel", flagMasks[0].getDescription());
        assertTrue(flagMasks[0].isOverlayMask());

        assertEquals("pixel_classification", flagMasks[1].getBandName());
        assertEquals("land", flagMasks[1].getName());
        assertEquals(1, flagMasks[1].getValue());
        assertTrue(flagMasks[1].isOverlayMask());
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testGetProductDescriptor_invalidType() throws IOException {
        FlexDDDB.getInstance().getProductDescriptor("NON_EXISTENT_PRODUCT");
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testGetProductDescriptor_malformedJsonThrowsIOException() throws IOException {
        FlexDDDB.getInstance().getProductDescriptor("BROKEN_PRODUCT");
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors("test_variables", "TEST_PRODUCT");

        assertNotNull(descriptors);
        assertEquals(4, descriptors.length);

        assertEquals("latitude", descriptors[0].getName());
        assertEquals("latitude", descriptors[0].getNcVarName());
        assertEquals("Annotation_data/Geometry", descriptors[0].getNcGroupPath());
        assertEquals('v', descriptors[0].getType());
        assertEquals(FlexVariableType.VARIABLE, descriptors[0].getVariableType());
        assertEquals("float64", descriptors[0].getDataType());
        assertEquals("degrees_north", descriptors[0].getUnits());
        assertEquals("Latitude in WGS84", descriptors[0].getDescription());

        assertEquals("longitude", descriptors[1].getName());
        assertEquals("degrees_east", descriptors[1].getUnits());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors_withVersion() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors("test_variables", "TEST_PRODUCT", "1.0");

        assertNotNull(descriptors);
        assertEquals(1, descriptors.length);
        assertEquals("versioned_radiance", descriptors[0].getName());
        assertEquals("Versioned_data", descriptors[0].getNcGroupPath());
        assertEquals("float32", descriptors[0].getDataType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors_withMissingVersionFallsBackToDefault() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors("test_variables", "TEST_PRODUCT", "9.9");

        assertNotNull(descriptors);
        assertEquals(4, descriptors.length);
        assertEquals("latitude", descriptors[0].getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors_specialType() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors("test_variables", "TEST_PRODUCT");
        final FlexVariableDescriptor radiance = descriptors[2];

        assertEquals("radiance", radiance.getName());
        assertEquals("floris_toa_radiance", radiance.getNcVarName());
        assertEquals("Measurement_data", radiance.getNcGroupPath());
        assertEquals('s', radiance.getType());
        assertEquals(FlexVariableType.SPECIAL, radiance.getVariableType());
        assertEquals(580, radiance.getDepth());
        assertEquals("_ch_", radiance.getDepthPrefixToken());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors_metadataType() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors("test_variables", "TEST_PRODUCT");
        final FlexVariableDescriptor metadata = descriptors[3];

        assertEquals("coregistration_uncertainty", metadata.getName());
        assertEquals('m', metadata.getType());
        assertEquals(FlexVariableType.METADATA, metadata.getVariableType());
        assertEquals(2, metadata.getWidth());
        assertEquals(4, metadata.getHeight());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors_flagType() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors("test_flags", "TEST_PRODUCT");

        assertNotNull(descriptors);
        assertEquals(2, descriptors.length);

        assertEquals("pixel_classification", descriptors[0].getName());
        assertEquals('f', descriptors[0].getType());
        assertEquals(FlexVariableType.FLAG, descriptors[0].getVariableType());
        assertEquals("Annotation_data/Ancillary_data", descriptors[0].getNcGroupPath());

        assertEquals("quality_flags", descriptors[1].getName());
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors_nonExistentFile() throws IOException {
        FlexDDDB.getInstance().getVariableDescriptors("non_existent", "TEST_PRODUCT");
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testGetVariableDescriptors_malformedJsonThrowsIOException() throws IOException {
        FlexDDDB.getInstance().getVariableDescriptors("broken_variables", "BROKEN_PRODUCT");
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetFullNcPath() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors("test_variables", "TEST_PRODUCT");

        assertEquals("Annotation_data/Geometry/latitude", descriptors[0].getFullNcPath());
        assertEquals("Measurement_data/floris_toa_radiance", descriptors[2].getFullNcPath());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetProductDescriptor_isCached() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor first = dddb.getProductDescriptor("TEST_PRODUCT");
        final FlexProductDescriptor second = dddb.getProductDescriptor("TEST_PRODUCT");

        assertSame(first, second);
    }

    @Test
    @STTM("SNAP-4126")
    public void testResourceNames() {
        assertEquals("TEST_PRODUCT/TEST_PRODUCT.json",
                FlexDDDB.getProductResourceName("TEST_PRODUCT", null));
        assertEquals("TEST_PRODUCT/TEST_PRODUCT_1.0.json",
                FlexDDDB.getProductResourceName("TEST_PRODUCT", "1.0"));
        assertEquals("TEST_PRODUCT/variables/test_variables.json",
                FlexDDDB.getVariableResourceName("test_variables", "TEST_PRODUCT", null));
        assertEquals("TEST_PRODUCT/variables_1.0/test_variables_1.0.json",
                FlexDDDB.getVariableResourceName("test_variables", "TEST_PRODUCT", "1.0"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFlexL1CDescriptors_includeCurrentVariables() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor productDescriptor = dddb.getProductDescriptor("FLX_L1C_FLXSYN");
        assertTrue(productDescriptor.getBandGroupingPattern().contains("floris_toa_radiance_coregis_uncertainty_ch_*"));

        final FlexVariableDescriptor[] measurementDescriptors = dddb.getVariableDescriptors("measurement_data", "FLX_L1C_FLXSYN");
        final FlexVariableDescriptor coregisUncertainty = findDescriptor(measurementDescriptors, "floris_toa_radiance_coregis_uncertainty");
        assertEquals("Measurement_data", coregisUncertainty.getNcGroupPath());
        assertEquals('s', coregisUncertainty.getType());
        assertEquals("uint16", coregisUncertainty.getDataType());
        assertEquals(580, coregisUncertainty.getDepth());
        assertFalse(coregisUncertainty.isOptional());
        assertDescriptorMissing(measurementDescriptors, "slstr_nadir_tir_toa_radiance");

        final FlexVariableDescriptor[] qualityDescriptors = dddb.getVariableDescriptors("quality", "FLX_L1C_FLXSYN");
        final FlexVariableDescriptor olciFlags = findDescriptor(qualityDescriptors, "quality_flags_olci");
        assertEquals("Annotation_data/Quality", olciFlags.getNcGroupPath());
        assertEquals('b', olciFlags.getType());
        assertEquals("uint16", olciFlags.getDataType());
        assertFalse(olciFlags.isOptional());

        final FlexVariableDescriptor hr1Flags = findDescriptor(qualityDescriptors, "quality_flags_hr1");
        assertEquals("uint8", hr1Flags.getDataType());
        assertFalse(hr1Flags.isOptional());
    }

    @Test
    @STTM("SNAP-4126")
    public void testFlexL2Descriptors_includeCurrentVariables() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexProductDescriptor productDescriptor = dddb.getProductDescriptor("FLX_L2_FLXSYN");
        assertTrue(productDescriptor.getBandGroupingPattern().contains("olci_apparent_reflectance_uncertainty_ch_*"));
        assertTrue(productDescriptor.getBandGroupingPattern().contains("slstr_apparent_reflectance_uncertainty_ch_*"));
        assertTrue(productDescriptor.getFlagMasks().length > 0);
        assertFlagMask(productDescriptor, "quality_flags_atmosphere", "wsa", 1);
        assertFlagMask(productDescriptor, "quality_flags_s3_reflectance", "saturated_sample_o21", 0x80000000);
        assertFlagMask(productDescriptor, "pixel_classification", "dense_vegetation", 128);

        final FlexVariableDescriptor[] atmosphereDescriptors = dddb.getVariableDescriptors("atmosphere", "FLX_L2_FLXSYN");
        final FlexVariableDescriptor olciUncertainty = findDescriptor(atmosphereDescriptors, "olci_apparent_reflectance_uncertainty");
        assertEquals("L2_Atmosphere", olciUncertainty.getNcGroupPath());
        assertEquals('s', olciUncertainty.getType());
        assertEquals("uint16", olciUncertainty.getDataType());
        assertEquals(21, olciUncertainty.getDepth());
        assertFalse(olciUncertainty.isOptional());

        final FlexVariableDescriptor[] qualityDescriptors = dddb.getVariableDescriptors("quality", "FLX_L2_FLXSYN");
        assertDescriptorMissing(qualityDescriptors, "quality_flags");

        final FlexVariableDescriptor atmosphereFlags = findDescriptor(qualityDescriptors, "quality_flags_atmosphere");
        assertEquals("Quality", atmosphereFlags.getNcGroupPath());
        assertEquals('b', atmosphereFlags.getType());
        assertEquals("uint8", atmosphereFlags.getDataType());
        assertFalse(atmosphereFlags.isOptional());

        final FlexVariableDescriptor s3Flags = findDescriptor(qualityDescriptors, "quality_flags_s3_reflectance");
        assertEquals("uint32", s3Flags.getDataType());

        final FlexVariableDescriptor pixelClassification = findDescriptor(qualityDescriptors, "pixel_classification");
        assertEquals("uint16", pixelClassification.getDataType());
    }

    private static FlexVariableDescriptor findDescriptor(FlexVariableDescriptor[] descriptors, String name) {
        for (final FlexVariableDescriptor descriptor : descriptors) {
            if (name.equals(descriptor.getName())) {
                return descriptor;
            }
        }
        fail("Descriptor not found: " + name);
        return null;
    }

    private static void assertDescriptorMissing(FlexVariableDescriptor[] descriptors, String name) {
        for (final FlexVariableDescriptor descriptor : descriptors) {
            if (name.equals(descriptor.getName())) {
                fail("Descriptor should not be present: " + name);
            }
        }
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
}
