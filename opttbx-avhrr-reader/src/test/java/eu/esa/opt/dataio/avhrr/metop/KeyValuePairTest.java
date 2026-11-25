package eu.esa.opt.dataio.avhrr.metop;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyValuePairTest {

    @Test
    public void testConstruction() {
        final String field = "The key side                   :What remains";

        KeyValuePair keyValuePair = new KeyValuePair(field);
        assertEquals("The key side", keyValuePair.key);
        assertEquals("What remains", keyValuePair.value);
    }
}
