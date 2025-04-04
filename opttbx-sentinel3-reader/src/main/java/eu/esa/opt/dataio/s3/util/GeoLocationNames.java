package eu.esa.opt.dataio.s3.util;

public class GeoLocationNames {

    private final String longitudeName;
    private final String latitudeName;
    private final String tpLongitudeName;
    private final String tpLatitudeName;

    public GeoLocationNames(String longitudeName, String latitudeName, String tpLongitudeName, String tpLatitudeName) {
        this.longitudeName = longitudeName;
        this.latitudeName = latitudeName;
        this.tpLongitudeName = tpLongitudeName;
        this.tpLatitudeName = tpLatitudeName;
    }

    public String getLongitudeName() {
        return longitudeName;
    }

    public String getLatitudeName() {
        return latitudeName;
    }

    public String getTpLongitudeName() {
        return tpLongitudeName;
    }

    public String getTpLatitudeName() {
        return tpLatitudeName;
    }
}
