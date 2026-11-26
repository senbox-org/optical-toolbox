package eu.esa.opt.dataio.ecostress;

import eu.esa.snap.hdf.HDFLoader;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Reader plug-in for ECOSTRESS products
 *
 * @author adraghici
 */
public class EcostressProductReaderPlugIn implements ProductReaderPlugIn {

    static final String FORMAT_NAME_ECOSTRESS = "ECOSTRESS-L1B/L2/L3/L4";
    private static final Class<?>[] SUPPORTED_INPUT_TYPES = new Class[]{String.class, Path.class};
    private static final String DESCRIPTION = "ECOSTRESS Format";
    private static final String FILE_EXTENSION = ".H5";
    private static final String[] DEFAULT_FILE_EXTENSIONS = new String[]{FILE_EXTENSION};
    private static final String[] FORMAT_NAMES = new String[]{FORMAT_NAME_ECOSTRESS};

    private EcostressMetadata ecostressMetadata;

    /**
     * Gets the ECOSTRESS file object from reader input object
     *
     * @param productFile the product path
     * @return the ECOSTRESS file object
     */
    public static EcostressFile getEcostressFile(Path productFile) {
        HDFLoader.ensureHDF5Initialised();
        return new EcostressFile(productFile);
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
        if (isInputValid(getProductPath(input))) {
            return DecodeQualification.INTENDED;
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
     * @param productFile the input product file
     * @return {@code true} when the reader input object is a valid ECOSTRESS product
     */
    protected boolean isInputValid(Path productFile) {
        final EcostressFile ecostressFile = getEcostressFile(productFile);
        return isInputEcostressFileNameValid(ecostressFile.getFileName()) && isInputContentValid(ecostressFile);
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