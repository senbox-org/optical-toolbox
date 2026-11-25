package eu.esa.opt.dataio.pleiades.dimap;

public class BandInfo {
    String id;
    int index;
    String unit;
    String description;
    Double gain;
    Double bias;
    Float centralWavelength;
    Float bandwidth;

    public String getId() {
        return id;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }

    public Double getGain() {
        return gain;
    }

    public Double getBias() {
        return bias;
    }

    public Float getCentralWavelength() {
        return centralWavelength;
    }

    public Float getBandwidth() {
        return bandwidth;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
