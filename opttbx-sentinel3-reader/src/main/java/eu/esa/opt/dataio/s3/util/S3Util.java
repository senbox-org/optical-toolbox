package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.olci.OlciContext;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.forward.TiePointBilinearForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.dataio.geocoding.inverse.TiePointInverse;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.dataio.netcdf.util.Constants;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import org.esa.snap.runtime.Config;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.util.prefs.Preferences;

import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;
import static org.esa.snap.core.dataio.geocoding.InverseCoding.KEY_SUFFIX_INTERPOLATING;

public class S3Util {

    /**
     * Defines the transformation keys for forward and inverse pixel-geocoding transformations
     *
     * @param inverseCodingProperty the property defining the preferences key storing the desired inverse geocoding
     *                              algorithm. Uses the OptTbx part of the preferences.
     * @return and array of keys. Index 0: forward coding, index 1: inverse coding
     */
    public static String[] getForwardAndInverseKeys_pixelCoding(String inverseCodingProperty) {
        final String[] codingNames = new String[2];

        final Preferences snapPreferences = Config.instance("snap").preferences();
        final boolean useFractAccuracy = snapPreferences.getBoolean(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, false);

        // @todo 1 tb/tb move to factory for type ... 2025-04-03
        final Preferences opttbxPreferences = Config.instance("opttbx").preferences();
        codingNames[1] = opttbxPreferences.get(inverseCodingProperty, PixelQuadTreeInverse.KEY);
        if (useFractAccuracy) {
            codingNames[0] = PixelInterpolatingForward.KEY;
            codingNames[1] = codingNames[1].concat(KEY_SUFFIX_INTERPOLATING);
        } else {
            codingNames[0] = PixelForward.KEY;
        }

        return codingNames;
    }

    public static String[] getForwardAndInverseKeys_tiePointCoding() {
        final String[] codingNames = new String[2];

        final Preferences preferences = Config.instance("opttbx").preferences();

        // @todo 2 tb/tb refactor, get type from outside ... 2025-04-24
        final SensorContext olciContext = SensorContextFactory.get("OL_1_EFR");
        codingNames[0] = preferences.get(olciContext.getTiePointForwardGeoCodingKey(), TiePointBilinearForward.KEY);
        codingNames[1] = TiePointInverse.KEY;

        return codingNames;
    }

    public static String replaceNonWordCharacters(String flagName) {
        return flagName.replaceAll("\\W+", "_");
    }

    public static float getSpectralWavelength(Variable variable) {
        final Attribute attribute = variable.findAttribute("wavelength");
        if (attribute != null) {
            return getAttributeValue(attribute).floatValue();
        }
        return 0f;
    }

    public static float getSpectralBandwidth(Variable variable) {
        final Attribute attribute = variable.findAttribute("bandwidth");
        if (attribute != null) {
            return S3Util.getAttributeValue(attribute).floatValue();
        }
        return 0f;
    }

    public static Number getAttributeValue(Attribute attribute) {
        if (attribute.isString()) {
            String stringValue = attribute.getStringValue();
            if (stringValue.endsWith("b")) {
                // Special management for bytes; Can occur in e.g. ASCAT files from EUMETSAT
                return Byte.parseByte(stringValue.substring(0, stringValue.length() - 1));
            } else {
                return Double.parseDouble(stringValue);
            }
        } else {
            return attribute.getNumericValue();
        }
    }

    public static double getScalingFactor(Variable variable) {
        Attribute attribute = variable.findAttribute(CFConstants.SCALE_FACTOR);
        if (attribute == null) {
            attribute = variable.findAttribute(Constants.SLOPE_ATT_NAME);
        }
        if (attribute == null) {
            attribute = variable.findAttribute("scaling_factor");
        }
        if (attribute != null) {
            return S3Util.getAttributeValue(attribute).doubleValue();
        }
        return 1.0;
    }

    public static double getAddOffset(Variable variable) {
        Attribute attribute = variable.findAttribute(CFConstants.ADD_OFFSET);
        if (attribute == null) {
            attribute = variable.findAttribute(Constants.INTERCEPT_ATT_NAME);
        }
        if (attribute != null) {
            return S3Util.getAttributeValue(attribute).doubleValue();
        }
        return 0.0;
    }

    public static int getRasterDataType(Variable variable) {
        int rasterDataType = DataTypeUtils.getRasterDataType(variable);
        if (rasterDataType == -1 && variable.getDataType() == DataType.LONG) {
            // who invented this conditional - to my understanding, this can never happen tb 2025-04-07
            rasterDataType = variable.getDataType().isUnsigned() ? ProductData.TYPE_UINT32 : ProductData.TYPE_INT32;
        }
        return rasterDataType;
    }

    public static void addFillValue(Band band, Variable variable) {
        final Attribute fillValueAttribute = variable.findAttribute(CFConstants.FILL_VALUE);
        if (fillValueAttribute != null) {
            band.setNoDataValueUsed(!band.isFlagBand());
            band.setNoDataValue(fillValueAttribute.getNumericValue().doubleValue());
        }
    }

