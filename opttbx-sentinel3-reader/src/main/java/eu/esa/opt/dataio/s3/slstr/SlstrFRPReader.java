package eu.esa.opt.dataio.s3.slstr;

import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import eu.esa.opt.snap.core.datamodel.band.DataPoint;
import eu.esa.opt.snap.core.datamodel.band.SparseDataBand;
import eu.esa.opt.snap.core.datamodel.band.SparseDataProvider;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
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
            new BandInfo("used_channel", ProductData.TYPE_INT32)
    };

    @Override
    protected void addBands(Product product) {
        super.addBands(product);

        final String productName = product.getName();
        if ("FRP_an".equals(productName) || "FRP_bn".equals(productName)) {

        } else if ("FRP_in".equals(productName)) {
            for (final BandInfo bandInfo : FRP_IN_INFOS) {
                final MetaDataProvider metaDataProvider = new MetaDataProvider(product, new String[]{"Variable_Attributes"}, bandInfo.name, "i", "j");
                final SparseDataBand sparseDataBand = new SparseDataBand(bandInfo.name, bandInfo.dataType, 1500, 1200, metaDataProvider);
                product.addBand(sparseDataBand);
            }
        }

        // add sparse data bands and connectors for
        // FRP_in
        //  - BT_MIR
        //  - BT_window
        //  - Day_night
        //  - F1_Fire_pixel_radiance
        //  - FRP_MWIR
        //  - FRP_uncertainty_MWIR
        //  - Glint_angle
        //  - IFOV_area
        //  - Radiance_window
        //  - S7_Fire_pixel_radiance
        //  - Satellite_zenith_angle
        //  - Sun_zenith_angle
        //  - TCWV
        //  - n_cloud
        //  - n_water
        //  - n_window
        //  - (time)
        //  - transmittance_MWIR
        //  - used_channel

        // FRP_an/FRP_bn
    }

    private static class BandInfo {

        public BandInfo(String name, int dataType) {
            this.name = name;
            this.dataType = dataType;
        }

        String name;
        int dataType;
    }

    //@todo 1 tb/tb move to separate class, add tests tb 2025-05-21
    private static class MetaDataProvider implements SparseDataProvider {

        private final Product product;
        private final String[] metaPath;
        private final String dataElementName;
        private final String xElementName;
        private final String yElementName;

        MetaDataProvider(Product product, String[] metaPath, String dataElementName, String xElementName, String yElementName) {
            this.product = product;
            this.metaPath = metaPath;
            this.dataElementName = dataElementName;
            this.xElementName = xElementName;
            this.yElementName = yElementName;
        }

        @Override
        public DataPoint[] get() {
            MetadataElement targetElement = product.getMetadataRoot();
            for (String elementName : metaPath) {
                targetElement = targetElement.getElement(elementName);
            }

            final MetadataElement dataElement = targetElement.getElement(dataElementName);
            MetadataAttribute value = dataElement.getAttribute("value");
            final ProductData data = value.getData();

            final MetadataElement xElement = targetElement.getElement(xElementName);
            value = xElement.getAttribute("value");
            final ProductData xData = value.getData();

            final MetadataElement yElement = targetElement.getElement(yElementName);
            value = yElement.getAttribute("value");
            final ProductData yData = value.getData();

            final int numElems = data.getNumElems();
            final DataPoint[] dataPoints = new DataPoint[numElems];
            for (int i = 0; i < numElems; i++) {
                final int x = xData.getElemIntAt(i);
                final int y = yData.getElemIntAt(i);
                final double mdsValue = data.getElemDoubleAt(i);
                dataPoints[i] = new DataPoint(x, y, mdsValue);
            }

            return dataPoints;
        }
    }
}
