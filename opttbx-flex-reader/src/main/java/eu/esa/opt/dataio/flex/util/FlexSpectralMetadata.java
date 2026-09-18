package eu.esa.opt.dataio.flex.util;

import eu.esa.opt.dataio.flex.FlexProductReader;
import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public final class FlexSpectralMetadata {


    private FlexSpectralMetadata() {
    }


    public static Map<String, float[]> resolveReferences(Product product,
                                                         Collection<FlexVariableDescriptor> descriptors) {
        final Map<String, Integer> referenceChannelCounts = new HashMap<>();
        for (final FlexVariableDescriptor descriptor : descriptors) {
            registerReference(referenceChannelCounts, descriptor.getWavelengthReference(), descriptor.getDepth());
            registerReference(referenceChannelCounts, descriptor.getFwhmReference(), descriptor.getDepth());
        }

        final Map<String, float[]> references = new HashMap<>();
        for (final Map.Entry<String, Integer> entry : referenceChannelCounts.entrySet()) {
            references.put(entry.getKey(), resolveReference(product, entry.getKey(), entry.getValue()));
        }
        return references;
    }

    public static float[] resolveReference(Product product, String metadataElementName, int channelCount) {
        if (!hasText(metadataElementName) || channelCount <= 0) {
            return new float[0];
        }

        final MetadataElement netcdfElement = product.getMetadataRoot().getElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        if (netcdfElement == null) {
            return new float[0];
        }

        final MetadataElement metadataElement = netcdfElement.getElement(metadataElementName);
        if (metadataElement == null) {
            return new float[0];
        }

        final MetadataAttribute valueAttribute = metadataElement.getAttribute("value");
        if (valueAttribute == null) {
            return new float[0];
        }

        final ProductData data = valueAttribute.getData();
        final int valueCount = data.getNumElems();
        if (valueCount < channelCount) {
            return new float[0];
        }

        final Float fillValue = getFillValue(metadataElement);
        final float[] values = new float[channelCount];
        Arrays.fill(values, Float.NaN);

        if (valueCount == channelCount || valueCount % channelCount != 0) {
            resolveOneDimensionalReference(data, channelCount, fillValue, values);
        } else {
            resolveTwoDimensionalReference(data, channelCount, valueCount / channelCount, fillValue, values);
        }
        return values;
    }

    public static void setSpectralWavelength(Band band, float[] values, int channel) {
        final Float value = getValue(values, channel);
        if (value != null) {
            band.setSpectralWavelength(value);
        }
    }

    public static void setSpectralBandwidth(Band band, float[] values, int channel) {
        final Float value = getValue(values, channel);
        if (value != null) {
            band.setSpectralBandwidth(value);
        }
    }

    private static void resolveOneDimensionalReference(ProductData data, int channelCount, Float fillValue,
                                                       float[] values) {
        for (int channel = 0; channel < channelCount; channel++) {
            final float value = data.getElemFloatAt(channel);
            if (isUsableSpectralValue(value, fillValue)) {
                values[channel] = value;
            }
        }
    }

    private static void resolveTwoDimensionalReference(ProductData data, int channelCount, int sampleCount,
                                                       Float fillValue, float[] values) {
        final float[] channelValues = new float[sampleCount];
        for (int channel = 0; channel < channelCount; channel++) {
            int validCount = 0;
            for (int sample = 0; sample < sampleCount; sample++) {
                final float value = data.getElemFloatAt(sample * channelCount + channel);
                if (isUsableSpectralValue(value, fillValue)) {
                    channelValues[validCount++] = value;
                }
            }
            if (validCount > 0) {
                values[channel] = median(channelValues, validCount);
            }
        }
    }

    private static float median(float[] values, int validCount) {
        Arrays.sort(values, 0, validCount);
        final int middle = validCount / 2;
        if (validCount % 2 == 1) {
            return values[middle];
        }
        return (values[middle - 1] + values[middle]) / 2.0f;
    }

    private static Float getValue(float[] values, int channel) {
        if (values == null || channel <= 0 || channel > values.length) {
            return null;
        }
        final float value = values[channel - 1];
        if (!Float.isFinite(value) || value <= 0.0f) {
            return null;
        }
        return value;
    }

    private static void registerReference(Map<String, Integer> referenceChannelCounts, String referenceName, int channelCount) {
        if (hasText(referenceName) && channelCount > 0) {
            referenceChannelCounts.merge(referenceName, channelCount, Math::max);
        }
    }

    private static boolean isUsableSpectralValue(float value, Float fillValue) {
        return Float.isFinite(value) && value > 0.0f && (fillValue == null || Float.compare(value, fillValue) != 0);
    }

    private static Float getFillValue(MetadataElement metadataElement) {
        final MetadataAttribute fillValueAttribute = metadataElement.getAttribute("_FillValue");
        if (fillValueAttribute == null || fillValueAttribute.getData().getNumElems() == 0) {
            return null;
        }
        return fillValueAttribute.getData().getElemFloatAt(0);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