    // @todo 3 tb/** merge with duplicated implementation is snap_netcdf module 2025-04-07
    public static String[] getSampleMeanings(Attribute sampleMeanings) {
        final int sampleMeaningsCount = sampleMeanings.getLength();
        if (sampleMeaningsCount == 0) {
            return new String[0];
        }
        if (sampleMeaningsCount > 1) {
            // handle a common misunderstanding of CF conventions, where flag meanings are stored as array of strings
            final String[] strings = new String[sampleMeaningsCount];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = sampleMeanings.getStringValue(i);
            }
            return strings;
        }
        return sampleMeanings.getStringValue().split(" ");
    }

    public static void addSamplesByte(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleValues) {
        final int sampleCount = Math.min(uniqueNames.length, sampleValues.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            final short value = DataType.unsignedByteToShort(sampleValues.getNumericValue(i).byteValue());
            sampleCoding.addSample(sampleName, value, null);
        }
    }

    public static void addSamplesByte(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleMasks, Attribute sampleValues) {
        final int sampleCount = Math.min(uniqueNames.length, sampleMasks.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            final short mask = DataType.unsignedByteToShort(sampleMasks.getNumericValue(i).byteValue());
            final short value = DataType.unsignedByteToShort(sampleValues.getNumericValue(i).byteValue());
            if (mask == value) {
                sampleCoding.addSample(sampleName, mask, null);
            } else {
                sampleCoding.addSamples(sampleName, new int[]{mask, value}, null);
            }
        }
    }

    public static void addSamplesShort(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleValues) {
        final int sampleCount = Math.min(uniqueNames.length, sampleValues.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            final int value = DataType.unsignedShortToInt(sampleValues.getNumericValue(i).shortValue());
            sampleCoding.addSample(sampleName, value, null);
        }
    }

    public static void addSamplesShort(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleMasks, Attribute sampleValues) {
        final int sampleCount = Math.min(uniqueNames.length, sampleMasks.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            final int mask = DataType.unsignedShortToInt(sampleMasks.getNumericValue(i).shortValue());
            final int value = DataType.unsignedShortToInt(sampleValues.getNumericValue(i).shortValue());
            if (mask == value) {
                sampleCoding.addSample(sampleName, mask, null);
            } else {
                sampleCoding.addSamples(sampleName, new int[]{mask, value}, null);
            }
        }
    }

    public static void addSamplesInt(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleValues) {
        final int sampleCount = Math.min(uniqueNames.length, sampleValues.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            sampleCoding.addSample(sampleName, sampleValues.getNumericValue(i).intValue(), null);
        }
    }

    public static void addSamplesInt(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleMasks, Attribute sampleValues) {
        final int sampleCount = Math.min(uniqueNames.length, sampleMasks.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            final int mask = sampleMasks.getNumericValue(i).intValue();
            final int value = sampleValues.getNumericValue(i).intValue();
            if (mask == value) {
                sampleCoding.addSample(sampleName, mask, null);
            } else {
                sampleCoding.addSamples(sampleName, new int[]{mask, value}, null);
            }
        }
    }

    public static void addSamplesLong(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleValues, boolean msb) {
        final int sampleCount = Math.min(uniqueNames.length, sampleValues.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            final long longValue = sampleValues.getNumericValue(i).longValue();
            if (msb) {
                long shiftedValue = longValue >>> 32;
                if (shiftedValue > 0) {
                    sampleCoding.addSample(sampleName, (int) shiftedValue, null);
                }
            } else {
                long shiftedValue = longValue & 0x00000000FFFFFFFFL;
                if (shiftedValue > 0 || longValue == 0L) {
                    sampleCoding.addSample(sampleName, (int) shiftedValue, null);
                }
            }
        }
    }

    public static void addSamplesLong(SampleCoding sampleCoding, String[] uniqueNames, Attribute sampleMasks, Attribute sampleValues, boolean msb) {
        final int sampleCount = Math.min(uniqueNames.length, sampleValues.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = S3Util.replaceNonWordCharacters(uniqueNames[i]);
            final long value = sampleValues.getNumericValue(i).longValue();
            final long mask = sampleMasks.getNumericValue(i).longValue();
            if (msb) {
                int shiftedValue = (int) (value >>> 32);
                int shiftedMask = (int) (mask >>> 32);
                if (shiftedValue > 0) {
                    if (shiftedValue == shiftedMask) {
                        sampleCoding.addSample(sampleName, shiftedValue, null);
                    } else {
                        sampleCoding.addSamples(sampleName, new int[]{shiftedMask, shiftedValue}, null);
                    }
                }
            } else {
                int shiftedValue = (int) (value & 0x00000000FFFFFFFFL);
                int shiftedMask = (int) (mask & 0x00000000FFFFFFFFL);
                if (shiftedValue > 0 || value == 0L) {
                    if (shiftedValue == shiftedMask) {
                        sampleCoding.addSample(sampleName, shiftedValue, null);
                    } else {
                        sampleCoding.addSamples(sampleName, new int[]{shiftedMask, shiftedValue}, null);
                    }
                }
            }
        }
    }

    public static void addSamples(SampleCoding sampleCoding, Attribute sampleMeanings, Attribute sampleValues, boolean msb) {
        final String[] meanings = S3Util.getSampleMeanings(sampleMeanings);
        final String[] uniqueNames = StringUtils.makeStringsUnique(meanings);

        switch (sampleValues.getDataType()) {
            case BYTE:
            case UBYTE:
                addSamplesByte(sampleCoding, uniqueNames, sampleValues);
                break;
            case SHORT:
            case USHORT:
                addSamplesShort(sampleCoding, uniqueNames, sampleValues);
                break;
            case INT:
            case UINT:
                addSamplesInt(sampleCoding, uniqueNames, sampleValues);
                break;
            case LONG:
            case ULONG:
                addSamplesLong(sampleCoding, uniqueNames, sampleValues, msb);
                break;
            default:
                throw new IllegalArgumentException("Unsupported data type: " + sampleValues.getDataType());
        }
    }

    public static void addSamples(SampleCoding sampleCoding, Attribute sampleMeanings, Attribute sampleValues,
                                  Attribute sampleMasks, boolean msb) {
        final String[] meanings = S3Util.getSampleMeanings(sampleMeanings);
        final String[] uniqueNames = StringUtils.makeStringsUnique(meanings);

        switch (sampleMasks.getDataType()) {
            case BYTE:
            case UBYTE:
                addSamplesByte(sampleCoding, uniqueNames, sampleMasks, sampleValues);
                break;
            case SHORT:
            case USHORT:
                addSamplesShort(sampleCoding, uniqueNames, sampleMasks, sampleValues);
                break;
            case INT:
            case UINT:
                addSamplesInt(sampleCoding, uniqueNames, sampleMasks, sampleValues);
                break;
            case LONG:
            case ULONG:
                addSamplesLong(sampleCoding, uniqueNames, sampleMasks, sampleValues, msb);
                break;
            default:
                throw new IllegalArgumentException("Unsupported data type: " + sampleValues.getDataType());
        }
    }

    public static IndexCoding createIndexCoding(String name, String description, Attribute flagMeanings, Attribute flagValues, boolean msb) {
        final IndexCoding indexCoding = new IndexCoding(name);

        indexCoding.setDescription(description);
        S3Util.addSamples(indexCoding, flagMeanings, flagValues, msb);

        return indexCoding;
    }

    public static FlagCoding createFlagCoding(String name, String description, Attribute flagMeanings, Attribute flagValues, boolean msb) {
        final FlagCoding flagCoding = new FlagCoding(name);

        flagCoding.setDescription(description);
        S3Util.addSamples(flagCoding, flagMeanings, flagValues, msb);

        return flagCoding;
    }

    public static FlagCoding createFlagCoding(String name, String description, Attribute flagMeanings, Attribute flagValues, Attribute flagMasks, boolean msb) {
        final FlagCoding flagCoding = new FlagCoding(name);

        flagCoding.setDescription(description);
        S3Util.addSamples(flagCoding, flagMeanings, flagValues, flagMasks, msb);

        return flagCoding;
    }

    public static void addSampleCodings(Product product, Band band, Variable variable, boolean msb) {
        final Attribute flagValuesAttribute = variable.findAttribute(CFConstants.FLAG_VALUES);
        final Attribute flagMasksAttribute = variable.findAttribute(CFConstants.FLAG_MASKS);
        final Attribute flagMeaningsAttribute = variable.findAttribute(CFConstants.FLAG_MEANINGS);
        if (flagValuesAttribute != null && flagMasksAttribute != null) {
            final FlagCoding flagCoding = S3Util.createFlagCoding(band.getName(), band.getDescription(), flagMeaningsAttribute, flagValuesAttribute, flagMasksAttribute, msb);
            band.setSampleCoding(flagCoding);

            final ProductNodeGroup<FlagCoding> flagCodingGroup = product.getFlagCodingGroup();
            if (!flagCodingGroup.contains(band.getName())) {
                flagCodingGroup.add(flagCoding);
            }
        } else if (flagValuesAttribute != null) {
            final IndexCoding indexCoding = S3Util.createIndexCoding(band.getName(), band.getDescription(), flagMeaningsAttribute, flagValuesAttribute, msb);
            band.setSampleCoding(indexCoding);

            final ProductNodeGroup<IndexCoding> indexCodingGroup = product.getIndexCodingGroup();
            if (!indexCodingGroup.contains(band.getName())) {
                indexCodingGroup.add(indexCoding);
            }
        } else if (flagMasksAttribute != null) {
            final FlagCoding flagCoding = S3Util.createFlagCoding(band.getName(), band.getDescription(), flagMeaningsAttribute, flagMasksAttribute, msb);
            band.setSampleCoding(flagCoding);

            final ProductNodeGroup<FlagCoding> flagCodingGroup = product.getFlagCodingGroup();
            if (!flagCodingGroup.contains(band.getName())) {
                flagCodingGroup.add(flagCoding);
            }
        }
    }
}
