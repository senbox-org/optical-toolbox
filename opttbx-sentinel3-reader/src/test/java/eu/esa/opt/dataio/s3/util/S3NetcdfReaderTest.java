package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.Product;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Tonio Fincke
 */
public class S3NetcdfReaderTest {

    private S3NetcdfReader reader;

    @Before
    public void setUp() throws IOException {
        reader = new S3NetcdfReader();
    }

    @Test
    public void testReadProduct() throws Exception {
        final Product product = reader.readProductNodes(getTestFilePath("../../s3/FRP_in.nc"), null);
        assertNotNull(product);
        assertEquals("FRP_in", product.getName());
        assertEquals("NetCDF", product.getProductType());
        assertEquals(1568, product.getSceneRasterWidth());
        assertEquals(266, product.getSceneRasterHeight());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetSampleMeanings() {
        Attribute flagAttribute = new Attribute("blank_separated", "flag_a flag_b flag_c flag_d");
        String[] sampleMeanings = S3NetcdfReader.getSampleMeanings(flagAttribute);
        assertEquals(4, sampleMeanings.length);
        assertEquals("flag_a", sampleMeanings[0]);
        assertEquals("flag_b", sampleMeanings[1]);

        flagAttribute = new Attribute("string_array", Array.factory(DataType.STRING, new int[]{4}, new String[]{"flag_a", "flag_b", "flag_c", "flag_d"}));
        sampleMeanings = S3NetcdfReader.getSampleMeanings(flagAttribute);
        assertEquals(4, sampleMeanings.length);
        assertEquals("flag_c", sampleMeanings[2]);
        assertEquals("flag_d", sampleMeanings[3]);
    }

    private String getTestFilePath(String name) throws URISyntaxException {
        URL url = getClass().getResource(name);
        URI uri = new URI(url.toString());
        return uri.getPath();
    }

}
