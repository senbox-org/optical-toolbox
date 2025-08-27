package eu.esa.opt.radiometry.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Logger;

import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsConstants.*;
import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.*;

/**
 * Generator class used for generate the [OpName]AlgorithmSpecification.html file, which is the help page for each Radiometric Indices Operator, using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsOperatorAlgorithmSpecificationHTMLGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of [OpName]AlgorithmSpecification.html file");
        }
        final Path radiometricIndicesOperatorsLayerXMLFileDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsLayerXMLFileDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of Radiometric Indices Operator Algorithm Specification ([OpName]AlgorithmSpecification.html) file");
        }
        generateRadiometricIndicesOperatorAlgorithmSpecificationFile(radiometricIndicesOperatorsLayerXMLFileDirectory);
    }

    private static void generateRadiometricIndicesOperatorAlgorithmSpecificationFile(Path radiometricIndicesOperatorAlgorithmSpecificationHTMLFileDirectory) {
        try {
            final String radiometricIndicesOperatorAlgorithmSpecificationHTMLTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsOperatorAlgorithmSpecificationHTMLGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_OPERATOR_ALGORITHM_SPECIFICATION_HTML_FILENAME)).toURI()));
            for (RadiometricIndicesDescriptor radiometricIndicesDescriptor : getRadiometricIndicesDescriptors()) {
                final String radiometricIndicesClassName = toCamelCase(radiometricIndicesDescriptor.alias);
                final String[] descriptionParts = radiometricIndicesDescriptor.description.split(" ");
                final StringBuilder descriptionAreaContents = new StringBuilder();
                for (String descriptionPart : descriptionParts) {
                    if (descriptionPart.matches("^[A-Z].*")) {
                        descriptionAreaContents
                                .append("<u><b>")
                                .append(descriptionPart.charAt(0))
                                .append("</b></u>")
                                .append(descriptionPart.substring(1))
                                .append(" ");
                    } else {
                        descriptionAreaContents.append(descriptionPart).append(" ");
                    }
                }
                final StringBuilder sourceBandsAreaContents = new StringBuilder();
                final StringBuilder parametersAreaContents = new StringBuilder();
                for (RadiometricIndicesBandsDescriptor radiometricIndicesBandDescriptor : getRadiometricIndicesBandsDescriptors()) {
                    if (radiometricIndicesDescriptor.equation.contains(radiometricIndicesBandDescriptor.alias)) {
                        sourceBandsAreaContents
                                .append("- <b>")
                                .append(radiometricIndicesBandDescriptor.alias)
                                .append("</b> is the <b>")
                                .append(radiometricIndicesBandDescriptor.description)
                                .append("</b> band <br/>");
                    }
                }
                for (RadiometricIndicesParametersDescriptor radiometricIndicesParameterDescriptor : getRadiometricIndicesParameterDescriptors()) {
                    if (radiometricIndicesDescriptor.equation.contains(radiometricIndicesParameterDescriptor.alias)) {
                        parametersAreaContents
                                .append("- <b>")
                                .append(radiometricIndicesParameterDescriptor.alias)
                                .append("</b> is the <b>")
                                .append(radiometricIndicesParameterDescriptor.description)
                                .append("</b> coefficient <br/>");
                    }
                }
                final String parametersListAreaContent;
                if (parametersAreaContents.isEmpty()) {
                    parametersListAreaContent = "";
                } else {
                    parametersListAreaContent = PARAMETERS_LIST_CONTENT_AREA_TEMPLATE.replace(PARAMETERS_AREA, parametersAreaContents);
                }
                final String generatedRadiometricIndicesOperatorAlgorithmSpecificationHTML = radiometricIndicesOperatorAlgorithmSpecificationHTMLTemplate
                        .replace(RADIOMETRIC_INDICES_NAME_AREA, radiometricIndicesDescriptor.alias)
                        .replace(OPERATOR_CLASS_NAME_LOWERCASE_AREA, radiometricIndicesClassName.toLowerCase())
                        .replace(DESCRIPTION_AREA, descriptionAreaContents)
                        .replace(EQUATION_FORMULA_AREA, radiometricIndicesDescriptor.equation.replaceAll("([+\\-*/,])", " $1 "))
                        .replace(SOURCE_BANDS_AREA, sourceBandsAreaContents)
                        .replace(PARAMETERS_LIST_AREA, parametersListAreaContent);
                final Path generatedRadiometricIndicesOperatorAlgorithmSpecificationHTMLFile = radiometricIndicesOperatorAlgorithmSpecificationHTMLFileDirectory
                        .resolve(radiometricIndicesClassName.toLowerCase())
                        .resolve(radiometricIndicesClassName + "AlgorithmSpecification.html");
                Files.createDirectories(generatedRadiometricIndicesOperatorAlgorithmSpecificationHTMLFile.getParent());
                if (Files.exists(generatedRadiometricIndicesOperatorAlgorithmSpecificationHTMLFile)) {
                    Files.writeString(generatedRadiometricIndicesOperatorAlgorithmSpecificationHTMLFile, generatedRadiometricIndicesOperatorAlgorithmSpecificationHTML, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    Files.writeString(generatedRadiometricIndicesOperatorAlgorithmSpecificationHTMLFile, generatedRadiometricIndicesOperatorAlgorithmSpecificationHTML, StandardOpenOption.CREATE_NEW);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsOperatorAlgorithmSpecificationHTMLGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operator tool [OpName]AlgorithmSpecification.html file using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

}
