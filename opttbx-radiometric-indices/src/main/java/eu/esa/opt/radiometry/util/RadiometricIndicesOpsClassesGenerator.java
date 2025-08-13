package eu.esa.opt.radiometry.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsConstants.*;
import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.*;

/**
 * Generator class used for generate the Radiometric Indices Operators classes using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsClassesGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of RadiometricIndicesOp classes");
        }
        final Path radiometricIndicesOperatorsClassesDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsClassesDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of RadiometricIndicesOp classes");
        }
        generateRadiometricIndicesOperatorsClasses(radiometricIndicesOperatorsClassesDirectory);
    }

    private static void generateRadiometricIndicesOperatorsClasses(Path radiometricIndicesOperatorsClassesDirectory) {
        try {
            final String radiometricIndicesOpClassTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsClassesGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_OP_FILENAME)).toURI()));
            for (RadiometricIndicesDescriptor radiometricIndicesDescriptor : getRadiometricIndicesDescriptors()) {
                final String radiometricIndicesClassName = toCamelCase(radiometricIndicesDescriptor.alias);
                final String radiometricIndicesLowercaseClassName = radiometricIndicesDescriptor.alias.toLowerCase();
                final StringBuilder parametersAreaContents = new StringBuilder();
                final StringBuilder sourceBandsAreaContents = new StringBuilder();
                final StringBuilder tileVarsAreaContents = new StringBuilder();
                final StringBuilder equationVarsAreaContents = new StringBuilder();
                final StringBuilder powMethodAreaContent = new StringBuilder();
                String equationFormulaAreaContent = radiometricIndicesDescriptor.equation.replaceAll("(\\d+\\.\\d+)", "$1f");
                for (RadiometricIndicesBandsDescriptor radiometricIndicesBandsDescriptor : getRadiometricIndicesBandsDescriptors()) {
                    if (equationFormulaAreaContent.contains(radiometricIndicesBandsDescriptor.alias)) {
                        final String sourceBandName = radiometricIndicesBandsDescriptor.description;
                        final String sourceBandNameCc = toCamelCase(sourceBandName);
                        final String[] sourceBandNameParts = sourceBandName.split("\\s+");
                        final String sourceBandVarName;
                        if (sourceBandNameParts.length > 1) {
                            sourceBandNameParts[0] = sourceBandNameParts[0].toLowerCase();
                            sourceBandVarName = String.join("", sourceBandNameParts);
                        } else {
                            sourceBandVarName = sourceBandName.toLowerCase().replaceAll("\\s+", "");
                        }
                        final String sourceBandMinWavelength = "" + radiometricIndicesBandsDescriptor.minSpectralRange;
                        final String sourceBandMaxWavelength = "" + radiometricIndicesBandsDescriptor.maxSpectralRange;
                        final String equationVarName;
                        if (radiometricIndicesBandsDescriptor.alias.equalsIgnoreCase("y")) {
                            equationVarName = "yw";
                        } else {
                            equationVarName = radiometricIndicesBandsDescriptor.alias.toLowerCase();
                        }
                        sourceBandsAreaContents.append("\n\t").append(SOURCE_BANDS_AREA_CONTENT_TEMPLATE
                                .replace(SOURCE_BAND_NAME_CC_AREA, sourceBandNameCc)
                                .replace(SOURCE_BAND_NAME_AREA, sourceBandName)
                                .replace(SOURCE_BAND_VAR_NAME_AREA, sourceBandVarName)
                                .replace(SOURCE_BAND_MIN_WAVELENGTH_AREA, sourceBandMinWavelength)
                                .replace(SOURCE_BAND_MAX_WAVELENGTH_AREA, sourceBandMaxWavelength)).append("\n");
                        tileVarsAreaContents.append("\n\t\t\t").append(TILE_VARS_AREA_CONTENT_TEMPLATE
                                .replace(SOURCE_BAND_VAR_NAME_AREA, sourceBandVarName));
                        equationVarsAreaContents.append("\n\t\t\t\t\t").append(EQUATION_VARS_AREA_CONTENT_TEMPLATE
                                .replace(EQUATION_VAR_NAME_AREA, equationVarName)
                                .replace(SOURCE_BAND_VAR_NAME_AREA, sourceBandVarName));
                        equationFormulaAreaContent = equationFormulaAreaContent.replace(radiometricIndicesBandsDescriptor.alias, equationVarName);
                    }
                }
                for (RadiometricIndicesParametersDescriptor radiometricIndicesParametersDescriptor : getRadiometricIndicesParameterDescriptors()) {
                    if (radiometricIndicesDescriptor.equation.contains(radiometricIndicesParametersDescriptor.alias)) {
                        equationFormulaAreaContent = equationFormulaAreaContent.replace(radiometricIndicesParametersDescriptor.alias.toLowerCase(), radiometricIndicesParametersDescriptor.alias);
                        parametersAreaContents.append("\n\t").append(PARAMETERS_AREA_CONTENT_TEMPLATE
                                .replace(PARAMETER_NAME_CC_AREA, toCamelCase(radiometricIndicesParametersDescriptor.alias))
                                .replace(PARAMETER_NAME_AREA, radiometricIndicesParametersDescriptor.alias)).append("\n");
                    }
                }

                if (radiometricIndicesDescriptor.equation.contains("pow")) {
                    powMethodAreaContent.append(POW_METHOD_AREA_CONTENT);
                }
                if (radiometricIndicesDescriptor.equation.contains("sqrt")) {
                    powMethodAreaContent.append(SQRT_METHOD_AREA_CONTENT);
                }
                final String generatedRadiometricIndicesOpClass = radiometricIndicesOpClassTemplate
                        .replace(DOMAIN_AREA, radiometricIndicesDescriptor.domain)
                        .replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName)
                        .replace(OPERATOR_CLASS_NAME_LOWERCASE_AREA, radiometricIndicesLowercaseClassName)
                        .replace(CATEGORY_AREA, radiometricIndicesDescriptor.category)
                        .replace(DESCRIPTION_AREA, radiometricIndicesDescriptor.description)
                        .replace(PARAMETERS_AREA, parametersAreaContents.toString())
                        .replace(SOURCE_BANDS_AREA, sourceBandsAreaContents.toString())
                        .replace(TILE_VARS_AREA, tileVarsAreaContents.toString())
                        .replace(EQUATION_VARS_AREA, equationVarsAreaContents.toString())
                        .replace(EQUATION_FORMULA_AREA, equationFormulaAreaContent)
                        .replace(POW_METHOD_AREA, powMethodAreaContent.toString());
                final Path generatedRadiometricIndicesOpClassFile = radiometricIndicesOperatorsClassesDirectory
                        .resolve(radiometricIndicesDescriptor.domain)
                        .resolve(radiometricIndicesClassName + "Op.java");
                Files.createDirectories(generatedRadiometricIndicesOpClassFile.getParent());
                if (Files.exists(generatedRadiometricIndicesOpClassFile)) {
                    Files.writeString(generatedRadiometricIndicesOpClassFile, generatedRadiometricIndicesOpClass, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    Files.writeString(generatedRadiometricIndicesOpClassFile, generatedRadiometricIndicesOpClass, StandardOpenOption.CREATE_NEW);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsClassesGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operators classes using the JSON descriptors. Reason:" + e.getMessage());
        }
    }
}
