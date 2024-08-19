package eu.esa.opt.dataio.prisma;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.io.File;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.DESCRIPTION;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.FORMAT_NAME;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.IO_TYPES;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_HDF_EXTENSION;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToFile;

public class PrismaProductReaderPlugin implements ProductReaderPlugIn {

    static {
        registerRGBProfiles();
    }

    @java.lang.Override
    public DecodeQualification getDecodeQualification(java.lang.Object input) {
        final File inputFile = convertToFile(input);
        if (inputFile == null) {
            return DecodeQualification.UNABLE;
        }
        final String filename = inputFile.getName();
        if (PRISMA_FILENAME_PATTERN.matcher(filename).matches()) {
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
        return new java.lang.String[]{PRISMA_HDF_EXTENSION};
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

    private static void registerRGBProfiles() {
        final String[] rgbExpressionsL1 = {
                "HCO_Vnir_Ltoa_33",  // red channel band-maths expression
                "HCO_Vnir_Ltoa_46",  // green channel band-maths expression
                "HCO_Vnir_Ltoa_62"   // blue channel band-maths expression
        };
        final String[] rgbExpressionsL2b = {
                "HCO_Vnir_Lboa_33",  // red channel band-maths expression
                "HCO_Vnir_Lboa_46",  // green channel band-maths expression
                "HCO_Vnir_Lboa_62"   // blue channel band-maths expression
        };
        final String[] rgbExpressionsL2cL2d = {
                "HCO_Vnir_Rrs_33",  // red channel band-maths expression
                "HCO_Vnir_Rrs_46",  // green channel band-maths expression
                "HCO_Vnir_Rrs_62",  // blue channel band-maths expression
        };
        final RGBImageProfileManager manager = RGBImageProfileManager.getInstance();
        manager.addProfile(new RGBImageProfile("PRISMA_L1_RGB_674_560_439", // display name
                                               rgbExpressionsL1L2b,
                                               new String[]{
                                                       "*L1*",
                                                       "PRS_L1_*",
                                                       ""
                                               }));
        manager.addProfile(new RGBImageProfile("PRISMA_L2B_RGB_674_560_439", // display name
                                               rgbExpressionsL1L2b,
                                               new String[]{
                                                       "*L2B*",
                                                       "PRS_L2B_*",
                                                       ""
                                               }));
        manager.addProfile(new RGBImageProfile("PRISMA_L2C_RGB_674_560_439", // display name
                                               rgbExpressionsL2cL2d,
                                               new String[]{
                                                       "*L2C*",
                                                       "PRS_L2C_*",
                                                       ""
                                               }));
        manager.addProfile(new RGBImageProfile("PRISMA_L2D_RGB_674_560_439", // display name
                                               rgbExpressionsL2cL2d,
                                               new String[]{
                                                       "*L2D*",
                                                       "PRS_L2D_*",
                                                       ""
                                               }));
    }
}
