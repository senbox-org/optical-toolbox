package eu.esa.opt.dataio.s3;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

public class Sentinel3Level1ReaderPlugIn extends S3ReaderPlugIn {

    private static final String[] FILE_EXTENSIONS = {".xml", ".zip"};
    private static final String FORMAT_NAME = "Sen3L1";
    private static final String DESCRIPTION = "Sentinel-3 Level 1 products";

    static final Class[] INPUT_TYPES = {Path.class, File.class, String.class};
    private static final InputConverter[] INPUT_CONVERTERS = {
            input -> (Path) input,
            input -> ((File) input).toPath(),
            input -> Paths.get((String) input)

    };
    private final Pattern sourceNamePattern;
    private final String manifestName;

    private final String altManifestName;

    public Sentinel3Level1ReaderPlugIn() {
        sourceNamePattern = Pattern.compile("S3.?_(OL_1_E[FR]R|ER1_AT_1_RBT|ER2_AT_1_RBT|ENV_AT_1_RBT)_.*(.SEN3)?(.zip)?");
        manifestName = MANIFEST_BASE + ".xml";
        altManifestName = ALTERNATIVE_MANIFEST_BASE + ".xml";
    }

    @Override
    public Class[] getInputTypes() {
        return INPUT_TYPES;
    }

    @Override
    public String[] getFormatNames() {
        return new String[] {FORMAT_NAME};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String getDescription(Locale locale) {
        return DESCRIPTION;
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(FORMAT_NAME, FILE_EXTENSIONS, DESCRIPTION);
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        String inputString = input.toString();
        Path path = Paths.get(inputString);
        final String filename = FileUtils.getFilenameFromPath(path.toString());

        // check if we have a directory name (not ending on xml)
        final String fileExtension = FileUtils.getExtension(filename);
        if (isDirectory(fileExtension)) {
            inputString = inputString + File.separator + MANIFEST_BASE + ".xml";
        }

        path = Paths.get(inputString);
        String parentFileName;
        if (path.getParent() == null) {
            parentFileName = path.getFileName().toString();
        } else {
            int nameCount = path.getNameCount();
            parentFileName = path.getName(nameCount - 2).toString();
        }
        parentFileName = FileUtils.getFilenameFromPath(parentFileName);

        boolean matches = isValidSourceName(parentFileName);
        if (matches) {
            return DecodeQualification.INTENDED;
        }
        return DecodeQualification.UNABLE;
    }

    // package access for testing only tb 2025-02-05
    boolean isValidSourceName(String parentFileName) {
        return sourceNamePattern.matcher(parentFileName).matches();
    }

    // public access for testing only 2025-02-05
    public boolean isValidInputFileName(String name) {
         if (manifestName.equalsIgnoreCase(name) || altManifestName.equalsIgnoreCase(name)) {
             return true;
         }

        final String extension = FileUtils.getExtension(name);
        if (".zip".equalsIgnoreCase(extension)) {
            final String nameWoExtension = FileUtils.getFilenameWithoutExtension(name);
            return isValidSourceName(nameWoExtension);
        } else if (".SEN3".equalsIgnoreCase(extension) || StringUtils.isNullOrBlank(extension)) {
            return isValidSourceName(name);
        }
         return false;
    }

    // public access for testing only 2024-05-28
    public boolean isInputValid(Object input) {
        String inputString = input.toString();
        final String filename = FileUtils.getFilenameFromPath(inputString);

        final String fileExtension = FileUtils.getExtension(filename);
        if (isDirectory(fileExtension)) {
            inputString = inputString + File.separator + MANIFEST_BASE;
        }

        if (!isValidInputFileName(filename)) {
            return false;
        }

        if (".zip".equalsIgnoreCase(fileExtension)) {
            String zipName = FileUtils.getFilenameWithoutExtension(filename);
            return isValidSourceName(zipName);
        }

        final Path path = Paths.get(inputString);
        final String parentFileName;
        if (path.getParent() == null) {
            parentFileName = path.getFileName().toString();
        } else {
            int nameCount = path.getNameCount();
            parentFileName = path.getName(nameCount - 2).toString();
        }

        return isValidSourceName(parentFileName);
    }

    @Override
    public ProductReader createReaderInstance() {
        return new Sentinel3Level1Reader(this);
    }

    private interface InputConverter {
        Path convertInput(Object input);
    }

    static Path convertToPath(final Object object) {
        for (int i = 0; i < INPUT_TYPES.length; i++) {
            if (INPUT_TYPES[i].isInstance(object)) {
                return INPUT_CONVERTERS[i].convertInput(object);
            }
        }
        return null;
    }
}
