package eu.esa.opt.dataio.alos.geotiff;

import org.esa.snap.core.datamodel.ProductData;

public class DataUtils {

    public static int getDataType(int value) {

        return switch (value) {
            case 8 -> ProductData.TYPE_UINT8;
            case 16 -> ProductData.TYPE_INT16;
            case 32 -> ProductData.TYPE_FLOAT32;
            default -> ProductData.TYPE_UINT8;
        };
    }
}
