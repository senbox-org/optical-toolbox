package eu.esa.opt.dataio.s3;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

public class Sentinel3Level1ReaderPlugIn implements ProductReaderPlugIn  {

    private Pattern sourceNamePattern;

    public Sentinel3Level1ReaderPlugIn() {
        sourceNamePattern = Pattern.compile("S3.?_(OL_1_E[FR]R|ER1_AT_1_RBT|ER2_AT_1_RBT|ENV_AT_1_RBT)_.*(.SEN3)?(.zip)?");
    }

    @Override
    public String[] getFormatNames() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String[] getDefaultFileExtensions() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getDescription(Locale locale) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final Path path = Paths.get(input.toString());
        final String parentFileName;
        if (path.getParent() == null) {
            parentFileName = path.getFileName().toString();
        } else {
            int nameCount = path.getNameCount();
            parentFileName = path.getName(nameCount - 2).toString();
        }

        boolean matches = sourceNamePattern.matcher(parentFileName).matches();
        if (matches) {
            return DecodeQualification.INTENDED;
        }
        return DecodeQualification.UNABLE;
    }

    @Override
    public Class[] getInputTypes() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ProductReader createReaderInstance() {
        throw new RuntimeException("not implemented");
    }
}
