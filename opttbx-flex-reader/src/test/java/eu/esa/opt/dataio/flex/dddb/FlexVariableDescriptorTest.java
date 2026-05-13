package eu.esa.opt.dataio.flex.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.*;


public class FlexVariableDescriptorTest {


    @Test
    @STTM("SNAP-4126")
    public void testDefaultValues() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();

        assertEquals('v', descriptor.getType());
        assertEquals(-1, descriptor.getWidth());
        assertEquals(-1, descriptor.getHeight());
        assertEquals(-1, descriptor.getDepth());
        assertEquals("", descriptor.getDepthPrefixToken());
        assertEquals("", descriptor.getUnits());
        assertEquals("", descriptor.getDescription());
        assertEquals("", descriptor.getNcGroupPath());
        assertEquals("", descriptor.getNcDataFile());
        assertEquals("", descriptor.getWavelengthReference());
        assertEquals("", descriptor.getFwhmReference());
        assertFalse(descriptor.isOptional());
    }

    @Test
    @STTM("SNAP-4126")
    public void testSettersAndGetters() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();

        descriptor.setName("latitude");
        descriptor.setNcVarName("latitude");
        descriptor.setNcGroupPath("Annotation_data/Geometry");
        descriptor.setType('v');
        descriptor.setDataType("float64");
        descriptor.setWidth(536);
        descriptor.setHeight(3640);
        descriptor.setDepth(4);
        descriptor.setDepthPrefixToken("_instrument_");
        descriptor.setUnits("degrees_north");
        descriptor.setDescription("Latitude in WGS84");
        descriptor.setOptional(true);
        descriptor.setWavelengthReference("floris_spectral_grid");
        descriptor.setFwhmReference("floris_spectral_bandwidth_grid");
        descriptor.setNcDataFile("hre1.nc");

        assertEquals("latitude", descriptor.getName());
        assertEquals("latitude", descriptor.getNcVarName());
        assertEquals("Annotation_data/Geometry", descriptor.getNcGroupPath());
        assertEquals('v', descriptor.getType());
        assertEquals("float64", descriptor.getDataType());
        assertEquals(536, descriptor.getWidth());
        assertEquals(3640, descriptor.getHeight());
        assertEquals(4, descriptor.getDepth());
        assertEquals("_instrument_", descriptor.getDepthPrefixToken());
        assertEquals("degrees_north", descriptor.getUnits());
        assertEquals("Latitude in WGS84", descriptor.getDescription());
        assertTrue(descriptor.isOptional());
        assertEquals("floris_spectral_grid", descriptor.getWavelengthReference());
        assertEquals("floris_spectral_bandwidth_grid", descriptor.getFwhmReference());
        assertEquals("hre1.nc", descriptor.getNcDataFile());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetVariableType() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();

        descriptor.setType('v');
        assertEquals(FlexVariableType.VARIABLE, descriptor.getVariableType());

        descriptor.setType('f');
        assertEquals(FlexVariableType.FLAG, descriptor.getVariableType());

        descriptor.setType('s');
        assertEquals(FlexVariableType.SPECIAL, descriptor.getVariableType());

        descriptor.setType('m');
        assertEquals(FlexVariableType.METADATA, descriptor.getVariableType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetFullNcPath_withGroupPath() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setNcVarName("latitude");
        descriptor.setNcGroupPath("Annotation_data/Geometry");

        assertEquals("Annotation_data/Geometry/latitude", descriptor.getFullNcPath());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetFullNcPath_withoutGroupPath() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setNcVarName("latitude");

        assertEquals("latitude", descriptor.getFullNcPath());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetFullNcPath_emptyGroupPath() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setNcVarName("latitude");
        descriptor.setNcGroupPath("");

        assertEquals("latitude", descriptor.getFullNcPath());
    }
}
