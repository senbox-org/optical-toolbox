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
 * Generator class used for generate the map.jhm file to integrate the Radiometric Indices Operators help pages in SNAP Help, using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsMapJHMGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of RadiometricIndices map.jhm file");
        }
        final Path radiometricIndicesOperatorsMapJHMFileDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsMapJHMFileDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of RadiometricIndices help structure descriptor (map.jhm) file");
        }
        generateRadiometricIndicesOperatorsMapFile(radiometricIndicesOperatorsMapJHMFileDirectory);
    }

    private static void generateRadiometricIndicesOperatorsMapFile(Path radiometricIndicesOperatorsMapJHMFileDirectory) {
        try {
            final String radiometricIndicesOpsMapJHMTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsMapJHMGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_MAP_JHM_FILENAME)).toURI()));
            final StringBuilder operatorsMapEntryAreaContents = new StringBuilder();
            for (RadiometricIndicesDescriptor radiometricIndicesDescriptor : getRadiometricIndicesDescriptors()) {
                final String radiometricIndicesClassName = toCamelCase(radiometricIndicesDescriptor.alias);
                final String operatorsMapEntryAreaContent = OPERATOR_MAP_ENTRY_CONTENT_AREA_TEMPLATE
                        .replace(RADIOMETRIC_INDICES_NAME_AREA, radiometricIndicesDescriptor.alias)
                        .replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName)
                        .replace(OPERATOR_CLASS_NAME_LOWERCASE_AREA, radiometricIndicesClassName.toLowerCase());
                operatorsMapEntryAreaContents.append("\n").append(operatorsMapEntryAreaContent);
            }
            final String generatedRadiometricIndicesOpsMapJHM = radiometricIndicesOpsMapJHMTemplate
                    .replace(OPERATORS_MAP_ENTRY_AREA, operatorsMapEntryAreaContents);
            final Path generatedRadiometricIndicesOpsMapJHMFile = radiometricIndicesOperatorsMapJHMFileDirectory
                    .resolve("map.jhm");
            Files.createDirectories(generatedRadiometricIndicesOpsMapJHMFile.getParent());
            if (Files.exists(generatedRadiometricIndicesOpsMapJHMFile)) {
                Files.writeString(generatedRadiometricIndicesOpsMapJHMFile, generatedRadiometricIndicesOpsMapJHM, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.writeString(generatedRadiometricIndicesOpsMapJHMFile, generatedRadiometricIndicesOpsMapJHM, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsMapJHMGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operators map.jhm file using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

}
