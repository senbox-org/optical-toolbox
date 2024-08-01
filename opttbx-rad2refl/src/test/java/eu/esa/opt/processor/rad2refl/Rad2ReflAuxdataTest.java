package eu.esa.opt.processor.rad2refl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Rad2ReflAuxdataTest {

    @Test
    public void testGetResolutionType() {
        assertEquals(ResolutionType.REDUCED, Rad2ReflAuxdata.getResolutionType("MER_RR__1P"));
        assertEquals(ResolutionType.REDUCED, Rad2ReflAuxdata.getResolutionType("Derived from(MER_RR__1P)"));

        assertEquals(ResolutionType.FULL, Rad2ReflAuxdata.getResolutionType("MER_FR__1P"));
        assertEquals(ResolutionType.FULL, Rad2ReflAuxdata.getResolutionType("Derived from(MER_FRS_1P)"));
        assertEquals(ResolutionType.FULL, Rad2ReflAuxdata.getResolutionType("Derived from (MER_FRS_1P)"));

        assertEquals(ResolutionType.UNKNOWN, Rad2ReflAuxdata.getResolutionType("STRANGE-TYPE"));
    }
}
