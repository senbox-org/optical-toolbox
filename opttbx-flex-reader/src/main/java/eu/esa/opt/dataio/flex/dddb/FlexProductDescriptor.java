package eu.esa.opt.dataio.flex.dddb;

public class FlexProductDescriptor {

    private String productType;
    private int width;
    private int height;
    private String[] dataFiles;
    private String bandGroupingPattern;
    private FlexFlagMask[] flagMasks;

    public FlexProductDescriptor() {
        productType = "";
        width = -1;
        height = -1;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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
