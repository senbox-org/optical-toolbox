package eu.esa.opt.dataio.flex;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductSpectralAxis;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FlexSpectralHelperTest {

    @Test
    @STTM("SNAP-4126")
    public void testProductLevelChecks_areNullSafeAndCaseInsensitive() {
        assertTrue(FlexSpectralHelper.isL1bProduct("FLX_L1B_OBS"));
        assertTrue(FlexSpectralHelper.isL1cProduct("flx_l1c_flxsyn"));
        assertTrue(FlexSpectralHelper.isL2Product("FLX_L2_FLXSYN"));

        assertFalse(FlexSpectralHelper.isL1bProduct(null));
        assertFalse(FlexSpectralHelper.isL1cProduct("FLX_L2_FLXSYN"));
        assertFalse(FlexSpectralHelper.isL2Product("FLX_L1C_FLXSYN"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testCreateSpectralAxes_unknownProductTypeReturnsNoExplicitAxes() {
        final Product product = new Product("p", "unknown", 4, 3);

        final List<ProductSpectralAxis> axes = FlexSpectralHelper.createSpectralAxes(product, "unknown",
                new LinkedHashMap<>());

        assertTrue(axes.isEmpty());
    }

    @Test
    @STTM("SNAP-4126")
    public void testCreateSpectralAxes_l1cUsesDescriptorSeriesAndMergesSlstrRadiance() {
        final Product product = new Product("p", "FLX_L1C_FLXSYN", 4, 3);
        final Map<String, FlexVariableDescriptor> specials = new LinkedHashMap<>();
        addSpectralBand(product, "floris_toa_radiance_ch_1", 500.0f);
        addSpectralBand(product, "floris_toa_radiance_ch_2", 501.0f);
        addSpectralBand(product, "floris_toa_radiance_noise_uncertainty_ch_1", 500.0f);
        addSpectralBand(product, "olci_toa_radiance_ch_1", 600.0f);
        addSpectralBand(product, "olci_toa_radiance_ch_2", 601.0f);
        addSpectralBand(product, "slstr_nadir_toa_radiance_ch_1", 800.0f);
        addSpectralBand(product, "slstr_nadir_toa_radiance_ch_2", 801.0f);
        addSpectralBand(product, "slstr_nadir_tir_toa_radiance_ch_1", 11000.0f);
        addSpectralBand(product, "slstr_nadir_brightness_temperature_ch_1", 11000.0f);
        addSpectralBand(product, "slstr_oblique_toa_radiance_ch_1", 800.0f);

        specials.put("floris_toa_radiance",
                specialDescriptor("floris_toa_radiance", 2, "_ch_", "floris_spectral_channel_central_wavelengths"));
        specials.put("floris_toa_radiance_noise_uncertainty",
                specialDescriptor("floris_toa_radiance_noise_uncertainty", 1, "_ch_", "floris_spectral_channel_central_wavelengths"));
        specials.put("olci_toa_radiance",
                specialDescriptor("olci_toa_radiance", 2, "_ch_", "olci_spectral_channel_central_wavelengths"));
        specials.put("slstr_nadir_toa_radiance",
                specialDescriptor("slstr_nadir_toa_radiance", 2, "_ch_", "slstr_vswir_spectral_channel_central_wavelengths"));
        specials.put("slstr_nadir_tir_toa_radiance",
                specialDescriptor("slstr_nadir_tir_toa_radiance", 1, "_ch_", "slstr_tir_spectral_channel_central_wavelengths"));
        specials.put("slstr_nadir_brightness_temperature",
                specialDescriptor("slstr_nadir_brightness_temperature", 1, "_ch_", "slstr_tir_spectral_channel_central_wavelengths"));
        specials.put("slstr_oblique_toa_radiance",
                specialDescriptor("slstr_oblique_toa_radiance", 1, "_ch_", "slstr_vswir_spectral_channel_central_wavelengths"));

        final List<ProductSpectralAxis> axes = FlexSpectralHelper.createSpectralAxes(product, "FLX_L1C_FLXSYN",
                specials);

        assertEquals(6, axes.size());
        assertAxis(axes.get(0), "floris_toa_radiance", "FLORIS TOA Radiance",
                "floris_toa_radiance_ch_1", "floris_toa_radiance_ch_2");
        assertAxis(axes.get(1), "floris_toa_radiance_noise_uncertainty", "FLORIS TOA Radiance Noise Uncertainty",
                "floris_toa_radiance_noise_uncertainty_ch_1");
        assertAxis(axes.get(2), "olci_toa_radiance", "OLCI TOA Radiance",
                "olci_toa_radiance_ch_1", "olci_toa_radiance_ch_2");
        assertAxis(axes.get(3), "slstr_nadir_toa_radiance", "SLSTR Nadir TOA Radiance",
                "slstr_nadir_toa_radiance_ch_1", "slstr_nadir_toa_radiance_ch_2",
                "slstr_nadir_tir_toa_radiance_ch_1");
        assertAxis(axes.get(4), "slstr_nadir_brightness_temperature", "SLSTR Nadir Brightness Temperature",
                "slstr_nadir_brightness_temperature_ch_1");
        assertAxis(axes.get(5), "slstr_oblique_toa_radiance", "SLSTR Oblique TOA Radiance",
                "slstr_oblique_toa_radiance_ch_1");
    }

    @Test
    @STTM("SNAP-4126")
    public void testCreateSpectralAxes_l1cSkipsDescriptorsWithoutUsableBandsOrWavelengthReference() {
        final Product product = new Product("p", "FLX_L1C_FLXSYN", 4, 3);
        final Map<String, FlexVariableDescriptor> specials = new LinkedHashMap<>();
        addSpectralBand(product, "floris_toa_radiance_ch_1", 500.0f);
        addSpectralBand(product, "floris_toa_radiance_ch_2", 0.0f);
        addSpectralBand(product, "olci_toa_radiance_ch_1", 600.0f);

        specials.put("floris_toa_radiance",
                specialDescriptor("floris_toa_radiance", 3, "_ch_", "floris_spectral_channel_central_wavelengths"));
        specials.put("olci_toa_radiance",
                specialDescriptor("olci_toa_radiance", 1, "_ch_", ""));

        final List<ProductSpectralAxis> axes = FlexSpectralHelper.createSpectralAxes(product, "FLX_L1C_FLXSYN",
                specials);

        assertEquals(1, axes.size());
        assertAxis(axes.get(0), "floris_toa_radiance", "FLORIS TOA Radiance",
                "floris_toa_radiance_ch_1");
    }

    @Test
    @STTM("SNAP-4126")
    public void testCreateSpectralAxes_l2UsesAllWavelengthReferenceSeries() {
        final Product product = new Product("p", "FLX_L2_FLXSYN", 4, 3);
        final Map<String, FlexVariableDescriptor> specials = new LinkedHashMap<>();

        addL2Series(product, specials, "floris_apparent_reflectance", "floris_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "floris_apparent_reflectance_uncertainty", "floris_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "olci_apparent_reflectance", "olci_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "slstr_apparent_reflectance", "slstr_vswir_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "direct_irradiance", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "direct_irradiance_uncertainty", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "diffuse_irradiance", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "diffuse_irradiance_uncertainty", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(product, specials, "sif_emission_spectrum", "sif_spectral_grid");
        addL2Series(product, specials, "sif_emission_spectrum_uncertainty", "sif_spectral_grid");
        addL2Series(product, specials, "floris_real_reflectance", "floris_real_reflectance_spectral_grid");
        addL2Series(product, specials, "floris_real_reflectance_uncertainty", "floris_real_reflectance_spectral_grid");
        addL2Series(product, specials, "fluorescence_escape_probability", "sif_spectral_grid");
        addL2Series(product, specials, "fluorescence_escape_probability_uncertainty", "sif_spectral_grid");
        addSpectralBand(product, "sif_peak_values_peak_1", 760.0f);
        specials.put("sif_peak_values", specialDescriptor("sif_peak_values", 1, "_peak_", ""));

        final List<ProductSpectralAxis> axes = FlexSpectralHelper.createSpectralAxes(product, "FLX_L2_FLXSYN",
                specials);

        assertEquals(14, axes.size());
        assertAxis(axes.get(0), "floris_apparent_reflectance", "FLORIS Apparent Reflectance",
                "floris_apparent_reflectance_ch_1");
        assertAxis(axes.get(4), "direct_irradiance", "Direct Irradiance",
                "direct_irradiance_ch_1");
        assertAxis(axes.get(8), "sif_emission_spectrum", "SIF Emission Spectrum",
                "sif_emission_spectrum_ch_1");
        assertAxis(axes.get(10), "floris_real_reflectance", "FLORIS Real Reflectance",
                "floris_real_reflectance_ch_1");
        assertAxis(axes.get(12), "fluorescence_escape_probability", "Fluorescence Escape Probability",
                "fluorescence_escape_probability_ch_1");
        for (final ProductSpectralAxis axis : axes) {
            assertFalse(axis.getId().startsWith("sif_peak"));
        }
    }

    @Test
    @STTM("SNAP-4126")
    public void testCreateSpectralAxes_l1bCreatesSeparatedAxesAndSetsWavelengths() {
        final Product product = new Product("p", "FLX_L1B_OBS", 4, 3);
        addL1bRadianceBands(product, "HR1", 140, false);
        addL1bRadianceBands(product, "HR1", 140, true);
        addL1bRadianceBands(product, "HR2", 269, false);
        addL1bRadianceBands(product, "LR", 235, false);
        addL1bSpectralMetadata(product, "HRE1_spectral_channel_centre_wavelength", 140, 500.0f);
        addL1bSpectralMetadata(product, "HRE1_FWHM", 140, 0.1f);
        addL1bSpectralMetadata(product, "HRE2_spectral_channel_centre_wavelength", 269, 640.0f);
        addL1bSpectralMetadata(product, "HRE2_FWHM", 269, 0.2f);
        addL1bSpectralMetadata(product, "LRES_spectral_channel_centre_wavelength", 235, 700.0f);
        addL1bSpectralMetadata(product, "LRES_FWHM", 235, 0.3f);

        final List<ProductSpectralAxis> axes = FlexSpectralHelper.createSpectralAxes(product, "FLX_L1B_OBS",
                new LinkedHashMap<>());

        assertEquals(4, axes.size());
        assertEquals("floris_hr1_radiance", axes.get(0).getId());
        assertEquals("FLORIS HR1 Radiance", axes.get(0).getName());
        assertEquals(140, axes.get(0).getBandNames().size());
        assertEquals("floris_hr1_radiance_uncertainty", axes.get(1).getId());
        assertEquals(140, axes.get(1).getBandNames().size());
        assertEquals("floris_hr2_radiance", axes.get(2).getId());
        assertEquals(269, axes.get(2).getBandNames().size());
        assertEquals("floris_lr_radiance", axes.get(3).getId());
        assertEquals(235, axes.get(3).getBandNames().size());

        assertEquals(500.0f, product.getBand("FLORIS_HR1B_1_radiance").getSpectralWavelength(), 1.0e-6f);
        assertEquals(639.0f, product.getBand("FLORIS_HR1B_140_radiance").getSpectralWavelength(), 1.0e-6f);
        assertEquals(0.1f, product.getBand("FLORIS_HR1B_1_radiance").getSpectralBandwidth(), 1.0e-6f);
        assertEquals(640.0f, product.getBand("FLORIS_HR2B_1_radiance").getSpectralWavelength(), 1.0e-6f);
        assertEquals(700.0f, product.getBand("FLORIS_LRB_1_radiance").getSpectralWavelength(), 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testCreateSpectralAxes_l1bUsesFirstPositiveAcrossTrackSpectralValue() {
        final Product product = new Product("p", "FLX_L1B_OBS", 4, 3);
        product.addBand("FLORIS_HR1B_2_radiance", ProductData.TYPE_FLOAT32);
        addL1bSpectralMetadata(product, "HRE1_spectral_channel_centre_wavelength", 140, 2,
                channel -> channel == 2 ? new float[]{-1.0f, 512.5f} : new float[]{-1.0f, -1.0f});
        addL1bSpectralMetadata(product, "HRE1_FWHM", 140, 2,
                channel -> channel == 2 ? new float[]{-1.0f, 0.25f} : new float[]{-1.0f, -1.0f});

        final List<ProductSpectralAxis> axes = FlexSpectralHelper.createSpectralAxes(product, "FLX_L1B_OBS",
                new LinkedHashMap<>());

        assertEquals(1, axes.size());
        assertAxis(axes.get(0), "floris_hr1_radiance", "FLORIS HR1 Radiance",
                "FLORIS_HR1B_2_radiance");
        assertEquals(512.5f, product.getBand("FLORIS_HR1B_2_radiance").getSpectralWavelength(), 1.0e-6f);
        assertEquals(0.25f, product.getBand("FLORIS_HR1B_2_radiance").getSpectralBandwidth(), 1.0e-6f);
    }

    private static void addL2Series(Product product, Map<String, FlexVariableDescriptor> specials,
                                    String descriptorName, String wavelengthReference) {
        addSpectralBand(product, descriptorName + "_ch_1", 500.0f);
        specials.put(descriptorName, specialDescriptor(descriptorName, 1, "_ch_", wavelengthReference));
    }

    private static void addSpectralBand(Product product, String name, float wavelength) {
        final Band band = new Band(name, ProductData.TYPE_FLOAT32, product.getSceneRasterWidth(),
                product.getSceneRasterHeight());
        band.setSpectralWavelength(wavelength);
        product.addBand(band);
    }

    private static FlexVariableDescriptor specialDescriptor(String name, int depth, String depthPrefixToken,
                                                            String wavelengthReference) {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setName(name);
        descriptor.setType('s');
        descriptor.setDepth(depth);
        descriptor.setDepthPrefixToken(depthPrefixToken);
        descriptor.setWavelengthReference(wavelengthReference);
        descriptor.setDataType("float32");
        return descriptor;
    }

    private static void addL1bRadianceBands(Product product, String family, int channelCount, boolean uncertainty) {
        final String detector = uncertainty ? "U" : "B";
        final String suffix = uncertainty ? "_unc" : "";
        for (int channel = 1; channel <= channelCount; channel++) {
            product.addBand("FLORIS_" + family + detector + "_" + channel + "_radiance" + suffix,
                    ProductData.TYPE_FLOAT32);
        }
    }

    private static void addL1bSpectralMetadata(Product product, String name, int channelCount, float firstValue) {
        addL1bSpectralMetadata(product, name, channelCount, 1,
                channel -> new float[]{firstValue + channel - 1});
    }

    private static void addL1bSpectralMetadata(Product product, String name, int channelCount,
                                               int acrossTrackCount, ChannelValues valuesProvider) {
        MetadataElement baseElement = product.getMetadataRoot().getElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        if (baseElement == null) {
            baseElement = new MetadataElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
            product.getMetadataRoot().addElement(baseElement);
        }
        final float[] values = new float[channelCount * acrossTrackCount];
        for (int channel = 1; channel <= channelCount; channel++) {
            final float[] channelValues = valuesProvider.getValues(channel);
            for (int acrossTrack = 0; acrossTrack < acrossTrackCount; acrossTrack++) {
                values[(channel - 1) * acrossTrackCount + acrossTrack] = channelValues[acrossTrack];
            }
        }
        final MetadataElement element = new MetadataElement(name);
        element.addAttribute(new MetadataAttribute("value", ProductData.createInstance(values), true));
        baseElement.addElement(element);
    }

    private static void assertAxis(ProductSpectralAxis axis, String id, String name, String... bandNames) {
        assertEquals(id, axis.getId());
        assertEquals(name, axis.getName());
        assertEquals(Arrays.asList(bandNames), axis.getBandNames());
    }

    private interface ChannelValues {
        float[] getValues(int channel);
    }
}
