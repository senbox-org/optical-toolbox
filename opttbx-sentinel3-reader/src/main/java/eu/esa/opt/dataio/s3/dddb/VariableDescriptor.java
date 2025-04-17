package eu.esa.opt.dataio.s3.dddb;

public class VariableDescriptor {

    private String name;
    private String ncVarName;
    private char type;
    private String dataType;
    private String widthXPath;
    private String heightXPath;
    private String tpXSubsamplingXPath;
    private String tpYSubsamplingXPath;
    private int width;
    private int height;
    private int tpSubsamplingX;
    private int tpSubsamplingY;
    private int depth;
    private String depthPrefixToken;
    private String fileName;
    private String validExpression;
    private String units;
    private String description;

    public VariableDescriptor() {
        type = 'v';
        width = -1;
        height = -1;
        depth = -1;
        depthPrefixToken = "";
        validExpression = "";
        units = "";
        description = "";
        tpXSubsamplingXPath = "";
        tpYSubsamplingXPath = "";
        tpSubsamplingX = -1;
        tpSubsamplingY = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNcVarName() {
        return ncVarName;
    }

    public void setNcVarName(String ncVarName) {
        this.ncVarName = ncVarName;
    }

    public char getType() {
        return type;
    }

    public VariableType getVariableType() {
        return VariableType.fromChar(type);
    }

    public void setType(char type) {
        this.type = type;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getWidthXPath() {
        return widthXPath;
    }

    public void setWidthXPath(String widthXPath) {
        this.widthXPath = widthXPath;
    }

    public String getHeightXPath() {
        return heightXPath;
    }

    public void setHeightXPath(String heightXPath) {
        this.heightXPath = heightXPath;
    }

    public String getTpXSubsamplingXPath() {
        return tpXSubsamplingXPath;
    }

    public void settpXSubsamplingXPath(String tpXSubsamplingXPath) {
        this.tpXSubsamplingXPath = tpXSubsamplingXPath;
    }

    public String getTpYSubsamplingXPath() {
        return tpYSubsamplingXPath;
    }

    public void settpYSubsamplingXPath(String tpYSubsamplingXPath) {
        this.tpYSubsamplingXPath = tpYSubsamplingXPath;
    }

    public int getTpSubsamplingX() {
        return tpSubsamplingX;
    }

    public int getTpSubsamplingY() {
        return tpSubsamplingY;
    }

    public void setTpSubsamplingX(int tpSubsamplingX) {
        this.tpSubsamplingX = tpSubsamplingX;
    }

    public void setTpSubsamplingY(int tpSubsamplingY) {
        this.tpSubsamplingY = tpSubsamplingY;
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

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getDepthPrefixToken() {
        return depthPrefixToken;
    }

    public void setDepthPrefixToken(String depthPrefixToken) {
        this.depthPrefixToken = depthPrefixToken;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getValidExpression() {
        return validExpression;
    }

    public void setValidExpression(String validExpression) {
        this.validExpression = validExpression;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
