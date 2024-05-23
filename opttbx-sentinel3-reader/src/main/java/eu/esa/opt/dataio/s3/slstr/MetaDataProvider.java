package eu.esa.opt.dataio.s3.slstr;

import eu.esa.opt.snap.core.datamodel.band.DataPoint;
import eu.esa.opt.snap.core.datamodel.band.SparseDataBand;
import eu.esa.opt.snap.core.datamodel.band.SparseDataProvider;
import org.esa.snap.core.datamodel.*;

class MetaDataProvider implements SparseDataProvider {

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
        MetadataElement targetElement = getTargetElement();

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

    private MetadataElement getTargetElement() {
        MetadataElement targetElement = product.getMetadataRoot();
        for (String elementName : metaPath) {
            targetElement = targetElement.getElement(elementName);
        }
        return targetElement;
    }

    void addBandProperties(Band band) {
        MetadataElement targetElement = getTargetElement();

        final MetadataElement dataElement = targetElement.getElement(dataElementName);
        final MetadataAttribute longName = dataElement.getAttribute("long_name");
        if (longName != null) {
            band.setDescription(longName.getData().getElemString());
        }

        final MetadataAttribute units = dataElement.getAttribute("units");
        if (units != null) {
            band.setUnit(units.getData().getElemString());
        }
    }

    boolean elementsExist() {
        final MetadataElement targetElement = getTargetElement();
        if (targetElement == null) {
            return false;
        }

        final MetadataElement dataElement = targetElement.getElement(dataElementName);
        if (dataElement == null) {
            return false;
        }
        final MetadataElement xElement = targetElement.getElement(xElementName);
        if (xElement == null) {
            return false;
        }

        final MetadataElement yElement = targetElement.getElement(yElementName);
        if (yElement == null) {
            return false;
        }
        return true;
    }
}
