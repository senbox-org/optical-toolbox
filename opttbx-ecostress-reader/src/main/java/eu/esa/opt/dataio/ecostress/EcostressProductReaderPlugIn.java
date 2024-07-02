package eu.esa.opt.dataio.ecostress;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Reader plug-in for ECOSTRESS products
 *
 * @author adraghici
 */
public class EcostressProductReaderPlugIn implements ProductReaderPlugIn {

    static final String FORMAT_NAME_ECOSTRESS = "ECOSTRESS-L1A/L2A/L1/L3/L4";
    private static final String _H5_CLASS_NAME = "ncsa.hdf.hdf5lib.H5";
    private static final Class<?>[] SUPPORTED_INPUT_TYPES = new Class[]{String.class, File.class};
    private static final String DESCRIPTION = "ECOSTRESS Format";
    private static final String FILE_EXTENSION = ".H5";
    private static final String[] DEFAULT_FILE_EXTENSIONS = new String[]{FILE_EXTENSION};
    private static final String[] FORMAT_NAMES = new String[]{FORMAT_NAME_ECOSTRESS};

    private static Boolean hdf5LibAvailable = null;

    private static final Logger logger = Logger.getLogger(EcostressProductReaderPlugIn.class.getName());

    private EcostressMetadata ecostressMetadata;

    /**
     * Checks whether the HDF5 native library is loaded in SNAP and initialise it
     *
     * @return {@code true} when the HDF5 native library is loaded in SNAP
     */
    private static boolean checkAndInitHdf5Lib() {
        final ClassLoader classLoader = EcostressProductReaderPlugIn.class.getClassLoader();

        final String classResourceName = "/" + EcostressProductReaderPlugIn._H5_CLASS_NAME.replace('.', '/') + ".class";
        if (EcostressProductReaderPlugIn.class.getResource(classResourceName) != null) {
            try {
                Class.forName(EcostressProductReaderPlugIn._H5_CLASS_NAME, true, classLoader);
                return true;
            } catch (Throwable error) {
                logger.warning(MessageFormat.format("{0}: HDF-5 library not available: {1}: {2}", EcostressProductReaderPlugIn.class, error.getClass(), error.getMessage()));
                logger.warning("ECOSTRESS readers disabled.");
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Gets the ECOSTRESS file object from reader input object
     *
     * @param input the reader input object
     * @return the ECOSTRESS file object
     */
    public static EcostressFile getEcostressFile(Object input) {
        ensureHDF5Initialised();
        if (input instanceof String) {
            return new EcostressFile((String) input);
        } else if (input instanceof File) {
            return new EcostressFile((File) input);
        }
        final String typeArray = StringUtils.arrayToString(SUPPORTED_INPUT_TYPES, ",");
        throw new IllegalStateException(String.format("Unsupported input data type should be one of [%s] but is %s", typeArray, input.getClass()));
    }

    /**
     * Checks whether the ECOSTRESS file name is valid
     *
     * @param fileName the ECOSTRESS file name
     * @return {@code true} when the ECOSTRESS file name is valid
     */
    private static boolean isInputEcostressFileNameValid(String fileName) {
        // just check file extension
        return fileName.toUpperCase().endsWith(FILE_EXTENSION);
    }

    /**
     * Ensures that the HDF5 native library is loaded and initialised
     */
    private static void ensureHDF5Initialised() {
        if (hdf5LibAvailable == null) {
            synchronized (logger) {
                hdf5LibAvailable = checkAndInitHdf5Lib();
            }
        }
        if (!hdf5LibAvailable) {
            throw new IllegalStateException("HDF5 NOT initialised! Check log for details.");
        }
    }

    /**
     * Checks whether the ECOSTRESS product file is a valid ECOSTRESS product
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @return {@code true} when the ECOSTRESS file name is valid ECOSTRESS product
     */
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        ecostressMetadata = EcostressMetadata.getEcostressMetadataForFile(ecostressFile);
        return ecostressMetadata != null;
    }

    /**
     * Creates the ECOSTRESS reader instance
     *
     * @return the ECOSTRESS reader instance
     */
    public ProductReader createReaderInstance() {
        return new EcostressProductReader(this);
    }

    /**
     * Gets the decode qualification for a reader input object
     *
     * @param input the reader input object
     * @return the decode qualification
     */
    public DecodeQualification getDecodeQualification(Object input) {
        if (isInputValid(input)) {
            return DecodeQualification.SUITABLE;
        }
        return DecodeQualification.UNABLE;
    }

    /**
     * Gets the format names from this reader plugin
     *
     * @return the format names
     */
    public String[] getFormatNames() {
        return ecostressMetadata != null ? new String[]{FORMAT_NAME_ECOSTRESS, ecostressMetadata.getFormatName()} : new String[]{FORMAT_NAME_ECOSTRESS};
    }

    /**
     * Checks whether the reader input object is a valid ECOSTRESS product
     *
     * @param input the reader input object
     * @return {@code true} when the reader input object is a valid ECOSTRESS product
     */
    protected boolean isInputValid(Object input) {
        final EcostressFile ecostressFile = getEcostressFile(input);
        return isInputEcostressFileNameValid(ecostressFile.getName()) && isInputContentValid(ecostressFile);
    }

    /**
     * Gets the supported input types for this reader plugin
     *
     * @return the supported input types for this reader plugin
     */
    @Override
    public Class<?>[] getInputTypes() {
        return SUPPORTED_INPUT_TYPES;
    }

    /**
     * Gets the supported input file extensions for this reader plugin
     *
     * @return the supported input file extensions for this reader plugin
     */
    @Override
    public String[] getDefaultFileExtensions() {
        return DEFAULT_FILE_EXTENSIONS;
    }

    /**
     * Gets the description for this reader plugin
     *
     * @param locale the local for the given description string, if {@code null} the default locale is used
     * @return the description for this reader plugin
     */
    @Override
    public String getDescription(Locale locale) {
        return ecostressMetadata != null ? ecostressMetadata.getFormatName() : FORMAT_NAME_ECOSTRESS + " Format";
    }

    /**
     * Gets the product file filter for this reader plugin
     *
     * @return the product file filter for this reader plugin
     */
    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(FORMAT_NAMES[0], FILE_EXTENSION, DESCRIPTION);
    }
}