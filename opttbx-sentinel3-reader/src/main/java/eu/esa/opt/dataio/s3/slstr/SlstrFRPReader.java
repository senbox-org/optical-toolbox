package eu.esa.opt.dataio.s3.slstr;

import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import eu.esa.snap.core.datamodel.band.SparseDataBand;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

/**
 * @author Tonio Fincke
 */
class SlstrFRPReader extends S3NetcdfReader {

    private static final BandInfo[] FRP_IN_INFOS = {
            new BandInfo("BT_MIR", ProductData.TYPE_FLOAT64),
            new BandInfo("BT_window", ProductData.TYPE_FLOAT64),
            new BandInfo("Day_night", ProductData.TYPE_INT32),
            new BandInfo("F1_Fire_pixel_radiance", ProductData.TYPE_FLOAT64),
            new BandInfo("FRP_MWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("FRP_uncertainty_MWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("Glint_angle", ProductData.TYPE_FLOAT64),
            new BandInfo("IFOV_area", ProductData.TYPE_FLOAT64),
            new BandInfo("Radiance_window", ProductData.TYPE_FLOAT64),
            new BandInfo("S7_Fire_pixel_radiance", ProductData.TYPE_FLOAT64),
            new BandInfo("Satellite_zenith_angle", ProductData.TYPE_FLOAT64),
            new BandInfo("Sun_zenith_angle", ProductData.TYPE_FLOAT64),
            new BandInfo("TCWV", ProductData.TYPE_FLOAT64),
            new BandInfo("classification", ProductData.TYPE_INT32),
            new BandInfo("n_cloud", ProductData.TYPE_INT32),
            new BandInfo("n_water", ProductData.TYPE_INT32),
            new BandInfo("n_window", ProductData.TYPE_INT32),
            new BandInfo("transmittance_MWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("used_channel", ProductData.TYPE_INT32),
            // bands available in older product formats
            new BandInfo("FRP_SWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("FRP_uncertainty_SWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("confidence", ProductData.TYPE_FLOAT64),
            new BandInfo("n_SWIR_fire", ProductData.TYPE_INT32),
            new BandInfo("transmittance_SWIR", ProductData.TYPE_FLOAT64),
    };

    private static final BandInfo[] FRP_AN_BN_INFOS = {
            new BandInfo("FRP_MWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("FRP_SWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("FRP_uncertainty_SWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("IFOV_area", ProductData.TYPE_FLOAT64),
            new BandInfo("Radiance_window_S6", ProductData.TYPE_FLOAT64),
            new BandInfo("Ratio_S56", ProductData.TYPE_FLOAT64),
            new BandInfo("S5_Fire_pixel_radiance", ProductData.TYPE_FLOAT64),
            new BandInfo("S5_confirm", ProductData.TYPE_INT32),
            new BandInfo("S6_Fire_pixel_radiance", ProductData.TYPE_FLOAT64),
            new BandInfo("TCWV", ProductData.TYPE_FLOAT64),
            new BandInfo("classification", ProductData.TYPE_INT32),
            new BandInfo("transmittance_SWIR", ProductData.TYPE_FLOAT64),
            new BandInfo("used_channel", ProductData.TYPE_INT32)
    };

    static Number getDefaultFillValue(int dataType) {
        switch (dataType) {
            case ProductData.TYPE_FLOAT64:
                return 9.969209968386869E36;

            case ProductData.TYPE_FLOAT32:
                return 9.96921E36F;

            case ProductData.TYPE_INT32:
                return -2147483647;
        }
        throw new IllegalStateException("Unsupported data type");
    }

    @Override
    protected void addBands(Product product) {
        super.addBands(product);

        final String productName = product.getName();
        if ("FRP_an".equals(productName) || "FRP_bn".equals(productName)) {
            for (final BandInfo bandInfo : FRP_AN_BN_INFOS) {
                addBandAndDataProvider(product, bandInfo, 3000, 2400);
            }
        } else if ("FRP_in".equals(productName)) {
            for (final BandInfo bandInfo : FRP_IN_INFOS) {
                addBandAndDataProvider(product, bandInfo, 1500, 1200);
            }
        }
    }

    // package access for testing only tb 2024-05-22
    static void addBandAndDataProvider(Product product, BandInfo bandInfo, int width, int heigh) {
        final MetaDataProvider metaDataProvider = new MetaDataProvider(product, new String[]{"Variable_Attributes"}, bandInfo.name, "i", "j");

        if (metaDataProvider.elementsExist()) {
            final SparseDataBand sparseDataBand = new SparseDataBand(bandInfo.name, bandInfo.dataType, width, heigh, metaDataProvider);

            metaDataProvider.addBandProperties(sparseDataBand);

            // unfortunately, there is no CF conformant fill value attribute provided. Set to CF default here tb 2024-05-22
            final Number defaultFillValue = getDefaultFillValue(bandInfo.dataType);
            sparseDataBand.setNoDataValue(defaultFillValue.doubleValue());
            sparseDataBand.setNoDataValueUsed(true);

            product.addBand(sparseDataBand);
        }
    }

    static class BandInfo {

        BandInfo(String name, int dataType) {
            this.name = name;
            this.dataType = dataType;
        }

        String name;
        int dataType;
    }
}
