package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.olci.OlciContext;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelGeoIndexInverse;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.*;
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

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesByte() {
        final String[] uniqueNames = new String[] {"nasenmann", "heffalump"};
        final Attribute byteAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.BYTE, new int[]{2}, new byte[]{28, 33}));
        SampleCoding sampleCoding = new SampleCoding("byteTest");

        S3Util.addSamplesByte(sampleCoding, uniqueNames, byteAttribute);

        assertEquals(28, sampleCoding.getSampleValue(0));
        assertEquals("nasenmann", sampleCoding.getSampleName(0));
        assertEquals(33, sampleCoding.getSampleValue(1));
        assertEquals("heffalump", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesByte_withMasks() {
        final String[] uniqueNames = new String[] {"nasenmann", "heffalump"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.BYTE, new int[]{2}, new byte[]{1, 2}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.BYTE, new int[]{2}, new byte[]{1, 2}));
        SampleCoding sampleCoding = new SampleCoding("byteTest");

        S3Util.addSamplesByte(sampleCoding, uniqueNames, masksAttribute, valuesAttribute);

        assertEquals(1, sampleCoding.getSampleValue(0));
        assertEquals("nasenmann", sampleCoding.getSampleName(0));
        assertEquals(2, sampleCoding.getSampleValue(1));
        assertEquals("heffalump", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesByte_withMasks_differentValues() {
        final String[] uniqueNames = new String[] {"nasenmann", "heffalump"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.BYTE, new int[]{2}, new byte[]{3, 4}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.BYTE, new int[]{2}, new byte[]{5, 6}));
        SampleCoding sampleCoding = new SampleCoding("byteTest");

        S3Util.addSamplesByte(sampleCoding, uniqueNames, masksAttribute, valuesAttribute);

        assertEquals(5, sampleCoding.getSampleValue(0));
        assertEquals("nasenmann", sampleCoding.getSampleName(0));
        assertEquals(6, sampleCoding.getSampleValue(1));
        assertEquals("heffalump", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesShort() {
        final String[] uniqueNames = new String[] {"Alice", "Bertie?", "Charly"};
        final Attribute shortAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.SHORT, new int[]{3}, new short[]{106, 108, 111}));
        SampleCoding sampleCoding = new SampleCoding("shortTest");

        S3Util.addSamplesShort(sampleCoding, uniqueNames, shortAttribute);

        assertEquals(106, sampleCoding.getSampleValue(0));
        assertEquals("Alice", sampleCoding.getSampleName(0));
        assertEquals(108, sampleCoding.getSampleValue(1));
        assertEquals("Bertie_", sampleCoding.getSampleName(1));
        assertEquals(111, sampleCoding.getSampleValue(2));
        assertEquals("Charly", sampleCoding.getSampleName(2));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesShort_withMasks() {
        final String[] uniqueNames = new String[] {"Alice", "Bertie?", "Charly"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.SHORT, new int[]{3}, new short[]{7, 8, 9}));
        final Attribute maskAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.SHORT, new int[]{3}, new short[]{7, 8, 9}));
        SampleCoding sampleCoding = new SampleCoding("shortTest");

        S3Util.addSamplesShort(sampleCoding, uniqueNames, maskAttribute, valuesAttribute);

        assertEquals(7, sampleCoding.getSampleValue(0));
        assertEquals("Alice", sampleCoding.getSampleName(0));
        assertEquals(8, sampleCoding.getSampleValue(1));
        assertEquals("Bertie_", sampleCoding.getSampleName(1));
        assertEquals(9, sampleCoding.getSampleValue(2));
        assertEquals("Charly", sampleCoding.getSampleName(2));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesShort_withMasks_differentValues() {
        final String[] uniqueNames = new String[] {"Alice", "Bertie?", "Charly"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.SHORT, new int[]{3}, new short[]{7, 8, 9}));
        final Attribute maskAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.SHORT, new int[]{3}, new short[]{10, 11, 12}));
        SampleCoding sampleCoding = new SampleCoding("shortTest");

        S3Util.addSamplesShort(sampleCoding, uniqueNames, maskAttribute, valuesAttribute);

        assertEquals(10, sampleCoding.getSampleValue(0));
        assertEquals("Alice", sampleCoding.getSampleName(0));
        assertEquals(11, sampleCoding.getSampleValue(1));
        assertEquals("Bertie_", sampleCoding.getSampleName(1));
        assertEquals(12, sampleCoding.getSampleValue(2));
        assertEquals("Charly", sampleCoding.getSampleName(2));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesInt() {
        final String[] uniqueNames = new String[] {"DonaldDuck", "Dorette::"};
        final Attribute intAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{-23, -44}));
        SampleCoding sampleCoding = new SampleCoding("intTest");

        S3Util.addSamplesInt(sampleCoding, uniqueNames, intAttribute);

        assertEquals(-23, sampleCoding.getSampleValue(0));
        assertEquals("DonaldDuck", sampleCoding.getSampleName(0));
        assertEquals(-44, sampleCoding.getSampleValue(1));
        assertEquals("Dorette_", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesInt_withMasks() {
        final String[] uniqueNames = new String[] {"DonaldDuck", "Dorette::"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{13, 14}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.INT, new int[]{2}, new int[]{13, 14}));
        SampleCoding sampleCoding = new SampleCoding("intTest");

        S3Util.addSamplesInt(sampleCoding, uniqueNames, masksAttribute, valuesAttribute);

        assertEquals(13, sampleCoding.getSampleValue(0));
        assertEquals("DonaldDuck", sampleCoding.getSampleName(0));
        assertEquals(14, sampleCoding.getSampleValue(1));
        assertEquals("Dorette_", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesInt_withMasks_differentValues() {
        final String[] uniqueNames = new String[] {"DonaldDuck", "Dorette::"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{15, 16}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.INT, new int[]{2}, new int[]{17, 18}));
        SampleCoding sampleCoding = new SampleCoding("intTest");

        S3Util.addSamplesInt(sampleCoding, uniqueNames, masksAttribute, valuesAttribute);

        assertEquals(17, sampleCoding.getSampleValue(0));
        assertEquals("DonaldDuck", sampleCoding.getSampleName(0));
        assertEquals(18, sampleCoding.getSampleValue(1));
        assertEquals("Dorette_", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesLong_msb() {
        final String[] uniqueNames = new String[] {"Loop", "LongJohnSilver"};
        final Attribute longAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 23456789}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamplesLong(sampleCoding, uniqueNames, longAttribute, true);

        assertEquals(28744, sampleCoding.getSampleValue(0));
        assertEquals("Loop", sampleCoding.getSampleName(0));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesLong_msb_withMasks() {
        final String[] uniqueNames = new String[] {"Loop", "LongJohnSilver"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 23456789}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 23456789}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamplesLong(sampleCoding, uniqueNames, masksAttribute, valuesAttribute, true);

        assertEquals(28744, sampleCoding.getSampleValue(0));
        assertEquals("Loop", sampleCoding.getSampleName(0));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesLong_msb_withMasks_differentValues() {
        final String[] uniqueNames = new String[] {"Loop", "LongJohnSilver"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 23456789}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.ULONG, new int[]{2}, new long[]{223456789012345L, 33456789}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamplesLong(sampleCoding, uniqueNames, masksAttribute, valuesAttribute, true);

        assertEquals(52027, sampleCoding.getSampleValue(0));
        assertEquals("Loop", sampleCoding.getSampleName(0));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesLong_lsb() {
        final String[] uniqueNames = new String[] {"Loop", "LongJohnSilver"};
        final Attribute longAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 234567890123456L}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamplesLong(sampleCoding, uniqueNames, longAttribute, false);

        assertEquals(-2045911175, sampleCoding.getSampleValue(0));
        assertEquals("Loop", sampleCoding.getSampleName(0));
        assertEquals(-1748747584, sampleCoding.getSampleValue(1));
        assertEquals("LongJohnSilver", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesLong_lsb_withMasks() {
        final String[] uniqueNames = new String[] {"Loop", "LongJohnSilver"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 23456789}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 23456789}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamplesLong(sampleCoding, uniqueNames, masksAttribute, valuesAttribute, false);

        assertEquals(23456789, sampleCoding.getSampleValue(0));
        assertEquals("LongJohnSilver", sampleCoding.getSampleName(0));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamplesLong_lsb_withMasks_differentValues() {
        final String[] uniqueNames = new String[] {"Loop", "LongJohnSilver"};
        final Attribute valuesAttribute = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{123456789012345L, 23456789}));
        final Attribute masksAttribute = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.ULONG, new int[]{2}, new long[]{223456789012345L, 33456789}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamplesLong(sampleCoding, uniqueNames, masksAttribute, valuesAttribute, false);

        assertEquals(33456789, sampleCoding.getSampleValue(0));
        assertEquals("LongJohnSilver", sampleCoding.getSampleName(0));
    }

    @Test    
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_byte() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Lars", "Luisa"}));
        final Attribute flagValues_byte = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.UBYTE, new int[]{2}, new byte[]{18, 19}));
        SampleCoding sampleCoding = new SampleCoding("byteTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_byte, false);

        assertEquals(18, sampleCoding.getSampleValue(0));
        assertEquals("Lars", sampleCoding.getSampleName(0));
        assertEquals(19, sampleCoding.getSampleValue(1));
        assertEquals("Luisa", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_short() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Mara", "Michael"}));
        final Attribute flagValues_short = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.USHORT, new int[]{2}, new short[]{20, 21}));
        SampleCoding sampleCoding = new SampleCoding("shortTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_short, false);

        assertEquals(20, sampleCoding.getSampleValue(0));
        assertEquals("Mara", sampleCoding.getSampleName(0));
        assertEquals(21, sampleCoding.getSampleValue(1));
        assertEquals("Michael", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_int() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Norbert", "Nadine"}));
        final Attribute flagValues_int = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.UINT, new int[]{2}, new int[]{32768, 32769}));
        SampleCoding sampleCoding = new SampleCoding("intTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_int, false);

        assertEquals(32768, sampleCoding.getSampleValue(0));
        assertEquals("Norbert", sampleCoding.getSampleName(0));
        assertEquals(32769, sampleCoding.getSampleValue(1));
        assertEquals("Nadine", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_long() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Oppa", "Omma"}));
        final Attribute flagValues_int = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{24, 25}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_int, false);

        assertEquals(24, sampleCoding.getSampleValue(0));
        assertEquals("Oppa", sampleCoding.getSampleName(0));
        assertEquals(25, sampleCoding.getSampleValue(1));
        assertEquals("Omma", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_invalidDataType() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"inv", "alid"}));
        final Attribute flagValues_int = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.FLOAT, new int[]{2}, new float[]{25, 26}));
        SampleCoding sampleCoding = new SampleCoding("invalidTest");

        try {
            S3Util.addSamples(sampleCoding, flagMeanings, flagValues_int, false);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_byte_withMasks() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Lars", "Luisa"}));
        final Attribute flagMasks  = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.UBYTE, new int[]{2}, new byte[]{18, 19}));
        final Attribute flagValues_byte = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.UBYTE, new int[]{2}, new byte[]{18, 19}));
        SampleCoding sampleCoding = new SampleCoding("byteTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_byte, flagMasks, false);

        assertEquals(18, sampleCoding.getSampleValue(0));
        assertEquals("Lars", sampleCoding.getSampleName(0));
        assertEquals(19, sampleCoding.getSampleValue(1));
        assertEquals("Luisa", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_short_withMasks() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Mara", "Michael"}));
        final Attribute flagMasks = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.USHORT, new int[]{2}, new short[]{20, 21}));
        final Attribute flagValues_short = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.USHORT, new int[]{2}, new short[]{22, 23}));
        SampleCoding sampleCoding = new SampleCoding("shortTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_short, flagMasks,false);

        assertEquals(20, sampleCoding.getSampleValue(0));
        assertEquals("Mara", sampleCoding.getSampleName(0));
        assertEquals(21, sampleCoding.getSampleValue(1));
        assertEquals("Michael", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_int_withMasks() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Norbert", "Nadine"}));
        final Attribute flagMasks = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.UINT, new int[]{2}, new int[]{32770, 32771}));
        final Attribute flagValues_int = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.UINT, new int[]{2}, new int[]{32768, 32769}));
        SampleCoding sampleCoding = new SampleCoding("intTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_int, flagMasks,false);

        assertEquals(32770, sampleCoding.getSampleValue(0));
        assertEquals("Norbert", sampleCoding.getSampleName(0));
        assertEquals(32771, sampleCoding.getSampleValue(1));
        assertEquals("Nadine", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_long_withMasks() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"Oppa", "Omma"}));
        final Attribute flagMasks = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.ULONG, new int[]{2}, new long[]{24, 25}));
        final Attribute flagValues_long = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.ULONG, new int[]{2}, new long[]{24, 25}));
        SampleCoding sampleCoding = new SampleCoding("longTest");

        S3Util.addSamples(sampleCoding, flagMeanings, flagValues_long, flagMasks,false);

        assertEquals(24, sampleCoding.getSampleValue(0));
        assertEquals("Oppa", sampleCoding.getSampleName(0));
        assertEquals(25, sampleCoding.getSampleValue(1));
        assertEquals("Omma", sampleCoding.getSampleName(1));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddSamples_invalidDataType_withMasks() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"inv", "alid"}));
        final Attribute flagMasks = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.FLOAT, new int[]{2}, new float[]{25, 26}));
        final Attribute flagValues = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.FLOAT, new int[]{2}, new float[]{25, 26}));
        SampleCoding sampleCoding = new SampleCoding("invalidTest");

        try {
            S3Util.addSamples(sampleCoding, flagMeanings, flagValues, flagMasks,false);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testCreateIndexCoding() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"for", "index"}));
        final Attribute flagValues = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{24, 25}));
        final IndexCoding indexCoding = S3Util.createIndexCoding("theName", "theDescription", flagMeanings, flagValues, true);

        assertEquals("theName", indexCoding.getName());
        assertEquals("theDescription", indexCoding.getDescription());
        assertEquals(24, indexCoding.getIndexValue("for"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testCreateFlagCoding() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"flag", "coding"}));
        final Attribute flagValues = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{26, 27}));
        final FlagCoding flagCoding = S3Util.createFlagCoding("theName", "theDescription", flagMeanings, flagValues, true);

        assertEquals("theName", flagCoding.getName());
        assertEquals("theDescription", flagCoding.getDescription());
        assertEquals(27, flagCoding.getFlagMask("coding"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testCreateFlagCoding_withMasks() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"flag", "coding"}));
        final Attribute flagMasks = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.INT, new int[]{2}, new int[]{27, 28}));
        final Attribute flagValues = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{26, 27}));
        final FlagCoding flagCoding = S3Util.createFlagCoding("theName", "theDescription", flagMeanings, flagValues, flagMasks, true);

        assertEquals("theName", flagCoding.getName());
        assertEquals("theDescription", flagCoding.getDescription());
        assertEquals(27, flagCoding.getFlagMask("flag"));
    }

    @Test
    @STTM("SNAP-3978")
    public void testAddSampleCodings_values_and_masks() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"flag", "coding"}));
        final Attribute flagMasks = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.INT, new int[]{2}, new int[]{27, 28}));
        final Attribute flagValues = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{26, 27}));

        final Variable variable = mock(Variable.class);
        when(variable.findAttribute(CFConstants.FLAG_MEANINGS)).thenReturn(flagMeanings);
        when(variable.findAttribute(CFConstants.FLAG_MASKS)).thenReturn(flagMasks);
        when(variable.findAttribute(CFConstants.FLAG_VALUES)).thenReturn(flagValues);

        final Product product = new Product("test", "test_type", 3, 4);
        final Band testBand = new Band("test_band", ProductData.TYPE_INT16, 3, 4);

        S3Util.addSampleCodings(product, testBand, variable, false);

        final FlagCoding flagCoding = testBand.getFlagCoding();
        assertNotNull(flagCoding);
        MetadataAttribute coding = flagCoding.getFlag("coding");
        assertEquals(28, coding.getData().getElemInt());

        final ProductNodeGroup<FlagCoding> flagCodingGroup = product.getFlagCodingGroup();
        final FlagCoding codingFromProduct = flagCodingGroup.get("test_band");
        assertSame(flagCoding, codingFromProduct);
    }

    @Test
    @STTM("SNAP-3978")
    public void testAddSampleCodings_values() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"flag", "coding"}));
        final Attribute flagValues = new Attribute(CFConstants.FLAG_VALUES, Array.factory(DataType.INT, new int[]{2}, new int[]{31, 32}));

        final Variable variable = mock(Variable.class);
        when(variable.findAttribute(CFConstants.FLAG_MEANINGS)).thenReturn(flagMeanings);
        when(variable.findAttribute(CFConstants.FLAG_MASKS)).thenReturn(null);
        when(variable.findAttribute(CFConstants.FLAG_VALUES)).thenReturn(flagValues);

        final Product product = new Product("test", "test_type", 3, 4);
        final Band testBand = new Band("test_band", ProductData.TYPE_INT16, 3, 4);

        S3Util.addSampleCodings(product, testBand, variable, false);

        final SampleCoding sampleCoding = testBand.getSampleCoding();
        assertNotNull(sampleCoding);
        MetadataAttribute coding = sampleCoding.getAttribute("coding");
        assertEquals(32, coding.getData().getElemInt());

        final ProductNodeGroup<IndexCoding> indexCodingGroup = product.getIndexCodingGroup();
        final IndexCoding codingFromProduct = indexCodingGroup.get("test_band");
        assertSame(sampleCoding, codingFromProduct);
    }

    @Test
    @STTM("SNAP-3978")
    public void masks_and_meanings() {
        final Attribute flagMeanings  = new Attribute(CFConstants.FLAG_MEANINGS, Array.factory(DataType.STRING, new int[]{2}, new String[]{"flag", "coding"}));
        final Attribute flagMasks = new Attribute(CFConstants.FLAG_MASKS, Array.factory(DataType.INT, new int[]{2}, new int[]{33, 34}));

        final Variable variable = mock(Variable.class);
        when(variable.findAttribute(CFConstants.FLAG_MEANINGS)).thenReturn(flagMeanings);
        when(variable.findAttribute(CFConstants.FLAG_MASKS)).thenReturn(flagMasks);
        when(variable.findAttribute(CFConstants.FLAG_VALUES)).thenReturn(null);

        final Product product = new Product("test", "test_type", 3, 4);
        final Band testBand = new Band("test_band", ProductData.TYPE_INT16, 3, 4);

        S3Util.addSampleCodings(product, testBand, variable, false);

        final SampleCoding sampleCoding = testBand.getSampleCoding();
        assertNotNull(sampleCoding);
        MetadataAttribute coding = sampleCoding.getAttribute("coding");
        assertEquals(34, coding.getData().getElemInt());

        final ProductNodeGroup<FlagCoding> flagCodingGroup = product.getFlagCodingGroup();
        final FlagCoding codingFromProduct = flagCodingGroup.get("test_band");
        assertSame(sampleCoding, codingFromProduct);
    }
}
