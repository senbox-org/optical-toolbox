package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariableDescriptorTest {

    @Test
    @STTM("SNAP-3711")
    public void testConstruction() {
        final VariableDescriptor descriptor = new VariableDescriptor();

        assertEquals('v', descriptor.getType());
        assertEquals(-1, descriptor.getWidth());
        assertEquals(-1, descriptor.getHeight());
        assertEquals(-1, descriptor.getDepth());
    }
}
