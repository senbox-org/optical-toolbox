package eu.esa.opt.dataio.flex.dddb;

import org.junit.Test;

import static org.junit.Assert.*;

public class FlexVariableDescriptorTest {

    @Test
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
        assertFalse(descriptor.isOptional());
    }

    @Test
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
    }

    @Test
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

        descriptor.setType('t');
        assertEquals(FlexVariableType.TIE_POINT, descriptor.getVariableType());
    }

    @Test
    public void testGetFullNcPath_withGroupPath() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setNcVarName("latitude");
        descriptor.setNcGroupPath("Annotation_data/Geometry");

        assertEquals("Annotation_data/Geometry/latitude", descriptor.getFullNcPath());
    }

    @Test
    public void testGetFullNcPath_withoutGroupPath() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setNcVarName("latitude");

        assertEquals("latitude", descriptor.getFullNcPath());
    }

    @Test
    public void testGetFullNcPath_emptyGroupPath() {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setNcVarName("latitude");
        descriptor.setNcGroupPath("");

        assertEquals("latitude", descriptor.getFullNcPath());
    }
}
