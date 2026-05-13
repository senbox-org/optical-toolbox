package eu.esa.opt.dataio.flex;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

abstract class FlexReaderPlugIn implements ProductReaderPlugIn {

    private static final String[] FILE_EXTENSIONS = {".xml"};

    private final Pattern sourceNamePattern;
    private final String formatName;
    private final String description;

    FlexReaderPlugIn(String formatName, String description, Pattern sourceNamePattern) {
        this.formatName = formatName;
        this.description = description;
        this.sourceNamePattern = sourceNamePattern;
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final Path path = convertToPath(input);
        if (path == null) {
            return DecodeQualification.UNABLE;
        }

        final File file = path.toFile();

        if (file.isDirectory()) {
            return sourceNamePattern.matcher(file.getName()).matches()
                    && containsHeaderXml(file.toPath())
                    ? DecodeQualification.INTENDED
                    : DecodeQualification.UNABLE;
        }

        final String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xml")) {
            return DecodeQualification.UNABLE;
        }

        final String parentName = getParentDirectoryName(path);
        return parentName != null && sourceNamePattern.matcher(parentName).matches()
                ? DecodeQualification.INTENDED
                : DecodeQualification.UNABLE;
    }

    private boolean containsHeaderXml(Path dir) {
        final File[] files = dir.toFile().listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(".xml"));
        return files != null && files.length > 0;
    }

    @Override
    public Class<?>[] getInputTypes() {
        return new Class[]{String.class, File.class, Path.class};
    }

    @Override
    public ProductReader createReaderInstance() {
        return new FlexProductReader(this);
    }

    @Override
    public String[] getFormatNames() {
        return new String[]{formatName};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String getDescription(Locale locale) {
        return description;
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(formatName, FILE_EXTENSIONS, description);
    }

    private Path convertToPath(Object input) {
        if (input instanceof Path) {
            return (Path) input;
        } else if (input instanceof File) {
            return ((File) input).toPath();
        } else if (input instanceof String) {
            return Paths.get((String) input);
        }
        return null;
    }

    private String getParentDirectoryName(Path path) {
        final Path parent = path.getParent();
        if (parent != null) {
            return parent.getFileName().toString();
        }
        return null;
    }
}
