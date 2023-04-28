package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNodeGroup;
import org.esa.snap.core.util.BitSetter;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.*;

class QualityLayerInfo {
    static QualityLayerInfo QL_CLASSES_LAND = create("Land", "Classified as land",
            QUALITY_CLASSES_KEY, 0b11, 0b01, Color.GREEN);
    static QualityLayerInfo QL_CLASSES_WATER = create("Water", "Classified as water",
            QUALITY_CLASSES_KEY, 0b11, 0b10, Color.BLUE);
    static QualityLayerInfo QL_CLASSES_BG = create("Background", "Background",
            QUALITY_CLASSES_KEY, 0b11, 0b11, Color.BLACK);

    static QualityLayerInfo QL_CLOUD_CLOUD = create("Cloud", "Classified as cloud",
            QUALITY_CLOUD_KEY, 1, Color.WHITE);

    static QualityLayerInfo QL_CLOUDSHADOW_SHADOW = create("Cloud_Shadow", "Classified as cloud shadow",
            QUALITY_CLOUDSHADOW_KEY, 1, Color.BLACK.brighter());

    static QualityLayerInfo QL_HAZE_HAZE = create("Haze", "Classified as haze",
            QUALITY_HAZE_KEY, 1, Color.YELLOW.brighter());

    static QualityLayerInfo QL_CIRRUS_THIN = create("Thin", "Thin_Cirrus", "Classified as thin cirrus",
            QUALITY_CIRRUS_KEY, 0b11, 0b01, Color.LIGHT_GRAY);

    static QualityLayerInfo QL_CIRRUS_MEDIUM = create("Medium", "Medium_Cirrus", "Classified as medium cirrus",
            QUALITY_CIRRUS_KEY, 0b11, 0b10, Color.GRAY);

    static QualityLayerInfo QL_CIRRUS_THICK = create("Thick", "Thick_Cirrus", "Classified as thick cirrus",
            QUALITY_CIRRUS_KEY, 0b11, 0b11, Color.DARK_GRAY);

    static QualityLayerInfo QL_SNOW_SNOW = create("Snow", "Classified as snow",
            QUALITY_SNOW_KEY, 1, Color.YELLOW);

    static QualityLayerInfo QL_PM_DEFECTIVE_SERIES = create("Defective", "Defective pixel",
            QUALITY_PIXELMASK_KEY, 1, Color.RED);

    static QualityLayerInfo QL_TF_NOMINAL = create("Nominal", "Nominal quality",
            QUALITY_TESTFLAGS_KEY, 0b11, 0b00, Color.GREEN.brighter());

    static QualityLayerInfo QL_TF_REDUCED = create("Reduced", "Reduced quality",
            QUALITY_TESTFLAGS_KEY, 0b11, 0b01, Color.ORANGE);

    static QualityLayerInfo QL_TF_LOW = create("Low", "Low Quality",
            QUALITY_TESTFLAGS_KEY, 0b11, 0b10, Color.RED);

    static QualityLayerInfo QL_TF_NOT = create("Not_Produced", "Quality information not produced",
            QUALITY_TESTFLAGS_KEY, 0b11, 0b11, Color.CYAN);

    static QualityLayerInfo QL_TF_INTERPOLATED_SWIR = create("Interpolated_Swir", "Interpolated SWIR pixel",
            QUALITY_TESTFLAGS_KEY, BitSetter.setFlag(0, 2), Color.MAGENTA);

    static QualityLayerInfo QL_TF_INTERPOLATED_VNIR = create("Interpolated_Vnir", "Interpolated VNIR pixel",
            QUALITY_TESTFLAGS_KEY, BitSetter.setFlag(0, 3), Color.PINK);

    static QualityLayerInfo QL_TF_SATURATION_SWIR = create("Saturated_Swir", "Saturated SWIR pixel",
            QUALITY_TESTFLAGS_KEY, BitSetter.setFlag(0, 4), Color.CYAN.darker());

    static QualityLayerInfo QL_TF_SATURATION_VNIR = create("Saturated_Vnir", "Saturated VNIR pixel",
            QUALITY_TESTFLAGS_KEY, BitSetter.setFlag(0, 5), Color.ORANGE.darker());

    static QualityLayerInfo QL_TF_ARTEFACT_SWIR = create("Artefact_Swir", "Artefact SWIR pixel",
            QUALITY_TESTFLAGS_KEY, BitSetter.setFlag(0, 6), Color.RED.darker());

    static QualityLayerInfo QL_TF_ARTEFACT_VNIR = create("Artefact_Vnir", "Artefact VNIR pixel",
            QUALITY_TESTFLAGS_KEY, BitSetter.setFlag(0, 7), Color.BLUE.darker());

    static QualityLayerInfo QL_TF_VNIR_NOMINAL = create("Nominal_Vnir", "Nominal quality for VNIR",
            QUALITY_TESTFLAGS_VNIR_KEY, 0b11, 0b00, Color.GREEN.brighter());

