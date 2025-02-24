package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.dataio.DecodeQualification;

import java.io.File;

public class SeadasHelper {

    // Set to "true" to output debugging information.
    // Don't forget to set back to "false" in production code!
    //
    private static final boolean DEBUG = false;

    static DecodeQualification checkInputObject(File inputFile) {
        if (inputFile == null) {
            return DecodeQualification.UNABLE;
        }
        if (!inputFile.isFile()) {
            if (DEBUG) {
                System.out.println("# Not a file: " + inputFile);
            }
            return DecodeQualification.UNABLE;
        }

        return DecodeQualification.SUITABLE;
    }

    static File getInputFile(Object input) {
        File inputFile;
        if (input instanceof File) {
            inputFile = (File) input;
        } else if (input instanceof String) {
            inputFile = new File((String) input);
        } else {
            return null;
        }
        return inputFile;
    }
}
