package eu.esa.opt.dataio.s3;

import java.io.File;

public class S3ReaderPlugInTest {

    static String createManifestFilePath(String sensorId, String levelId, String productId, String suffix) {
        String validParentDirectory = String.format("S3_%s_%s_%s_TTTTTTTTTTTT_%s" , sensorId,
                levelId, productId, suffix);
        String manifestFile = "xfdumanifest.xml";
        return validParentDirectory +  File.separator + manifestFile;
    }
}
