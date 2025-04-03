package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.olci.OlciContext;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.forward.TiePointBilinearForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.dataio.geocoding.inverse.TiePointInverse;
import org.esa.snap.runtime.Config;

import java.util.prefs.Preferences;

import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;
import static org.esa.snap.core.dataio.geocoding.InverseCoding.KEY_SUFFIX_INTERPOLATING;

public class S3Util {

    /**
     * Defines the transformation keys for forward and inverse pixel-geocoding transformations
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
}
