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
        assertEquals(536, descriptor.getWidth());
        assertEquals(3640, descriptor.getHeight());
        assertEquals(2, descriptor.getDataFiles().length);
        assertEquals("test_variables", descriptor.getDataFiles()[0]);
        assertEquals("test_flags", descriptor.getDataFiles()[1]);
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

        assertEquals("pixel_classification", flagMasks[1].getBandName());
        assertEquals("land", flagMasks[1].getName());
        assertEquals(1, flagMasks[1].getValue());
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testGetProductDescriptor_invalidType() throws IOException {
        FlexDDDB.getInstance().getProductDescriptor("NON_EXISTENT_PRODUCT");
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
}
