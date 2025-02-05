package eu.esa.opt.dataio;

import org.esa.snap.core.dataio.ProductReaderPlugIn;

import java.io.File;

abstract public class S3ReaderPlugIn implements ProductReaderPlugIn {

    protected static final Class[] SUPPORTED_INPUT_TYPES = {String.class, File.class};
    protected static final String MANIFEST_BASE = "xfdumanifest";
    protected static final String ALTERNATIVE_MANIFEST_BASE = "L1c_Manifest";

    @Override
    public Class[] getInputTypes() {
        return SUPPORTED_INPUT_TYPES;
    }

    protected static boolean isDirectory(String extension) {
        return !(".zip".equalsIgnoreCase(extension) || ".xml".equalsIgnoreCase(extension));
    }
}
