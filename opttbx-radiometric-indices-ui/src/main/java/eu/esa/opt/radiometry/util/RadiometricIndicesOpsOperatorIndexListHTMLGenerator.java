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
 * Generator class used for generate the OperatorsIndexList.html file to integrate the Radiometric Indices Operators in SNAP Help, using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsOperatorIndexListHTMLGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of OperatorsIndexList.html file");
        }
        final Path radiometricIndicesOperatorsLayerXMLFileDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsLayerXMLFileDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of Radiometric Indices Operators list(OperatorsIndexList.html) file");
        }
        generateRadiometricIndicesOperatorIndexListFile(radiometricIndicesOperatorsLayerXMLFileDirectory);
    }

    private static void generateRadiometricIndicesOperatorIndexListFile(Path radiometricIndicesOperatorIndexListHTMLFileDirectory) {
        try {
            final String radiometricIndicesOperatorIndexListHTMLTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsOperatorIndexListHTMLGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_OPERATORS_INDEX_LIST_HTML_FILENAME)).toURI()));
            final StringBuilder operatorsIndexListEntryVegetationAreaContents = new StringBuilder();
            final StringBuilder operatorsIndexListEntrySoilAreaContents = new StringBuilder();
            final StringBuilder operatorsIndexListEntryBurnAreaContents = new StringBuilder();
            final StringBuilder operatorsIndexListEntryUrbanAreaContents = new StringBuilder();
            final StringBuilder operatorsIndexListEntryWaterAreaContents = new StringBuilder();
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
                final String operatorsIndexListEntryAreaContents = "\n" + OPERATOR_INDEX_LIST_ENTRY_CONTENT_AREA_TEMPLATE
                        .replace(OPERATOR_CLASS_NAME_LOWERCASE_AREA, radiometricIndicesClassName.toLowerCase())
                        .replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName)
                        .replace(RADIOMETRIC_INDICES_NAME_AREA,radiometricIndicesDescriptor.alias)
                        .replace(DESCRIPTION_AREA, descriptionAreaContents)
                        .replace(DOMAIN_AREA, toCamelCase(radiometricIndicesDescriptor.domain));
                switch (radiometricIndicesDescriptor.domain) {
                    case "vegetation":
                        operatorsIndexListEntryVegetationAreaContents.append(operatorsIndexListEntryAreaContents);
                        break;
                    case "soil":
                        operatorsIndexListEntrySoilAreaContents.append(operatorsIndexListEntryAreaContents);
                        break;
                    case "burn":
                        operatorsIndexListEntryBurnAreaContents.append(operatorsIndexListEntryAreaContents);
                        break;
                    case "urban":
                        operatorsIndexListEntryUrbanAreaContents.append(operatorsIndexListEntryAreaContents);
                        break;
                    case "water":
                        operatorsIndexListEntryWaterAreaContents.append(operatorsIndexListEntryAreaContents);
                        break;
                }
            }
            final String generatedRadiometricIndicesOperatorIndexListHTML = radiometricIndicesOperatorIndexListHTMLTemplate
                    .replace(OPERATORS_INDEX_LIST_ENTRY_VEGETATION_AREA, operatorsIndexListEntryVegetationAreaContents)
                    .replace(OPERATORS_INDEX_LIST_ENTRY_SOIL_AREA, operatorsIndexListEntrySoilAreaContents)
                    .replace(OPERATORS_INDEX_LIST_ENTRY_BURN_AREA, operatorsIndexListEntryBurnAreaContents)
                    .replace(OPERATORS_INDEX_LIST_ENTRY_URBAN_AREA, operatorsIndexListEntryUrbanAreaContents)
                    .replace(OPERATORS_INDEX_LIST_ENTRY_WATER_AREA, operatorsIndexListEntryWaterAreaContents);
            final Path generatedRadiometricIndicesOperatorIndexListHTMLFile = radiometricIndicesOperatorIndexListHTMLFileDirectory
                    .resolve("OperatorsIndexList.html");
            Files.createDirectories(generatedRadiometricIndicesOperatorIndexListHTMLFile.getParent());
            if (Files.exists(generatedRadiometricIndicesOperatorIndexListHTMLFile)) {
                Files.writeString(generatedRadiometricIndicesOperatorIndexListHTMLFile, generatedRadiometricIndicesOperatorIndexListHTML, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.writeString(generatedRadiometricIndicesOperatorIndexListHTMLFile, generatedRadiometricIndicesOperatorIndexListHTML, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsOperatorIndexListHTMLGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operators layer.xml file using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

}
