package eu.esa.opt.dataio.flex.dddb;

import org.junit.Test;

import static org.junit.Assert.*;

public class FlexVariableTypeTest {

    @Test
    public void testFromChar_variable() {
        assertEquals(FlexVariableType.VARIABLE, FlexVariableType.fromChar('v'));
    }

    @Test
    public void testFromChar_flag() {
        assertEquals(FlexVariableType.FLAG, FlexVariableType.fromChar('f'));
    }

    @Test
    public void testFromChar_tiePoint() {
        assertEquals(FlexVariableType.TIE_POINT, FlexVariableType.fromChar('t'));
    }

    @Test
    public void testFromChar_metadata() {
        assertEquals(FlexVariableType.METADATA, FlexVariableType.fromChar('m'));
    }

    @Test
    public void testFromChar_special() {
        assertEquals(FlexVariableType.SPECIAL, FlexVariableType.fromChar('s'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromChar_unknownType() {
        FlexVariableType.fromChar('x');
    }
}
