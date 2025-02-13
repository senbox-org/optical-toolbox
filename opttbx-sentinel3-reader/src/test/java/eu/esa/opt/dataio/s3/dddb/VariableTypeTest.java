package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class VariableTypeTest {

    @Test
    @STTM("SNAP-3711")
    public void testFromChar() {
        assertEquals(VariableType.VARIABLE, VariableType.fromChar('v'));
        assertEquals(VariableType.FLAG, VariableType.fromChar('f'));
        assertEquals(VariableType.TIE_POINT, VariableType.fromChar('t'));
        assertEquals(VariableType.METADATA, VariableType.fromChar('m'));
        assertEquals(VariableType.SPECIAL, VariableType.fromChar('s'));

        try {
            VariableType.fromChar('K');
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
