package eu.esa.opt.dataio.flex.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.*;


public class FlexVariableTypeTest {


    @Test
    @STTM("SNAP-4126")
    public void testFromChar_variable() {
        assertEquals(FlexVariableType.VARIABLE, FlexVariableType.fromChar('v'));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFromChar_flag() {
        assertEquals(FlexVariableType.FLAG, FlexVariableType.fromChar('f'));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFromChar_bitMask() {
        assertEquals(FlexVariableType.BITMASK_FLAG, FlexVariableType.fromChar('b'));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFromChar_metadata() {
        assertEquals(FlexVariableType.METADATA, FlexVariableType.fromChar('m'));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFromChar_special() {
        assertEquals(FlexVariableType.SPECIAL, FlexVariableType.fromChar('s'));
    }

    @Test(expected = IllegalArgumentException.class)
    @STTM("SNAP-4126")
    public void testFromChar_unknownType() {
        FlexVariableType.fromChar('x');
    }
}
