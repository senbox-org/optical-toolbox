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
    public void testGetResourceFileName() {
        assertEquals("OL_1_EFR_004.json", DDDB.getResourceFileName("OL_1_EFR", "004"));
        assertEquals("SL_1_RBT_004.json", DDDB.getResourceFileName("SL_1_RBT", "004"));

        assertEquals("SL_1_RBT.json", DDDB.getResourceFileName("SL_1_RBT", ""));
        assertEquals("OL_1_EFR.json", DDDB.getResourceFileName("OL_1_EFR", null));

        assertEquals("/variables/instrument_data_004.json", DDDB.getResourceFileName("/variables/instrument_data", "004"));
        assertEquals("/variables/instrument_data.json", DDDB.getResourceFileName("/variables/instrument_data", null));
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetDDDBResourceName() {
        assertEquals("dddb/SL_1_RBT/heffalump.org", DDDB.getDddbResourceName("SL_1_RBT", "heffalump.org"));
        assertEquals("dddb/OL_1_EFR/variables/geo_coordinates.json", DDDB.getDddbResourceName("OL_1_EFR", "variables/geo_coordinates.json"));

        assertEquals("dddb/SL_1_RBT", DDDB.getDddbResourceName("SL_1_RBT", ""));
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetVariableDescriptors() throws IOException {
        final VariableDescriptor[] variableDescriptors = dddb.getVariableDescriptors("geo_coordinates.nc", "OL_1_EFR", "004");

        assertEquals(3, variableDescriptors.length);

        assertEquals("altitude", variableDescriptors[0].getName());
        assertEquals("float32", variableDescriptors[1].getDataType());
    }
}
