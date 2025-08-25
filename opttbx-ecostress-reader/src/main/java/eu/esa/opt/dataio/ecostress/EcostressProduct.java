package eu.esa.opt.dataio.ecostress;

import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.Product;

public class EcostressProduct extends Product {

    private boolean isReversed;
    public EcostressProduct(String name, String type, int sceneRasterWidth, int sceneRasterHeight, ProductReader reader) {
        super(name, type, sceneRasterWidth, sceneRasterHeight, reader);
    }

    public boolean isReversed() {
        return isReversed;
    }

    public void setReversed(boolean reversed) {
        isReversed = reversed;
    }
}