    static QualityLayerInfo QL_TF_VNIR_REDUCED = create("Reduced_Vnir", "Reduced quality for VNIR",
            QUALITY_TESTFLAGS_VNIR_KEY, 0b11, 0b01, Color.ORANGE);

    static QualityLayerInfo QL_TF_VNIR_LOW = create("Low_Vnir", "Low Quality for VNIR",
            QUALITY_TESTFLAGS_VNIR_KEY, 0b11, 0b10, Color.RED);

    static QualityLayerInfo QL_TF_VNIR_NOT = create("Not_Produced_Vnir", "Quality information not produced for VNIR",
            QUALITY_TESTFLAGS_VNIR_KEY, 0b11, 0b11, Color.CYAN);

    static QualityLayerInfo QL_TF_VNIR_INTERPOLATED_SWIR = create("Interpolated_Swir", "Interpolated SWIR pixel",
            QUALITY_TESTFLAGS_VNIR_KEY, BitSetter.setFlag(0, 2), Color.MAGENTA);

    static QualityLayerInfo QL_TF_VNIR_INTERPOLATED_VNIR = create("Interpolated_Vnir", "Interpolated VNIR pixel",
            QUALITY_TESTFLAGS_VNIR_KEY, BitSetter.setFlag(0, 3), Color.PINK);

    static QualityLayerInfo QL_TF_VNIR_SATURATION_SWIR = create("Saturated_Swir", "Saturated SWIR pixel",
            QUALITY_TESTFLAGS_VNIR_KEY, BitSetter.setFlag(0, 4), Color.CYAN.darker());

    static QualityLayerInfo QL_TF_VNIR_SATURATION_VNIR = create("Saturated_Vnir", "Saturated VNIR pixel",
            QUALITY_TESTFLAGS_VNIR_KEY, BitSetter.setFlag(0, 5), Color.ORANGE.darker());

    static QualityLayerInfo QL_TF_VNIR_ARTEFACT_SWIR = create("Artefact_Swir", "Artefact SWIR pixel",
            QUALITY_TESTFLAGS_VNIR_KEY, BitSetter.setFlag(0, 6), Color.RED.darker());

    static QualityLayerInfo QL_TF_VNIR_ARTEFACT_VNIR = create("Artefact_Vnir", "Artefact VNIR pixel",
            QUALITY_TESTFLAGS_VNIR_KEY, BitSetter.setFlag(0, 7), Color.BLUE.darker());

    static QualityLayerInfo QL_TF_SWIR_NOMINAL = create("Nominal_Swir", "Nominal quality for SWIR",
            QUALITY_TESTFLAGS_SWIR_KEY, 0b11, 0b00, Color.GREEN.brighter());

    static QualityLayerInfo QL_TF_SWIR_REDUCED = create("Reduced_Swir", "Reduced quality for SWIR",
            QUALITY_TESTFLAGS_SWIR_KEY, 0b11, 0b01, Color.ORANGE);

    static QualityLayerInfo QL_TF_SWIR_LOW = create("Low_Swir", "Low Quality for SWIR",
            QUALITY_TESTFLAGS_SWIR_KEY, 0b11, 0b10, Color.RED);

    static QualityLayerInfo QL_TF_SWIR_NOT = create("Not_Produced_Swir", "Quality information not produced for SWIR",
            QUALITY_TESTFLAGS_SWIR_KEY, 0b11, 0b11, Color.CYAN);

    static QualityLayerInfo QL_TF_SWIR_INTERPOLATED_SWIR = create("Interpolated_Swir", "Interpolated SWIR pixel",
            QUALITY_TESTFLAGS_SWIR_KEY, BitSetter.setFlag(0, 2), Color.MAGENTA);

    static QualityLayerInfo QL_TF_SWIR_INTERPOLATED_VNIR = create("Interpolated_Vnir", "Interpolated VNIR pixel",
            QUALITY_TESTFLAGS_SWIR_KEY, BitSetter.setFlag(0, 3), Color.PINK);

    static QualityLayerInfo QL_TF_SWIR_SATURATION_SWIR = create("Saturated_Swir", "Saturated SWIR pixel",
            QUALITY_TESTFLAGS_SWIR_KEY, BitSetter.setFlag(0, 4), Color.CYAN.darker());

    static QualityLayerInfo QL_TF_SWIR_SATURATION_VNIR = create("Saturated_Vnir", "Saturated VNIR pixel",
            QUALITY_TESTFLAGS_SWIR_KEY, BitSetter.setFlag(0, 5), Color.ORANGE.darker());

    static QualityLayerInfo QL_TF_SWIR_ARTEFACT_SWIR = create("Artefact_Swir", "Artefact SWIR pixel",
            QUALITY_TESTFLAGS_SWIR_KEY, BitSetter.setFlag(0, 6), Color.RED.darker());

