package eu.esa.opt.owt;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuxdataExceptionTest {

    @Test
    public void testConstruction() {
        AuxdataException auxdataException = new AuxdataException("BÄM!");
        assertEquals("BÄM!", auxdataException.getMessage());
        assertNull(auxdataException.getCause());

        final IllegalArgumentException cause = new IllegalArgumentException("whatever");
        auxdataException = new AuxdataException("Zing", cause);
        assertEquals("Zing", auxdataException.getMessage());
        assertSame(cause, auxdataException.getCause());
    }
}
