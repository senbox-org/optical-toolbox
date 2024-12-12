package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DDDBTest {

    private DDDB dddb;

    @Before
    public void setUp() {
        dddb = DDDB.getInstance();
    }

    @Test
    @STTM("SNAP-3711")
    public void testIsSingleton() {
        final DDDB dddb_2 = DDDB.getInstance();

        assertSame(dddb_2, dddb);
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetProductDescriptor() throws IOException {
        final ProductDescriptor olciL1Descriptor = dddb.getProductDescriptor("OL_1_EFR", "004");
        assertEquals("removedPixelsData", olciL1Descriptor.getExcludedIds());

        final ProductDescriptor slstrL1Descriptor = dddb.getProductDescriptor("SL_1_RBT", "004");
        assertEquals("", slstrL1Descriptor.getExcludedIds());
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetProductDescriptor_invalidResource() throws IOException {
        final ProductDescriptor invalid = dddb.getProductDescriptor("IN_V_ALI", "D");
        assertNull(invalid);
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetResourceName() {
        assertEquals("OL_1_EFR_004.json", DDDB.getResourceName("OL_1_EFR", "004"));
        assertEquals("SL_1_RBT_004.json", DDDB.getResourceName("SL_1_RBT", "004"));
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetDDDBResourceName() {
        assertEquals("dddb/SL_1_RBT/heffalump.org", DDDB.getDddbResourceName("SL_1_RBT", "heffalump.org"));
    }
}
