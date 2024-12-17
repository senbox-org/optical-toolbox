package eu.esa.opt.dataio.s3.dddb;

public enum VariableType {
    VARIABLE,
    FLAG,
    TIE_POINT,
    METADATA;

    public static VariableType fromChar(char type){
        return switch (type) {
            case 'v' -> VARIABLE;
            case 'f' -> FLAG;
            case 't' -> TIE_POINT;
            case 'm' -> METADATA;
            default -> throw new RuntimeException("not implemented: " + type);
        };
    }
}
