package eu.esa.opt.dataio.flex;

import com.bc.ceres.core.runtime.internal.IOHelper;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
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
        final Path file = getProductPath(input);
        if (file == null) {
            return DecodeQualification.UNABLE;
        }

        if (Files.isDirectory(file)) {
            return sourceNamePattern.matcher(file.getFileName().toString()).matches()
                    && containsHeaderXml(file)
                    ? DecodeQualification.INTENDED
                    : DecodeQualification.UNABLE;
        }

        final String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xml")) {
            return DecodeQualification.UNABLE;
        }

        final String parentName = getParentDirectoryName(file);
        return parentName != null && sourceNamePattern.matcher(parentName).matches()
                ? DecodeQualification.INTENDED
                : DecodeQualification.UNABLE;
    }

    private boolean containsHeaderXml(Path dir) {
        final Collection<Path> files = IOHelper.listFiles(dir, file -> file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xml"));
        return files != null && !files.isEmpty();
    }

    @Override
    public Class<?>[] getInputTypes() {
        return new Class[]{String.class, Path.class, Path.class};
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

    private String getParentDirectoryName(Path path) {
        final Path parent = path.getParent();
        if (parent != null) {
            return parent.getFileName().toString();
        }
        return null;
    }
}
