package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.engine_utilities.util.TestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.*;

public class PrismaL2DProductReaderTest {

    private static final String prismaZipFilePath = "D:\\Arbeit_BC\\Projekte\\aaaaaaa_GIT\\SNAP11\\PRS_L2D_STD_20240402102837_20240402102842_0001.zip";
    private static final String prismaFilePath = "D:\\Arbeit_BC\\Projekte\\aaaaaaa_GIT\\SNAP11\\PRS_L2D_STD_20240402102837_20240402102842_0001.he5";

    private PrismaProductReader ppr;

    @Before
    @STTM("SNAP-3445")
    public void setUp() throws Exception {
        ppr = new PrismaProductReader(new PrismaProductReaderPlugin());

    }

    @After
    @STTM("SNAP-3445")
    public void tearDown() throws Exception {
    }

    @Test
    @STTM("SNAP-3445")
    public void readProductNodes_he5_inputObjectString() throws IOException {
        final Product product = ppr.readProductNodes(prismaFilePath, null);
        checkL2DProduct(product);
    }

    @Test
    @Ignore
    @STTM("SNAP-3445")
    public void readProductNodes_zip_inputObjectString() throws IOException {
        final Product product = ppr.readProductNodes(prismaZipFilePath, null);
        checkL2DProduct(product);
    }

    private static void checkL2DProduct(Product product) {
        assertThat(product, is(notNullValue()));
        assertThat(product.getSceneRasterWidth(), is(1213));
        assertThat(product.getSceneRasterHeight(), is(1239));
    }

    @Test
    @STTM("SNAP-3445")
    public void readBandRasterData() {
    }
}