package eu.esa.opt.dataio.flex.dddb;

public class FlexFlagMask {

    private String bandName;
    private String name;
    private int value;
    private String description;
    private boolean bitmask;

    public FlexFlagMask() {}

    public FlexFlagMask(String bandName, String name, int value, String description, boolean bitmask) {
        this.bandName = bandName;
        this.name = name;
        this.value = value;
        this.description = description;
        this.bitmask = bitmask;
    }

    public String getBandName() {
        return bandName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBitmask() {
        return bitmask;
    }

    public void setBitmask(boolean bitmask) {
        this.bitmask = bitmask;
    }
}
