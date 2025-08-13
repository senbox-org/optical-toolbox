package eu.esa.opt.radiometry.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Logger;

import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsConstants.*;
import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.getRadiometricIndicesDescriptors;
import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.toCamelCase;

/**
 * Generator class used for generate the [OpName]Toll.html file, which is the index file for each Radiometric Indices Operator help page, using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsOperatorToolHTMLGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of [OpName]Tool.html file");
        }
        final Path radiometricIndicesOperatorsLayerXMLFileDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsLayerXMLFileDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of Radiometric Indices Operator tool ([OpName]Tool.html) file");
        }
        generateRadiometricIndicesOperatorToolFile(radiometricIndicesOperatorsLayerXMLFileDirectory);
    }

    private static void generateRadiometricIndicesOperatorToolFile(Path radiometricIndicesOperatorToolHTMLFileDirectory) {
        try {
            final String radiometricIndicesOperatorToolHTMLTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsOperatorToolHTMLGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_OPERATOR_TOOL_HTML_FILENAME)).toURI()));
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
                final String generatedRadiometricIndicesOperatorToolHTML = radiometricIndicesOperatorToolHTMLTemplate
                        .replace(RADIOMETRIC_INDICES_NAME_AREA, radiometricIndicesDescriptor.alias)
                        .replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName)
                        .replace(DESCRIPTION_AREA, descriptionAreaContents);
                final Path generatedRadiometricIndicesOperatorToolHTMLFile = radiometricIndicesOperatorToolHTMLFileDirectory
                        .resolve(radiometricIndicesClassName.toLowerCase())
                        .resolve(radiometricIndicesClassName + "Tool.html");
                Files.createDirectories(generatedRadiometricIndicesOperatorToolHTMLFile.getParent());
                if (Files.exists(generatedRadiometricIndicesOperatorToolHTMLFile)) {
                    Files.writeString(generatedRadiometricIndicesOperatorToolHTMLFile, generatedRadiometricIndicesOperatorToolHTML, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    Files.writeString(generatedRadiometricIndicesOperatorToolHTMLFile, generatedRadiometricIndicesOperatorToolHTML, StandardOpenOption.CREATE_NEW);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsOperatorToolHTMLGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operator tool [OpName]Tool.html file using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

}
