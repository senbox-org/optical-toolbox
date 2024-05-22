package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.snap.core.datamodel.band.DataPoint;
import eu.esa.opt.snap.core.datamodel.band.SparseDataBand;
import eu.esa.opt.snap.core.datamodel.band.SparseDataProvider;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetaDataProviderTest {

    @Test
    @STTM("SNAP-1691")
    public void testGet() {
        final Product product = new Product("test", "test_type");
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement variableAttributes = new MetadataElement("Variable_Attributes");
        final MetadataElement nadir = new MetadataElement("nadir");

        final MetadataElement frpIn = new MetadataElement("FRP_in");
        frpIn.addAttribute(new MetadataAttribute("value", ProductData.createInstance(new double[]{2.8, 3.1, 4.7}), true));
        nadir.addElement(frpIn);

        final MetadataElement iElement = new MetadataElement("i");
        iElement.addAttribute(new MetadataAttribute("value", ProductData.createInstance(new int[]{108, 111, 114}), true));
        nadir.addElement(iElement);

        final MetadataElement jElement = new MetadataElement("j");
        jElement.addAttribute(new MetadataAttribute("value", ProductData.createInstance(new int[]{23, 31, 19}), true));
        nadir.addElement(jElement);

        variableAttributes.addElement(nadir);
        metadataRoot.addElement(variableAttributes);

        final MetaDataProvider metaDataProvider = new MetaDataProvider(product, new String[]{"Variable_Attributes", "nadir"}, "FRP_in", "i", "j");

        final DataPoint[] dataPoints = metaDataProvider.get();
        assertEquals(3, dataPoints.length);

        assertEquals(108, dataPoints[0].getX());
        assertEquals(23, dataPoints[0].getY());
        assertEquals(2.8, dataPoints[0].getValue(), 1e-8);

        assertEquals(111, dataPoints[1].getX());
        assertEquals(31, dataPoints[1].getY());
        assertEquals(3.1, dataPoints[1].getValue(), 1e-8);

        assertEquals(114, dataPoints[2].getX());
        assertEquals(19, dataPoints[2].getY());
        assertEquals(4.7, dataPoints[2].getValue(), 1e-8);
    }

    @Test
    public void testAddBandProperties() {
        final Product product = new Product("test", "test_type");
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement metazeugs = new MetadataElement("metazeugs");
        final MetadataElement proppy = new MetadataElement("proppy");

        proppy.addAttribute(new MetadataAttribute("long_name", ProductData.createInstance("the description"), true));
        proppy.addAttribute(new MetadataAttribute("units", ProductData.createInstance("K"), true));

        metazeugs.addElement(proppy);
        metadataRoot.addElement(metazeugs);

        final MetaDataProvider metaDataProvider = new MetaDataProvider(product, new String[]{"metazeugs"}, "proppy", "i", "j");
        final SparseDataBand band = new SparseDataBand("proppy", ProductData.TYPE_INT32, 5, 3, metaDataProvider);

        metaDataProvider.addBandProperties(band);
        assertEquals("the description", band.getDescription());
        assertEquals("K", band.getUnit());
    }
}
