/*
 * Copyright (C) 2015 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.PropertyMap;
import org.esa.snap.core.util.io.CsvReader;
import org.esa.snap.rcp.SnapApp;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.*;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.System.arraycopy;

/**
 * NASA SeaDAS File Reader.
 *
 * @author NASA OBPG
 * @version $Revision$ $Date$
 * @since
 */
//APR2021 - Bing Yang - added capability to read 3d products


public abstract class SeadasFileReader {

    protected boolean mustFlipX;
    protected boolean mustFlipY;
    protected List<Attribute> globalAttributes;
    protected Map<Band, Variable> variableMap;
    protected NetcdfFile ncFile;
    protected SeadasProductReader productReader;
    protected int[] start = new int[2];
    protected int[] stride = new int[2];
    protected int[] count = new int[2];
    protected String sensor = null;

    protected int leadLineSkip = 0;
    protected int tailLineSkip = 0;

    private static final String FLAG_MASKS = "flag_masks";
    private static final String FLAG_MEANINGS = "flag_meanings";
    protected Logger logger = Logger.getLogger(getClass().getSimpleName());

    protected static final SkipBadNav LAT_SKIP_BAD_NAV = new SkipBadNav() {
        @Override
        public final boolean isBadNav(double value) {
            return Double.isNaN(value) || value > 90.0 || value < -90.0;
        }
    };

    public SeadasFileReader(SeadasProductReader productReader) {
        this.productReader = productReader;
        ncFile = productReader.getNcfile();
        globalAttributes = ncFile.getGlobalAttributes();

    }

    public abstract Product createProduct() throws IOException;

