package eu.esa.opt.radiometry.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Logger;

import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsConstants.*;
import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.getRadiometricIndicesDescriptors;

/**
 * Generator class used for generate the toc.xml file to integrate and organize the Radiometric Indices Operators help pages in SNAP Help, using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsTocXMLGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of RadiometricIndices toc.xml file");
        }
        final Path radiometricIndicesOperatorsTocXMLFileDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsTocXMLFileDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of RadiometricIndices help descriptor (toc.xml) file");
        }
        generateRadiometricIndicesOperatorsTocFile(radiometricIndicesOperatorsTocXMLFileDirectory);
    }

    private static void generateRadiometricIndicesOperatorsTocFile(Path radiometricIndicesOperatorsTocXMLFileDirectory) {
        try {
            final String radiometricIndicesOpsTocXMLTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsTocXMLGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_TOC_XML_FILENAME)).toURI()));
            final StringBuilder operatorsTocEntryVegetationAreaContents = new StringBuilder();
            final StringBuilder operatorsTocEntrySoilAreaContents = new StringBuilder();
            final StringBuilder operatorsTocEntryBurnAreaContents = new StringBuilder();
            final StringBuilder operatorsTocEntryUrbanAreaContents = new StringBuilder();
            final StringBuilder operatorsTocEntryWaterAreaContents = new StringBuilder();
            for (RadiometricIndicesDescriptor radiometricIndicesDescriptor : getRadiometricIndicesDescriptors()) {
                final String operatorsTocEntryAreaContents = OPERATOR_TOC_ENTRY_CONTENT_AREA_TEMPLATE
                        .replace(RADIOMETRIC_INDICES_NAME_AREA, radiometricIndicesDescriptor.alias);
                switch (radiometricIndicesDescriptor.domain) {
                    case "vegetation":
                        operatorsTocEntryVegetationAreaContents.append(operatorsTocEntryAreaContents);
                        break;
                    case "soil":
                        operatorsTocEntrySoilAreaContents.append(operatorsTocEntryAreaContents);
                        break;
                    case "burn":
                        operatorsTocEntryBurnAreaContents.append(operatorsTocEntryAreaContents);
                        break;
                    case "urban":
                        operatorsTocEntryUrbanAreaContents.append(operatorsTocEntryAreaContents);
                        break;
                    case "water":
                        operatorsTocEntryWaterAreaContents.append(operatorsTocEntryAreaContents);
                        break;
                }
            }
            final String generatedRadiometricIndicesOpsTocXML = radiometricIndicesOpsTocXMLTemplate
                    .replace(OPERATORS_TOC_ENTRY_VEGETATION_AREA, operatorsTocEntryVegetationAreaContents)
                    .replace(OPERATORS_TOC_ENTRY_SOIL_AREA, operatorsTocEntrySoilAreaContents)
                    .replace(OPERATORS_TOC_ENTRY_BURN_AREA, operatorsTocEntryBurnAreaContents)
                    .replace(OPERATORS_TOC_ENTRY_URBAN_AREA, operatorsTocEntryUrbanAreaContents)
                    .replace(OPERATORS_TOC_ENTRY_WATER_AREA, operatorsTocEntryWaterAreaContents);
            final Path generatedRadiometricIndicesOpsTocXMLFile = radiometricIndicesOperatorsTocXMLFileDirectory
                    .resolve("toc.xml");
            Files.createDirectories(generatedRadiometricIndicesOpsTocXMLFile.getParent());
            if (Files.exists(generatedRadiometricIndicesOpsTocXMLFile)) {
                Files.writeString(generatedRadiometricIndicesOpsTocXMLFile, generatedRadiometricIndicesOpsTocXML, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.writeString(generatedRadiometricIndicesOpsTocXMLFile, generatedRadiometricIndicesOpsTocXML, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsTocXMLGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operators toc.xml file using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

}
