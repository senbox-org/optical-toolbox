package eu.esa.opt.dataio.s3.util;

import org.esa.snap.core.datamodel.TiePointGrid;

public class LayeredTiePointGrid extends TiePointGrid {

    private String variableName;

    public LayeredTiePointGrid(String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY);
    }

    public LayeredTiePointGrid(String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints);
    }

    public LayeredTiePointGrid(String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints, boolean containsAngles) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints, containsAngles);
    }

    public LayeredTiePointGrid(String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints, int discontinuity) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints, discontinuity);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
