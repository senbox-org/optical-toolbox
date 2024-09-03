package eu.esa.opt.dataio.s3.slstr.dddb;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SlstrDDDBTest {

    @Test
    public void testInstanceCreation() {
        final SlstrDDDB instance_1 = SlstrDDDB.instance();
        assertNotNull(instance_1);

        final SlstrDDDB instance_2 = SlstrDDDB.instance();
        assertNotNull(instance_2);

        assertSame(instance_1, instance_2);
    }

    @Test
    public void testGetProductFileNames() throws IOException {
        final SlstrDDDB slstrDDDB = new SlstrDDDB();

        final String[] productFileNames = slstrDDDB.getProductFileNames();
        assertEquals(4, productFileNames.length);
    }
}