    static QualityLayerInfo QL_TF_SWIR_ARTEFACT_VNIR = create("Artefact_Vnir", "Artefact VNIR pixel",
            QUALITY_TESTFLAGS_SWIR_KEY, BitSetter.setFlag(0, 7), Color.BLUE.darker());
    private final Integer flagValue;
    String flagName;
    String maskName;
    String description;
    String qualityKey;
    String maskExpression;
    int flagMask;
    Color maskColor;
    float transparency;

    private QualityLayerInfo(String flagName, String maskName, String description, String qualityKey, String maskExpression, int flagMask, Integer flagValue, Color maskColor, float transparency) {
        this.flagName = flagName;
        this.maskName = maskName;
        this.description = description;
        this.qualityKey = qualityKey;
        this.maskExpression = maskExpression;
        this.flagMask = flagMask;
        this.flagValue = flagValue;
        this.maskColor = maskColor;
        this.transparency = transparency;
    }

    private static QualityLayerInfo create(String name, String description, String qualityKey, int flagMask, int flagValue, Color maskColor) {
        return new QualityLayerInfo(name, name, description, qualityKey, String.format("%s.%s", qualityKey, name),
                flagMask, flagValue, maskColor, 0.5f);
    }

    private static QualityLayerInfo create(String name, String description, String qualityKey, int flagMask, Color maskColor) {
        return new QualityLayerInfo(name, name, description, qualityKey, String.format("%s.%s", qualityKey, name),
                flagMask, null, maskColor, 0.5f);
    }

    private static QualityLayerInfo create(String flagName, String maskName, String description, String qualityKey, int flagMask, Color maskColor) {
        return new QualityLayerInfo(flagName, maskName, description, qualityKey, String.format("%s.%s", qualityKey, flagName),
                flagMask, null, maskColor, 0.5f);
    }

    private static QualityLayerInfo create(String flagName, String maskName, String description, String qualityKey, int flagMask, int flagValue, Color maskColor) {
        return new QualityLayerInfo(flagName, maskName, description, qualityKey, String.format("%s.%s", qualityKey, flagName),
                flagMask, flagValue, maskColor, 0.5f);
    }

    private static void addMaskToCombinedExpression(StringBuilder vnirMaskExpression, String seriesMaskName) {
        if (vnirMaskExpression.length() > 0) {
            vnirMaskExpression.append(" || ");
        }
        vnirMaskExpression.append(seriesMaskName);
    }

    void addFlagTo(FlagCoding flagCoding) {
        if (flagValue != null) {
            flagCoding.addFlag(flagName, flagMask, flagValue, description);
        } else {
            flagCoding.addFlag(flagName, flagMask, description);
        }
    }

    void addMaskTo(Product product) {
        int width = product.getSceneRasterWidth();
        int height = product.getSceneRasterHeight();
        product.getMaskGroup().add(Mask.BandMathsType.create(maskName, description, width, height,
                String.format("%s.%s", qualityKey, flagName), maskColor, transparency));
    }

    void addMasksTo(Product product, EnmapMetadata meta) throws IOException {
        int width = product.getSceneRasterWidth();
        int height = product.getSceneRasterHeight();
        ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
        int[] spectralIndices = meta.getSpectralIndices();
        StringBuilder vnirMaskExpression = new StringBuilder();
        StringBuilder swirMaskExpression = new StringBuilder();
        List<Mask> spectralMasks = new ArrayList<>();
        for (int i = 0; i < meta.getNumSpectralBands(); i++) {
            int spectralIndex = spectralIndices[i];
            String seriesMaskName = String.format("%s_%03d", maskName, spectralIndex);
            String maskExpression = String.format("%s_%03d.%s", qualityKey, spectralIndex, flagName);
            spectralMasks.add(Mask.BandMathsType.create(seriesMaskName, description, width, height,
                    maskExpression, maskColor, transparency));
            if (i < meta.getNumVnirBands()) {
                addMaskToCombinedExpression(vnirMaskExpression, seriesMaskName);
            } else {
                addMaskToCombinedExpression(swirMaskExpression, seriesMaskName);
            }
        }

        String vnirDefectiveMaskName = "VNIR_Defective_Pixels";
        maskGroup.add(Mask.BandMathsType.create(vnirDefectiveMaskName, "Masks all defective VNIR pixels",
                width, height, vnirMaskExpression.toString(), Color.RED, 0.3));
        String swirDefectiveMaskName = "SWIR_Defective_Pixels";
        maskGroup.add(Mask.BandMathsType.create(swirDefectiveMaskName, "Masks all defective SWIR pixels",
                width, height, swirMaskExpression.toString(), Color.RED, 0.3));
        maskGroup.add(Mask.BandMathsType.create("All_Defective_Pixels", "Masks all defective pixels",
                width, height, String.format("%s || %s", vnirDefectiveMaskName, swirDefectiveMaskName), Color.RED, 0.3));

        spectralMasks.forEach(maskGroup::add);

    }
}
