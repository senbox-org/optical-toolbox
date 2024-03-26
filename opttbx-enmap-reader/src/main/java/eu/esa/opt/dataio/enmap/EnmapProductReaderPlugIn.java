package eu.esa.opt.dataio.enmap;

import com.bc.ceres.core.VirtualDir;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.engine_utilities.dataio.VirtualDirTgz;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.*;

public class EnmapProductReaderPlugIn implements ProductReaderPlugIn {

    private static final String[] FILE_EXTENSIONS = {".zip", ".xml", ".tar.gz", ".tgz"};
    private static final String DESCRIPTION = "EnMAP L1B/L1C/L2A Product Reader";
    private static final String[] FORMAT_NAMES = {"EnMAP L1B/L1C/L2A"};
    private static final Pattern l1bPattern = Pattern.compile(".*" + L1B_BASEFILENAME + ".(zip|ZIP)");
    private static final Pattern l1cPattern = Pattern.compile(".*" + L1C_BASEFILENAME + ".(zip|ZIP)");
    private static final Pattern l2aPattern = Pattern.compile(".*" + L2A_BASEFILENAME + ".(zip|ZIP)");

    static {
        EnMapRgbProfiles.registerRGBProfiles();
    }

    static Path convertToPath(final Object object) {
        try {
            return InputTypes.toPath(object);
        } catch (Exception e) {
            return null;
        }
    }

    static boolean isValidEnmapName(String path) {
        return l1bPattern.matcher(path).matches() || l1cPattern.matcher(path).matches() || l2aPattern.matcher(path).matches();
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        try {
            Path path = convertToPath(input);
            if (path == null) {
                return DecodeQualification.UNABLE;
            }

            List<Path> filePaths = new ArrayList<>();
            if (EnmapFileUtils.isTar(path)) {
                // we do not want to extract the whole tar, so just check for naming convention of entries
                if (isValidTar(path)) {
                    return DecodeQualification.INTENDED;
                }
            } else if (EnmapFileUtils.isZip(path)) {
                filePaths = extractPathsFromZip(path);
            } else {
                filePaths = extractPathsFromDir(path);
            }
            if (areEnmapL1bFiles(filePaths) || areEnmapL1cFiles(filePaths) || areEnmapL2aFiles(filePaths)) {
                return DecodeQualification.INTENDED;
            }
        } catch (Throwable t) {
            return DecodeQualification.UNABLE;
        }
        return DecodeQualification.UNABLE;
    }

    // package access for testing only tb 2024-03-08
    static List<Path> extractPathsFromDir(Path path) throws IOException {
        List<Path> filePaths;
        try (Stream<Path> list = Files.list(path.getParent())) {
            filePaths = list.collect(Collectors.toList());
        }
        return filePaths;
    }

    // @todo 1 tb/tb add tests 2024-03-11
    static boolean isValidTar(Path path) throws IOException {
        final VirtualDir virtualDir = new VirtualDirTgz(path);
        try {
            final String[] fileNames = virtualDir.listAllFiles();
            String zipFileName = null;
            for (final String fileName : fileNames) {
                if (fileName.endsWith(".ZIP") || fileName.endsWith(".zip")) {
                    zipFileName = fileName;
                    break;
                }
            }
            if (StringUtils.isNullOrEmpty(zipFileName)) {
               return false;
            }
            return isValidEnmapName(zipFileName);
        } finally {
            virtualDir.close();
        }
    }

    // package access for testing only tb 2024-03-07
    static List<Path> extractPathsFromZip(Path path) throws IOException {
        final VirtualDir virtualDir = VirtualDir.create(path.toFile());

        final List<Path> filePaths = new ArrayList<>();
        try {
            final String[] fileNames = virtualDir.listAllFiles();
            for (final String fileName : fileNames) {
                filePaths.add(Paths.get(fileName));
            }
        } finally {
            virtualDir.close();
        }
        return filePaths;
    }

    @Override
    public ProductReader createReaderInstance() {
        return new EnmapProductReader(this);
    }

    @Override
    public Class<?>[] getInputTypes() {
        return InputTypes.getTypes();
    }

    @Override
    public String[] getFormatNames() {
        return FORMAT_NAMES;
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
        return new SnapFileFilter(FORMAT_NAMES[0], FILE_EXTENSIONS, DESCRIPTION);
    }

    private static boolean areEnmapL1bFiles(List<Path> filePaths) {
        return Arrays.stream(L1B_FILENAME_PATTERNS).allMatch(p ->
                filePaths.stream().anyMatch(path -> p.matcher(path.getFileName().toString()).matches()));
    }

    private static boolean areEnmapL1cFiles(List<Path> filePaths) {
        return Arrays.stream(L1C_FILENAME_PATTERNS).allMatch(p ->
                filePaths.stream().anyMatch(path -> p.matcher(path.getFileName().toString()).matches()));
    }

    private static boolean areEnmapL2aFiles(List<Path> filePaths) {
        return Arrays.stream(L2A_FILENAME_PATTERNS).allMatch(p ->
                filePaths.stream().anyMatch(path -> p.matcher(path.getFileName().toString()).matches()));
    }
}
