package eu.esa.opt.dataio.ecostress;

import org.esa.snap.core.datamodel.Band;

public final class EcostressBand extends Band {

    private final String bandPathInEcostressProduct;

    /**
     * Constructs a new <code>EcostressBand</code>.
     *
     * @param name                       the name of the new ECOSTRESS Band
     * @param dataType                   the raster data type, must be one of the multiple <code>ProductData.TYPE_<i>X</i></code>
     *                                   constants, with the exception of <code>ProductData.TYPE_UINT32</code>
     * @param width                      the width of the raster in pixels
     * @param height                     the height of the raster in pixels
     * @param bandPathInEcostressProduct the path of the HDF object which contains the band data
     */
    public EcostressBand(String name, int dataType, int width, int height, String bandPathInEcostressProduct) {
        super(name, dataType, width, height);
        this.bandPathInEcostressProduct = bandPathInEcostressProduct;
    }

    /**
     * Gets the path of the HDF object which contains the band data
     *
     * @return the path of the HDF object which contains the band data
     */
    public String getBandPathInEcostressProduct() {
        return bandPathInEcostressProduct;
    }
}
