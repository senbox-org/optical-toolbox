package eu.esa.opt.dataio.enmap;

class GeoReferencing {

    String projection;
    double easting;
    double northing;
    double resolution;
    double refX;
    double refY;

    public GeoReferencing(String projection, double resolution) {
        this.projection = projection;
        this.resolution = resolution;
        easting = 0.0;
        northing = 0.0;
        refX = 0.0;
        refY = 0.0;
    }
}
