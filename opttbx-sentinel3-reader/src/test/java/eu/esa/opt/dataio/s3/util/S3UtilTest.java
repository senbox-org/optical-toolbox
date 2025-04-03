package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.olci.OlciContext;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelGeoIndexInverse;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.junit.Test;

import static eu.esa.opt.dataio.s3.slstr.SlstrLevel1ProductFactory.SLSTR_L1B_PIXEL_GEOCODING_INVERSE;
import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;
import static org.junit.Assert.assertEquals;

public class S3UtilTest {

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetForwardAndInverseKeys_default() {
        final String inverseKey = System.getProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
        final String fractionalAccuracy = System.getProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

        try {
            System.clearProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            System.clearProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

            final String[] keys = S3Util.getForwardAndInverseKeys_pixelCoding(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            assertEquals(PixelForward.KEY, keys[0]);
            assertEquals(PixelQuadTreeInverse.KEY, keys[1]);
        } finally {
            if (inverseKey != null) {
                System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, inverseKey);
            }
            if (fractionalAccuracy != null) {
                System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, fractionalAccuracy);
            }
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetForwardAndInverseKeys_interpolating() {
        final String inverseKey = System.getProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
        final String fractionalAccuracy = System.getProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

        try {
            System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, "true");

            final String[] keys = S3Util.getForwardAndInverseKeys_pixelCoding(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            assertEquals(PixelInterpolatingForward.KEY, keys[0]);
            assertEquals(PixelQuadTreeInverse.KEY_INTERPOLATING, keys[1]);
        } finally {
            if (inverseKey != null) {
                System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, inverseKey);
            }
            if (fractionalAccuracy != null) {
                System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, fractionalAccuracy);
            }
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetForwardAndInverseKeys_inverse() {
        final String inverseKey = System.getProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
        final String fractionalAccuracy = System.getProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

        try {
            System.clearProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);
            System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, PixelGeoIndexInverse.KEY);

            final String[] keys = S3Util.getForwardAndInverseKeys_pixelCoding(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            assertEquals(PixelForward.KEY, keys[0]);
            assertEquals(PixelGeoIndexInverse.KEY, keys[1]);
        } finally {
            if (inverseKey != null) {
                System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, inverseKey);
            }
            if (fractionalAccuracy != null) {
                System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, fractionalAccuracy);
            }
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetForwardAndInverseKeys_tiePointCoding_forwardKey() {
        // @todo 2 tb/tb add tests for other sensors 2025-04-03
        final String tiePointForwardGeoCodingKey = new OlciContext().getTiePointForwardGeoCodingKey();
        final String forwardKey = System.getProperty(tiePointForwardGeoCodingKey);
        try {
            System.setProperty(tiePointForwardGeoCodingKey, "YEAH!");

            final String[] codingKeys = S3Util.getForwardAndInverseKeys_tiePointCoding();
            assertEquals("YEAH!", codingKeys[0]);
            assertEquals("INV_TIE_POINT", codingKeys[1]);

        } finally {
            if (forwardKey != null) {
                System.setProperty(tiePointForwardGeoCodingKey, forwardKey);
            } else {
                System.clearProperty(tiePointForwardGeoCodingKey);
            }
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetForwardAndInverseKeys_tiePointCoding_default() {
        // @todo 2 tb/tb add tests for other sensors 2025-04-03
        final String tiePointForwardGeoCodingKey = new OlciContext().getTiePointForwardGeoCodingKey();
        final String forwardKey = System.getProperty(tiePointForwardGeoCodingKey);
        try {
            System.clearProperty(tiePointForwardGeoCodingKey);

            final String[] codingKeys = S3Util.getForwardAndInverseKeys_tiePointCoding();
            assertEquals("FWD_TIE_POINT_BILINEAR", codingKeys[0]);
            assertEquals("INV_TIE_POINT", codingKeys[1]);

        } finally {
            if (forwardKey != null) {
                System.setProperty(tiePointForwardGeoCodingKey, forwardKey);
            }
        }
    }
}
