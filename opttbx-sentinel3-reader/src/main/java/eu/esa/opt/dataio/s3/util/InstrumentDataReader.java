package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import org.esa.snap.core.datamodel.*;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Tonio Fincke
 */
class InstrumentDataReader extends S3NetcdfReader {

    private final static String DETECTOR_INDEX_NAME = "detector_index";
    private final static String FRAME_OFFSET_NAME = "frame_offset";
    private Band detectorIndexBand;
    private boolean is2dFrameOffset;

    static String getMetadataElementName(String attributeName) {
        switch (attributeName) {
            case "relative_spectral_covariance":
                return "Covariances";
            case "lambda0":
                return "Central wavelengths";
            case "FWHM":
                return "Bandwidths";
            case "solar_flux":
                return "Solar fluxes";
        }
        return "";
    }

    static String getMetadataAttributeName(String attributeName) {
        switch (attributeName) {
            case "relative_spectral_covariance":
                return "Covariance";
            case "lambda0":
                return "Central wavelength";
            case "FWHM":
                return "Bandwidth";
            case "solar_flux":
                return "Solar flux";
        }
        return "";
    }

    @Override
    protected void addBands(Product product) {
        final NetcdfFile netcdfFile = getNetcdfFile();

        final List<Variable> variables = netcdfFile.getVariables();
        for (final Variable variable : variables) {
            final String variableFullName = variable.getFullName();
            if (variableFullName.equals(DETECTOR_INDEX_NAME)) {
                addVariableAsBand(product, variable, DETECTOR_INDEX_NAME, false);
                detectorIndexBand = product.getBand(DETECTOR_INDEX_NAME);
                continue;
            }

            if (variableFullName.equals(FRAME_OFFSET_NAME)) {
                final int rank = variable.getRank();
                final boolean isSynthetic = rank == 1;
                is2dFrameOffset = !isSynthetic; // store that frame offset is a full variable for later use tb 2024-01-11
                addVariableAsBand(product, variable, variableFullName, isSynthetic);
                continue;
            }

            final int bandsDimensionIndex = variable.findDimensionIndex("bands");
            final int detectorsDimensionIndex = variable.findDimensionIndex("detectors");
            if (bandsDimensionIndex != -1 && detectorsDimensionIndex != -1) {
                final int numBands = variable.getDimension(bandsDimensionIndex).getLength();
                for (int i = 1; i <= numBands; i++) {
                    addVariableAsBand(product, variable, variableFullName + "_band_" + i, true);
                }
            } else if (variable.getDimensions().size() == 1 && detectorsDimensionIndex != -1) {
                addVariableAsBand(product, variable, variableFullName, true);
            } else {
                addVariableMetadata(variable, product);
            }
        }
    }

    @Override
    protected RenderedImage createSourceImage(Band band) {
        final String bandName = band.getName();
        if (bandName.equals(DETECTOR_INDEX_NAME)) {
            return createSourceImageForInstrument(band);
        }
        if (bandName.equals(FRAME_OFFSET_NAME)) {
            if (is2dFrameOffset) {
                return createSourceImageForInstrument(band);
            }
        }
        String variableName = bandName;
        Variable variable;
        int dimensionIndex = -1;
        String dimensionName = "";
        if (bandName.contains("_band")) {
            final int suffixIndex = variableName.indexOf("_band");
            variableName = bandName.substring(0, suffixIndex);
            variable = getNetcdfFile().findVariable(variableName);
            dimensionName = "bands";
            dimensionIndex = Integer.parseInt(bandName.substring(suffixIndex + 6)) - 1;
        } else {
            variable = getNetcdfFile().findVariable(variableName);
        }
        S3MultiLevelOpSource levelSource = new S3MultiLevelOpSource(band, variable, new String[]{dimensionName},
                new int[]{dimensionIndex},
                detectorIndexBand, "detectors", dimensionName);
        return new DefaultMultiLevelImage(levelSource);
    }

    private RenderedImage createSourceImageForInstrument(Band band) {
        final String bandName = band.getName();
        String variableName = bandName;
        if (variableName.endsWith("_lsb")) {
            variableName = variableName.substring(0, variableName.indexOf("_lsb"));
        } else if (variableName.endsWith("_msb")) {
            variableName = variableName.substring(0, variableName.indexOf("_msb"));
        }

        Variable variable = null;
        List<String> dimensionNameList = new ArrayList<>();
        List<Integer> dimensionIndexList = new ArrayList<>();
        final String[] separatingDimensions = getSeparatingDimensions();
        final String[] suffixesForSeparatingThirdDimensions = getSuffixesForSeparatingDimensions();
        int lowestSuffixIndex = Integer.MAX_VALUE;

        for (int ii = 0; ii < separatingDimensions.length; ii++) {
            final String dimension = separatingDimensions[ii];
            final String suffix = suffixesForSeparatingThirdDimensions[ii];
            if (bandName.contains(suffix)) {
                final int suffixIndex = bandName.indexOf(suffix) - 1;
                if (suffixIndex < lowestSuffixIndex) {
                    lowestSuffixIndex = suffixIndex;
                }
                dimensionNameList.add(dimension);
                dimensionIndexList.add(getDimensionIndexFromBandName(bandName));
            }
        }
        if (lowestSuffixIndex < bandName.length()) {
            variableName = bandName.substring(0, lowestSuffixIndex);
            variable = getNetcdfFile().findVariable(variableName);
        }
        if (variable == null) {
            variable = getNetcdfFile().findVariable(variableName);
        }
        Dimension widthDimension = getWidthDimension();
        int xIndex = -1;
        int yIndex = -1;

        if (widthDimension != null) {
            xIndex = variable.findDimensionIndex(widthDimension.getFullName());
        }
        Dimension heightDimension = getHeightDimension();

        if (heightDimension != null) {
            yIndex = variable.findDimensionIndex(heightDimension.getFullName());
        }
        String[] dimensionNames = dimensionNameList.toArray(new String[dimensionNameList.size()]);
        int[] dimensionIndexes = new int[dimensionIndexList.size()];

        for (int ii = 0; ii < dimensionIndexList.size(); ii++) {
            dimensionIndexes[ii] = dimensionIndexList.get(ii);
        }

        S3MultiLevelOpSource levelSource = new S3MultiLevelOpSource(band, variable, dimensionNames, dimensionIndexes, xIndex, yIndex);
        return new DefaultMultiLevelImage(levelSource);
    }

    @Override
    protected void addVariableMetadata(Variable variable, Product product) {
        super.addVariableMetadata(variable, product);
        if (variable.getRank() == 2 && variable.getDimension(0).getFullName().equals("bands")) {
            try {
                final String variableName = variable.getFullName();
                final MetadataElement variableElement = new MetadataElement(variableName);
                final float[][] contentMatrix = (float[][]) variable.read().copyToNDJavaArray();
                final int length = contentMatrix.length;

                for (int i = 0; i < length; i++) {
                    final MetadataElement xElement = new MetadataElement(getMetadataElementName(variableName) +
                            " for band " + (i + 1));
                    final ProductData content = ProductData.createInstance(contentMatrix[i]);
                    final MetadataAttribute covarianceAttribute =
                            new MetadataAttribute(getMetadataAttributeName(variableName), content, true);
                    xElement.addAttribute(covarianceAttribute);
                    variableElement.addElement(xElement);
                }
                product.getMetadataRoot().getElement("Variable_Attributes").addElement(variableElement);
            } catch (IOException e) {
                Logger logger = Logger.getLogger(this.getClass().getName());
                logger.warning("Could not read variable " + variable.getFullName());
            }
        }
    }

}
