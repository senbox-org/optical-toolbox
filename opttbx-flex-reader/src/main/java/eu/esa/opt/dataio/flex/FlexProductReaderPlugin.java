package eu.esa.opt.dataio.flex;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.ProductReaderUtils;
import org.esa.snap.core.util.io.SnapFileFilter;

import static eu.esa.opt.dataio.flex.FlexConstantsAndUtils.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

public class FlexProductReaderPlugin implements ProductReaderPlugIn {

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final Path inputPath = ProductReaderUtils.convertToPath(input);
        if (inputPath == null) {
            return DecodeQualification.UNABLE;
        }
        final String fileName = inputPath.getFileName().toString();
        if (FLEX_FILENAME_PATTERN.matcher(fileName).matches()) {
            return DecodeQualification.INTENDED;
        }
        return DecodeQualification.UNABLE;
    }

    @Override
    public Class[] getInputTypes() {
        return ProductReaderUtils.IO_TYPES;
    }

    @Override
    public ProductReader createReaderInstance() {
        return new FlexProductReader(this);
    }

    @Override
    public String[] getFormatNames() {
        return new String[]{FORMAT_NAME};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[]{FLEX_EXTENSION};
    }

    @Override
    public String getDescription(Locale locale) {
        return DESCRIPTION;
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(FORMAT_NAME, getDefaultFileExtensions(), getDescription(null)) {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                String filName = file.getName();
                return FLEX_FILENAME_PATTERN.matcher(filName).matches();
            }
        };
    }
}