    public synchronized void readBandData(Band destBand, int sourceOffsetX, int sourceOffsetY, int sourceWidth,
                                          int sourceHeight, int sourceStepX, int sourceStepY, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException, InvalidRangeException {

        if (mustFlipY) {
            sourceOffsetY = destBand.getRasterHeight() - (sourceOffsetY + sourceHeight);
        }
        if (mustFlipX) {
            sourceOffsetX = destBand.getRasterWidth() - (sourceOffsetX + sourceWidth);
        }
        sourceOffsetY += leadLineSkip;
        int widthRemainder = destBand.getRasterWidth() - (sourceOffsetX + sourceWidth);

        if (widthRemainder < 0) {
            sourceWidth += widthRemainder;
        }
        start[0] = sourceOffsetY;
        start[1] = sourceOffsetX;
        stride[0] = sourceStepY;
        stride[1] = sourceStepX;
        count[0] = sourceHeight;
        count[1] = sourceWidth;
        Object buffer = destBuffer.getElems();
        Variable variable = variableMap.get(destBand);

        pm.beginTask("Reading band '" + variable.getShortName() + "'...", sourceHeight);
        try {
            Section section = new Section(start, count, stride);

            Array array;
            int[] newshape = {sourceHeight, sourceWidth};

            array = variable.read(section);
            if (array.getRank() > 2) {
                array = array.reshapeNoCopy(newshape);
            }
            Object storage;

            if (mustFlipX && !mustFlipY) {
                storage = array.flip(1).copyTo1DJavaArray();
            } else if (!mustFlipX && mustFlipY) {
                storage = array.flip(0).copyTo1DJavaArray();
            } else if (mustFlipX && mustFlipY) {
                storage = array.flip(0).flip(1).copyTo1DJavaArray();
            } else {
                storage = array.copyTo1DJavaArray();
            }

            if (widthRemainder < 0) {
                arraycopy(storage, 0, buffer, 0, destBuffer.getNumElems() + widthRemainder);
            } else {
                arraycopy(storage, 0, buffer, 0, destBuffer.getNumElems());

            }
        } finally {
            pm.done();
        }

    }

    public FlagCoding readFlagCoding(Product product, Band bandName) {
        Variable variable = variableMap.get(bandName);
        if (variable.getFullName().contains("flag")) {
            final String codingName = variable.getShortName() + "_coding";
            return readFlagCoding(variable, codingName);
        } else {
            return null;
        }
    }

    private static FlagCoding readFlagCoding(Variable variable, String codingName) {
        final Attribute flagMasks = variable.findAttribute(FLAG_MASKS);
        final int[] maskValues;
        if (flagMasks != null) {
            final Array flagMasksArray = flagMasks.getValues();
            maskValues = new int[flagMasks.getLength()];
            for (int i = 0; i < maskValues.length; i++) {
                maskValues[i] = flagMasksArray.getInt(i);
            }
        } else {
            maskValues = null;
        }

        final Attribute flagMeanings = variable.findAttribute(FLAG_MEANINGS);
        final String[] flagNames;
        if (flagMeanings != null) {
            flagNames = flagMeanings.getStringValue().split(" ");
        } else {
            flagNames = null;
        }

        return createFlagCoding(codingName, maskValues, flagNames);
    }

    private static FlagCoding createFlagCoding(String codingName, int[] maskValues, String[] flagNames) {
        if (maskValues != null && flagNames != null && maskValues.length == flagNames.length) {
            final FlagCoding coding = new FlagCoding(codingName);
            for (int i = 0; i < maskValues.length; i++) {
                final String sampleName = replaceNonWordCharacters(flagNames[i]);
                final int sampleValue = maskValues[i];
                coding.addSample(sampleName, sampleValue, "");
            }
            if (coding.getNumAttributes() > 0) {
                return coding;
            }
        }
        return null;
    }

    static String replaceNonWordCharacters(String flagName) {
        return flagName.replaceAll("\\W+", "_");
    }

    final static Color LandBrown = new Color(80, 60, 0);
    final static Color LightBrown = new Color(137, 99, 31);
    final static Color FailRed = new Color(255, 0, 26);
    final static Color DeepBlue = new Color(0, 16, 143);
    final static Color BrightPink = new Color(255, 61, 245);
    final static Color LightCyan = new Color(193, 255, 254);
    final static Color NewGreen = new Color(132, 199, 101);
    final static Color Mustard = new Color(206, 204, 70);
    final static Color MediumGray = new Color(160, 160, 160);
    final static Color Purple = new Color(141, 11, 134);
    final static Color Coral = new Color(255, 0, 95);
    final static Color DarkGreen = new Color(0, 101, 28);
    final static Color TealGreen = new Color(0, 80, 79);
    final static Color LightPink = new Color(255, 208, 241);
    final static Color LightPurple = new Color(191, 143, 247);
    final static Color BurntUmber = new Color(165, 0, 11);
    final static Color TealBlue = new Color(0, 103, 144);
    final static Color Cornflower = new Color(38, 115, 245);

    protected void addFlagsAndMasks(Product product) {

        if (product.getProductType().contains("VIIRS L1B")) {
            for (Band bandName : product.getBands()) {
                FlagCoding flagCoding = readFlagCoding(product, bandName);
                if (flagCoding != null) {
                    product.getFlagCodingGroup().add(flagCoding);
                    bandName.setSampleCoding(flagCoding);
                }
            }
        } else {
            try {
                sensor = product.getMetadataRoot().getElement("Global_Attributes").getAttribute("Sensor_Name").getData().getElemString();
            } catch (Exception ignore) {
                try {
                    sensor = product.getMetadataRoot().getElement("Global_Attributes").getAttribute("instrument").getData().getElemString();
                } catch (Exception ignored) {
                }
            }

            Band QFBand = product.getBand("l2_flags");
            if (QFBand != null) {
                String ATMFAIL_Description = "Atmospheric correction failure";
                String LAND_Description = "Land";
                String PRODWARN_Description = "One (or more) product algorithms generated a warning";
                String HIGLINT_Description = "High glint determined";
                String HILT_Description = "High (or saturating) TOA radiance";
                String HISATZEN_Description = "Large satellite zenith angle";
                String COASTZ_Description = "Shallow water (<30m)";
                String SPARE8_Description = "Unused";
                String STRAYLIGHT_Description = "Straylight determined";
                String CLDICE_Description = "Cloud/Ice determined";
                String COCCOLITH_Description = "Coccolithophores detected";
                String TURBIDW_Description = "Turbid water determined";
                String HISOLZEN_Description = "High solar zenith angle";
                String SPARE14_Description = "Unused";
                String LOWLW_Description = "Low Lw @ 555nm (possible cloud shadow)";
                String CHLFAIL_Description = "Chlorophyll algorithm failure";
                String NAVWARN_Description = "Navigation suspect";
                String ABSAER_Description = "Absorbing Aerosols determined";
                String SPARE19_Description = "Unused";
                String MAXAERITER_Description = "Maximum iterations reached for NIR iteration";
                String MODGLINT_Description = "Moderate glint determined";
                String CHLWARN_Description = "Chlorophyll out-of-bounds (<0.01 or >100 mg m^-3)";
                String ATMWARN_Description = "Atmospheric correction warning; Epsilon out-of-bounds";
                String SPARE24_Description = "Unused";
                String SEAICE_Description = "Sea ice determined";
                String NAVFAIL_Description = "Navigation failure";
                String FILTER_Description = "Insufficient data for smoothing filter";
                String SPARE28_Description = "Unused";
                String BOWTIEDEL_Description = "Bowtie deleted pixels (VIIRS)";
                String HIPOL_Description = "High degree of polariztion determined";
                String PRODFAIL_Description = "One (or more) product algorithms produced a failure";
                String SPARE32_Description = "Unused";
                
                String composite1Description = "Composite1 Mask (see preferences to set)";
                String composite2Description = "Composite2 Mask (see preferences to set)";
                String composite3Description = "Composite3 Mask (see preferences to set)";
                String Water_Description = "Not land (l2_flags.LAND)";
                
                
                
                FlagCoding flagCoding = new FlagCoding("L2Flags");
                
                flagCoding.addFlag("ATMFAIL", 0x01, ATMFAIL_Description);
                flagCoding.addFlag("LAND", 0x02, LAND_Description);
                flagCoding.addFlag("PRODWARN", 0x04, PRODWARN_Description);
                flagCoding.addFlag("HIGLINT", 0x08, HIGLINT_Description);
                flagCoding.addFlag("HILT", 0x10, HILT_Description);
                flagCoding.addFlag("HISATZEN", 0x20, HISATZEN_Description);
                flagCoding.addFlag("COASTZ", 0x40, COASTZ_Description);
                flagCoding.addFlag("SPARE8", 0x80, SPARE8_Description);
                flagCoding.addFlag("STRAYLIGHT", 0x100, STRAYLIGHT_Description);
                flagCoding.addFlag("CLDICE", 0x200, CLDICE_Description);
                flagCoding.addFlag("COCCOLITH", 0x400, COCCOLITH_Description);
                flagCoding.addFlag("TURBIDW", 0x800, TURBIDW_Description);
                flagCoding.addFlag("HISOLZEN", 0x1000, HISOLZEN_Description);
                flagCoding.addFlag("SPARE14", 0x2000, SPARE14_Description);
                flagCoding.addFlag("LOWLW", 0x4000, LOWLW_Description);
                flagCoding.addFlag("CHLFAIL", 0x8000, CHLFAIL_Description);
                flagCoding.addFlag("NAVWARN", 0x10000, NAVWARN_Description);
                flagCoding.addFlag("ABSAER", 0x20000, ABSAER_Description);
                flagCoding.addFlag("SPARE19", 0x40000, SPARE19_Description);
                flagCoding.addFlag("MAXAERITER", 0x80000, MAXAERITER_Description);
                flagCoding.addFlag("MODGLINT", 0x100000, MODGLINT_Description);
                flagCoding.addFlag("CHLWARN", 0x200000, CHLWARN_Description);
                flagCoding.addFlag("ATMWARN", 0x400000, ATMWARN_Description);
                flagCoding.addFlag("SPARE24", 0x800000, SPARE24_Description);
                flagCoding.addFlag("SEAICE", 0x1000000, SEAICE_Description);
                flagCoding.addFlag("NAVFAIL", 0x2000000, NAVFAIL_Description);
                flagCoding.addFlag("FILTER", 0x4000000, FILTER_Description);
                flagCoding.addFlag("SPARE28", 0x8000000, SPARE28_Description);
                flagCoding.addFlag("BOWTIEDEL", 0x10000000, BOWTIEDEL_Description);
                flagCoding.addFlag("HIPOL", 0x20000000, HIPOL_Description);
                flagCoding.addFlag("PRODFAIL", 0x40000000, PRODFAIL_Description);
                flagCoding.addFlag("SPARE32", 0x80000000, SPARE32_Description);
                
                
                product.getFlagCodingGroup().add(flagCoding);
                QFBand.setSampleCoding(flagCoding);



                Mask ATMFAIL_Mask = null;
                Mask LAND_Mask = null;
                Mask PRODWARN_Mask = null;
                Mask HIGLINT_Mask = null;
                Mask HILT_Mask = null;
                Mask HISATZEN_Mask = null;
                Mask COASTZ_Mask = null;
                Mask STRAYLIGHT_Mask = null;
                Mask CLDICE_Mask = null;
                Mask COCCOLITH_Mask = null;
                Mask TURBIDW_Mask = null;
                Mask HISOLZEN_Mask = null;
                Mask LOWLW_Mask = null;
                Mask CHLFAIL_Mask = null;
                Mask NAVWARN_Mask = null;
                Mask ABSAER_Mask = null;
                Mask MAXAERITER_Mask = null;
                Mask MODGLINT_Mask = null;
                Mask CHLWARN_Mask = null;
                Mask ATMWARN_Mask = null;
                Mask SEAICE_Mask = null;
                Mask NAVFAIL_Mask = null;
                Mask FILTER_Mask = null;
                Mask BOWTIEDEL_Mask = null;
                Mask HIPOL_Mask = null;
                Mask PRODFAIL_Mask = null;

                Mask composite1Mask = null;
                Mask composite2Mask = null;
                Mask composite3Mask = null;
                Mask Water_Mask = null;


                Mask SPARE8_Mask = null;
                Mask SPARE14_Mask = null;
                Mask SPARE19_Mask = null;
                Mask SPARE24_Mask = null;
                Mask SPARE28_Mask = null;
                Mask SPARE32_Mask = null;





                if (isMaskSort()) {
                    String flagNamesOrdered = getMaskSort();
                    String[] flagNamesOrderedArray = flagNamesOrdered.split("\\s+|,");
                    for (String flagName : flagNamesOrderedArray) {
                        switch (flagName) {
                            case "ATMFAIL":
                                if (ATMFAIL_Mask == null) {
                                    ATMFAIL_Mask = createMaskATMFAIL(product, ATMFAIL_Description);
                                }
                                break;
                            case "LAND":
                                if (LAND_Mask == null) {
                                    LAND_Mask = createMaskLAND(product, LAND_Description);
                                }
                                break;
                            case "PRODWARN":
                                if (PRODWARN_Mask == null) {
                                    PRODWARN_Mask = createMaskPRODWARN(product, PRODWARN_Description);
                                }
                                break;
                            case "HIGLINT":
                                if (HIGLINT_Mask == null) {
                                    HIGLINT_Mask = createMaskHIGLINT(product, HIGLINT_Description);
                                }
                                break;
                            case "HILT":
                                if (HILT_Mask == null) {
                                    HILT_Mask = createMaskHILT(product, HILT_Description);
                                }
                                break;
                            case "HISATZEN":
                                if (HISATZEN_Mask == null) {
                                    HISATZEN_Mask = createMaskHISATZEN(product, HISATZEN_Description);
                                }
                                break;
                            case "COASTZ":
                                if (COASTZ_Mask == null) {
                                    COASTZ_Mask = createMaskCOASTZ(product, COASTZ_Description);
                                }
                                break;
                            case "STRAYLIGHT":
                                if (STRAYLIGHT_Mask == null) {
                                    STRAYLIGHT_Mask = createMaskSTRAYLIGHT(product, STRAYLIGHT_Description);
                                }
                                break;
                            case "CLDICE":
                                if (CLDICE_Mask == null) {
                                    CLDICE_Mask = createMaskCLDICE(product, CLDICE_Description);
                                }
                                break;
                            case "COCCOLITH":
                                if (COCCOLITH_Mask == null) {
                                    COCCOLITH_Mask = createMaskCOCCOLITH(product, COCCOLITH_Description);
                                }
                                break;
                            case "TURBIDW":
                                if (TURBIDW_Mask == null) {
                                    TURBIDW_Mask = createMaskTURBIDW(product, TURBIDW_Description);
                                }
                                break;
                            case "HISOLZEN":
                                if (HISOLZEN_Mask == null) {
                                    HISOLZEN_Mask = createMaskHISOLZEN(product, HISOLZEN_Description);
                                }
                                break;
                            case "LOWLW":
                                if (LOWLW_Mask == null) {
                                    LOWLW_Mask = createMaskLOWLW(product, LOWLW_Description);
                                }
                                break;
                            case "CHLFAIL":
                                if (CHLFAIL_Mask == null) {
                                    CHLFAIL_Mask = createMaskCHLFAIL(product, CHLFAIL_Description);
                                }
                                break;
                            case "NAVWARN":
                                if (NAVWARN_Mask == null) {
                                    NAVWARN_Mask = createMaskNAVWARN(product, NAVWARN_Description);
                                }
                                break;
                            case "ABSAER":
                                if (ABSAER_Mask == null) {
                                    ABSAER_Mask = createMaskABSAER(product, ABSAER_Description);
                                }
                                break;
                            case "MAXAERITER":
                                if (MAXAERITER_Mask == null) {
                                    MAXAERITER_Mask = createMaskMAXAERITER(product, MAXAERITER_Description);
                                }
                                break;
                            case "MODGLINT":
                                if (MODGLINT_Mask == null) {
                                    MODGLINT_Mask = createMaskMODGLINT(product, MODGLINT_Description);
                                }
                                break;
                            case "CHLWARN":
                                if (CHLWARN_Mask == null) {
                                    CHLWARN_Mask = createMaskCHLWARN(product, CHLWARN_Description);
                                }
                                break;
                            case "ATMWARN":
                                if (ATMWARN_Mask == null) {
                                    ATMWARN_Mask = createMaskATMWARN(product, ATMWARN_Description);
                                }
                                break;
                            case "SEAICE":
                                if (SEAICE_Mask == null) {
                                    SEAICE_Mask = createMaskSEAICE(product, SEAICE_Description);
                                }
                                break;
                            case "NAVFAIL":
                                if (NAVFAIL_Mask == null) {
                                    NAVFAIL_Mask = createMaskNAVFAIL(product, NAVFAIL_Description);
                                }
                                break;
                            case "FILTER":
                                if (FILTER_Mask == null) {
                                    FILTER_Mask = createMaskFILTER(product, FILTER_Description);
                                }
                                break;
                            case "BOWTIEDEL":
                                if (BOWTIEDEL_Mask == null) {
                                    BOWTIEDEL_Mask = createMaskBOWTIEDEL(product, BOWTIEDEL_Description);
                                }
                                break;
                            case "HIPOL":
                                if (HIPOL_Mask == null) {
                                    HIPOL_Mask = createMaskHIPOL(product, HIPOL_Description);
                                }
                                break;
                            case "PRODFAIL":
                                if (PRODFAIL_Mask == null) {
                                    PRODFAIL_Mask = createMaskPRODFAIL(product, PRODFAIL_Description);
                                }
                                break;
                        }

                        if (isComposite1MaskInclude() && composite1Mask == null && flagName.equals(getComposite1MaskName())) {
                            composite1Mask = createMaskComposite1(product, composite1Description);
                            continue;
                        }
                        if (isComposite2MaskInclude() && composite2Mask == null && flagName.equals(getComposite2MaskName())) {
                            composite2Mask = createMaskComposite2(product, composite2Description);
                            continue;
                        }
                        if (isComposite3MaskInclude() && composite3Mask == null && flagName.equals(getComposite3MaskName())) {
                            composite3Mask = createMaskComposite3(product, composite3Description);
                            continue;
                        }
                        if (Water_Mask == null && flagName.equals("Water")) {
                            Water_Mask = createWaterMask(product, Water_Description);
                            continue;
                        }

                        if (is_SPARE_MaskInclude()) {
                            if (SPARE8_Mask == null && flagName.equals("SPARE8")) {
                                SPARE8_Mask = createSPARE8Mask(product, SPARE8_Description);
                            }
                            if (SPARE14_Mask == null && flagName.equals("SPARE14")) {
                                SPARE14_Mask = createSPARE14Mask(product, SPARE14_Description);
                            }
                            if (SPARE19_Mask == null && flagName.equals("SPARE19")) {
                                SPARE19_Mask = createSPARE19Mask(product, SPARE19_Description);
                            }
                            if (SPARE24_Mask == null && flagName.equals("SPARE24")) {
                                SPARE24_Mask = createSPARE24Mask(product, SPARE24_Description);
                            }
                            if (SPARE28_Mask == null && flagName.equals("SPARE28")) {
                                SPARE28_Mask = createSPARE28Mask(product, SPARE28_Description);
                            }
                            if (SPARE32_Mask == null && flagName.equals("SPARE32")) {
                                SPARE32_Mask = createSPARE32Mask(product, SPARE32_Description);
                            }
                        }
                    }
                }



                if (ATMFAIL_Mask == null) {
                    ATMFAIL_Mask = createMaskATMFAIL(product, ATMFAIL_Description);
                }

                if (LAND_Mask == null) {
                    LAND_Mask = createMaskLAND(product, LAND_Description);
                }

                if (PRODWARN_Mask == null) {
                    PRODWARN_Mask = createMaskPRODWARN(product, PRODWARN_Description);
                }

                if (HIGLINT_Mask == null) {
                    HIGLINT_Mask = createMaskHIGLINT(product, HIGLINT_Description);
                }

                if (HILT_Mask == null) {
                    HILT_Mask = createMaskHILT(product, HILT_Description);
                }
                
                if (HISATZEN_Mask == null) {
                    HISATZEN_Mask = createMaskHISATZEN(product, HISATZEN_Description);
                }
                
                if (COASTZ_Mask == null) {
                    COASTZ_Mask = createMaskCOASTZ(product, COASTZ_Description);
                }
                
                if (STRAYLIGHT_Mask == null) {
                    STRAYLIGHT_Mask = createMaskSTRAYLIGHT(product, STRAYLIGHT_Description);
                }

                if (CLDICE_Mask == null) {
                    CLDICE_Mask = createMaskCLDICE(product, CLDICE_Description);
                }

                if (COCCOLITH_Mask == null) {
                    COCCOLITH_Mask = createMaskCOCCOLITH(product, COCCOLITH_Description);
                }

                if (TURBIDW_Mask == null) {
                    TURBIDW_Mask = createMaskTURBIDW(product, TURBIDW_Description);
                }

                if (HISOLZEN_Mask == null) {
                    HISOLZEN_Mask = createMaskHISOLZEN(product, HISOLZEN_Description);
                }

                if (LOWLW_Mask == null) {
                    LOWLW_Mask = createMaskLOWLW(product, LOWLW_Description);
                }

                if (CHLFAIL_Mask == null) {
                    CHLFAIL_Mask = createMaskCHLFAIL(product, CHLFAIL_Description);
                }
                
                if (NAVWARN_Mask == null) {
                    NAVWARN_Mask = createMaskNAVWARN(product, NAVWARN_Description);
                }

                if (ABSAER_Mask == null) {
                    ABSAER_Mask = createMaskABSAER(product, ABSAER_Description);
                }

                if (MAXAERITER_Mask == null) {
                    MAXAERITER_Mask = createMaskMAXAERITER(product, MAXAERITER_Description);
                }

                if (MODGLINT_Mask == null) {
                    MODGLINT_Mask = createMaskMODGLINT(product, MODGLINT_Description);
                }

                if (CHLWARN_Mask == null) {
                    CHLWARN_Mask = createMaskCHLWARN(product, CHLWARN_Description);
                }

                if (ATMWARN_Mask == null) {
                    ATMWARN_Mask = createMaskATMWARN(product, ATMWARN_Description);
                }

                if (SEAICE_Mask == null) {
                    SEAICE_Mask = createMaskSEAICE(product, SEAICE_Description);
                }

                if (NAVFAIL_Mask == null) {
                    NAVFAIL_Mask = createMaskNAVFAIL(product, NAVFAIL_Description);
                }

                if (FILTER_Mask == null) {
                    FILTER_Mask = createMaskFILTER(product, FILTER_Description);
                }

                if (BOWTIEDEL_Mask == null) {
                    BOWTIEDEL_Mask = createMaskBOWTIEDEL(product, BOWTIEDEL_Description);
                }

                if (HIPOL_Mask == null) {
                    HIPOL_Mask = createMaskHIPOL(product, HIPOL_Description);
                }

                if (PRODFAIL_Mask == null) {
                    PRODFAIL_Mask = createMaskPRODFAIL(product, PRODFAIL_Description);
                }

                if (isComposite1MaskInclude() && composite1Mask == null) {
                    composite1Mask = createMaskComposite1(product, composite1Description);
                }

                if (isComposite2MaskInclude() && composite2Mask == null) {
                    composite2Mask = createMaskComposite2(product, composite2Description);
                }

                if (isComposite3MaskInclude() && composite3Mask == null) {
                    composite3Mask = createMaskComposite3(product, composite3Description);
                }

                if (Water_Mask == null) {
                    Water_Mask = createWaterMask(product, Water_Description);
                }


                if (is_SPARE_MaskInclude()) {
                    if (SPARE8_Mask == null) {
                        SPARE8_Mask = createSPARE8Mask(product, SPARE8_Description);
                    }
                    if (SPARE14_Mask == null) {
                        SPARE14_Mask = createSPARE14Mask(product, SPARE14_Description);
                    }
                    if (SPARE19_Mask == null) {
                        SPARE19_Mask = createSPARE19Mask(product, SPARE19_Description);
                    }
                    if (SPARE24_Mask == null) {
                        SPARE24_Mask = createSPARE24Mask(product, SPARE24_Description);
                    }
                    if (SPARE28_Mask == null) {
                        SPARE28_Mask = createSPARE28Mask(product, SPARE28_Description);
                    }
                    if (SPARE32_Mask == null) {
                        SPARE32_Mask = createSPARE32Mask(product, SPARE32_Description);
                    }
                }
                


                

                String[] bandNames = product.getBandNames();
                for (String bandName : bandNames) {
                    RasterDataNode raster = product.getRasterDataNode(bandName);
                    if (is_ATMFAIL_MaskEnabled()) {raster.getOverlayMaskGroup().add(ATMFAIL_Mask);}
                    if (is_LAND_MaskEnabled()) {raster.getOverlayMaskGroup().add(LAND_Mask);}
                    if (is_PRODWARN_MaskEnabled()) {raster.getOverlayMaskGroup().add(PRODWARN_Mask);}
                    if (is_HIGLINT_MaskEnabled()) {raster.getOverlayMaskGroup().add(HIGLINT_Mask);}
                    if (is_HILT_MaskEnabled()) {raster.getOverlayMaskGroup().add(HILT_Mask);}
                    if (is_HISATZEN_MaskEnabled()) {raster.getOverlayMaskGroup().add(HISATZEN_Mask);}
                    if (is_COASTZ_MaskEnabled()) {raster.getOverlayMaskGroup().add(COASTZ_Mask);}
                    if (is_STRAYLIGHT_MaskEnabled()) {raster.getOverlayMaskGroup().add(STRAYLIGHT_Mask);}
                    if (is_CLDICE_MaskEnabled()) {raster.getOverlayMaskGroup().add(CLDICE_Mask);}
                    if (is_COCCOLITH_MaskEnabled()) {raster.getOverlayMaskGroup().add(COCCOLITH_Mask);}
                    if (is_TURBIDW_MaskEnabled()) {raster.getOverlayMaskGroup().add(TURBIDW_Mask);}
                    if (is_HISOLZEN_MaskEnabled()) {raster.getOverlayMaskGroup().add(HISOLZEN_Mask);}
                    if (is_LOWLW_MaskEnabled()) {raster.getOverlayMaskGroup().add(LOWLW_Mask);}
                    if (is_CHLFAIL_MaskEnabled()) {raster.getOverlayMaskGroup().add(CHLFAIL_Mask);}
                    if (is_NAVWARN_MaskEnabled()) {raster.getOverlayMaskGroup().add(NAVWARN_Mask);}
                    if (is_ABSAER_MaskEnabled()) {raster.getOverlayMaskGroup().add(ABSAER_Mask);}
                    if (is_MAXAERITER_MaskEnabled()) {raster.getOverlayMaskGroup().add(MAXAERITER_Mask);}
                    if (is_MODGLINT_MaskEnabled()) {raster.getOverlayMaskGroup().add(MODGLINT_Mask);}
                    if (is_CHLWARN_MaskEnabled()) {raster.getOverlayMaskGroup().add(CHLWARN_Mask);}
                    if (is_ATMWARN_MaskEnabled()) {raster.getOverlayMaskGroup().add(ATMWARN_Mask);}
                    if (is_SEAICE_MaskEnabled()) {raster.getOverlayMaskGroup().add(SEAICE_Mask);}
                    if (is_NAVFAIL_MaskEnabled()) {raster.getOverlayMaskGroup().add(NAVFAIL_Mask);}
                    if (is_FILTER_MaskEnabled()) {raster.getOverlayMaskGroup().add(FILTER_Mask);}
                    if (is_BOWTIEDEL_MaskEnabled()) {raster.getOverlayMaskGroup().add(BOWTIEDEL_Mask);}
                    if (is_HIPOL_MaskEnabled()) {raster.getOverlayMaskGroup().add(HIPOL_Mask);}
                    if (is_PRODFAIL_MaskEnabled()) {raster.getOverlayMaskGroup().add(PRODFAIL_Mask);}
                    if (composite1Mask != null && isComposite1MaskEnabled()) {raster.getOverlayMaskGroup().add(composite1Mask);}
                    if (composite2Mask != null && isComposite2MaskEnabled()) {raster.getOverlayMaskGroup().add(composite2Mask);}
                    if (composite3Mask != null && isComposite3MaskEnabled()) {raster.getOverlayMaskGroup().add(composite3Mask);}
                    if (is_Water_MaskEnabled()) {raster.getOverlayMaskGroup().add(Water_Mask);}
                }

                
                
            }
            Band QFBandSST = product.getBand("flags_sst");
            if (QFBandSST != null) {
                FlagCoding flagCoding = new FlagCoding("SST_Flags");
                flagCoding.addFlag("ISMASKED", 0x01, "Pixel was already masked");
                flagCoding.addFlag("BTBAD", 0x02, "Bad Brightness Temperatures");
                flagCoding.addFlag("BTRANGE", 0x04, "Brightness Temperatures outside valid range");
                flagCoding.addFlag("BTDIFF", 0x08, "Brightness Temperatures are too different");
                flagCoding.addFlag("SSTRANGE", 0x10, "Computed SST outside valid range");
                flagCoding.addFlag("SSTREFDIFF", 0x20, "Computed SST too different from reference SST");
                flagCoding.addFlag("SST4DIFF", 0x40, "Computed SST too different from computed 4 micron SST");
                flagCoding.addFlag("SST4VDIFF", 0x80, "Computed SST very different from computed 4 micron SST");
//            flagCoding.addFlag("SST3DIFF", 0x40, "Computed SST too different from computed triple-window SST");
//            flagCoding.addFlag("SST3VDIFF", 0x80, "Computed SST very different from computed triple-window SST");
                flagCoding.addFlag("BTNONUNIF", 0x100, "Spatially Non-uniform Brightness Temperatures");
                flagCoding.addFlag("BTVNONUNIF", 0x200, "Very Spatially Non-uniform Brightness Temperatures");
                flagCoding.addFlag("BT4REFDIFF", 0x400, "4 micron Brightness Temperature differs from reference");
                flagCoding.addFlag("REDNONUNIF", 0x800, "Spatially Non-uniform red band");
                flagCoding.addFlag("HISENZ", 0x1000, "High sensor zenith angle");
                flagCoding.addFlag("VHISENZ", 0x2000, "Very High sensor zenith angle");
                flagCoding.addFlag("SSTREFVDIFF", 0x4000, "Computed SST very different from reference SST)");
                flagCoding.addFlag("CLOUD", 0x8000, "Cloud Detected");
                if (sensor.equalsIgnoreCase("AVHRR")) {
                    flagCoding.addFlag("SUNLIGHT", 0x40, "Stray sunlight detected (AVHRR)");
                    flagCoding.addFlag("ASCEND", 0x80, "AVHRR in ascending node (daytime)");
                    flagCoding.addFlag("GLINT", 0x400, "Sun glint detected (AVHRR)");
                }

                product.getFlagCodingGroup().add(flagCoding);
                QFBandSST.setSampleCoding(flagCoding);

                product.getMaskGroup().add(Mask.BandMathsType.create("ISMASKED", "Pixel was already masked",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.ISMASKED",
                        LandBrown, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTBAD", "Bad Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.BTBAD",
                        FailRed, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTRANGE", "Brightness Temperatures outside valid range",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.BTRANGE",
                        DeepBlue, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTDIFF", "Brightness Temperatures are too different",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.BTDIFF",
                        Color.GRAY, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTRANGE", "Computed SST outside valid range",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.SSTRANGE",
                        BrightPink, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTREFDIFF", "Computed SST too different from reference SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.SSTREFDIFF",
                        LightCyan, 0.5));
                if (sensor.equalsIgnoreCase("AVHRR")) {
                    product.getMaskGroup().add(Mask.BandMathsType.create("SUNLIGHT", "Stray sunlight detected (AVHRR)",
                            product.getSceneRasterWidth(),
                            product.getSceneRasterHeight(), "flags_sst.SUNLIGHT",
                            BurntUmber, 0.5));
                    product.getMaskGroup().add(Mask.BandMathsType.create("ASCEND", "AVHRR in ascending node (daytime)",
                            product.getSceneRasterWidth(),
                            product.getSceneRasterHeight(), "flags_sst.ASCEND",
                            LightBrown, 0.2));
                    product.getMaskGroup().add(Mask.BandMathsType.create("GLINT", "Sun glint detected (AVHRR)",
                            product.getSceneRasterWidth(),
                            product.getSceneRasterHeight(), "flags_sst.GLINT",
                            Color.YELLOW, 0.5));

                } else {
                    product.getMaskGroup().add(Mask.BandMathsType.create("SST4DIFF", "Computed SST too different from computed 4 micron SST",
                            product.getSceneRasterWidth(),
                            product.getSceneRasterHeight(), "flags_sst.SST4DIFF",
                            BurntUmber, 0.5));
                    product.getMaskGroup().add(Mask.BandMathsType.create("SST4VDIFF", "Computed SST very different from computed 4 micron SST",
                            product.getSceneRasterWidth(),
                            product.getSceneRasterHeight(), "flags_sst.SST4VDIFF",
                            LightBrown, 0.2));
                    product.getMaskGroup().add(Mask.BandMathsType.create("BT4REFDIFF", "4 micron Brightness Temperature differs from reference",
                            product.getSceneRasterWidth(),
                            product.getSceneRasterHeight(), "flags_sst.BT4REFDIFF",
                            Color.YELLOW, 0.5));
                }
                product.getMaskGroup().add(Mask.BandMathsType.create("BTNONUNIF", "Spatially Non-uniform Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.BTNONUNIF",
                        Color.ORANGE, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTVNONUNIF", "Very Spatially Non-uniform Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.BTVNONUNIF",
                        Color.CYAN, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("REDNONUNIF", "Spatially Non-uniform red band",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.REDNONUNIF",
                        Purple, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("HISENZ", "High sensor zenith angle",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.HISENZ",
                        Cornflower, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("VHISENZ", "Very High sensor zenith angle",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.VHISENZ",
                        LightPurple, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTREFVDIFF", "Computed SST very different from reference SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.SSTREFVDIFF",
                        Color.MAGENTA, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("CLOUD", "Cloud Detected",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst.CLOUD",
                        Color.WHITE, 0.5));
            }
            Band QFBandSST4 = product.getBand("flags_sst4");
            if (QFBandSST4 != null) {
                FlagCoding flagCoding = new FlagCoding("SST4_Flags");
                flagCoding.addFlag("ISMASKED", 0x01, "Pixel was already masked");
                flagCoding.addFlag("BTBAD", 0x02, "Bad Brightness Temperatures");
                flagCoding.addFlag("BTRANGE", 0x04, "Brightness Temperatures outside valid range");
                flagCoding.addFlag("BTDIFF", 0x08, "Brightness Temperatures are too different");
                flagCoding.addFlag("SSTRANGE", 0x10, "Computed SST outside valid range");
                flagCoding.addFlag("SSTREFDIFF", 0x20, "Computed SST too different from reference SST");
                flagCoding.addFlag("SST4DIFF", 0x40, "Computed SST too different from computed 4 micron SST");
                flagCoding.addFlag("SST4VDIFF", 0x80, "Computed SST very different from computed 4 micron SST");
//            flagCoding.addFlag("SST3DIFF", 0x40, "Computed SST too different from computed triple-window SST");
//            flagCoding.addFlag("SST3VDIFF", 0x80, "Computed SST very different from computed triple-window SST");
                flagCoding.addFlag("BTNONUNIF", 0x100, "Spatially Non-uniform Brightness Temperatures");
                flagCoding.addFlag("BTVNONUNIF", 0x200, "Very Spatially Non-uniform Brightness Temperatures");
                flagCoding.addFlag("BT4REFDIFF", 0x400, "4 micron Brightness Temperature differs from reference");
                flagCoding.addFlag("REDNONUNIF", 0x800, "Spatially Non-uniform red band");
                flagCoding.addFlag("HISENZ", 0x1000, "High sensor zenith angle");
                flagCoding.addFlag("VHISENZ", 0x2000, "Very High sensor zenith angle");
                flagCoding.addFlag("SSTREFVDIFF", 0x4000, "Computed SST very different from reference SST)");
                flagCoding.addFlag("CLOUD", 0x8000, "Cloud Detected");

                product.getFlagCodingGroup().add(flagCoding);
                QFBandSST4.setSampleCoding(flagCoding);

                product.getMaskGroup().add(Mask.BandMathsType.create("ISMASKED", "Pixel was already masked",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.ISMASKED",
                        LandBrown, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTBAD", "Bad Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.BTBAD",
                        FailRed, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTRANGE", "Brightness Temperatures outside valid range",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.BTRANGE",
                        DeepBlue, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTDIFF", "Brightness Temperatures are too different",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.BTDIFF",
                        Color.GRAY, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTRANGE", "Computed SST outside valid range",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.SSTRANGE",
                        BrightPink, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTREFDIFF", "Computed SST too different from reference SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.SSTREFDIFF",
                        LightCyan, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("SST4DIFF", "Computed SST too different from computed 4 micron SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.SST4DIFF",
                        BurntUmber, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("SST4VDIFF", "Computed SST very different from computed 4 micron SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.SST4VDIFF",
                        LightBrown, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTNONUNIF", "Spatially Non-uniform Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.BTNONUNIF",
                        Color.ORANGE, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTVNONUNIF", "Very Spatially Non-uniform Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.BTVNONUNIF",
                        Color.CYAN, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("BT4REFDIFF", "4 micron Brightness Temperature differs from reference",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.BT4REFDIFF",
                        Color.YELLOW, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("REDNONUNIF", "Spatially Non-uniform red band",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.REDNONUNIF",
                        Purple, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("HISENZ", "High sensor zenith angle",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.HISENZ",
                        Cornflower, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("VHISENZ", "Very High sensor zenith angle",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.VHISENZ",
                        LightPurple, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTREFVDIFF", "Computed SST very different from reference SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.SSTREFVDIFF",
                        Color.MAGENTA, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("CLOUD", "Cloud Detected",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst4.CLOUD",
                        Color.WHITE, 0.5));
            }
            Band QFBandSSTtriple = product.getBand("flags_sst_triple");
            if (QFBandSSTtriple != null) {
                FlagCoding flagCoding = new FlagCoding("SST_Triple_Flags");
                flagCoding.addFlag("ISMASKED", 0x01, "Pixel was already masked");
                flagCoding.addFlag("BTBAD", 0x02, "Bad Brightness Temperatures");
                flagCoding.addFlag("BTRANGE", 0x04, "Brightness Temperatures outside valid range");
                flagCoding.addFlag("BTDIFF", 0x08, "Brightness Temperatures are too different");
                flagCoding.addFlag("SSTRANGE", 0x10, "Computed SST outside valid range");
                flagCoding.addFlag("SSTREFDIFF", 0x20, "Computed SST too different from reference SST");
//            flagCoding.addFlag("SST4DIFF", 0x40, "Computed SST too different from computed 4 micron SST");
//            flagCoding.addFlag("SST4VDIFF", 0x80, "Computed SST very different from computed 4 micron SST");
                flagCoding.addFlag("SST3DIFF", 0x40, "Computed SST too different from computed triple-window SST");
                flagCoding.addFlag("SST3VDIFF", 0x80, "Computed SST very different from computed triple-window SST");
                flagCoding.addFlag("BTNONUNIF", 0x100, "Spatially Non-uniform Brightness Temperatures");
                flagCoding.addFlag("BTVNONUNIF", 0x200, "Very Spatially Non-uniform Brightness Temperatures");
//            flagCoding.addFlag("BT4REFDIFF", 0x400, "4 micron Brightness Temperature differs from reference");
                flagCoding.addFlag("REDNONUNIF", 0x800, "Spatially Non-uniform red band");
                flagCoding.addFlag("HISENZ", 0x1000, "High sensor zenith angle");
                flagCoding.addFlag("VHISENZ", 0x2000, "Very High sensor zenith angle");
                flagCoding.addFlag("SSTREFVDIFF", 0x4000, "Computed SST very different from reference SST)");
                flagCoding.addFlag("CLOUD", 0x8000, "Cloud Detected");

                product.getFlagCodingGroup().add(flagCoding);
                QFBandSSTtriple.setSampleCoding(flagCoding);

                product.getMaskGroup().add(Mask.BandMathsType.create("ISMASKED", "Pixel was already masked",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.ISMASKED",
                        LandBrown, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTBAD", "Bad Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.BTBAD",
                        FailRed, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTRANGE", "Brightness Temperatures outside valid range",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.BTRANGE",
                        DeepBlue, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTDIFF", "Brightness Temperatures are too different",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.BTDIFF",
                        Color.GRAY, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTRANGE", "Computed SST outside valid range",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.SSTRANGE",
                        BrightPink, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTREFDIFF", "Computed SST too different from reference SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.SSTREFDIFF",
                        LightCyan, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("SST3DIFF", "Computed SST too different from computed triple window SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.SST3DIFF",
                        BurntUmber, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("SST3VDIFF", "Computed SST very different from computed triple window SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.SST3VDIFF",
                        LightBrown, 0.2));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTNONUNIF", "Spatially Non-uniform Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.BTNONUNIF",
                        Color.ORANGE, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("BTVNONUNIF", "Very Spatially Non-uniform Brightness Temperatures",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.BTVNONUNIF",
                        Color.CYAN, 0.5));
//            product.getMaskGroup().add(Mask.BandMathsType.create("BT4REFDIFF", "4 micron Brightness Temperature differs from reference",
//                    product.getSceneRasterWidth(),
//                    product.getSceneRasterHeight(), "flags_sst_triple.BT4REFDIFF",
//                    Color.YELLOW, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("REDNONUNIF", "Spatially Non-uniform red band",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.REDNONUNIF",
                        Purple, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("HISENZ", "High sensor zenith angle",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.HISENZ",
                        Cornflower, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("VHISENZ", "Very High sensor zenith angle",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.VHISENZ",
                        LightPurple, 0.0));
                product.getMaskGroup().add(Mask.BandMathsType.create("SSTREFVDIFF", "Computed SST very different from reference SST",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.SSTREFVDIFF",
                        Color.MAGENTA, 0.5));
                product.getMaskGroup().add(Mask.BandMathsType.create("CLOUD", "Cloud Detected",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "flags_sst_triple.CLOUD",
                        Color.WHITE, 0.5));
            }

            Band QFBandSSTqual = product.getBand("qual_sst");
            if (QFBandSSTqual != null) {

                product.getMaskGroup().add(Mask.BandMathsType.create("Best (SST)", "Highest quality SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst == 0",
                        SeadasFileReader.Cornflower, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Good (SST)", "Good quality SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst == 1",
                        SeadasFileReader.LightPurple, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Questionable (SST)", "Questionable quality SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst == 2",
                        SeadasFileReader.BurntUmber, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Bad (SST)", "Bad quality SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst == 3",
                        SeadasFileReader.FailRed, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("No SST Retrieval", "No SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst == 4",
                        SeadasFileReader.FailRed, 0.6));
            }
            Band QFBandSSTqual4 = product.getBand("qual_sst4");
            if (QFBandSSTqual4 != null) {

                product.getMaskGroup().add(Mask.BandMathsType.create("Best (SST4)", "Highest quality SST4 retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst4 == 0",
                        SeadasFileReader.Cornflower, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Good (SST4)", "Good quality SST4 retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst4 == 1",
                        SeadasFileReader.LightPurple, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Questionable (SST4)", "Questionable quality SST4 retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst4 == 2",
                        SeadasFileReader.BurntUmber, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Bad (SST4)", "Bad quality SST4 retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst4 == 3",
                        SeadasFileReader.FailRed, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("No SST4 Retrieval", "No SST4 retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst4 == 4",
                        SeadasFileReader.FailRed, 0.6));
            }
            Band QFBandSSTqualTriple = product.getBand("qual_sst_triple");
            if (QFBandSSTqualTriple != null) {

                product.getMaskGroup().add(Mask.BandMathsType.create("Best (SST triple)", "Highest quality triple window SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst_triple == 0",
                        SeadasFileReader.Cornflower, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Good (SST triple)", "Good quality triple window SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst_triple == 1",
                        SeadasFileReader.LightPurple, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Questionable (SST triple)", "Questionable quality triple window SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst_triple == 2",
                        SeadasFileReader.BurntUmber, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("Bad (SST triple)", "Bad quality triple window SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst_triple == 3",
                        SeadasFileReader.FailRed, 0.6));
                product.getMaskGroup().add(Mask.BandMathsType.create("No SST triple window Retrieval", "No triple window SST retrieval",
                        product.getSceneRasterWidth(),
                        product.getSceneRasterHeight(), "qual_sst_triple == 4",
                        SeadasFileReader.FailRed, 0.6));
            }
        }
    }

    
    



    private Mask createMaskATMFAIL(Product product, String ATMFAIL_Description) {
        Mask ATMFAIL_Mask = Mask.BandMathsType.create("ATMFAIL", ATMFAIL_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.ATMFAIL",
                get_ATMFAIL_MaskColor(), get_ATMFAIL_MaskTransparency());
        product.getMaskGroup().add(ATMFAIL_Mask);

        return ATMFAIL_Mask;
    }


    private Mask createMaskLAND(Product product, String LAND_Description) {
        Mask LAND_Mask = Mask.BandMathsType.create("LAND", LAND_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.LAND",
                get_LAND_MaskColor(), get_LAND_MaskTransparency());
        product.getMaskGroup().add(LAND_Mask);

        return LAND_Mask;
    }



    private Mask createMaskPRODWARN(Product product, String PRODWARN_Description) {
        Mask PRODWARN_Mask = Mask.BandMathsType.create("PRODWARN", PRODWARN_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.PRODWARN",
                get_PRODWARN_MaskColor(), get_PRODWARN_MaskTransparency());
        product.getMaskGroup().add(PRODWARN_Mask);

        return PRODWARN_Mask;
    }




    private Mask createMaskHIGLINT(Product product, String HIGLINT_Description) {
        Mask HIGLINT_Mask = Mask.BandMathsType.create("HIGLINT", HIGLINT_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.HIGLINT",
                get_HIGLINT_MaskColor(), get_HIGLINT_MaskTransparency());
        product.getMaskGroup().add(HIGLINT_Mask);

        return HIGLINT_Mask;
    }

    
    

    private Mask createMaskHILT(Product product, String HILT_Description) {
        Mask HILT_Mask = Mask.BandMathsType.create("HILT", HILT_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.HILT",
                get_HILT_MaskColor(), get_HILT_MaskTransparency());
        product.getMaskGroup().add(HILT_Mask);

        return HILT_Mask;
    }

    
    
    

    private Mask createMaskHISATZEN(Product product, String HISATZEN_Description) {
        Mask HISATZEN_Mask = Mask.BandMathsType.create("HISATZEN", HISATZEN_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.HISATZEN",
                get_HISATZEN_MaskColor(), get_HISATZEN_MaskTransparency());
        product.getMaskGroup().add(HISATZEN_Mask);

        return HISATZEN_Mask;
    }





    private Mask createMaskCOASTZ(Product product, String COASTZ_Description) {
        Mask COASTZ_Mask = Mask.BandMathsType.create("COASTZ", COASTZ_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.COASTZ",
                get_COASTZ_MaskColor(), get_COASTZ_MaskTransparency());
        product.getMaskGroup().add(COASTZ_Mask);

        return COASTZ_Mask;
    }
    

    
    private Mask createMaskSTRAYLIGHT(Product product, String STRAYLIGHT_Description) {
        Mask STRAYLIGHT_Mask = Mask.BandMathsType.create("STRAYLIGHT", STRAYLIGHT_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.STRAYLIGHT",
                get_STRAYLIGHT_MaskColor(), get_STRAYLIGHT_MaskTransparency());
        product.getMaskGroup().add(STRAYLIGHT_Mask);

        return STRAYLIGHT_Mask;
    }

    
    
    private Mask createMaskCLDICE(Product product, String CLDICE_Description) {
        Mask CLDICE_Mask = Mask.BandMathsType.create("CLDICE", CLDICE_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.CLDICE",
                get_CLDICE_MaskColor(), get_CLDICE_MaskTransparency());
        product.getMaskGroup().add(CLDICE_Mask);

        return CLDICE_Mask;
    }

    
    
    private Mask createMaskCOCCOLITH(Product product, String COCCOLITH_Description) {
        Mask COCCOLITH_Mask = Mask.BandMathsType.create("COCCOLITH", COCCOLITH_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.COCCOLITH",
                get_COCCOLITH_MaskColor(), get_COCCOLITH_MaskTransparency());
        product.getMaskGroup().add(COCCOLITH_Mask);

        return COCCOLITH_Mask;
    }

    
    
    private Mask createMaskTURBIDW(Product product, String TURBIDW_Description) {
        Mask TURBIDW_Mask = Mask.BandMathsType.create("TURBIDW", TURBIDW_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.TURBIDW",
                get_TURBIDW_MaskColor(), get_TURBIDW_MaskTransparency());
        product.getMaskGroup().add(TURBIDW_Mask);

        return TURBIDW_Mask;
    }

    
    
    private Mask createMaskHISOLZEN(Product product, String HISOLZEN_Description) {
        Mask HISOLZEN_Mask = Mask.BandMathsType.create("HISOLZEN", HISOLZEN_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.HISOLZEN",
                get_HISOLZEN_MaskColor(), get_HISOLZEN_MaskTransparency());
        product.getMaskGroup().add(HISOLZEN_Mask);

        return HISOLZEN_Mask;
    }

    
    
    private Mask createMaskLOWLW(Product product, String LOWLW_Description) {
        Mask LOWLW_Mask = Mask.BandMathsType.create("LOWLW", LOWLW_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.LOWLW",
                get_LOWLW_MaskColor(), get_LOWLW_MaskTransparency());
        product.getMaskGroup().add(LOWLW_Mask);

        return LOWLW_Mask;
    }

    
    

    private Mask createMaskCHLFAIL(Product product, String CHLFAIL_Description) {
        Mask CHLFAIL_Mask = Mask.BandMathsType.create("CHLFAIL", CHLFAIL_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.CHLFAIL",
                get_CHLFAIL_MaskColor(), get_CHLFAIL_MaskTransparency());
        product.getMaskGroup().add(CHLFAIL_Mask);

        return CHLFAIL_Mask;
    }

    
    
    private Mask createMaskNAVWARN(Product product, String NAVWARN_Description) {
        Mask NAVWARN_Mask = Mask.BandMathsType.create("NAVWARN", NAVWARN_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.NAVWARN",
                get_NAVWARN_MaskColor(), get_NAVWARN_MaskTransparency());
        product.getMaskGroup().add(NAVWARN_Mask);

        return NAVWARN_Mask;
    }

    
    
    private Mask createMaskABSAER(Product product, String ABSAER_Description) {
        Mask ABSAER_Mask = Mask.BandMathsType.create("ABSAER", ABSAER_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.ABSAER",
                get_ABSAER_MaskColor(), get_ABSAER_MaskTransparency());
        product.getMaskGroup().add(ABSAER_Mask);

        return ABSAER_Mask;
    }

    
    
    private Mask createMaskMAXAERITER(Product product, String MAXAERITER_Description) {
        Mask MAXAERITER_Mask = Mask.BandMathsType.create("MAXAERITER", MAXAERITER_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.MAXAERITER",
                get_MAXAERITER_MaskColor(), get_MAXAERITER_MaskTransparency());
        product.getMaskGroup().add(MAXAERITER_Mask);

        return MAXAERITER_Mask;
    }

    
    

    private Mask createMaskMODGLINT(Product product, String MODGLINT_Description) {
        Mask MODGLINT_Mask = Mask.BandMathsType.create("MODGLINT", MODGLINT_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.MODGLINT",
                get_MODGLINT_MaskColor(), get_MODGLINT_MaskTransparency());
        product.getMaskGroup().add(MODGLINT_Mask);

        return MODGLINT_Mask;
    }

    
    
    private Mask createMaskCHLWARN(Product product, String CHLWARN_Description) {
        Mask CHLWARN_Mask = Mask.BandMathsType.create("CHLWARN", CHLWARN_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.CHLWARN",
                get_CHLWARN_MaskColor(), get_CHLWARN_MaskTransparency());
        product.getMaskGroup().add(CHLWARN_Mask);

        return CHLWARN_Mask;
    }

    
    

    private Mask createMaskATMWARN(Product product, String ATMWARN_Description) {
        Mask ATMWARN_Mask = Mask.BandMathsType.create("ATMWARN", ATMWARN_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.ATMWARN",
                get_ATMWARN_MaskColor(), get_ATMWARN_MaskTransparency());
        product.getMaskGroup().add(ATMWARN_Mask);

        return ATMWARN_Mask;
    }

    
    
    private Mask createMaskSEAICE(Product product, String SEAICE_Description) {
        Mask SEAICE_Mask = Mask.BandMathsType.create("SEAICE", SEAICE_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.SEAICE",
                get_SEAICE_MaskColor(), get_SEAICE_MaskTransparency());
        product.getMaskGroup().add(SEAICE_Mask);

        return SEAICE_Mask;
    }

    

    private Mask createMaskNAVFAIL(Product product, String NAVFAIL_Description) {
        Mask NAVFAIL_Mask = Mask.BandMathsType.create("NAVFAIL", NAVFAIL_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.NAVFAIL",
                get_NAVFAIL_MaskColor(), get_NAVFAIL_MaskTransparency());
        product.getMaskGroup().add(NAVFAIL_Mask);

        return NAVFAIL_Mask;
    }

    
    
    private Mask createMaskFILTER(Product product, String FILTER_Description) {
        Mask FILTER_Mask = Mask.BandMathsType.create("FILTER", FILTER_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.FILTER",
                get_FILTER_MaskColor(), get_FILTER_MaskTransparency());
        product.getMaskGroup().add(FILTER_Mask);

        return FILTER_Mask;
    }

    

    private Mask createMaskBOWTIEDEL(Product product, String BOWTIEDEL_Description) {
        Mask BOWTIEDEL_Mask = Mask.BandMathsType.create("BOWTIEDEL", BOWTIEDEL_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.BOWTIEDEL",
                get_BOWTIEDEL_MaskColor(), get_BOWTIEDEL_MaskTransparency());
        product.getMaskGroup().add(BOWTIEDEL_Mask);

        return BOWTIEDEL_Mask;
    }

    
    
    private Mask createMaskHIPOL(Product product, String HIPOL_Description) {
        Mask HIPOL_Mask = Mask.BandMathsType.create("HIPOL", HIPOL_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.HIPOL",
                get_HIPOL_MaskColor(), get_HIPOL_MaskTransparency());
        product.getMaskGroup().add(HIPOL_Mask);

        return HIPOL_Mask;
    }

    
    
    private Mask createMaskPRODFAIL(Product product, String PRODFAIL_Description) {
        Mask PRODFAIL_Mask = Mask.BandMathsType.create("PRODFAIL", PRODFAIL_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.PRODFAIL",
                get_PRODFAIL_MaskColor(), get_PRODFAIL_MaskTransparency());
        product.getMaskGroup().add(PRODFAIL_Mask);

        return PRODFAIL_Mask;
    }



    private Mask createMaskComposite1(Product product, String composite1Description) {

        Mask composite1Mask = null;
        String composite1Expression = getComposite1Expression();
        String composite1MaskName = getComposite1MaskName();
        if (isComposite1MaskInclude() &&
                composite1Expression != null && composite1Expression.trim().length() > 1 &&
                composite1MaskName != null && composite1MaskName.trim().length() > 1) {

            composite1Mask = Mask.BandMathsType.create(composite1MaskName, composite1Description,
                    product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                    composite1Expression,
                    getComposite1MaskColor(), getComposite1MaskTransparency());

            product.getMaskGroup().add(composite1Mask);
        }
        
        return composite1Mask;
    }



    private Mask createMaskComposite2(Product product, String composite2Description) {

        Mask composite2Mask = null;
        String composite2Expression = getComposite2Expression();
        String composite2MaskName = getComposite2MaskName();
        if (isComposite2MaskInclude() &&
                composite2Expression != null && composite2Expression.trim().length() > 1 &&
                composite2MaskName != null && composite2MaskName.trim().length() > 1) {

            composite2Mask = Mask.BandMathsType.create(composite2MaskName, composite2Description,
                    product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                    composite2Expression,
                    getComposite2MaskColor(), getComposite2MaskTransparency());

            product.getMaskGroup().add(composite2Mask);
        }

        return composite2Mask;
    }


    private Mask createMaskComposite3(Product product, String composite3Description) {

        Mask composite3Mask = null;
        String composite3Expression = getComposite3Expression();
        String composite3MaskName = getComposite3MaskName();
        if (isComposite3MaskInclude() &&
                composite3Expression != null && composite3Expression.trim().length() > 1 &&
                composite3MaskName != null && composite3MaskName.trim().length() > 1) {

            composite3Mask = Mask.BandMathsType.create(composite3MaskName, composite3Description,
                    product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                    composite3Expression,
                    getComposite3MaskColor(), getComposite3MaskTransparency());

            product.getMaskGroup().add(composite3Mask);
        }

        return composite3Mask;
    }



    private Mask createWaterMask(Product product, String Water_Description) {

    Mask Water_Mask = Mask.BandMathsType.create("Water", Water_Description,
            product.getSceneRasterWidth(), product.getSceneRasterHeight(),
            "!l2_flags.LAND",
            get_Water_MaskColor(), get_Water_MaskTransparency());
                product.getMaskGroup().add(Water_Mask);
        return Water_Mask;
    }





    private Mask createSPARE8Mask(Product product, String SPARE8_Description) {
        Mask SPARE8_Mask = Mask.BandMathsType.create("SPARE8", SPARE8_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.SPARE8",
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        if (is_SPARE_MaskInclude()) {
            product.getMaskGroup().add(SPARE8_Mask);
            return SPARE8_Mask;
        }
        
        return null;
    }


    private Mask createSPARE14Mask(Product product, String SPARE14_Description) {
        Mask SPARE14_Mask = Mask.BandMathsType.create("SPARE14", SPARE14_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.SPARE14",
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        if (is_SPARE_MaskInclude()) {
            product.getMaskGroup().add(SPARE14_Mask);
            return SPARE14_Mask;
        }

        return null;
    }




    private Mask createSPARE19Mask(Product product, String SPARE19_Description) {
        Mask SPARE19_Mask = Mask.BandMathsType.create("SPARE19", SPARE19_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.SPARE19",
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        if (is_SPARE_MaskInclude()) {
            product.getMaskGroup().add(SPARE19_Mask);
            return SPARE19_Mask;
        }

        return null;
    }


    
    private Mask createSPARE24Mask(Product product, String SPARE24_Description) {
        Mask SPARE24_Mask = Mask.BandMathsType.create("SPARE24", SPARE24_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.SPARE24",
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        if (is_SPARE_MaskInclude()) {
            product.getMaskGroup().add(SPARE24_Mask);
            return SPARE24_Mask;
        }

        return null;
    }



    private Mask createSPARE28Mask(Product product, String SPARE28_Description) {
        Mask SPARE28_Mask = Mask.BandMathsType.create("SPARE28", SPARE28_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.SPARE28",
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        if (is_SPARE_MaskInclude()) {
            product.getMaskGroup().add(SPARE28_Mask);
            return SPARE28_Mask;
        }

        return null;
    }




    private Mask createSPARE32Mask(Product product, String SPARE32_Description) {
        Mask SPARE32_Mask = Mask.BandMathsType.create("SPARE32", SPARE32_Description,
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                "l2_flags.SPARE32",
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        if (is_SPARE_MaskInclude()) {
            product.getMaskGroup().add(SPARE32_Mask);
            return SPARE32_Mask;
        }

        return null;
    }

    



                
                


    public Map<Band, Variable> addBands(Product product,
                                        List<Variable> variables) {
        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        for (Variable variable : variables) {
            int variableRank = variable.getRank();
            if (variableRank == 2) {
                Band band = addNewBand(product, variable);
                if (band != null) {
                    bandToVariableMap.put(band, variable);
                }
            } else if (variableRank == 3) {
                add3DNewBands(product, variable, bandToVariableMap);
            }

        }
        setSpectralBand(product);

        return bandToVariableMap;
    }

    protected void setSpectralBand(Product product) {
        int spectralBandIndex = 0;
        for (String name : product.getBandNames()) {
            Band band = product.getBandAt(product.getBandIndex(name));
            if (name.matches("\\w+_\\d{3,}")) {
                String[] parts = name.split("_");
                String wvlstr = parts[parts.length - 1].trim();
                //Some bands have the wvl portion in the middle...
                if (!wvlstr.matches("^\\d{3,}")) {
                    wvlstr = parts[parts.length - 2].trim();
                }
                final float wavelength = Float.parseFloat(wvlstr);
                band.setSpectralWavelength(wavelength);
                band.setSpectralBandIndex(spectralBandIndex++);
            }
        }
    }

    protected Map<Band, Variable> add3DNewBands(Product product, Variable variable, Map<Band, Variable> bandToVariableMap) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        int spectralBandIndex = 0;
        Array wavelengths = null;

        final int[] dimensions = variable.getShape();
        final int bands = dimensions[2];
        final int height = dimensions[0];
        final int width = dimensions[1];
        int dim = 0;
        Variable wvl = null;

        if (height == sceneRasterHeight && width == sceneRasterWidth) {
            // final List<Attribute> list = variable.getAttributes();
            List<Dimension> dims = ncFile.getDimensions();
            for (Dimension d: dims){
                if (d.getShortName().equalsIgnoreCase("wavelength_3d")) {
                    dim = d.getLength();
                }
            }
            if (dim == bands) {
                wvl = ncFile.findVariable("sensor_band_parameters/wavelength_3d");
            } else {
                wvl = ncFile.findVariable("sensor_band_parameters/wavelength");
            }
            // wavenlengths for modis L2 files
            if (wvl == null) {
                if (bands == 2 || bands == 3) {
                    wvl = ncFile.findVariable("HDFEOS/SWATHS/Aerosol_NearUV_Swath/Data_Fields/Wavelength");
                    // wavelenghs for DSCOVR EPIC L2 files
                }
            }
            if (wvl != null) {
                try {
                    wavelengths = wvl.read();
                } catch (IOException e) {
                }

                for (int i = 0; i < bands; i++) {
                    final String shortname = variable.getShortName();
                    StringBuilder longname = new StringBuilder(shortname);
                    longname.append("_");
                    longname.append(wavelengths.getInt(i));
                    String name = longname.toString();
                    String safeName = (name != null && name.contains("-")) ? "'" + name + "'" : name;
                    final int dataType = getProductDataType(variable);

                    if (!product.containsBand(name)) {

                        final Band band = new Band(name, dataType, width, height);
                        product.addBand(band);

                        Variable sliced = null;
                        try {
                            sliced = variable.slice(2, i);
                        } catch (InvalidRangeException e) {
                            e.printStackTrace();  //Todo change body of catch statement.
                        }
                        bandToVariableMap.put(band, sliced);

                        try {
                            Attribute fillValue = variable.findAttribute("_FillValue");
                            if (fillValue == null) {
                                fillValue = variable.findAttribute("bad_value_scaled");
                            }
                            band.setNoDataValue((double) fillValue.getNumericValue().floatValue());
                            band.setNoDataValueUsed(true);
                        } catch (Exception ignored) {
                        }

                        final List<Attribute> list = variable.getAttributes();
                        double[] validMinMax = {0.0, 0.0};
                        for (Attribute attribute : list) {
                            final String attribName = attribute.getShortName();
                            if ("units".equals(attribName)) {
                                band.setUnit(attribute.getStringValue());
                            } else if ("long_name".equals(attribName)) {
                                band.setDescription(attribute.getStringValue());
                            } else if ("slope".equals(attribName)) {
                                band.setScalingFactor(attribute.getNumericValue(0).doubleValue());
                            } else if ("intercept".equals(attribName)) {
                                band.setScalingOffset(attribute.getNumericValue(0).doubleValue());
                            } else if ("scale_factor".equals(attribName)) {
                                band.setScalingFactor(attribute.getNumericValue(0).doubleValue());
                            } else if ("add_offset".equals(attribName)) {
                                band.setScalingOffset(attribute.getNumericValue(0).doubleValue());
                            } else if (attribName.startsWith("valid_")) {
                                if ("valid_min".equals(attribName)) {
                                    if (attribute.getDataType().isUnsigned()) {
                                        validMinMax[0] = getUShortAttribute(attribute);
                                    } else {
                                        validMinMax[0] = attribute.getNumericValue(0).doubleValue();
                                    }
                                } else if ("valid_max".equals(attribName)) {
                                    if (attribute.getDataType().isUnsigned()) {
                                        validMinMax[1] = getUShortAttribute(attribute);
                                    } else {
                                        validMinMax[1] = attribute.getNumericValue(0).doubleValue();
                                    }
                                } else if ("valid_range".equals(attribName)) {
                                    validMinMax[0] = attribute.getNumericValue(0).doubleValue();
                                    validMinMax[1] = attribute.getNumericValue(1).doubleValue();
                                }
                            }
                        }
                        if (validMinMax[0] != validMinMax[1]) {
                            String validExp;
                            if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                                validExp = format("%s >= %.05f && %s <= %.05f", safeName, validMinMax[0], safeName, validMinMax[1]);

                            } else {
                                double[] minmax = {0.0, 0.0};
                                minmax[0] = validMinMax[0];
                                minmax[1] = validMinMax[1];

                                if (band.getScalingFactor() != 1.0) {
                                    minmax[0] *= band.getScalingFactor();
                                    minmax[1] *= band.getScalingFactor();
                                }
                                if (band.getScalingOffset() != 0.0) {
                                    minmax[0] += band.getScalingOffset();
                                    minmax[1] += band.getScalingOffset();
                                }
                                validExp = format("%s >= %.05f && %s <= %.05f", safeName, minmax[0], safeName, minmax[1]);

                            }
                            band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                        }
                    } else {
                        logger.log(Level.WARNING, "The Product '" + product.getName() + "' contains duplicate bands" +
                                " with the name '" + name + "', one will be ignored.");
                    }
                }
            }
        }
        return bandToVariableMap;
    }

    protected Band addNewBand(Product product, Variable variable) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band = null;

        int variableRank = variable.getRank();
        if (variableRank == 2) {
            final int[] dimensions = variable.getShape();
            final int height = dimensions[0] - leadLineSkip - tailLineSkip;
            final int width = dimensions[1];
            if (height == sceneRasterHeight && width == sceneRasterWidth) {
                final String name = variable.getShortName();
                String safeName = (name != null && name.contains("-")) ? "'" + name + "'" : name;

                final int dataType = getProductDataType(variable);

                if (!product.containsBand(name)) {

                    band = new Band(name, dataType, width, height);

                    product.addBand(band);

                    try {
                        Attribute fillValue = variable.findAttribute("_FillValue");
                        if (fillValue == null) {
                            fillValue = variable.findAttribute("bad_value_scaled");
                        }
                        band.setNoDataValue((double) fillValue.getNumericValue().floatValue());
                        band.setNoDataValueUsed(true);
                    } catch (Exception ignored) {
                    }

                    final List<Attribute> list = variable.getAttributes();
                    double[] validMinMax = {0.0, 0.0};
                    for (Attribute attribute : list) {
                        final String attribName = attribute.getShortName();
                        if ("units".equals(attribName)) {
                            band.setUnit(attribute.getStringValue());
                        } else if ("long_name".equals(attribName)) {
                            band.setDescription(attribute.getStringValue());
                        } else if ("slope".equals(attribName)) {
                            band.setScalingFactor(attribute.getNumericValue(0).doubleValue());
                        } else if ("intercept".equals(attribName)) {
                            band.setScalingOffset(attribute.getNumericValue(0).doubleValue());
                        } else if ("scale_factor".equals(attribName)) {
                            band.setScalingFactor(attribute.getNumericValue(0).doubleValue());
                        } else if ("add_offset".equals(attribName)) {
                            band.setScalingOffset(attribute.getNumericValue(0).doubleValue());
                        } else if (attribName.startsWith("valid_")) {
                            if ("valid_min".equals(attribName)) {
                                if (attribute.getDataType().isUnsigned()) {
                                    validMinMax[0] = getUShortAttribute(attribute);
                                } else {
                                    validMinMax[0] = attribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_max".equals(attribName)) {
                                if (attribute.getDataType().isUnsigned()) {
                                    validMinMax[1] = getUShortAttribute(attribute);
                                } else {
                                    validMinMax[1] = attribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_range".equals(attribName)) {
                                validMinMax[0] = attribute.getNumericValue(0).doubleValue();
                                validMinMax[1] = attribute.getNumericValue(1).doubleValue();
                            }
                        }
                    }
                    if (validMinMax[0] != validMinMax[1]) {
                        String validExp;

                        if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                            validExp = format("%s >= %.05f && %s <= %.05f", safeName, validMinMax[0], safeName, validMinMax[1]);

                        } else {
                            double[] minmax = {0.0, 0.0};
                            minmax[0] = validMinMax[0];
                            minmax[1] = validMinMax[1];

                            if (band.getScalingFactor() != 1.0) {
                                minmax[0] *= band.getScalingFactor();
                                minmax[1] *= band.getScalingFactor();
                            }
                            if (band.getScalingOffset() != 0.0) {
                                minmax[0] += band.getScalingOffset();
                                minmax[1] += band.getScalingOffset();
                            }

                            validExp = format("%s >= %.05f && %s <= %.05f", safeName, minmax[0], safeName, minmax[1]);
                        }
                        band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                    }
                } else {
                    logger.log(Level.WARNING, "The Product '" + product.getName() + "' contains duplicate" +
                            "bands with the name '" + name + "', one will be ignored.");
                }
            }
        }
        return band;
    }

    public void addGlobalMetadata(Product product) {
        final MetadataElement globalElement = new MetadataElement("Global_Attributes");
        addAttributesToElement(globalAttributes, globalElement);

        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(globalElement);
    }

    public void addInputParamMetadata(Product product) throws ProductIOException {

        Variable inputParams = ncFile.findVariable("Input_Parameters");
        if (inputParams != null) {
            final MetadataElement inputParamsMeta = new MetadataElement("Input_Parameters");
            Array array;
            try {
                array = inputParams.read();
            } catch (IOException e) {
                throw new ProductIOException(e.getMessage(), e);
            }

            String[] lines = array.toString().split("\n");
            for (String line : lines) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    final String name = parts[0].trim();
                    final String value = parts[1].trim();
                    final ProductData data = ProductData.createInstance(ProductData.TYPE_ASCII, value);
                    final MetadataAttribute attribute = new MetadataAttribute(name, data, true);
                    inputParamsMeta.addAttribute(attribute);

                }
            }

            final MetadataElement metadataRoot = product.getMetadataRoot();
            metadataRoot.addElement(inputParamsMeta);

        }
    }

    public void addScientificMetadata(Product product) throws ProductIOException {

        Group group = ncFile.findGroup("Scan-Line_Attributes");
        if (group == null) {
            group = ncFile.findGroup("scan_line_attributes");
        }
        if (group != null) {
            final MetadataElement scanLineAttrib = getMetadataElementSave(product, "Scan_Line_Attributes");
            handleMetadataGroup(group, scanLineAttrib);
        }

        group = ncFile.findGroup("Sensor_Band_Parameters");
        if (group == null) {
            group = ncFile.findGroup("sensor_band_parameters");
        }
        if (group != null) {
            final MetadataElement sensorBandParam = getMetadataElementSave(product, "Sensor_Band_Parameters");
            handleMetadataGroup(group, sensorBandParam);
        }
    }

    public void addBandMetadata(Product product) throws ProductIOException {
        Group group = ncFile.findGroup("geophysical_data");
        if (group == null) {
            group = ncFile.findGroup("Geophysical_Data");
        }
        if (productReader.getProductType() == SeadasProductReader.ProductType.Level2_Aquarius) {
            group = ncFile.findGroup("Aquarius_Data");
        }
        if (productReader.getProductType() == SeadasProductReader.ProductType.Level1B_HICO) {
            group = ncFile.findGroup("products");
        }
        if (productReader.getProductType() == SeadasProductReader.ProductType.Level1C_Pace) {
            group = ncFile.findGroup("observation_data");
        }

        if (group != null) {
            final MetadataElement bandAttributes = new MetadataElement("Band_Attributes");
            List<Variable> variables = group.getVariables();
            for (Variable variable : variables) {
                final String name = variable.getShortName();
                final MetadataElement sdsElement = new MetadataElement(name);
                final int dataType = getProductDataType(variable);
                final MetadataAttribute prodtypeattr = new MetadataAttribute("data_type", dataType);

                sdsElement.addAttribute(prodtypeattr);
                bandAttributes.addElement(sdsElement);

                final List<Attribute> list = variable.getAttributes();
                for (Attribute varAttribute : list) {
                    addAttributeToElement(sdsElement, varAttribute);
                }
            }
            final MetadataElement metadataRoot = product.getMetadataRoot();
            metadataRoot.addElement(bandAttributes);
        }
    }

    public void computeLatLonBandData(int height, int width, Band latBand, Band lonBand,
                                      final float[] latRawData, final float[] lonRawData,
                                      final int[] colPoints) {

        float[] latFloats = new float[height * width];
        float[] lonFloats = new float[height * width];
        final int rawWidth = colPoints.length;

        int colPointIdx = 0;
        int p1 = colPoints[colPointIdx] - 1;
        int p2 = colPoints[++colPointIdx] - 1;

        for (int x = 0; x < width; x++) {
            if (x == p2 && colPointIdx < rawWidth - 1) {
                p1 = p2;
                p2 = colPoints[++colPointIdx] - 1;
            }
            final int steps = p2 - p1;
            final double step = 1.0 / steps;
            final double weight = step * (x - p1);
            for (int y = 0; y < height; y++) {
                final int rawPos2 = y * rawWidth + colPointIdx;
                final int rawPos1 = rawPos2 - 1;
                final int pos = y * width + x;
                latFloats[pos] = computeGeoPixel(latRawData[rawPos1], latRawData[rawPos2], weight);
                lonFloats[pos] = computeGeoPixel(lonRawData[rawPos1], lonRawData[rawPos2], weight);
            }
        }

        latBand.setDataElems(latFloats);
        lonBand.setDataElems(lonFloats);
    }

    public float computeGeoPixel(final float a, final float b, final double weight) {
        if ((b - a) > 180) {
            final float b2 = b - 360;
            final double v = a + (b2 - a) * weight;
            if (v >= -180) {
                return (float) v;
            } else {
                return (float) (v + 360);
            }
        } else {
            return (float) (a + (b - a) * weight);
        }
    }

    public boolean getDefaultFlip() throws ProductIOException {
        boolean startNodeAscending = false;
        boolean endNodeAscending = false;
        try {
            Attribute start_node = findAttribute("Start_Node");
            if (start_node == null) {
                start_node = findAttribute("startDirection");
            }
            String startAttr = start_node.getStringValue();

            if (startAttr != null) {
                startNodeAscending = startAttr.equalsIgnoreCase("Ascending");
            }
            Attribute end_node = findAttribute("End_Node");
            if (end_node == null) {
                end_node = findAttribute("startDirection");
            }
            String endAttr = end_node.getStringValue();

            if (endAttr != null) {
                endNodeAscending = endAttr.equalsIgnoreCase("Ascending");
            }

        } catch (Exception ignored) {
        }

        return (startNodeAscending && endNodeAscending);
    }

    protected static HashMap<String, String> readTwoColumnTable(String resourceName) {
        final InputStream stream = SeadasProductReader.class.getResourceAsStream(resourceName);
        if (stream != null) {
            try {
                HashMap<String, String> validExpressionMap = new HashMap<String, String>(32);
                final CsvReader csvReader = new CsvReader(new InputStreamReader(stream), new char[]{';'});
                final List<String[]> table = csvReader.readStringRecords();
                for (String[] strings : table) {
                    if (strings.length == 2) {
                        validExpressionMap.put(strings[0], strings[1]);
                    }
                }
                return validExpressionMap;
            } catch (IOException e) {
                // ?
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ok
                }
            }
        }
        return new HashMap<String, String>(0);
    }

    public static void reverse(float[] data) {
        final int n = data.length;
        for (int i = 0; i < n / 2; i++) {
            float temp = data[i];
            data[i] = data[n - i - 1];
            data[n - i - 1] = temp;
        }
    }

    public Attribute findAttribute(String name, List<Attribute> attributesList) {
        for (Attribute a : attributesList) {
            if (name.equalsIgnoreCase(a.getShortName())) {
                return a;
            }
        }
        return null;
    }

    public Attribute findAttribute(String name) {
        return findAttribute(name, globalAttributes);
    }

    public String getStringAttribute(String key, List<Attribute> attributeList) throws ProductIOException {
        Attribute attribute = findAttribute(key, attributeList);
        if (attribute == null || attribute.getLength() != 1) {
            return null;
//            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getStringValue().trim();
        }
    }

    public String getStringAttribute(String key) throws ProductIOException {
        return getStringAttribute(key, globalAttributes);
    }

    public int getIntAttribute(String key, List<Attribute> globalAttributes) throws ProductIOException {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute == null) {
            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getNumericValue(0).intValue();
        }
    }

    public int getUShortAttribute(Attribute attribute) {
        return (attribute.getNumericValue(0).shortValue() & 0xffff);
    }

    public int getIntAttribute(String key) throws ProductIOException {
        return getIntAttribute(key, globalAttributes);
    }

    public float getFloatAttribute(String key, List<Attribute> globalAttributes) throws ProductIOException {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute == null) {
            throw new ProductIOException("Global attribute '" + key + "' is missing.");
        } else {
            return attribute.getNumericValue(0).floatValue();
        }
    }

    public float getFloatAttribute(String key) throws ProductIOException {
        return getFloatAttribute(key, globalAttributes);
    }

    private ProductData.UTC getUTCAttribute(String key, List<Attribute> globalAttributes) {
        Attribute attribute = findAttribute(key, globalAttributes);
        if (attribute != null) {
            String timeString = attribute.getStringValue().trim();
            return parseUtcDate(timeString);
        }
        return null;
    }

    static ProductData.UTC parseUtcDate(String timeString) {
        try {
            if (timeString.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")) {
                // ISO
                return ProductData.UTC.parse(timeString, "yyyy-MM-dd'T'HH:mm:ss'Z'");
            } else if (timeString.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z")) {
                // ISO with micros
                timeString = timeString.substring(0, timeString.length() - 1);
                return ProductData.UTC.parse(timeString, "yyyy-MM-dd'T'HH:mm:ss");
            } else if (timeString.matches("\\d{4}\\d{2}\\d{2}T\\d{6}Z")) {
                // ISO no-punctation
                return ProductData.UTC.parse(timeString, "yyyyMMdd'T'HHmmss'Z'");
            } else if (timeString.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}")) {
                // MODIS
                return ProductData.UTC.parse(timeString, "yyyy-MM-dd HH:mm:ss");
            } else if (timeString.matches("\\d{4}\\d{2}\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}")) {
                // OCTS
                return ProductData.UTC.parse(timeString, "yyyyMMdd HH:mm:ss");
            } else if (timeString.matches("\\d{4}\\d{3}\\d{2}\\d{2}\\d{2}\\d{3}")) {
                Date date = ProductData.UTC.createDateFormat("yyyyDDDHHmmssSSS").parse(timeString);
                String milliSeconds = timeString.substring(timeString.length() - 3);
                return ProductData.UTC.create(date, Long.parseLong(milliSeconds) * 1000);
            }
        } catch (ParseException ignored) {
        }
        return null;
    }

    public ProductData.UTC getUTCAttribute(String key) {
        return getUTCAttribute(key, globalAttributes);
    }

    private MetadataElement getMetadataElementSave(Product product, String name) {
        final MetadataElement metadataElement = product.getMetadataRoot().getElement(name);
        final MetadataElement namedElem;
        if (metadataElement == null) {
            namedElem = new MetadataElement(name);
            product.getMetadataRoot().addElement(namedElem);
        } else {
            namedElem = metadataElement;
        }
        return namedElem;
    }

    private void handleMetadataGroup(Group group, MetadataElement metadataElement) throws ProductIOException {
        List<Variable> variables = group.getVariables();
        for (Variable variable : variables) {
            final String name = variable.getShortName();
            final int dataType = getProductDataType(variable);
            Array array;
            try {
                array = variable.read();
            } catch (IOException e) {
                throw new ProductIOException(e.getMessage(), e);
            }
            final ProductData data = ProductData.createInstance(dataType, array.getStorage());
            final MetadataAttribute attribute = new MetadataAttribute("data", data, true);

            final MetadataElement sdsElement = new MetadataElement(name);
            sdsElement.addAttribute(attribute);
            metadataElement.addElement(sdsElement);

            final List<Attribute> list = variable.getAttributes();
            for (Attribute hdfAttribute : list) {
                final String attribName = hdfAttribute.getShortName();
                if ("units".equals(attribName)) {
                    attribute.setUnit(hdfAttribute.getStringValue());
                } else if ("long_name".equals(attribName)) {
                    attribute.setDescription(hdfAttribute.getStringValue());
                } else {
                    addAttributeToElement(sdsElement, hdfAttribute);
                }
            }
        }
    }

    protected void addAttributesToElement(List<Attribute> globalAttributes, final MetadataElement element) {
        for (Attribute attribute : globalAttributes) {
            //if (attribute.getName().contains("EV")) {
            if (attribute.getShortName().matches(".*(EV|Value|Bad|Nois|Electronics|Dead|Detector).*")) {
            } else {
                addAttributeToElement(element, attribute);
            }
        }
    }

    protected void addAttributeToElement(final MetadataElement element, final Attribute attribute) {
        final MetadataAttribute metadataAttribute = attributeToMetadata(attribute);
        element.addAttribute(metadataAttribute);
    }

    MetadataAttribute attributeToMetadata(Attribute attribute) {
        final int productDataType = getProductDataType(attribute.getDataType(), false, false);
        if (productDataType != -1) {
            ProductData productData;
            if (attribute.isString()) {
                productData = ProductData.createInstance(attribute.getStringValue());
            } else if (attribute.isArray()) {
                productData = ProductData.createInstance(productDataType, attribute.getLength());
                productData.setElems(attribute.getValues().getStorage());
            } else if (attribute.getValues() == null) {
                productData = ProductData.createInstance(" ");
            } else {
                productData = ProductData.createInstance(productDataType, 1);
                productData.setElems(attribute.getValues().getStorage());
            }
            return new MetadataAttribute(attribute.getShortName(), productData, true);
        }
        return null;
    }

    public static int getProductDataType(Variable variable) {
        return getProductDataType(variable.getDataType(), variable.getDataType().isUnsigned(), true);
    }

    public static int getProductDataType(DataType dataType, boolean unsigned, boolean rasterDataOnly) {
        if (dataType == DataType.BYTE) {
            return unsigned ? ProductData.TYPE_UINT8 : ProductData.TYPE_INT8;
        } else if (dataType == DataType.UBYTE) {
            return ProductData.TYPE_UINT8;
        } else if (dataType == DataType.SHORT) {
            return unsigned ? ProductData.TYPE_UINT16 : ProductData.TYPE_INT16;
        } else if (dataType == DataType.USHORT) {
            return ProductData.TYPE_UINT16;
        } else if (dataType == DataType.INT) {
            return unsigned ? ProductData.TYPE_UINT32 : ProductData.TYPE_INT32;
        } else if (dataType == DataType.UINT) {
            return ProductData.TYPE_UINT32;
        } else if (dataType == DataType.FLOAT) {
            return ProductData.TYPE_FLOAT32;
        } else if (dataType == DataType.DOUBLE) {
            return ProductData.TYPE_FLOAT64;
        } else if (!rasterDataOnly) {
            if (dataType == DataType.CHAR) {
                // return ProductData.TYPE_ASCII; TODO - handle this case
            } else if (dataType == DataType.STRING) {
                return ProductData.TYPE_ASCII;
            }
        } else if (dataType == DataType.CHAR) {
            return ProductData.TYPE_UINT8;
        }
        return -1;
    }

    public int convertToFlagMask(String name) {
        if (name.matches("f\\d\\d_name")) {
            final String number = name.substring(1, 3);
            final int i = Integer.parseInt(number) - 1;
            if (i >= 0) {
                return 1 << i;
            }
        }
        return 0;
    }

    public ProductData readData(Variable variable) throws ProductIOException {
        final int dataType = getProductDataType(variable);
        Array array;
        try {
            array = variable.read();

        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
        return ProductData.createInstance(dataType, array.getStorage());
    }

    float[] flatten2DimArray(float[][] twoDimArray) {
        // Converts an array of two dimensions into a single dimension array row by row.
        float[] flatArray = new float[twoDimArray.length * twoDimArray[0].length];
        for (int row = 0; row < twoDimArray.length; row++) {
            int offset = row * twoDimArray[row].length;
            arraycopy(twoDimArray[row], 0, flatArray, offset, twoDimArray[row].length);
        }
        return flatArray;
    }

    protected synchronized void invalidateLines(SkipBadNav skipBadNav, Variable latitude) throws IOException {
        final int[] shape = latitude.getShape();
        try {
            int lineCount = shape[0];
            final int[] start = new int[]{0, 0};
            final int[] count = new int[]{shape[0], shape[1]};
            Section section = new Section(start, count);
            Array array;
            synchronized (ncFile) {
                array = latitude.read(section);
            }
            for (int i = 0; i < lineCount; i++) {
                boolean find_valstart = false;
                for (int j = 0; j < shape[1]; j++) {
                    float valstart = array.getFloat(i * shape[1] + j);
                    if (!skipBadNav.isBadNav(valstart)) {
                        find_valstart = true;
                        break;
                    }
                }
                if (!find_valstart) {
                    leadLineSkip++;
                } else {
                    break;
                }
            }
            for (int i = lineCount; i-- > 0; ) {
                boolean find_valstart = false;
                for (int j = 0; j < shape[1]; j++) {
                    float valstart = array.getFloat(i * shape[1] + j);
                    if (!skipBadNav.isBadNav(valstart)) {
                        find_valstart = true;
                        break;
                    }
                }
                if (!find_valstart) {
                    tailLineSkip++;
                } else {
                    break;
                }
            }
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }



    // ATMFAIL

    private boolean is_ATMFAIL_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_DEFAULT);
    }

    private Color get_ATMFAIL_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_DEFAULT);
    }

    private double get_ATMFAIL_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_DEFAULT);
    }
    
    
    
    // LAND

    private Color get_LAND_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_DEFAULT);
    }


    private boolean is_LAND_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_DEFAULT);
    }

    private double get_LAND_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_DEFAULT);
    }


    // PRODWARN

    private boolean is_PRODWARN_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_DEFAULT);
    }

    private Color get_PRODWARN_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_DEFAULT);
    }

