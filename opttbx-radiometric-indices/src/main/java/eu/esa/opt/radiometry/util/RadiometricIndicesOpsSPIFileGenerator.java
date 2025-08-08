package eu.esa.opt.radiometry.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.getRadiometricIndicesDescriptors;
import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.toCamelCase;

/**
 * Generator class used for generate the SPI file to integrate the Radiometric Indices Operators classes in SNAP, using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsSPIFileGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of RadiometricIndicesOp SPI file");
        }
        final Path radiometricIndicesOperatorsSPIFileDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsSPIFileDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of RadiometricIndicesOp SPI file");
        }
        generateRadiometricIndicesOperatorsSPIFile(radiometricIndicesOperatorsSPIFileDirectory);
    }

    private static void generateRadiometricIndicesOperatorsSPIFile(Path radiometricIndicesOperatorsSPIFileDirectory) {
        try {
            final StringBuilder spiFileContent = new StringBuilder();
            for (RadiometricIndicesDescriptor radiometricIndicesDescriptor : getRadiometricIndicesDescriptors()) {
                final String radiometricIndicesClassName = toCamelCase(radiometricIndicesDescriptor.alias);
                final String generatedRadiometricIndicesOperatorClass = "eu.esa.opt.spectral." + radiometricIndicesDescriptor.domain + "." + radiometricIndicesClassName;
                final String spiFileEntry = generatedRadiometricIndicesOperatorClass + "Op$Spi";
                spiFileContent.append(spiFileEntry).append("\n");

            }
            final Path radiometricIndicesOperatorsSPIFile = radiometricIndicesOperatorsSPIFileDirectory
                    .resolve("META-INF")
                    .resolve("services")
                    .resolve("org.esa.snap.core.gpf.OperatorSpi");
            Files.createDirectories(radiometricIndicesOperatorsSPIFile.getParent());
            if (Files.exists(radiometricIndicesOperatorsSPIFile)) {
                Files.writeString(radiometricIndicesOperatorsSPIFile, spiFileContent, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.writeString(radiometricIndicesOperatorsSPIFile, spiFileContent, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            Logger.getLogger(RadiometricIndicesOpsSPIFileGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operators SPI file using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

}
