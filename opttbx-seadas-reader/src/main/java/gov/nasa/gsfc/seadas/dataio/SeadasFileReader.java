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
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.PropertyMap;
import org.esa.snap.core.util.ResourceInstaller;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.CsvReader;
import org.esa.snap.rcp.SnapApp;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
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
    //AUG 2024 - Daniel Knowles - added PACE OCI actual center wavelengths lookup in resources


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

    protected String[] flagNames = null;
    protected String flagMeanings = null;


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



    static String getFlagDescription(String flagName) {
        if (flagName == null) {
            return null;
        }

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

        String GEOREGION_Description = "User supplied geolocation region";

        String composite1Description = "Composite1 Mask (see preferences to set)";
        String composite2Description = "Composite2 Mask (see preferences to set)";
        String composite3Description = "Composite3 Mask (see preferences to set)";
        String Water_Description = "Not land (l2_flags.LAND)";

        String flagDescription = null;
        switch (flagName) {
            case "ATMFAIL":
                flagDescription = ATMFAIL_Description;
                break;
            case SeadasReaderDefaults.PROPERTY_MASK_LAND_NAME:
                flagDescription = LAND_Description;
                break;
            case "PRODWARN":
                flagDescription = PRODWARN_Description;
                break;
            case "HIGLINT":
                flagDescription = HIGLINT_Description;
                break;
            case "HILT":
                flagDescription = HILT_Description;
                break;
            case "HISATZEN":
                flagDescription = HISATZEN_Description;
                break;
            case "COASTZ":
                flagDescription = COASTZ_Description;
                break;
            case "STRAYLIGHT":
                flagDescription = STRAYLIGHT_Description;
                break;
            case "CLDICE":
                flagDescription = CLDICE_Description;
                break;
            case "COCCOLITH":
                flagDescription = COCCOLITH_Description;
                break;
            case "TURBIDW":
                flagDescription = TURBIDW_Description;
                break;
            case "HISOLZEN":
                flagDescription = HISOLZEN_Description;
                break;
            case "LOWLW":
                flagDescription = LOWLW_Description;
                break;
            case "CHLFAIL":
                flagDescription = CHLFAIL_Description;
                break;
            case "NAVWARN":
                flagDescription = NAVWARN_Description;
                break;
            case "ABSAER":
                flagDescription = ABSAER_Description;
                break;
            case "MAXAERITER":
                flagDescription = MAXAERITER_Description;
                break;
            case "MODGLINT":
                flagDescription = MODGLINT_Description;
                break;
            case "CHLWARN":
                flagDescription = CHLWARN_Description;
                break;
            case "ATMWARN":
                flagDescription = ATMWARN_Description;
                break;
            case "SEAICE":
                flagDescription = SEAICE_Description;
                break;
            case "NAVFAIL":
                flagDescription = NAVFAIL_Description;
                break;
            case "FILTER":
                flagDescription = FILTER_Description;
                break;
            case "BOWTIEDEL":
                flagDescription = BOWTIEDEL_Description;
                break;
            case "HIPOL":
                flagDescription = HIPOL_Description;
                break;
            case "PRODFAIL":
                flagDescription = PRODFAIL_Description;
                break;
            case "GEOREGION":
                flagDescription = GEOREGION_Description;
                break;
        }


        if (flagDescription == null) {
            if (flagName.startsWith("SPARE")) {
                flagDescription = "Unused spare flag";
            }
        }

        if (flagDescription == null) {
            flagDescription = flagName;
        }

        return flagDescription;

    }

    private void addFlagMask(Product product, String flagName, FlagCoding flagCoding) {
        switch (flagName) {
            case SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_NAME, get_ATMFAIL_MaskColor(), get_ATMFAIL_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_LAND_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_LAND_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_LAND_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_LAND_NAME, get_LAND_MaskColor(), get_LAND_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_NAME, get_PRODWARN_MaskColor(), get_PRODWARN_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_NAME, get_HIGLINT_MaskColor(), get_HIGLINT_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_HILT_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_HILT_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HILT_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_HILT_NAME, get_HILT_MaskColor(), get_HILT_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_NAME, get_HISATZEN_MaskColor(), get_HISATZEN_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_COASTZ_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_COASTZ_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_COASTZ_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_NAME, get_COASTZ_MaskColor(), get_COASTZ_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_NAME, get_STRAYLIGHT_MaskColor(), get_STRAYLIGHT_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_CLDICE_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_CLDICE_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_CLDICE_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_NAME, get_CLDICE_MaskColor(), get_CLDICE_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_NAME, get_COCCOLITH_MaskColor(), get_COCCOLITH_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_NAME, get_TURBIDW_MaskColor(), get_TURBIDW_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_NAME, get_HISOLZEN_MaskColor(), get_HISOLZEN_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_LOWLW_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_LOWLW_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_LOWLW_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_NAME, get_LOWLW_MaskColor(), get_LOWLW_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_NAME, get_CHLFAIL_MaskColor(), get_CHLFAIL_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_NAME, get_NAVWARN_MaskColor(), get_NAVWARN_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_ABSAER_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_ABSAER_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_ABSAER_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_NAME, get_ABSAER_MaskColor(), get_ABSAER_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_NAME, get_MAXAERITER_MaskColor(), get_MAXAERITER_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_NAME, get_MODGLINT_MaskColor(), get_MODGLINT_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_NAME, get_CHLWARN_MaskColor(), get_CHLWARN_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_NAME, get_ATMWARN_MaskColor(), get_ATMWARN_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_SEAICE_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_SEAICE_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_SEAICE_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_NAME, get_SEAICE_MaskColor(), get_SEAICE_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_NAME, get_NAVFAIL_MaskColor(), get_NAVFAIL_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_FILTER_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_FILTER_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_FILTER_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_FILTER_NAME, get_FILTER_MaskColor(), get_FILTER_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_NAME, get_BOWTIEDEL_MaskColor(), get_BOWTIEDEL_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_HIPOL_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_HIPOL_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HIPOL_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_NAME, get_HIPOL_MaskColor(), get_HIPOL_MaskTransparency());
                }
                break;

            case SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_NAME, get_PRODFAIL_MaskColor(), get_PRODFAIL_MaskTransparency());
                }
                break;


            case SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_NAME:
                if (flagCoding.getFlag(SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_NAME) != null && !product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_NAME)) {
                    createMask(product, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_NAME, get_GEOREGION_MaskColor(), get_GEOREGION_MaskTransparency());
                }
                break;

            default:
                if (flagName.startsWith("SPARE")) {
                    if (is_SPARE_MaskInclude()) {
                        if (flagCoding.getFlag(flagName) != null && !product.getMaskGroup().contains(flagName)) {
                            createSPAREMask(product, flagName);
                        }
                    }
                } else {
                    if (flagCoding.getFlag(flagName) != null && !product.getMaskGroup().contains(flagName)) {
                        createMaskMisc(product, flagName);
                    }
                }
        }

    }

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


            try {
                MetadataElement metadataElementBand = product.getMetadataRoot().getElement("Band_Attributes");
                if (metadataElementBand != null) {
                    MetadataElement metadataElementL2Flags = metadataElementBand.getElement("l2_flags");
                    if (metadataElementL2Flags != null) {
                        setFlagMeaningsAndNames(product, metadataElementL2Flags);
                    }
                }
            } catch (Exception ignore) {
            }


            Band QFBand = product.getBand("l2_flags");
            if (QFBand != null) {

                String composite1Description = "Composite1 Mask (see preferences to set)";
                String composite2Description = "Composite2 Mask (see preferences to set)";
                String composite3Description = "Composite3 Mask (see preferences to set)";
                String Water_Description = "Not land (l2_flags.LAND)";


                FlagCoding flagCoding = new FlagCoding("L2Flags");
                int flagBits[] = {0x01,0x02,0x04,0x08,0x010,0x020,0x040,0x080,0x100,0x200,0x400,0x800,0x1000,0x2000,0x4000,0x8000,0x10000,0x20000,0x40000,0x80000,0x100000,0x200000,0x400000,0x800000,0x1000000,0x2000000,0x4000000,0x8000000,0x10000000,0x20000000,0x40000000,0x80000000}; //todo finish this list

                if (flagNames != null) {
                    for (int bit = 0; (bit < flagNames.length && bit < flagBits.length); bit++) {
                        String flagName = flagNames[bit];
                        if (flagName.startsWith("SPARE")) {
                            flagName = flagName + Integer.toString(bit + 1);
                        }
//                    System.out.println("flag=" + flagName);
                        if (!flagCoding.containsAttribute(flagName)) {
//                        System.out.println("Adding flag=" + flagName);
                            flagCoding.addFlag(flagName, flagBits[bit], getFlagDescription(flagName));
                        } else {
                            flagName = flagName + Integer.toString(bit + 1);
//                        System.out.println("Adding flag=" + flagName);
                            flagCoding.addFlag(flagName, flagBits[bit], getFlagDescription(flagName));
                        }
                    }
                }

                product.getFlagCodingGroup().add(flagCoding);
                QFBand.setSampleCoding(flagCoding);


                Mask composite1Mask = null;
                Mask composite2Mask = null;
                Mask composite3Mask = null;
                Mask Water_Mask = null;




                if (isMaskSort()) {
                    String flagNamesOrdered = getMaskSort();
                    String[] flagNamesOrderedArray = flagNamesOrdered.split("\\s+|,");
                    for (String flagName : flagNamesOrderedArray) {

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

                        addFlagMask(product, flagName, flagCoding);
                    }
                }


//                System.out.println("Looping on flagNames");
                for (String flagName : flagCoding.getFlagNames()) {
//                    System.out.println("flagName=" + flagName);

                    addFlagMask(product, flagName, flagCoding);
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




                String[] bandNames = product.getBandNames();
                for (String bandName : bandNames) {
                    RasterDataNode raster = product.getRasterDataNode(bandName);

                    if (is_ATMFAIL_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_NAME));
                    }

                    if (is_LAND_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_LAND_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_LAND_NAME));
                    }

                    if (is_PRODWARN_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_NAME));
                    }

                    if (is_HIGLINT_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_NAME));
                    }

                    if (is_HILT_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HILT_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_HILT_NAME));
                    }

                    if (is_HISATZEN_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_NAME));
                    }

                    if (is_COASTZ_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_COASTZ_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_COASTZ_NAME));
                    }

                    if (is_STRAYLIGHT_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_NAME));
                    }

                    if (is_CLDICE_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_CLDICE_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_CLDICE_NAME));
                    }

                    if (is_COCCOLITH_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_NAME));
                    }

                    if (is_TURBIDW_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_NAME));
                    }

                    if (is_HISOLZEN_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_NAME));
                    }

                    if (is_LOWLW_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_LOWLW_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_LOWLW_NAME));
                    }

                    if (is_CHLFAIL_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_NAME));
                    }

                    if (is_NAVWARN_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_NAME));
                    }

                    if (is_ABSAER_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_ABSAER_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_ABSAER_NAME));
                    }

                    if (is_MAXAERITER_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_NAME));
                    }

                    if (is_MODGLINT_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_NAME));
                    }

                    if (is_CHLWARN_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_NAME));
                    }

                    if (is_ATMWARN_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_NAME));
                    }

                    if (is_SEAICE_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_SEAICE_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_SEAICE_NAME));
                    }

                    if (is_NAVFAIL_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_NAME));
                    }

                    if (is_FILTER_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_FILTER_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_FILTER_NAME));
                    }

                    if (is_BOWTIEDEL_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_NAME));
                    }

                    if (is_HIPOL_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_HIPOL_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_HIPOL_NAME));
                    }

                    if (is_PRODFAIL_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_NAME));
                    }

                    if (is_GEOREGION_MaskEnabled() && product.getMaskGroup().contains(SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_NAME)) {
                        raster.getOverlayMaskGroup().add(product.getMaskGroup().get(SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_NAME));
                    }


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

    private void setFlagMeaningsAndNames(Product product, MetadataElement metadataElementL2Flags) {
        final MetadataAttribute flagMeaningsAttribute = metadataElementL2Flags.getAttribute("FLAG_MEANINGS");
        if (flagMeaningsAttribute != null) {
            flagMeanings = flagMeaningsAttribute.getData().getElemString();
            flagNames = flagMeanings.split(" ");
        } else {
            final MetadataElement global = product.getMetadataRoot().getElement("Global_Attributes");
            if (global != null) {
                final MetadataAttribute maskNamesAttribute = global.getAttribute("Mask_Names");
                if (maskNamesAttribute != null) {
                    flagMeanings = maskNamesAttribute.getData().getElemString();
                    if (flagMeanings != null) {
                        flagNames = flagMeanings.split(",");
                    }
                }
            }
        }
    }


    private Mask createMask(Product product, String maskName, Color maskColor, double maskTransparency) {

        Mask mask = null;
        if (maskName != null && maskName.length() > 0 && maskColor != null) {
            mask = Mask.BandMathsType.create(maskName, getFlagDescription(maskName),
                    product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                    "l2_flags." + maskName,
                    maskColor, maskTransparency);
            product.getMaskGroup().add(mask);
        }

        return mask;
    }





    private Mask createMaskMisc(Product product, String flagName) {
        String flag = "l2_flags." + flagName;
        Mask Misc_Mask = Mask.BandMathsType.create(flagName, getFlagDescription(flagName),
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                flag,
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        product.getMaskGroup().add(Misc_Mask);

        return Misc_Mask;
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




    private Mask createSPAREMask(Product product, String maskName) {
        String flagName = "l2_flags." + maskName;
        Mask SPARE_Mask = Mask.BandMathsType.create(maskName, getFlagDescription(maskName),
                product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                flagName,
                get_SPARE_MaskColor(), get_SPARE_MaskTransparency());
        if (is_SPARE_MaskInclude()) {
            product.getMaskGroup().add(SPARE_Mask);
            return SPARE_Mask;
        }

        return null;
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

        HashMap<String, String> ociWavelengths = new HashMap<String, String>();

        String sensor = ProductUtils.getMetaData(product, ProductUtils.METADATA_POSSIBLE_SENSOR_KEYS);
        String platform = ProductUtils.getMetaData(product, ProductUtils.METADATA_POSSIBLE_PLATFORM_KEYS);
        String processing_level = ProductUtils.getMetaData(product, "processing_level");

        String SENSOR_INFO = "sensor_info";
        String AUXDATA = "auxdata";
        String OCI_BANDPASS_CSV = "oci_bandpass.csv";

        if (SeadasProductReader.Mission.OCI.toString().equals(sensor)) {

            File sensorInfoAuxDir = SystemUtils.getAuxDataPath().resolve(SENSOR_INFO).toFile();
            File ociBandPassFile = new File(sensorInfoAuxDir, OCI_BANDPASS_CSV);

            if (ociBandPassFile == null ||  !ociBandPassFile.exists()) {
                try {
                    Path auxdataDir = SystemUtils.getAuxDataPath().resolve(SENSOR_INFO);

                    Path sourceBasePath = ResourceInstaller.findModuleCodeBasePath(SeadasFileReader.class);
                    Path auxdirSource = sourceBasePath.resolve(AUXDATA);
                    Path sourceDirPath = auxdirSource.resolve(SENSOR_INFO);

                    final ResourceInstaller resourceInstaller = new ResourceInstaller(sourceDirPath, auxdataDir);

                    resourceInstaller.install(".*." + OCI_BANDPASS_CSV, ProgressMonitor.NULL);

                } catch (IOException e) {
                    SnapApp.getDefault().handleError("Unable to install " + AUXDATA + "/" + SENSOR_INFO + "/" + OCI_BANDPASS_CSV, e);
                }
            }

            if (sensorInfoAuxDir != null && sensorInfoAuxDir.exists()) {

                if (ociBandPassFile != null && ociBandPassFile.exists()) {

                    try (BufferedReader br = new BufferedReader(new FileReader(ociBandPassFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] values = line.split(",");
                            if (values != null && values.length > 3) {
                                ociWavelengths.put(values[1].trim(), values[2].trim());
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }

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

                if (SeadasProductReader.Mission.OCI.toString().equals(sensor) &&
                        (SeadasProductReader.ProcessingLevel.L2.toString().equals(processing_level) ||
                                SeadasProductReader.ProcessingLevel.L3m.toString().equals(processing_level))) {
                    wvlstr = getPaceOCIWavelengths(wvlstr, ociWavelengths);
                }

                final float wavelength = Float.parseFloat(wvlstr);
                band.setSpectralWavelength(wavelength);
                band.setSpectralBandIndex(spectralBandIndex++);
            }
        }
    }

    protected String getPaceOCIWavelengths(String wvlstr, HashMap<String, String> ociWavelengths) {
        String wvlstr_oci = ociWavelengths.get(wvlstr);

        if (wvlstr_oci != null && wvlstr_oci.length() > 0) {
            wvlstr = wvlstr_oci;
        }

        return wvlstr;
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
                if (wvl == null) {
                    wvl = ncFile.findVariable("wavelength_3d");
                }
                if (wvl == null) {
                    wvl = ncFile.findVariable("wavelength");
                }
            } else {
                wvl = ncFile.findVariable("sensor_band_parameters/wavelength");
                if (wvl == null) {
                    wvl = ncFile.findVariable("wavelength");
                }
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



    // GEOREGION

    private Color get_GEOREGION_MaskColor() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyColor(SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_DEFAULT);
    }


    private boolean is_GEOREGION_MaskEnabled() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyBool(SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_DEFAULT);
    }

    private double get_GEOREGION_MaskTransparency() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyDouble(SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_DEFAULT);
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
        // don't let composite mask name be same as any of the flags
        if (compositeMaskName != null) {
            if (flagNames == null) {
                return null;
            }
            for (String validFlag : flagNames) {
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
            validFlagComposite = new ArrayList<>();

            if (flagNames == null) {
                return validFlagComposite;
            }

            for (String flag : flagsArray) {
                if (flag != null) {
                    flag = flag.trim();
                    boolean flagIsComplement = false;
                    if (flag.startsWith("~")) {
                        flag = flag.replace("~", "");
                        flagIsComplement = true;
                    }

                    for (String validFlag : flagNames) {
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






    public String getBandGroupingLevel2() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_DEFAULT);
    }

    public String getBandFlipXLevel2() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_DEFAULT);
    }

    public String getBandFlipYLevel2() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_DEFAULT);
    }


    public String getBandGroupingL1BPace() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_DEFAULT);
    }

    public String getBandFlipXL1BPace() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_DEFAULT);
    }

    public String getBandFlipYL1BPace() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_DEFAULT);
    }



    public String getBandGroupingL3Mapped() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_DEFAULT);
    }

    public String getBandFlipXL3Mapped() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_DEFAULT);
    }

    public String getBandFlipYL3Mapped() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_DEFAULT);
    }

    


    public String getBandGroupingL1CPace() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_DEFAULT);
    }

    public String getBandFlipXL1CPace() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_DEFAULT);
    }

    public String getBandFlipYL1CPace() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_DEFAULT);
    }


    public String getBandGroupingL1CPaceHarp2() {
        final PropertyMap preferences = SnapApp.getDefault().getAppContext().getPreferences();
        return preferences.getPropertyString(SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_DEFAULT);
    }






    private interface SkipBadNav {

        boolean isBadNav(double value);
    }

}
