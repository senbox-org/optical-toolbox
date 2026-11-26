package eu.esa.opt.dos;

import com.bc.ceres.core.runtime.internal.IOHelper;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.fail;

public class DarkObjectSubtractionIntegrationTest {

    private Path targetDirectory;

    @Before
    public void setUp() {
        targetDirectory = IOHelper.createATempDir("dos_test_out_");
        if (!IOHelper.createDirectories(targetDirectory)) {
            fail("Unable to create test target directory");
        }
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(new DarkObjectSubtractionOp.Spi());
    }

    @After
    public void tearDown() {
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(new DarkObjectSubtractionOp.Spi());
        if (Files.isDirectory(targetDirectory)) {
            if (!FileUtils.deleteTree(targetDirectory)) {
                fail("Unable to delete test directory");
            }
        }
    }

}
