package eu.esa.opt.dataio.flex.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class FlexDDDBProductDescriptorsTest {


    @Test
    @STTM("SNAP-4126")
    public void testLoadL1bProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L1B_OBS");

        assertEquals("FLX_L1B_OBS", descriptor.getProductType());
        assertEquals(536, descriptor.getWidth());
        assertEquals(3640, descriptor.getHeight());
        assertEquals(6, descriptor.getDataFiles().length);
        assertEquals("measurement_data_hre1", descriptor.getDataFiles()[0]);
        assertEquals("measurement_data_hre2", descriptor.getDataFiles()[1]);
        assertEquals("measurement_data_lres", descriptor.getDataFiles()[2]);
        assertEquals("annotation_data_hre1", descriptor.getDataFiles()[3]);
        assertEquals("annotation_data_hre2", descriptor.getDataFiles()[4]);
        assertEquals("annotation_data_lres", descriptor.getDataFiles()[5]);
        assertTrue("L1B should have bitmask flag masks", descriptor.getFlagMasks().length > 0);
        assertTrue("L1B flag masks should be bitmask", descriptor.getFlagMasks()[0].isBitmask());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L1C_FLXSYN");

        assertEquals("FLX_L1C_FLXSYN", descriptor.getProductType());
        assertEquals(536, descriptor.getWidth());
        assertEquals(3640, descriptor.getHeight());
        assertEquals(7, descriptor.getDataFiles().length);
        assertTrue(descriptor.getFlagMasks().length > 0);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2ProductDescriptor() throws IOException {
        final FlexProductDescriptor descriptor = FlexDDDB.getInstance().getProductDescriptor("FLX_L2_FLXSYN");

        assertEquals("FLX_L2_FLXSYN", descriptor.getProductType());
        assertEquals(366, descriptor.getWidth());
        assertEquals(366, descriptor.getHeight());
        assertEquals(5, descriptor.getDataFiles().length);
        assertTrue(descriptor.getFlagMasks().length > 0);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1bVariableDescriptors() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();

        final FlexVariableDescriptor[] hre1 = dddb.getVariableDescriptors("measurement_data_hre1", "FLX_L1B_OBS");
        assertEquals(280, hre1.length);
        assertEquals("FLORIS_HR1B_1_radiance", hre1[0].getName());
        assertEquals("FLORIS_HR1U_140_radiance_unc", hre1[279].getName());
        assertEquals("Measurement_data", hre1[0].getNcGroupPath());

        final FlexVariableDescriptor[] hre2 = dddb.getVariableDescriptors("measurement_data_hre2", "FLX_L1B_OBS");
        assertEquals(538, hre2.length);
        assertEquals("FLORIS_HR2B_1_radiance", hre2[0].getName());

        final FlexVariableDescriptor[] lres = dddb.getVariableDescriptors("measurement_data_lres", "FLX_L1B_OBS");
        assertEquals(470, lres.length);
        assertEquals("FLORIS_LRB_1_radiance", lres[0].getName());
        assertEquals("FLORIS_LRU_235_radiance_unc", lres[469].getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cVariableDescriptors_allFiles() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();
        final FlexProductDescriptor pd = dddb.getProductDescriptor("FLX_L1C_FLXSYN");

        int totalVars = 0;
        for (String dataFile : pd.getDataFiles()) {
            final FlexVariableDescriptor[] vars = dddb.getVariableDescriptors(dataFile, "FLX_L1C_FLXSYN");
            assertNotNull(vars);
            assertTrue(dataFile + " should have at least 1 variable", vars.length > 0);
            totalVars += vars.length;
        }
        assertTrue("L1C should have many variable descriptors", totalVars > 20);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cGeometry() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("geometry", "FLX_L1C_FLXSYN");

        assertEquals(10, vars.length);
        assertEquals("latitude", vars[0].getName());
        assertEquals("Annotation_data/Geometry", vars[0].getNcGroupPath());
        assertEquals("float64", vars[0].getDataType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL1cMeasurementData() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("measurement_data", "FLX_L1C_FLXSYN");

        assertEquals(11, vars.length);
        assertEquals("floris_toa_radiance", vars[0].getName());
        assertEquals(580, vars[0].getDepth());
        assertEquals("_ch_", vars[0].getDepthPrefixToken());
        assertEquals('s', vars[0].getType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2VariableDescriptors_allFiles() throws IOException {
        final FlexDDDB dddb = FlexDDDB.getInstance();
        final FlexProductDescriptor pd = dddb.getProductDescriptor("FLX_L2_FLXSYN");

        int totalVars = 0;
        for (String dataFile : pd.getDataFiles()) {
            final FlexVariableDescriptor[] vars = dddb.getVariableDescriptors(dataFile, "FLX_L2_FLXSYN");
            assertNotNull(vars);
            assertTrue(dataFile + " should have at least 1 variable", vars.length > 0);
            totalVars += vars.length;
        }
        assertTrue("L2 should have many variable descriptors", totalVars > 40);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2Fluorescence() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("fluorescence", "FLX_L2_FLXSYN");

        assertEquals(14, vars.length);
        assertEquals("sif_emission_spectrum", vars[0].getName());
        assertEquals("L2_Fluorescence", vars[0].getNcGroupPath());
        assertEquals(111, vars[0].getDepth());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLoadL2Vegetation() throws IOException {
        final FlexVariableDescriptor[] vars = FlexDDDB.getInstance().getVariableDescriptors("vegetation", "FLX_L2_FLXSYN");

        assertEquals(20, vars.length);
        assertEquals("leaf_area_index", vars[0].getName());
        assertEquals("L2_Vegetation", vars[0].getNcGroupPath());
    }
}
