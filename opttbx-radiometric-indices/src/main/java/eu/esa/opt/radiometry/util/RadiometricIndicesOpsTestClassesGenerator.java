package eu.esa.opt.radiometry.util;

import eu.esa.opt.radiometry.BaseIndexOp;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.annotations.Parameter;

import java.awt.image.Raster;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsConstants.*;
import static eu.esa.opt.radiometry.util.RadiometricIndicesOpsGeneratorsUtils.*;

/**
 * Generator class used for generate the Radiometric Indices Operators jUnit test classes using the JSON descriptors
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsTestClassesGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("please provide the following:\n- directory for generation of RadiometricIndicesOp test classes");
        }
        final Path radiometricIndicesOperatorsTestClassesDirectory = Path.of(args[0]);
        if (!Files.exists(radiometricIndicesOperatorsTestClassesDirectory)) {
            throw new IllegalArgumentException("invalid outputDirectory for generation of RadiometricIndicesTestOp classes");
        }
        generateRadiometricIndicesOperatorsTestClasses(radiometricIndicesOperatorsTestClassesDirectory);
    }

    private static void generateRadiometricIndicesOperatorsTestClasses(Path radiometricIndicesOperatorsTestClassesDirectory) {
        try {
            final String radiometricIndicesOpTestClassTemplate = Files.readString(Paths.get(Objects.requireNonNull(RadiometricIndicesOpsTestClassesGenerator.class.getResource(RADIOMETRIC_INDICES_TEMPLATE_OP_TEST_FILENAME)).toURI()));
            final Random rnd = new Random();
            for (RadiometricIndicesDescriptor radiometricIndicesDescriptor : getRadiometricIndicesDescriptors()) {
                final String radiometricIndicesClassName = toCamelCase(radiometricIndicesDescriptor.alias);
                String equationFormulaAreaContent = radiometricIndicesDescriptor.equation.replaceAll("(\\d+\\.\\d+)", "$1f");
                final StringBuilder operatorBandsNamesAreaContents = new StringBuilder();
                final StringBuilder operatorBandsWavelengthsAreaContents = new StringBuilder();
                final StringBuilder operatorBandsMinValuesAreaContents = new StringBuilder();
                final StringBuilder operatorBandsMaxValuesAreaContents = new StringBuilder();
                final StringBuilder operatorParametersMapValuesAreaContents = new StringBuilder();
                final Map<String, Float> operatorParameters = new LinkedHashMap<>();
                final StringBuilder operatorTargetValuesAreaContents = new StringBuilder();
                for (RadiometricIndicesBandsDescriptor radiometricIndicesBandsDescriptor : getRadiometricIndicesBandsDescriptors()) {
                    if (equationFormulaAreaContent.contains(radiometricIndicesBandsDescriptor.alias)) {
                        final String sourceBandName = radiometricIndicesBandsDescriptor.description;
                        final String[] sourceBandNameParts = sourceBandName.split("\\s+");
                        final String sourceBandVarName;
                        if (sourceBandNameParts.length > 1) {
                            sourceBandNameParts[0] = sourceBandNameParts[0].toLowerCase();
                            sourceBandVarName = String.join("", sourceBandNameParts);
                        } else {
                            sourceBandVarName = sourceBandName.toLowerCase().replaceAll("\\s+", "");
                        }
                        if (!operatorBandsNamesAreaContents.isEmpty()) {
                            operatorBandsNamesAreaContents.append(", ");
                        }
                        operatorBandsNamesAreaContents.append("\"").append(sourceBandVarName).append("\"");
                        if (!operatorBandsWavelengthsAreaContents.isEmpty()) {
                            operatorBandsWavelengthsAreaContents.append(", ");
                        }
                        operatorBandsWavelengthsAreaContents.append(radiometricIndicesBandsDescriptor.minSpectralRange + ((radiometricIndicesBandsDescriptor.maxSpectralRange - radiometricIndicesBandsDescriptor.minSpectralRange) / 2 % 10 * 10));
                        if (!operatorBandsMinValuesAreaContents.isEmpty()) {
                            operatorBandsMinValuesAreaContents.append(", ");
                        }
                        final float bandValue1 = rnd.nextFloat();
                        final float bandValue2 = rnd.nextFloat();
                        operatorBandsMinValuesAreaContents.append(Math.min(bandValue1, bandValue2)).append("f");
                        if (!operatorBandsMaxValuesAreaContents.isEmpty()) {
                            operatorBandsMaxValuesAreaContents.append(", ");
                        }
                        operatorBandsMaxValuesAreaContents.append(Math.max(bandValue1, bandValue2)).append("f");
                    }
                }
                for (RadiometricIndicesParametersDescriptor radiometricIndicesParametersDescriptor : getRadiometricIndicesParameterDescriptors()) {
                    if (radiometricIndicesDescriptor.equation.contains(radiometricIndicesParametersDescriptor.alias)) {
                        final float parameterValue = rnd.nextFloat();
                        operatorParametersMapValuesAreaContents.append(OPERATOR_PARAMETERS_MAP_VALUES_CONTENT_AREA_TEMPLATE
                                .replace(PARAMETER_NAME_AREA, radiometricIndicesParametersDescriptor.alias)
                                .replace(PARAMETER_VALUE_AREA, parameterValue + "f"));
                        operatorParameters.put(radiometricIndicesParametersDescriptor.alias, parameterValue);
                    }
                }
                final String operatorParametersMapValuesAreaContent;
                if (operatorParametersMapValuesAreaContents.isEmpty()) {
                    operatorParametersMapValuesAreaContent = "";
                } else {
                    operatorParametersMapValuesAreaContent = OPERATOR_PARAMETERS_MAP_CONTENT_AREA_TEMPLATE.replace(OPERATOR_PARAMETERS_MAP_VALUES_AREA, operatorParametersMapValuesAreaContents);
                }
                final String generatedRadiometricIndicesOperatorClass = "eu.esa.opt.spectral." + radiometricIndicesDescriptor.domain + "." + radiometricIndicesClassName + "Op";
                @SuppressWarnings("unchecked") final Class<? extends BaseIndexOp> radiometricIndicesOperatorClass = (Class<? extends BaseIndexOp>) Class.forName(generatedRadiometricIndicesOperatorClass);
                final Float[] radiometricIndicesOperatorClassExpectedTestValues = generateExpectedValuesForTest(
                        radiometricIndicesOperatorClass,
                        operatorBandsNamesAreaContents.toString().replace("\"", "").split(", "),
                        OPERATOR_PRODUCT_WIDTH,
                        OPERATOR_PRODUCT_HEIGHT,
                        operatorBandsWavelengthsAreaContents.toString().split(", "),
                        operatorBandsMinValuesAreaContents.toString().split(", "),
                        operatorBandsMaxValuesAreaContents.toString().split(", "),
                        operatorParameters);
                for (float radiometricIndicesOperatorClassExpectedTestValue : radiometricIndicesOperatorClassExpectedTestValues) {
                    if (!operatorTargetValuesAreaContents.isEmpty()) {
                        operatorTargetValuesAreaContents.append(", ");
                    }
                    operatorTargetValuesAreaContents.append(radiometricIndicesOperatorClassExpectedTestValue).append("f");
                }
                final String generatedRadiometricIndicesOpTestClass = radiometricIndicesOpTestClassTemplate
                        .replace(DOMAIN_AREA, radiometricIndicesDescriptor.domain)
                        .replace(OPERATOR_CLASS_NAME_AREA, radiometricIndicesClassName)
                        .replace(OPERATOR_PRODUCT_WIDTH_AREA, "" + OPERATOR_PRODUCT_WIDTH)
                        .replace(OPERATOR_PRODUCT_HEIGHT_AREA, "" + OPERATOR_PRODUCT_HEIGHT)
                        .replace(OPERATOR_BANDS_NAMES_AREA, operatorBandsNamesAreaContents)
                        .replace(OPERATOR_BANDS_WAVELENGTHS_AREA, operatorBandsWavelengthsAreaContents)
                        .replace(OPERATOR_BANDS_MIN_VALUES_AREA, operatorBandsMinValuesAreaContents)
                        .replace(OPERATOR_BANDS_MAX_VALUES_AREA, operatorBandsMaxValuesAreaContents)
                        .replace(OPERATOR_PARAMETERS_MAP_AREA, operatorParametersMapValuesAreaContent)
                        .replace(OPERATOR_TARGET_VALUES_AREA, operatorTargetValuesAreaContents);
                final Path generatedRadiometricIndicesOpTestClassFile = radiometricIndicesOperatorsTestClassesDirectory
                        .resolve(radiometricIndicesDescriptor.domain)
                        .resolve(radiometricIndicesClassName + "OpTest.java");
                Files.createDirectories(generatedRadiometricIndicesOpTestClassFile.getParent());
                if (Files.exists(generatedRadiometricIndicesOpTestClassFile)) {
                    Files.writeString(generatedRadiometricIndicesOpTestClassFile, generatedRadiometricIndicesOpTestClass, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    Files.writeString(generatedRadiometricIndicesOpTestClassFile, generatedRadiometricIndicesOpTestClass, StandardOpenOption.CREATE_NEW);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(RadiometricIndicesOpsTestClassesGenerator.class.getName()).severe("Fail to generate the Radiometric Indices Operators test classes using the JSON descriptors. Reason:" + e.getMessage());
        }
    }

    private static <O extends BaseIndexOp> Float[] generateExpectedValuesForTest(Class<O> operatorClass, String[] operatorBandsNames, int operatorProductWidth, int operatorProductHeight, String[] operatorBandsWavelengths, String[] operatorBandsMinValues, String[] operatorBandsMaxValues, Map<String, Float> operatorParameters) {
        final List<Float> generatedExpectedValuesForTest = new ArrayList<>();
        O operator;
        try {
            Constructor<O> ctor = operatorClass.getConstructor();
            operator = ctor.newInstance();
        } catch (Exception e1) {
            try {
                Constructor<O> ctor = operatorClass.getDeclaredConstructor();
                operator = ctor.newInstance();
            } catch (Exception e) {
                throw new OperatorException(e.getMessage());
            }
        }
        final Product sourceProduct = new Product("IndexTest", "IndexTestType", operatorProductWidth, operatorProductHeight);
        int numElements = operatorProductWidth * operatorProductHeight;
        for (int i = 0; i < operatorBandsNames.length; i++) {
            final Band band = new Band(operatorBandsNames[i], ProductData.TYPE_FLOAT32, operatorProductWidth, operatorProductHeight);
            band.setSpectralWavelength(Float.parseFloat(operatorBandsWavelengths[i]));
            band.setSpectralBandIndex(i);
            band.setRasterData(ProductData.createInstance(sampleData(Float.parseFloat(operatorBandsMinValues[i]), Float.parseFloat(operatorBandsMaxValues[i]), numElements)));
            sourceProduct.addBand(band);
        }
        operator.setSourceProduct(sourceProduct);
        try {
            for (Map.Entry<String, Float> operatorParameter : operatorParameters.entrySet()) {
                final Field field = operatorClass.getDeclaredField(operatorParameter.getKey());
                field.setAccessible(true);
                if (field.isAnnotationPresent(Parameter.class)) {
                    field.setFloat(operator, operatorParameter.getValue());
                }
            }
        } catch (Exception e) {
            throw new OperatorException(e.getMessage());
        }
        final Product targetProduct = operator.getTargetProduct();
        final Raster data = targetProduct.getBandAt(0).getSourceImage().getData();
        for (int i = 0; i < numElements; i++) {
            float generatedExpectedValueForTest = data.getSampleFloat(i % operatorProductHeight, i / operatorProductWidth, 0);
            generatedExpectedValuesForTest.add(generatedExpectedValueForTest);
        }
        return generatedExpectedValuesForTest.toArray(new Float[0]);
    }

    private static float[] sampleData(float min, float max, int elements) {
        float[] values = new float[elements];
        float step = (max - min) / (elements - 1);
        for (int i = 0; i < elements; i++) {
            values[i] = min + i * step;
        }
        return values;
    }
}
