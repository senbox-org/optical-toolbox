package eu.esa.opt.dataio.flex.dddb;

public enum FlexVariableType {
    VARIABLE,
    FLAG,
    BITMASK_FLAG,
    TIE_POINT,
    METADATA,
    SPECIAL;

    public static FlexVariableType fromChar(char type) {
        return switch (type) {
            case 'v' -> VARIABLE;
            case 'f' -> FLAG;
            case 'b' -> BITMASK_FLAG;
            case 't' -> TIE_POINT;
            case 'm' -> METADATA;
            case 's' -> SPECIAL;
            default -> throw new IllegalArgumentException("Unknown variable type: " + type);
        };
    }
}
