package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariableDescriptorTest {

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testConstruction() {
        final VariableDescriptor descriptor = new VariableDescriptor();

        assertEquals('v', descriptor.getType());
        assertEquals(-1, descriptor.getWidth());
        assertEquals(-1, descriptor.getHeight());
        assertEquals(-1, descriptor.getDepth());
        assertEquals("", descriptor.getDepthPrefixToken());
        assertEquals("", descriptor.getValidExpression());
        assertEquals("", descriptor.getUnits());
        assertEquals("", descriptor.getDescription());
        assertEquals("", descriptor.getTpXSubsamplingXPath());
        assertEquals("", descriptor.getTpYSubsamplingXPath());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetVaraibleType() {
        final VariableDescriptor descriptor = new VariableDescriptor();

        descriptor.setType('v');
        assertEquals(VariableType.VARIABLE, descriptor.getVariableType());

        descriptor.setType('m');
        assertEquals(VariableType.METADATA, descriptor.getVariableType());
    }
}
