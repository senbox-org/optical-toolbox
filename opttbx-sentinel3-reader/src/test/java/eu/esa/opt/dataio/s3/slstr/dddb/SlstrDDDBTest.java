package eu.esa.opt.dataio.s3.slstr.dddb;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SlstrDDDBTest {

    private SlstrDDDB dddb;

    @Before
    public void before() {
        dddb = SlstrDDDB.instance();
    }

    @Test
    public void testInstanceCreation() {
        final SlstrDDDB instance_2 = SlstrDDDB.instance();
        assertNotNull(instance_2);

        assertSame(dddb, instance_2);
    }

    @Test
    public void testGetProductFileNames() throws IOException {
        final String[] productFileNames = dddb.getProductFileNames();
        assertEquals(4, productFileNames.length);
    }

    @Test
    public void testGetVariableInformations() throws IOException {
        final String type = "SLP_6AG_L2_AOP";
        final String processingVersion = "28.0.4_b3";

        final VariableInformation[] variableInformations = dddb.getVariableInformations(type, processingVersion);
        assertEquals(10, variableInformations.length);
    }
}