    private double get_PRODWARN_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_DEFAULT);
    }



    // HIGLINT

    private boolean is_HIGLINT_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_DEFAULT);
    }

    private Color get_HIGLINT_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_DEFAULT);
    }

    private double get_HIGLINT_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_DEFAULT);
    }



    // HILT

    private boolean is_HILT_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_DEFAULT);
    }

    private Color get_HILT_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_DEFAULT);
    }

    private double get_HILT_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_DEFAULT);
    }



    // HISATZEN

    private boolean is_HISATZEN_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_DEFAULT);
    }

    private Color get_HISATZEN_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_DEFAULT);
    }

    private double get_HISATZEN_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_DEFAULT);
    }



    // COASTZ

    private boolean is_COASTZ_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_DEFAULT);
    }

    private Color get_COASTZ_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_DEFAULT);
    }

    private double get_COASTZ_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_DEFAULT);
    }





    // STRAYLIGHT

    private boolean is_STRAYLIGHT_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_DEFAULT);
    }

    private Color get_STRAYLIGHT_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_DEFAULT);
    }

    private double get_STRAYLIGHT_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_DEFAULT);
    }



    // CLDICE
    
    private boolean is_CLDICE_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_DEFAULT);
    }

    private Color get_CLDICE_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_DEFAULT);
    }

    private double get_CLDICE_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_DEFAULT);
    }




    // COCCOLITH

    private Color get_COCCOLITH_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_DEFAULT);
    }


    private boolean is_COCCOLITH_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_DEFAULT);
    }

    private double get_COCCOLITH_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_DEFAULT);
    }
    
    
    
    // TURBIDW

    private Color get_TURBIDW_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_DEFAULT);
    }


    private boolean is_TURBIDW_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_DEFAULT);
    }

    private double get_TURBIDW_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_DEFAULT);
    }
    
    
    
    // HISOLZEN

    private Color get_HISOLZEN_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_DEFAULT);
    }


    private boolean is_HISOLZEN_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_DEFAULT);
    }

    private double get_HISOLZEN_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_DEFAULT);
    }
    
    
    
    
    // LOWLW

    private Color get_LOWLW_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_DEFAULT);
    }


    private boolean is_LOWLW_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_DEFAULT);
    }

    private double get_LOWLW_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // CHLFAIL

    private Color get_CHLFAIL_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_DEFAULT);
    }


    private boolean is_CHLFAIL_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_DEFAULT);
    }

    private double get_CHLFAIL_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_DEFAULT);
    }
    
    
    
    // NAVWARN

    private Color get_NAVWARN_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_DEFAULT);
    }


    private boolean is_NAVWARN_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_DEFAULT);
    }

    private double get_NAVWARN_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // ABSAER

    private Color get_ABSAER_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_DEFAULT);
    }


    private boolean is_ABSAER_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_DEFAULT);
    }

    private double get_ABSAER_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_DEFAULT);
    }
    
    
    
    
    // MAXAERITER

    private Color get_MAXAERITER_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_DEFAULT);
    }


    private boolean is_MAXAERITER_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_DEFAULT);
    }

    private double get_MAXAERITER_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // MODGLINT

    private Color get_MODGLINT_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_DEFAULT);
    }


    private boolean is_MODGLINT_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_DEFAULT);
    }

    private double get_MODGLINT_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // CHLWARN

    private Color get_CHLWARN_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_DEFAULT);
    }


    private boolean is_CHLWARN_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_DEFAULT);
    }

    private double get_CHLWARN_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // ATMWARN

    private Color get_ATMWARN_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_DEFAULT);
    }


    private boolean is_ATMWARN_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_DEFAULT);
    }

    private double get_ATMWARN_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_DEFAULT);
    }
    
    
    

    
    // SEAICE

    private Color get_SEAICE_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_DEFAULT);
    }


    private boolean is_SEAICE_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_DEFAULT);
    }

    private double get_SEAICE_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // NAVFAIL

    private Color get_NAVFAIL_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_DEFAULT);
    }


    private boolean is_NAVFAIL_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_DEFAULT);
    }

    private double get_NAVFAIL_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // FILTER

    private Color get_FILTER_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_DEFAULT);
    }


    private boolean is_FILTER_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_DEFAULT);
    }

    private double get_FILTER_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_DEFAULT);
    }
    
    
    
    
    // BOWTIEDEL

    private Color get_BOWTIEDEL_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_DEFAULT);
    }


    private boolean is_BOWTIEDEL_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_DEFAULT);
    }

    private double get_BOWTIEDEL_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // HIPOL

    private Color get_HIPOL_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_DEFAULT);
    }


    private boolean is_HIPOL_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_DEFAULT);
    }

    private double get_HIPOL_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_DEFAULT);
    }
    
    
    

    // PRODFAIL

    private Color get_PRODFAIL_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_DEFAULT);
    }


    private boolean is_PRODFAIL_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_DEFAULT);
    }

    private double get_PRODFAIL_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_DEFAULT);
    }




    private String getMaskSort() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_SORT_KEY, SeadasReaderDefaults.PROPERTY_MASK_SORT_DEFAULT);
    }


    private boolean isMaskSort() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_DEFAULT);
    }




    // Composite1
    
    private boolean isComposite1MaskInclude() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_DEFAULT);
    }
    
    private boolean isComposite1MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_DEFAULT);
    }
    
    private String getComposite1MaskName() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        String composite1MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT);

        // don't let mask name be same as any of the flags/composites
        if (composite1MaskName != null) {
            composite1MaskName = validateCompositeFlagsName(composite1MaskName, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY);
        }

        return composite1MaskName;
    }
    
    private String getComposite1Expression() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        String flags = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_DEFAULT);
        
        return getCompositeFlagsExpression(flags);
    }
    
    private double getComposite1MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_DEFAULT);
    }

    private Color getComposite1MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_DEFAULT);
    }





    // Composite2

    private boolean isComposite2MaskInclude() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_DEFAULT);
    }

    private boolean isComposite2MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_DEFAULT);
    }

    private String getComposite2MaskName() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        String composite2MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT);

        // don't let mask name be same as any of the flags/composites
        if (composite2MaskName != null) {
            composite2MaskName = validateCompositeFlagsName(composite2MaskName, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY);
        }

        return composite2MaskName;
    }

    private String getComposite2Expression() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        String flags = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_DEFAULT);

        return getCompositeFlagsExpression(flags);
    }

    private double getComposite2MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_DEFAULT);
    }

    private Color getComposite2MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_DEFAULT);
    }






    // Composite3

    private boolean isComposite3MaskInclude() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_DEFAULT);
    }

    private boolean isComposite3MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_DEFAULT);
    }

    private String getComposite3MaskName() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        String composite3MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_DEFAULT);

        // don't let mask name be same as any of the flags/composites
        if (composite3MaskName != null) {
            composite3MaskName = validateCompositeFlagsName(composite3MaskName, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY);
        }

        return composite3MaskName;
    }

    private String getComposite3Expression() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        String flags = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_DEFAULT);

        return getCompositeFlagsExpression(flags);
    }

    private double getComposite3MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_DEFAULT);
    }

    private Color getComposite3MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_DEFAULT);
    }

    



    private String validateCompositeFlagsName(String compositeMaskName, String propertyKey) {
        // don't let mask name be same as any of the flags
        if (compositeMaskName != null) {
            for (String validFlag : validFlags()) {
                if (compositeMaskName.equals(validFlag)) {
                    return null;
                }
            }
        }

        if (propertyKey != null) {
            final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();

            // don't let obvious name conflict occur which could result in duplicate masks and confusion
            // do this hierarchically (for instance such that Composite1 still gets set but Composite2 doesn't if they are the same name)
//            if (propertyKey.equals(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY)) {
//                String composite2MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT);
//                if (composite2MaskName != null && composite2MaskName.equals(compositeMaskName)) {
//                    return null;
//                }
//                String composite3MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_DEFAULT);
//                if (composite3MaskName != null && composite3MaskName.equals(compositeMaskName)) {
//                    return null;
//                }
//            }
            if (propertyKey.equals(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY)) {
                String composite1MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT);
                if (composite1MaskName != null && composite1MaskName.equals(compositeMaskName)) {
                    return null;
                }
//                String composite3MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_DEFAULT);
//                if (composite3MaskName != null && composite3MaskName.equals(compositeMaskName)) {
//                    return null;
//                }
            }
            if (propertyKey.equals(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY)) {
                String composite2MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT);
                if (composite2MaskName != null && composite2MaskName.equals(compositeMaskName)) {
                    return null;
                }
                String composite1MaskName = preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT);
                if (composite1MaskName != null && composite1MaskName.equals(compositeMaskName)) {
                    return null;
                }
            }
        }
        
        return compositeMaskName;
    }
    
    private String getCompositeFlagsExpression(String flags) {

        String[] flagsArray = flags.split("\\s+|,");

        for (int i=0; i < flagsArray.length; i++) {
            if (flagsArray[i].contains("l2_flags.")) {
                flagsArray[i] = flagsArray[i].replace("l2_flags.", "");
            }
        }
        ArrayList<String> flagsCompositeArray = getValidFlagsComposite(flagsArray);

        if (flagsCompositeArray == null || flagsCompositeArray.size() == 0) {
            return null;
        }

        String expression = String.join(" || ", flagsCompositeArray);

        return expression;
    }
    
    private ArrayList<String> getValidFlagsComposite(String[] flagsArray) {
        ArrayList<String> validFlagComposite = null;

        if (flagsArray != null) {
            validFlagComposite = new ArrayList<String>();

            for (String flag : flagsArray) {
                if (flag != null) {
                    flag = flag.trim();
                    boolean flagIsComplement = false;
                    if (flag.startsWith("~")) {
                        flag = flag.replace("~", "");
                        flagIsComplement = true;
                    }

                    for (String validFlag : validFlags()) {
                        if (validFlag != null) {
                                if (flag.equals(validFlag)) {
                                    if (flagIsComplement) {
                                        validFlagComposite.add("!l2_flags." + flag);
                                    } else {
                                        validFlagComposite.add("l2_flags." + flag);
                                    }
                                    continue;
                                }
                        }
                    }
                }
            }
        }

        return validFlagComposite;
    }

    private boolean isValidFlag(String flagMask) {
        
        if (flagMask == null) {
            return false;
        }

        flagMask = flagMask.trim();
        
        for (String validFlag : validFlags()) {
            if (validFlag != null) {
                if (flagMask.equals(validFlag)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    
    private ArrayList<String> validFlags() {
        ArrayList<String> validFlags = new ArrayList();
        validFlags.add("ATMFAIL");
        validFlags.add("LAND");
        validFlags.add("PRODWARN");
        validFlags.add("HIGLINT");
        validFlags.add("HILT");
        validFlags.add("HISATZEN");
        validFlags.add("COASTZ");
        validFlags.add("SPARE8");
        validFlags.add("STRAYLIGHT");
        validFlags.add("CLDICE");
        validFlags.add("COCCOLITH");
        validFlags.add("TURBIDW");
        validFlags.add("HISOLZEN");
        validFlags.add("SPARE14");
        validFlags.add("LOWLW");
        validFlags.add("CHLFAIL");
        validFlags.add("NAVWARN");
        validFlags.add("ABSAER");
        validFlags.add("SPARE19");
        validFlags.add("MAXAERITER");
        validFlags.add("MODGLINT");
        validFlags.add("CHLWARN");
        validFlags.add("ATMWARN");
        validFlags.add("SPARE24");
        validFlags.add("SEAICE");
        validFlags.add("NAVFAIL");
        validFlags.add("FILTER");
        validFlags.add("SPARE28");
        validFlags.add("BOWTIEDEL");
        validFlags.add("HIPOL");
        validFlags.add("PRODFAIL");
        validFlags.add("SPARE32");

        return validFlags;
    }

    

    // Water

    private Color get_Water_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_DEFAULT);
    }


    private boolean is_Water_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_DEFAULT);
    }

    private double get_Water_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_DEFAULT);
    }




    // SPARE

    private boolean is_SPARE_MaskInclude() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_DEFAULT);
    }
    
    private Color get_SPARE_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_DEFAULT);
    }


    private double get_SPARE_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_DEFAULT);
    }




    private interface SkipBadNav {

        boolean isBadNav(double value);
    }

}
