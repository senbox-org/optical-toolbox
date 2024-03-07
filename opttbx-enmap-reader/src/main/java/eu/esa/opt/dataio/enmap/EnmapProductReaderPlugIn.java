package eu.esa.opt.dataio.enmap;

import com.bc.ceres.core.VirtualDir;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.engine_utilities.dataio.VirtualDirTgz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnmapProductReaderPlugIn implements ProductReaderPlugIn {

    private static final String[] FILE_EXTENSIONS = {".zip", ".xml", ".tar.gz"};
    private static final String DESCRIPTION = "EnMAP L1B/L1C/L2A Product Reader";
    private static final String[] FORMAT_NAMES = {"EnMAP L1B/L1C/L2A"};

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

    @Override
    public DecodeQualification getDecodeQualification(Object o) {
        try {
            Path path = convertToPath(o);
            if (path == null) {
                return DecodeQualification.UNABLE;
            }

            // @todo 1 tb/tb check file name patterns tb 2024-03-07

            List<Path> filePaths;
            if (EnmapFileUtils.isZip(path)) {
                filePaths = extractPathsFromZip(path);
            } else if (EnmapFileUtils.isTar(path)) {
                filePaths = extractPathsFromTar(path);
            } else {
                try (Stream<Path> list = Files.list(path.getParent())) {
                    filePaths = list.collect(Collectors.toList());
                }
            }
            if (areEnmapL1bFiles(filePaths) || areEnmapL1cFiles(filePaths) || areEnmapL2aFiles(filePaths)) {
                return DecodeQualification.INTENDED;
            }
        } catch (Throwable t) {
            return DecodeQualification.UNABLE;
        }
        return DecodeQualification.UNABLE;
    }

    static List<Path> extractPathsFromTar(Path path) throws IOException {
        final List<Path> filePaths = new ArrayList<>();

        VirtualDir virtualDir = new VirtualDirTgz(path);
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

    private boolean areEnmapL1bFiles(List<Path> filePaths) {
        return Arrays.stream(EnmapFileUtils.L1B_FILENAME_PATTERNS).allMatch(p ->
                filePaths.stream().anyMatch(path -> p.matcher(path.getFileName().toString()).matches()));
    }

    private boolean areEnmapL1cFiles(List<Path> filePaths) {
        return Arrays.stream(EnmapFileUtils.L1C_FILENAME_PATTERNS).allMatch(p ->
                filePaths.stream().anyMatch(path -> p.matcher(path.getFileName().toString()).matches()));
    }

    private boolean areEnmapL2aFiles(List<Path> filePaths) {
        return Arrays.stream(EnmapFileUtils.L2A_FILENAME_PATTERNS).allMatch(p ->
                filePaths.stream().anyMatch(path -> p.matcher(path.getFileName().toString()).matches()));
    }

}
