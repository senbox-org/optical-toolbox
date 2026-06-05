package eu.esa.opt.dataio.flex;

import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductSpectralAxis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FlexSpectralHelper {

    private static final Pattern L1B_SPECTRAL_BAND_PATTERN = Pattern.compile("FLORIS_(HR1|HR2|LR)[BU]_(\\d+)_radiance(_unc)?");

    private FlexSpectralHelper() {
    }

    static List<ProductSpectralAxis> createSpectralAxes(Product product, String productType,
                                                        Map<String, FlexVariableDescriptor> specialsMap) {
        final List<ProductSpectralAxis> axes = new ArrayList<>();
        if (isL1bProduct(productType)) {
            assignL1bSpectralValues(product);
            addL1bSpectralAxes(product, axes);
        } else if (isL1cProduct(productType)) {
            addL1cSpectralAxes(product, axes, specialsMap);
        } else if (isL2Product(productType)) {
            addL2SpectralAxes(product, axes, specialsMap);
        }
        return axes;
    }

    static boolean isL1bProduct(String productType) {
        return containsProductLevel(productType, "l1b");
    }

    static boolean isL1cProduct(String productType) {
        return containsProductLevel(productType, "l1c");
    }

    static boolean isL2Product(String productType) {
        return containsProductLevel(productType, "l2");
    }

    private static boolean containsProductLevel(String productType, String level) {
        return productType != null && productType.toLowerCase(Locale.ROOT).contains(level);
    }

    private static void addL1cSpectralAxes(Product product, List<ProductSpectralAxis> axes,
                                           Map<String, FlexVariableDescriptor> specialsMap) {
        addAxisFromDescriptor(product, axes, specialsMap, "floris_toa_radiance", "FLORIS TOA Radiance",
                "floris_toa_radiance");
        addAxisFromDescriptor(product, axes, specialsMap, "floris_toa_radiance_noise_uncertainty",
                "FLORIS TOA Radiance Noise Uncertainty", "floris_toa_radiance_noise_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "olci_toa_radiance", "OLCI TOA Radiance",
                "olci_toa_radiance");
        addAxisFromDescriptor(product, axes, specialsMap, "olci_toa_radiance_uncertainty",
                "OLCI TOA Radiance Uncertainty", "olci_toa_radiance_uncertainty");
        addMergedAxisFromDescriptors(product, axes, specialsMap, "slstr_nadir_toa_radiance",
                "SLSTR Nadir TOA Radiance", "slstr_nadir_toa_radiance", "slstr_nadir_tir_toa_radiance");
        addAxisFromDescriptor(product, axes, specialsMap, "slstr_nadir_brightness_temperature",
                "SLSTR Nadir Brightness Temperature", "slstr_nadir_brightness_temperature");
        addAxisFromDescriptor(product, axes, specialsMap, "slstr_nadir_toa_radiance_uncertainty",
                "SLSTR Nadir TOA Radiance Uncertainty", "slstr_nadir_toa_radiance_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "slstr_nadir_brightness_temperature_uncertainty",
                "SLSTR Nadir Brightness Temperature Uncertainty", "slstr_nadir_brightness_temperature_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "slstr_oblique_toa_radiance",
                "SLSTR Oblique TOA Radiance", "slstr_oblique_toa_radiance");
        addAxisFromDescriptor(product, axes, specialsMap, "slstr_oblique_toa_radiance_uncertainty",
                "SLSTR Oblique TOA Radiance Uncertainty", "slstr_oblique_toa_radiance_uncertainty");
    }

    private static void addL2SpectralAxes(Product product, List<ProductSpectralAxis> axes,
                                          Map<String, FlexVariableDescriptor> specialsMap) {
        addAxisFromDescriptor(product, axes, specialsMap, "floris_apparent_reflectance",
                "FLORIS Apparent Reflectance", "floris_apparent_reflectance");
        addAxisFromDescriptor(product, axes, specialsMap, "floris_apparent_reflectance_uncertainty",
                "FLORIS Apparent Reflectance Uncertainty", "floris_apparent_reflectance_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "olci_apparent_reflectance",
                "OLCI Apparent Reflectance", "olci_apparent_reflectance");
        addAxisFromDescriptor(product, axes, specialsMap, "slstr_apparent_reflectance",
                "SLSTR Apparent Reflectance", "slstr_apparent_reflectance");
        addAxisFromDescriptor(product, axes, specialsMap, "direct_irradiance", "Direct Irradiance",
                "direct_irradiance");
        addAxisFromDescriptor(product, axes, specialsMap, "direct_irradiance_uncertainty",
                "Direct Irradiance Uncertainty", "direct_irradiance_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "diffuse_irradiance", "Diffuse Irradiance",
                "diffuse_irradiance");
        addAxisFromDescriptor(product, axes, specialsMap, "diffuse_irradiance_uncertainty",
                "Diffuse Irradiance Uncertainty", "diffuse_irradiance_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "sif_emission_spectrum", "SIF Emission Spectrum",
                "sif_emission_spectrum");
        addAxisFromDescriptor(product, axes, specialsMap, "sif_emission_spectrum_uncertainty",
                "SIF Emission Spectrum Uncertainty", "sif_emission_spectrum_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "floris_real_reflectance", "FLORIS Real Reflectance",
                "floris_real_reflectance");
        addAxisFromDescriptor(product, axes, specialsMap, "floris_real_reflectance_uncertainty",
                "FLORIS Real Reflectance Uncertainty", "floris_real_reflectance_uncertainty");
        addAxisFromDescriptor(product, axes, specialsMap, "fluorescence_escape_probability",
                "Fluorescence Escape Probability", "fluorescence_escape_probability");
        addAxisFromDescriptor(product, axes, specialsMap, "fluorescence_escape_probability_uncertainty",
                "Fluorescence Escape Probability Uncertainty", "fluorescence_escape_probability_uncertainty");
    }

    private static void addAxisFromDescriptor(Product product, List<ProductSpectralAxis> axes,
                                              Map<String, FlexVariableDescriptor> specialsMap, String id, String name,
                                              String descriptorName) {
        addSpectralAxisIfNotEmpty(axes, id, name, collectDescriptorBandNames(product, specialsMap, descriptorName));
    }

    private static void addMergedAxisFromDescriptors(Product product, List<ProductSpectralAxis> axes,
                                                     Map<String, FlexVariableDescriptor> specialsMap, String id,
                                                     String name, String... descriptorNames) {
        final List<String> bandNames = new ArrayList<>();
        for (final String descriptorName : descriptorNames) {
            bandNames.addAll(collectDescriptorBandNames(product, specialsMap, descriptorName));
        }
        addSpectralAxisIfNotEmpty(axes, id, name, bandNames);
    }

    private static List<String> collectDescriptorBandNames(Product product,
                                                           Map<String, FlexVariableDescriptor> specialsMap,
                                                           String descriptorName) {
        final FlexVariableDescriptor descriptor = specialsMap.get(descriptorName);
        if (!isSpectralSeriesDescriptor(descriptor)) {
            return Collections.emptyList();
        }

        final List<String> bandNames = new ArrayList<>();
        final String baseName = descriptor.getName();
        final String token = descriptor.getDepthPrefixToken();
        for (int layer = 1; layer <= descriptor.getDepth(); layer++) {
            final Band band = product.getBand(baseName + token + layer);
            if (isUsableSpectralBand(band)) {
                bandNames.add(band.getName());
            }
        }
        return bandNames;
    }

    private static boolean isSpectralSeriesDescriptor(FlexVariableDescriptor descriptor) {
        return descriptor != null
                && descriptor.getType() == 's'
                && descriptor.getDepth() > 0
                && hasText(descriptor.getDepthPrefixToken())
                && hasText(descriptor.getWavelengthReference());
    }

    private static void assignL1bSpectralValues(Product product) {
        for (final Band band : product.getBands()) {
            final Matcher matcher = L1B_SPECTRAL_BAND_PATTERN.matcher(band.getName());
            if (!matcher.matches()) {
                continue;
            }

            final String family = matcher.group(1);
            final int channel = Integer.parseInt(matcher.group(2));
            final int channelCount = getL1bChannelCount(family);
            if (channelCount <= 0) {
                continue;
            }

            final Float wavelength = getL1bSpectralValue(product, getL1bMetadataPrefix(family)
                    + "_spectral_channel_centre_wavelength", channel, channelCount);
            if (wavelength != null) {
                band.setSpectralWavelength(wavelength);
            }

            final Float fwhm = getL1bSpectralValue(product, getL1bMetadataPrefix(family) + "_FWHM",
                    channel, channelCount);
            if (fwhm != null) {
                band.setSpectralBandwidth(fwhm);
            }
        }
    }

    private static void addL1bSpectralAxes(Product product, List<ProductSpectralAxis> axes) {
        addL1bSpectralAxis(product, axes, "floris_hr1_radiance", "FLORIS HR1 Radiance", "HR1", false);
        addL1bSpectralAxis(product, axes, "floris_hr1_radiance_uncertainty",
                "FLORIS HR1 Radiance Uncertainty", "HR1", true);
        addL1bSpectralAxis(product, axes, "floris_hr2_radiance", "FLORIS HR2 Radiance", "HR2", false);
        addL1bSpectralAxis(product, axes, "floris_hr2_radiance_uncertainty",
                "FLORIS HR2 Radiance Uncertainty", "HR2", true);
        addL1bSpectralAxis(product, axes, "floris_lr_radiance", "FLORIS LR Radiance", "LR", false);
        addL1bSpectralAxis(product, axes, "floris_lr_radiance_uncertainty",
                "FLORIS LR Radiance Uncertainty", "LR", true);
    }

    private static void addL1bSpectralAxis(Product product, List<ProductSpectralAxis> axes, String id, String name,
                                           String family, boolean uncertainty) {
        final Map<Integer, String> channelBands = new TreeMap<>();
        for (final Band band : product.getBands()) {
            final Matcher matcher = L1B_SPECTRAL_BAND_PATTERN.matcher(band.getName());
            if (!matcher.matches() || !family.equals(matcher.group(1))) {
                continue;
            }

            final boolean bandIsUncertainty = matcher.group(3) != null;
            if (bandIsUncertainty != uncertainty || !isUsableSpectralBand(band)) {
                continue;
            }

            channelBands.put(Integer.parseInt(matcher.group(2)), band.getName());
        }
        addSpectralAxisIfNotEmpty(axes, id, name, new ArrayList<>(channelBands.values()));
    }

    private static Float getL1bSpectralValue(Product product, String metadataElementName, int channel,
                                             int channelCount) {
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement baseElement = metadataRoot.getElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        if (baseElement == null) {
            return null;
        }
        final MetadataElement metadataElement = baseElement.getElement(metadataElementName);
        if (metadataElement == null) {
            return null;
        }
        final MetadataAttribute valueAttribute = metadataElement.getAttribute("value");
        if (valueAttribute == null) {
            return null;
        }
        final ProductData data = valueAttribute.getData();
        final int numElems = data.getNumElems();
        if (channel < 1 || channel > channelCount || numElems < channelCount) {
            return null;
        }

        final int acrossTrackSampleCount = Math.max(1, numElems / channelCount);
        final int startIndex = (channel - 1) * acrossTrackSampleCount;
        final int endIndex = Math.min(startIndex + acrossTrackSampleCount, numElems);
        for (int index = startIndex; index < endIndex; index++) {
            final float value = data.getElemFloatAt(index);
            if (Float.isFinite(value) && value > 0.0f) {
                return value;
            }
        }
        return null;
    }

    private static int getL1bChannelCount(String family) {
        if ("HR1".equals(family)) {
            return 140;
        } else if ("HR2".equals(family)) {
            return 269;
        } else if ("LR".equals(family)) {
            return 235;
        }
        return -1;
    }

    private static String getL1bMetadataPrefix(String family) {
        if ("HR1".equals(family)) {
            return "HRE1";
        } else if ("HR2".equals(family)) {
            return "HRE2";
        } else if ("LR".equals(family)) {
            return "LRES";
        }
        return "";
    }

    private static boolean isUsableSpectralBand(Band band) {
        return band != null && !band.isFlagBand() && band.getSpectralWavelength() > 0.0f;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static void addSpectralAxisIfNotEmpty(List<ProductSpectralAxis> axes, String id, String name,
                                                  List<String> bandNames) {
        if (!bandNames.isEmpty()) {
            axes.add(new ProductSpectralAxis(id, name, bandNames));
        }
    }
}
