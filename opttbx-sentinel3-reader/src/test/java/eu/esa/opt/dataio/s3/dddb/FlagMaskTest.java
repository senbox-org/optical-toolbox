package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FlagMaskTest {

    @Test
    @STTM("SNAP-4149")
    public void testParameterConstructor() {
        final FlagMask flagMask = new FlagMask("hans", "im", "glück");

        assertEquals("hans", flagMask.getName());
        assertEquals("im", flagMask.getExpression());
        assertEquals("glück", flagMask.getDescription());
    }
}
