package eu.esa.opt.dataio.s2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GridPairTest {

    @Test
    public void testConstruction() {
        final GridPair gridPair = new GridPair("key", "theName");

        assertEquals("key", gridPair.getKey());
        assertEquals("theName", gridPair.getName());
    }
}
