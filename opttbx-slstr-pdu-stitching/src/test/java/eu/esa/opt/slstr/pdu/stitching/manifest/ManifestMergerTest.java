package eu.esa.opt.slstr.pdu.stitching.manifest;


import eu.esa.opt.slstr.pdu.stitching.PDUStitchingException;
import eu.esa.opt.slstr.pdu.stitching.TestUtils;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * @author Tonio Fincke
 */
public class ManifestMergerTest {

    @Test
    public void testMergeManifests_OneFile() throws IOException, ParserConfigurationException, TransformerException, PDUStitchingException {
        final File inputManifest = getManifestFile(TestUtils.FIRST_FILE_NAME);
        final Date now = Calendar.getInstance().getTime();
        final File productDir = new File(ManifestMergerTest.class.getResource("").getFile());
        ManifestMerger manifestMerger = new ManifestMerger();
        final File manifestFile = manifestMerger.createMergedManifest(new File[]{inputManifest}, now, productDir, 5000);
        assertTrue(manifestFile.exists());
    }

    @Test
    public void testMergeManifests_MultipleFiles() throws IOException, ParserConfigurationException, TransformerException, PDUStitchingException {
        final Date now = Calendar.getInstance().getTime();
        final File productDir = new File(ManifestMergerTest.class.getResource("").getFile());
        ManifestMerger manifestMerger = new ManifestMerger();
        final File manifestFile = manifestMerger.createMergedManifest(getManifestFiles(), now, productDir, 5000);
        assertTrue(manifestFile.exists());
    }

    private static File[] getManifestFiles() {
        return new File[]{getManifestFile(TestUtils.FIRST_FILE_NAME),
                getManifestFile(TestUtils.SECOND_FILE_NAME),
                getManifestFile(TestUtils.THIRD_FILE_NAME)
        };
    }

    private static File getManifestFile(String fileName) {
        final String fullFileName = fileName + "/xfdumanifest.xml";
        final URL resource = ManifestMergerTest.class.getResource(fullFileName);
        return new File(resource.getFile());
    }


}
