package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.olci.OlciContext;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelGeoIndexInverse;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.netcdf.util.Constants;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import static eu.esa.opt.dataio.s3.slstr.SlstrLevel1ProductFactory.SLSTR_L1B_PIXEL_GEOCODING_INVERSE;
import static junit.framework.TestCase.assertEquals;
import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testReplaceNonWordCharacters() {
        assertEquals("was_n_los_", S3Util.replaceNonWordCharacters("was'n_los???"));
        assertEquals("JavaMan", S3Util.replaceNonWordCharacters("JavaMan"));
        assertEquals("r8d1an__ce", S3Util.replaceNonWordCharacters("r8d1an__ce"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetSpectralWavelength() {
        final Variable variable = mock(Variable.class);
        final Attribute wavelength = new Attribute("wavelength", 654.6f);
        when(variable.findAttribute("wavelength")).thenReturn(wavelength);

        assertEquals(654.6f, S3Util.getSpectralWavelength(variable), 1e-8);

        when(variable.findAttribute("wavelength")).thenReturn(null);
        assertEquals(0.f, S3Util.getSpectralWavelength(variable), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetSpectralBandwidth() {
        final Variable variable = mock(Variable.class);
        final Attribute wavelength = new Attribute("bandwidth", 11.8);
        when(variable.findAttribute("bandwidth")).thenReturn(wavelength);

        assertEquals(11.8f, S3Util.getSpectralBandwidth(variable), 1e-8);

        when(variable.findAttribute("bandwidth")).thenReturn(null);
        assertEquals(0.f, S3Util.getSpectralWavelength(variable), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetAttributeValue() {
        final Attribute floatAttribute = new Attribute("a_float", 247.88f);
        assertEquals(247.88f, S3Util.getAttributeValue(floatAttribute).floatValue(), 1e-8);

        final Attribute stringAttribute = new Attribute("a_string", "247.89");
        assertEquals(247.89, S3Util.getAttributeValue(stringAttribute).doubleValue(), 1e-8);

        final Attribute byeAttribute = new Attribute("a_byte", "11b");
        assertEquals(11, S3Util.getAttributeValue(byeAttribute).doubleValue(), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetScalingFactor() {
        final Variable variable = mock(Variable.class);

        final Attribute cf_scale_factor = new Attribute(CFConstants.SCALE_FACTOR, 1.25f);
        when(variable.findAttribute(CFConstants.SCALE_FACTOR)).thenReturn(cf_scale_factor);
        assertEquals(1.25f, S3Util.getScalingFactor(variable), 1e-8);
        when(variable.findAttribute(CFConstants.SCALE_FACTOR)).thenReturn(null);

        final Attribute slope_scale_factor = new Attribute(Constants.SLOPE_ATT_NAME, 1.125f);
        when(variable.findAttribute(Constants.SLOPE_ATT_NAME)).thenReturn(slope_scale_factor);
        assertEquals(1.125f, S3Util.getScalingFactor(variable), 1e-8);
        when(variable.findAttribute(Constants.SLOPE_ATT_NAME)).thenReturn(null);

        final Attribute scaling_factor = new Attribute("scaling_factor", 1.0625f);
        when(variable.findAttribute("scaling_factor")).thenReturn(scaling_factor);
        assertEquals(1.0625f, S3Util.getScalingFactor(variable), 1e-8);
        when(variable.findAttribute("scaling_factor")).thenReturn(null);

        // no attribute present tb 2025-04-04
        assertEquals(1.f, S3Util.getScalingFactor(variable), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetAddOffset() {
        final Variable variable = mock(Variable.class);

        final Attribute cf_offset = new Attribute(CFConstants.ADD_OFFSET, 0.11f);
        when(variable.findAttribute(CFConstants.ADD_OFFSET)).thenReturn(cf_offset);
        assertEquals(0.11f, S3Util.getAddOffset(variable), 1e-8);
        when(variable.findAttribute(CFConstants.ADD_OFFSET)).thenReturn(null);

        final Attribute intercept = new Attribute(Constants.INTERCEPT_ATT_NAME, 0.22f);
        when(variable.findAttribute(Constants.INTERCEPT_ATT_NAME)).thenReturn(intercept);
        assertEquals(0.22f, S3Util.getAddOffset(variable), 1e-8);
        when(variable.findAttribute(Constants.INTERCEPT_ATT_NAME)).thenReturn(null);

        // no attribute present tb 2025-04-04
        assertEquals(0.f, S3Util.getAddOffset(variable), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetRasterDataType() {
        final Variable variable = mock(Variable.class);

        when(variable.getDataType()).thenReturn(DataType.DOUBLE);
        assertEquals(ProductData.TYPE_FLOAT64, S3Util.getRasterDataType(variable));

        when(variable.getDataType()).thenReturn(DataType.LONG);
        assertEquals(ProductData.TYPE_INT64, S3Util.getRasterDataType(variable));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddFillValue_int() {
        final Band band = new Band("nasenmann", ProductData.TYPE_INT32, 3, 3);
        final Variable variable = mock(Variable.class);
        final Attribute fillValueAttribute = new Attribute(CFConstants.FILL_VALUE, 16);
        when(variable.findAttribute(CFConstants.FILL_VALUE)).thenReturn(fillValueAttribute);

        S3Util.addFillValue(band, variable);
        assertTrue(band.isNoDataValueSet());
        assertTrue(band.isNoDataValueUsed());
        assertEquals(16, band.getNoDataValue(), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddFillValue_float() {
        final Band band = new Band("nasenmann", ProductData.TYPE_FLOAT32, 3, 3);
        final Variable variable = mock(Variable.class);
        final Attribute fillValueAttribute = new Attribute(CFConstants.FILL_VALUE, -99.98f);
        when(variable.findAttribute(CFConstants.FILL_VALUE)).thenReturn(fillValueAttribute);

        S3Util.addFillValue(band, variable);
        assertTrue(band.isNoDataValueSet());
        assertTrue(band.isNoDataValueUsed());
        assertEquals(-99.98f, band.getNoDataValue(), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddFillValue_flagBand() {
        final Band band = new Band("nasenmann", ProductData.TYPE_INT16, 3, 3);
        band.setSampleCoding(new FlagCoding("testFlags"));
        final Variable variable = mock(Variable.class);
        final Attribute fillValueAttribute = new Attribute(CFConstants.FILL_VALUE, 1);
        when(variable.findAttribute(CFConstants.FILL_VALUE)).thenReturn(fillValueAttribute);

        S3Util.addFillValue(band, variable);
        assertTrue(band.isNoDataValueSet());
        assertFalse(band.isNoDataValueUsed());
        assertEquals(1.f, band.getNoDataValue(), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetSampleMeanings() {
        Attribute flagAttribute = new Attribute("blank_separated", "flag_a flag_b flag_c flag_d");
        String[] sampleMeanings = S3Util.getSampleMeanings(flagAttribute);
        assertEquals(4, sampleMeanings.length);
        assertEquals("flag_a", sampleMeanings[0]);
        assertEquals("flag_b", sampleMeanings[1]);

        flagAttribute = new Attribute("string_array", Array.factory(DataType.STRING, new int[]{4}, new String[]{"flag_a", "flag_b", "flag_c", "flag_d"}));
        sampleMeanings = S3Util.getSampleMeanings(flagAttribute);
        assertEquals(4, sampleMeanings.length);
        assertEquals("flag_c", sampleMeanings[2]);
        assertEquals("flag_d", sampleMeanings[3]);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetSampleMeanings_emptyAttribute() {
        final Array emptyArray = Array.factory(DataType.FLOAT, new int[]{0});
        final Attribute flagAttribute = new Attribute("blank_separated", emptyArray);
        final String[] sampleMeanings = S3Util.getSampleMeanings(flagAttribute);
        assertEquals(0, sampleMeanings.length);
    }
}
