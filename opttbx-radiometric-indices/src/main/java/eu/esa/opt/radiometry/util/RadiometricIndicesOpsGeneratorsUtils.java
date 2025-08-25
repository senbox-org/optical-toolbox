package eu.esa.opt.radiometry.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Utility class with the common methods used by the Radiometric Indices Operators generators
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsGeneratorsUtils {

    private static RadiometricIndicesDescriptor[] radiometricIndicesDescriptors;
    private static RadiometricIndicesBandsDescriptor[] radiometricIndicesBandsDescriptors;
    private static RadiometricIndicesParametersDescriptor[] radiometricIndicesParameterDescriptors;

    static RadiometricIndicesDescriptor[] getRadiometricIndicesDescriptors() {
        if (radiometricIndicesDescriptors == null) {
            try {
                radiometricIndicesDescriptors = new ObjectMapper().readValue(RadiometricIndicesDescriptor.class.getResource("RadiometricIndicesDescriptors.json"), RadiometricIndicesDescriptor[].class);
            } catch (IOException e) {
                Logger.getLogger(RadiometricIndicesOpsGeneratorsUtils.class.getName()).severe("Fail to load the Radiometric Indices Descriptor JSON. Reason:" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return radiometricIndicesDescriptors;
    }

    static RadiometricIndicesBandsDescriptor[] getRadiometricIndicesBandsDescriptors() {
        if (radiometricIndicesBandsDescriptors == null) {
            try {
                radiometricIndicesBandsDescriptors = new ObjectMapper().readValue(RadiometricIndicesBandsDescriptor.class.getResource("RadiometricIndicesBandsDescriptors.json"), RadiometricIndicesBandsDescriptor[].class);
            } catch (IOException e) {
                Logger.getLogger(RadiometricIndicesOpsGeneratorsUtils.class.getName()).severe("Fail to load the Radiometric Indices Bands Descriptor JSON. Reason:" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return radiometricIndicesBandsDescriptors;
    }

    static RadiometricIndicesParametersDescriptor[] getRadiometricIndicesParameterDescriptors() {
        if (radiometricIndicesParameterDescriptors == null) {
            try {
                radiometricIndicesParameterDescriptors = new ObjectMapper().readValue(RadiometricIndicesBandsDescriptor.class.getResource("RadiometricIndicesParametersDescriptors.json"), RadiometricIndicesParametersDescriptor[].class);
            } catch (IOException e) {
                Logger.getLogger(RadiometricIndicesOpsGeneratorsUtils.class.getName()).severe("Fail to load the Radiometric Indices Parameters Descriptor JSON. Reason:" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return radiometricIndicesParameterDescriptors;
    }

    static String toCamelCase(String target) {
        return Character.toUpperCase(target.charAt(0)) + target.toLowerCase().substring(1);
    }
}
