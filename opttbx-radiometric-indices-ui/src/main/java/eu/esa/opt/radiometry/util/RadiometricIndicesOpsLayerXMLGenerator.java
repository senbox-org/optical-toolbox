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
 * Generator class used for generate the layer.xml file to integrate the Radiometric Indices Operators in SNAP UI, using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsLayerXMLGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of RadiometricIndicesOp layer.xml file");
        }
        final Path radiometricIndicesOperatorsLayerXMLFileDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsLayerXMLFileDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of RadiometricIndices UI descriptor (layer.xml) file");
        }
        generateRadiometricIndicesOperatorsLayerFile(radiometricIndicesOperatorsLayerXMLFileDirectory);
    }

    private static void generateRadiometricIndicesOperatorsLayerFile(Path radiometricIndicesOperatorsLayerXMLFileDirectory) {
        try {
            final String radiometricIndicesOpsLayerXMLTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsLayerXMLGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_LAYER_XML_FILENAME)).toURI()));
            final StringBuilder operatorsUisAreaContents = new StringBuilder();
            final StringBuilder operatorsUisActionsAreaContents = new StringBuilder();
            final StringBuilder operatorsUisMenuEntryVegetationAreaContents = new StringBuilder();
            final StringBuilder operatorsUisMenuEntrySoilAreaContents = new StringBuilder();
            final StringBuilder operatorsUisMenuEntryBurnAreaContents = new StringBuilder();
            final StringBuilder operatorsUisMenuEntryUrbanAreaContents = new StringBuilder();
            final StringBuilder operatorsUisMenuEntryWaterAreaContents = new StringBuilder();
            for (RadiometricIndicesDescriptor radiometricIndicesDescriptor : getRadiometricIndicesDescriptors()) {
                final String radiometricIndicesClassName = toCamelCase(radiometricIndicesDescriptor.alias);
                operatorsUisAreaContents.append(OPERATOR_UI_CONTENT_AREA_TEMPLATE.replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName));
                operatorsUisActionsAreaContents.append(OPERATOR_UI_ACTIONS_CONTENT_AREA_TEMPLATE
                        .replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName)
                        .replace(RADIOMETRIC_INDICES_NAME_AREA, radiometricIndicesDescriptor.alias)
                        .replace(OPERATOR_CLASS_NAME_LOWERCASE_AREA, radiometricIndicesDescriptor.alias.toLowerCase()));
                final String operatorsUisMenuEntryAreaContents = OPERATORS_UIS_MENU_ENTRY_CONTENT_AREA_TEMPLATE
                        .replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName);
                switch (radiometricIndicesDescriptor.domain) {
                    case "vegetation":
                        operatorsUisMenuEntryVegetationAreaContents.append(operatorsUisMenuEntryAreaContents);
                        break;
                    case "soil":
                        operatorsUisMenuEntrySoilAreaContents.append(operatorsUisMenuEntryAreaContents);
                        break;
                    case "burn":
                        operatorsUisMenuEntryBurnAreaContents.append(operatorsUisMenuEntryAreaContents);
                        break;
                    case "urban":
                        operatorsUisMenuEntryUrbanAreaContents.append(operatorsUisMenuEntryAreaContents);
                        break;
                    case "water":
                        operatorsUisMenuEntryWaterAreaContents.append(operatorsUisMenuEntryAreaContents);
                        break;
                }
            }
            final String generatedRadiometricIndicesOpsLayerXML = radiometricIndicesOpsLayerXMLTemplate
                    .replace(OPERATORS_UIS_AREA, operatorsUisAreaContents)
                    .replace(OPERATORS_UIS_ACTIONS_AREA, operatorsUisActionsAreaContents)
                    .replace(OPERATORS_UIS_MENU_ENTRY_VEGETATION_AREA, operatorsUisMenuEntryVegetationAreaContents)
                    .replace(OPERATORS_UIS_MENU_ENTRY_SOIL_AREA, operatorsUisMenuEntrySoilAreaContents)
                    .replace(OPERATORS_UIS_MENU_ENTRY_BURN_AREA, operatorsUisMenuEntryBurnAreaContents)
                    .replace(OPERATORS_UIS_MENU_ENTRY_URBAN_AREA, operatorsUisMenuEntryUrbanAreaContents)
                    .replace(OPERATORS_UIS_MENU_ENTRY_WATER_AREA, operatorsUisMenuEntryWaterAreaContents);
            final Path generatedRadiometricIndicesOpsLayerXMLFile = radiometricIndicesOperatorsLayerXMLFileDirectory
                    .resolve("layer.xml");
            Files.createDirectories(generatedRadiometricIndicesOpsLayerXMLFile.getParent());
            if (Files.exists(generatedRadiometricIndicesOpsLayerXMLFile)) {
                Files.writeString(generatedRadiometricIndicesOpsLayerXMLFile, generatedRadiometricIndicesOpsLayerXML, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.writeString(generatedRadiometricIndicesOpsLayerXMLFile, generatedRadiometricIndicesOpsLayerXML, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsLayerXMLGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operators layer.xml file using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

}
