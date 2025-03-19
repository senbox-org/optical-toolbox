package eu.esa.opt.dataio.probav;

import eu.esa.snap.hdf.HDFLoader;
import hdf.object.FileFormat;
import hdf.object.Group;
import hdf.object.HObject;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Reader plug-in for Proba-V L2A and L3 (Synthesis) products
 *
 * @author olafd
 */
public class ProbaVProductReaderPlugIn implements ProductReaderPlugIn {

    public static final String FORMAT_NAME_PROBA_V = "PROBA-V-L2A/L3";
    private static final Class<?>[] SUPPORTED_INPUT_TYPES = new Class[]{String.class, File.class};
    private static final String DESCRIPTION = "PROBA-V Format";
    private static final String FILE_EXTENSION = ".HDF5";
    private static final String[] DEFAULT_FILE_EXTENSIONS = new String[]{FILE_EXTENSION};
    private static final String[] FORMAT_NAMES = new String[]{FORMAT_NAME_PROBA_V};

    private static final Logger logger = Logger.getLogger(ProbaVProductReaderPlugIn.class.getName());

    static {
        RGBImageProfile toaProfile = new RGBImageProfile("PROBA-V TOA RGB",
                new String[]{"TOA_REFL_NIR", "TOA_REFL_RED", "TOA_REFL_BLUE"});

        RGBImageProfileManager manager = RGBImageProfileManager.getInstance();
        manager.addProfile(toaProfile);
    }

    /**
     * Returns the input object as file
     *
     * @param input - the input
     * @return file
     */
    static ProbaVFile getProbaVFile(Object input) {
        HDFLoader.ensureHDF5Initialised();
        if (input instanceof String) {
            return new ProbaVFile((String) input);
        } else if (input instanceof File) {
            return new ProbaVFile((File) input);
        }
        String typeArray = StringUtils.arrayToString(SUPPORTED_INPUT_TYPES, ",");
        throw new IllegalStateException(String.format("Unsupported input data type should be one of [%s] but is %s", typeArray, input.getClass()));
    }

    private static boolean isInputValid(Object input) {
        final ProbaVFile probaVFile = getProbaVFile(input);
        return isInputProbaVFileNameValid(probaVFile.getName()) && isInputContentValid(probaVFile);
    }

    /**
     * Checks whether the ProbaV product file is a valid ProbaV product
     *
     * @param probaVFile the ProbaV product file object
     * @return {@code true} when the ProbaV file name is valid ProbaV product
     */
    private static boolean isInputContentValid(ProbaVFile probaVFile) {
        final HObject probavTypeNode = getProbavTypeNode(probaVFile);
        if (probavTypeNode != null) {
            final String rootNodeName = probavTypeNode.getName();
            return rootNodeName.equals("LEVEL2A") || rootNodeName.equals("LEVEL3");
        }
        return false;
    }

    private static HObject getProbavTypeNode(ProbaVFile probaVFile) {
        try {
            final FileFormat h5File = probaVFile.getH5File();
            final Group probaVRootNode = (Group) h5File.getRootObject();
            return probaVRootNode.getMember(0);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return null;
    }

    private static boolean isInputProbaVFileNameValid(String fileName) {
        // just check file extension
        return fileName.toUpperCase().endsWith(".HDF5");
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        if (isInputValid(input)) {
            return DecodeQualification.INTENDED;
        } else {
            return DecodeQualification.UNABLE;
        }
    }

    @Override
    public Class<?>[] getInputTypes() {
        return SUPPORTED_INPUT_TYPES;
    }

    @Override
    public ProductReader createReaderInstance() {
        return new ProbaVProductReader(this);   // test!
    }

    @Override
    public String[] getFormatNames() {
        return FORMAT_NAMES;
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return DEFAULT_FILE_EXTENSIONS;
    }

    @Override
    public String getDescription(Locale locale) {
        return DESCRIPTION;
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(FORMAT_NAMES[0], FILE_EXTENSION, DESCRIPTION);
    }
}
