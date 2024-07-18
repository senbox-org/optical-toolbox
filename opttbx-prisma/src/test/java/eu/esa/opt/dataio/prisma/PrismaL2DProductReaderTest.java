package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.test.LongTestRunner;
import org.esa.snap.core.datamodel.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.*;

@RunWith(LongTestRunner.class)
public class PrismaL2DProductReaderTest {

    // TODO: 13.06.2024 SE -- change this 4 lines to a solution which runs on any computer
//    private static final String prismaZipFilePath = "D:\\Projekte\\aaaaaaa_GIT\\SNAP11\\PRS_L2D_STD_20240402102837_20240402102842_0001.zip";
    private static final String prismaFilePath = "D:\\Projekte\\aaaaaaa_GIT\\SNAP11\\PRS_L2D_STD_20240402102837_20240402102842_0001.he5";
//    private static final String prismaZipFilePath = "D:\\Arbeit_BC\\Projekte\\aaaaaaa_GIT\\SNAP11\\PRS_L2D_STD_20240402102837_20240402102842_0001.zip";
//    private static final String prismaFilePath = "D:\\Arbeit_BC\\Projekte\\aaaaaaa_GIT\\SNAP11\\PRS_L2D_STD_20240402102837_20240402102842_0001.he5";

    private PrismaProductReader ppr;

    @STTM("SNAP-3445")
    @Before
    public void setUp() throws Exception {
        ppr = new PrismaProductReader(new PrismaProductReaderPlugin());
    }

    @STTM("SNAP-3445")
    @Test
    public void readProductNodes_he5_inputObjectString() throws IOException {
        final Product product = ppr.readProductNodes(prismaFilePath, null);
        checkL2DProduct(product);
    }

    private static void checkL2DProduct(Product product) {
        assertThat(product, is(notNullValue()));
        assertThat(product.getSceneRasterWidth(), is(1239));
        assertThat(product.getSceneRasterHeight(), is(1213));
    }

    @STTM("SNAP-3445")
    @Test
    public void readBandRasterData() {
    }
}