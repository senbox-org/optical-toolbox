package eu.esa.opt.dataio.flex.dddb;

public class FlexProductDescriptor {

    private String productType;
    private String dimensionGroupPath;
    private String widthDimensionName;
    private String heightDimensionName;
    private String[] dataFiles;
    private String bandGroupingPattern;
    private FlexFlagMask[] flagMasks;

    public FlexProductDescriptor() {
        productType = "";
        dimensionGroupPath = "";
        widthDimensionName = "";
        heightDimensionName = "";
        dataFiles = new String[0];
        bandGroupingPattern = "";
        flagMasks = new FlexFlagMask[0];
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getDimensionGroupPath() {
        return dimensionGroupPath;
    }

    public void setDimensionGroupPath(String dimensionGroupPath) {
        this.dimensionGroupPath = dimensionGroupPath;
    }

    public String getWidthDimensionName() {
        return widthDimensionName;
    }

    public void setWidthDimensionName(String widthDimensionName) {
        this.widthDimensionName = widthDimensionName;
    }

    public String getHeightDimensionName() {
        return heightDimensionName;
    }

    public void setHeightDimensionName(String heightDimensionName) {
        this.heightDimensionName = heightDimensionName;
    }

    public String[] getDataFiles() {
        return dataFiles;
    }

    public void setDataFiles(String[] dataFiles) {
        this.dataFiles = dataFiles;
    }

    public String getBandGroupingPattern() {
        return bandGroupingPattern;
    }

    public void setBandGroupingPattern(String bandGroupingPattern) {
        this.bandGroupingPattern = bandGroupingPattern;
    }

    public FlexFlagMask[] getFlagMasks() {
        return flagMasks;
    }

    public void setFlagMasks(FlexFlagMask[] flagMasks) {
        this.flagMasks = flagMasks;
    }
}
