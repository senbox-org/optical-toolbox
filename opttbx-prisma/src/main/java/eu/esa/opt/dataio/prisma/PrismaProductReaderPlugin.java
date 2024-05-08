package eu.esa.opt.dataio.prisma;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.io.File;
import java.nio.file.Path;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.DESCRIPTION;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.FORMAT_NAME;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.IO_TYPES;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_HDF_EXTENSION;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_ZIP_CONTAINER_EXTENSION;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToPath;

public class PrismaProductReaderPlugin implements ProductReaderPlugIn {

    @java.lang.Override
    public DecodeQualification getDecodeQualification(java.lang.Object input) {
        final Path inputPath = convertToPath(input);
        if (inputPath == null) {
            return DecodeQualification.UNABLE;
        }
        final String filename = inputPath.getFileName().toString();
        final boolean matches = PRISMA_FILENAME_PATTERN.matcher(filename).matches();
        if (matches) {
            return DecodeQualification.INTENDED;
        }
        return DecodeQualification.UNABLE;
    }

    @java.lang.Override
    public Class[] getInputTypes() {
        return IO_TYPES;
    }

    @java.lang.Override
    public ProductReader createReaderInstance() {
        return new PrismaProductReader(this);
    }

    @java.lang.Override
    public java.lang.String[] getFormatNames() {
        return new java.lang.String[]{FORMAT_NAME};
    }

    @java.lang.Override
    public java.lang.String[] getDefaultFileExtensions() {
        return new java.lang.String[]{PRISMA_HDF_EXTENSION, PRISMA_ZIP_CONTAINER_EXTENSION};
    }

    @java.lang.Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(FORMAT_NAME, getDefaultFileExtensions(), getDescription(null)) {
            @Override
            public boolean accept(File file) {
                // directories are accepted right away
                if (file.isDirectory()) {
                    return true;
                }
                String fileName = file.getName();
                return PRISMA_FILENAME_PATTERN.matcher(fileName).matches();
            }
        };
    }

    @java.lang.Override
    public java.lang.String getDescription(java.util.Locale locale) {
        return DESCRIPTION;
    }
}
