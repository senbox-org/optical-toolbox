package eu.esa.opt.slstr.pdu.stitching;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.util.ArrayUtils;
import org.esa.snap.test.LongTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Tonio Fincke
 */
@RunWith(LongTestRunner.class)
public class PDUStitchingOpTest {

    private static final String EXPECTED_STITCHED_FILE_NAME_PATTERN =
            "S3A_SL_1_RBT____20130707T153252_20130707T154752_2[0-9]{7}T[0-9]{6}_0299_158_182______SVL_O_NR_001.SEN3";
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testOperator() throws URISyntaxException, IOException {
        File targetDirectory = tempFolder.newFolder("testOperator");

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("targetDir", targetDirectory);
        Map<String, Product> sourceProductMap = new HashMap<>();
        String[] productPaths = new String[3];
        productPaths[0] = getResource(TestUtils.FIRST_FILE_NAME).getAbsolutePath();
        productPaths[1] = getResource(TestUtils.SECOND_FILE_NAME).getAbsolutePath();
        productPaths[2] = getResource(TestUtils.THIRD_FILE_NAME).getAbsolutePath();
        parameterMap.put("sourceProductPaths", productPaths);

        assertEquals(0, targetDirectory.list().length);

        Operator stitchingOperator = GPF.getDefaultInstance().createOperator("PduStitching", parameterMap,
                sourceProductMap, null);
        stitchingOperator.execute(ProgressMonitor.NULL);

        assertProductHasBeenCreated(targetDirectory);
    }

    @Test
    public void testOperator_wildcards() throws URISyntaxException, IOException {
        File targetDirectory = tempFolder.newFolder("testOperator");

        Map<String, Object> parameterMap = new HashMap<>();
        Map<String, Product> sourceProductMap = new HashMap<>();
        parameterMap.put("targetDir", targetDirectory);
        String[] productPaths = new String[1];
        final URL resource = PDUStitchingOpTest.class.getResource("");
        URI uri = new URI(resource.toString());
        productPaths[0] = uri.getPath() + "S*/xfdumanifest.xml";
        parameterMap.put("sourceProductPaths", productPaths);

        assertEquals(0, targetDirectory.list().length);

        Operator stitchingOperator = GPF.getDefaultInstance().createOperator("PduStitching", parameterMap,
                sourceProductMap, null);
        stitchingOperator.execute(ProgressMonitor.NULL);

        assertProductHasBeenCreated(targetDirectory);
    }

    private void assertProductHasBeenCreated(File directory) {

        final Pattern pattern = Pattern.compile(EXPECTED_STITCHED_FILE_NAME_PATTERN);
        final File[] stitchedProducts = directory.listFiles();
        assertNotNull(stitchedProducts);
        assertEquals(1, stitchedProducts.length);
        assert (pattern.matcher(stitchedProducts[0].getName()).matches());
        final String[] productContents = stitchedProducts[0].list();
        assertNotNull(productContents);
        assertEquals(4, productContents.length);
        assert (ArrayUtils.isMemberOf("F1_BT_io.nc", productContents));
        assert(ArrayUtils.isMemberOf("met_tx.nc", productContents));
        assert(ArrayUtils.isMemberOf("viscal.nc", productContents));
        assert(ArrayUtils.isMemberOf("xfdumanifest.xml", productContents));
    }

    @Test
    public void testSpi() {
        final OperatorSpi spi = new PDUStitchingOp.Spi();

        assertTrue(spi.getOperatorClass().isAssignableFrom(PDUStitchingOp.class));
    }

    private static File getResource(String fileName) throws URISyntaxException {
        final String fullFileName = fileName + "/xfdumanifest.xml";
        final URL resource = PDUStitchingOpTest.class.getResource(fullFileName);
        URI uri = new URI(resource.toString());
        return new File(uri.getPath());
    }
}