package eu.esa.opt.dos;

import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.fail;

public class DarkObjectSubtractionIntegrationTest {

    private File targetDirectory;

    @Before
    public void setUp() throws IOException {
        targetDirectory = Files.createTempDirectory("dos_test_out_").toFile();
        if (!targetDirectory.mkdirs()) {
            fail("Unable to create test target directory");
        }
        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(new DarkObjectSubtractionOp.Spi());
    }

    @After
    public void tearDown() {
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(new DarkObjectSubtractionOp.Spi());
        if (targetDirectory.isDirectory()) {
            if (!FileUtils.deleteTree(targetDirectory)) {
                fail("Unable to delete test directory");
            }
        }
    }

}
