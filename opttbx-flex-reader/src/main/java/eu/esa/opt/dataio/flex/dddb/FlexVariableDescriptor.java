package eu.esa.opt.dataio.flex.dddb;

public class FlexVariableDescriptor {

    private String name;
    private String ncVarName;
    private String ncGroupPath;
    private char type;
    private String dataType;
    private int width;
    private int height;
    private int depth;
    private String depthPrefixToken;
    private String wavelengthReference;
    private String fwhmReference;
    private String units;
    private String description;
    private String ncDataFile;
    private boolean optional;

    public FlexVariableDescriptor() {
        type = 'v';
        width = -1;
        height = -1;
        depth = -1;
        depthPrefixToken = "";
        wavelengthReference = "";
        fwhmReference = "";
        units = "";
        description = "";
        ncDataFile = "";
        ncGroupPath = "";
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

    public String getNcGroupPath() {
        return ncGroupPath;
    }

    public void setNcGroupPath(String ncGroupPath) {
        this.ncGroupPath = ncGroupPath;
    }

    public char getType() {
        return type;
    }

    public FlexVariableType getVariableType() {
        return FlexVariableType.fromChar(type);
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

    public String getWavelengthReference() {
        return wavelengthReference;
    }

    public void setWavelengthReference(String wavelengthReference) {
        this.wavelengthReference = wavelengthReference;
    }

    public String getFwhmReference() {
        return fwhmReference;
    }

    public void setFwhmReference(String fwhmReference) {
        this.fwhmReference = fwhmReference;
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

    public String getNcDataFile() {
        return ncDataFile;
    }

    public void setNcDataFile(String ncDataFile) {
        this.ncDataFile = ncDataFile;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getFullNcPath() {
        if (ncGroupPath == null || ncGroupPath.isEmpty()) {
            return ncVarName;
        }
        return ncGroupPath + "/" + ncVarName;
    }
}
