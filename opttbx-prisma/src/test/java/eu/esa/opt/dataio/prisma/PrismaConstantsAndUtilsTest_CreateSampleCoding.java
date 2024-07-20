package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.IndexCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.SampleCoding;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class PrismaConstantsAndUtilsTest_CreateSampleCoding {

    private Product product;
    private String datasetName;

    @STTM("SNAP-3445")
    @Before
    public void setUp() throws Exception {
        product = new Product("P", "T");
    }

    @STTM("SNAP-3445")
    @Test
    public void test_unknownDatasetName() {
        //preparation
        datasetName = "test";

        //execution
        final SampleCoding coding = PrismaConstantsAndUtils.createSampleCoding(product, datasetName);

        //verification
        assertThat(coding, is(nullValue()));
        assertThat(product.getFlagCodingGroup().getNodeCount(), is(0));
        assertThat(product.getIndexCodingGroup().getNodeCount(), is(0));
    }

    @STTM("SNAP-3445")
    @Test
    public void createFlagCodingFor_MAPS_PIXEL_L2_ERR_MATRIX() {
        //preparation
        datasetName = "MAPS_PIXEL_L2_ERR_MATRIX";

        //execution
        final SampleCoding coding = PrismaConstantsAndUtils.createSampleCoding(product, datasetName);

        //verification
        assertThat(coding, is(instanceOf(FlagCoding.class)));
        final FlagCoding flagCoding = (FlagCoding) coding;
        assertThat(flagCoding.getName(), is(datasetName));

        final String[] expectedFlagNames = {"WVM_inv", "WVM>max", "WVM<min", "AOD_!ev", "AOD>max", "AOD<min", "AEX_inv", "COT_inv"};
        final int[] expectedFlagMasks = {1, 2, 4, 8, 16, 32, 64, 128};
        final String[] expectedDescriptions = {
                "Invalid pixel in WVM evaluation",
                "Full - scale pixel in WVM evaluation (> max)",
                "Full - scale pixel in WVM evaluation (< min)",
                "AOD map not evaluated (not Dark-Dense Vegetation pixel or invalid pixel)",
                "Full - scale pixel in AOD evaluation (> max)",
                "Full - scale pixel in AOD evaluation (< min)",
                "Invalid pixel in AEX evaluation",
                "Invalid pixel in COT evaluation",
        };
        final String[] flagNames = flagCoding.getFlagNames();
        assertThat(flagNames.length, is(expectedFlagNames.length));
        assertThat(flagNames, is(equalTo(expectedFlagNames)));
        for (int i = 0; i < flagNames.length; i++) {
            String flagName = flagNames[i];
            assertThat("Invalid mask value at idx " + i, flagCoding.getFlagMask(flagName), is(expectedFlagMasks[i]));
            assertThat("Invalid description at idx " + i, flagCoding.getFlag(flagName).getDescription(), is(expectedDescriptions[i]));
        }
        assertThat(product.getFlagCodingGroup().getNodeCount(), is(1));
        assertThat(product.getIndexCodingGroup().getNodeCount(), is(0));
        assertThat(product.getFlagCodingGroup().get(0), is(sameInstance(coding)));
    }

    @STTM("SNAP-3445")
    @Test
    public void createIndexCodingFor_SWIR_PIXEL_L2_ERR_MATRIX() {
        verifyIndexCodingForVnirAndSwirErrMask("SWIR_PIXEL_L2_ERR_MATRIX");
    }

    @STTM("SNAP-3445")
    @Test
    public void createIndexCodingFor_VNIR_PIXEL_L2_ERR_MATRIX() {
        verifyIndexCodingForVnirAndSwirErrMask("VNIR_PIXEL_L2_ERR_MATRIX");
    }

    private void verifyIndexCodingForVnirAndSwirErrMask(String name) {
        //preparation
        datasetName = name;

        //execution
        final SampleCoding coding = PrismaConstantsAndUtils.createSampleCoding(product, datasetName);

        //verification
        assertThat(coding, is(instanceOf(IndexCoding.class)));
        final IndexCoding indexCoding = (IndexCoding) coding;
        assertThat(indexCoding.getName(), is(datasetName));

        final String[] expectedIndexNames = {"ok", "inv", "neg", "sat"};
        final int[] expectedIndexValues = {0, 1, 2, 3};
        final String[] expectedDescriptions = {
                "pixel ok",
                "Invalid pixel from L1 product",
                "Negative value after atmospheric correction",
                "Saturated value after atmospheric correction"
        };
        final String[] indexNames = indexCoding.getIndexNames();
        assertThat(indexNames.length, is(expectedIndexNames.length));
        assertThat(indexNames, is(equalTo( expectedIndexNames)));
        for (int i = 0; i < expectedIndexNames.length; i++) {
            String indexName = expectedIndexNames[i];
            assertThat("Invalid value at idx " + i, indexCoding.getIndexValue(indexName), is(expectedIndexValues[i]));
            assertThat("Invalid description at idx " + i, indexCoding.getIndex(indexName).getDescription(), is(expectedDescriptions[i]));
        }
        assertThat(product.getFlagCodingGroup().getNodeCount(), is(0));
        assertThat(product.getIndexCodingGroup().getNodeCount(), is(1));
        assertThat(product.getIndexCodingGroup().get(0), is(sameInstance(coding)));
    }
}