package eu.esa.opt.dataio.flex.dddb;

public class FlexFlagMask {

    private String bandName;
    private String name;
    private int value;
    private String description;

    public FlexFlagMask() {
    }

    public FlexFlagMask(String bandName, String name, int value, String description) {
        this.bandName = bandName;
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
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

    public void setValue(int value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
