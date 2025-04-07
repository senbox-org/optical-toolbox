package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.olci.OlciContext;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.forward.TiePointBilinearForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.dataio.geocoding.inverse.TiePointInverse;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
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
        // @todo 1 tb/tb move to factory for type ... 2025-04-03
        codingNames[0] = preferences.get(new OlciContext().getTiePointForwardGeoCodingKey(), TiePointBilinearForward.KEY);
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
}
