package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.annotation.STTM;
import eu.esa.snap.core.datamodel.group.BandGroup;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SlstrWstProductFactoryTest {

    @Test
    @STTM("SNAP-4215")
    public void testGetBaseline004Prefix() {
        assertEquals("NADIR", SlstrWstProductFactory.getBaseline004Prefix("20260619070028-MAR-L2P_GHRSST-SSTskin-SLSTRA-NRT_NADIR_20260619083100-v02.1-fv01.0"));
        assertEquals("DUAL", SlstrWstProductFactory.getBaseline004Prefix("20260619070028-MAR-L2P_GHRSST-SSTskin-SLSTRA-NRT_DUAL_20260619083100-v02.1-fv01.0"));
        assertNull(SlstrWstProductFactory.getBaseline004Prefix("20240216024326-MAR-L2P_GHRSST-SSTskin-SLSTRB-20240216033441-v02.0-fv01.0"));
    }

    @Test
    @STTM("SNAP-4215")
    public void testSetAutoGrouping(){
        final Product product = new Product("test", "me", 6, 8);
        SlstrWstProductFactory.setAutoGrouping(product);

        final BandGroup autoGrouping = product.getAutoGrouping();
        assertEquals("NADIR:DUAL:brightness_temperature:nedt", autoGrouping.toString());
    }

    @Test
    @STTM("SNAP-4215")
    public void testGetBandCacheKey()  {
        final SlstrWstProductFactory slstrWstProductFactory = new SlstrWstProductFactory(null);

        final Band whateverWeMeasureNadir = new Band("whatever_we_measure_NADIR", ProductData.TYPE_FLOAT32, 3, 4);
        assertEquals("whatever_we_measure", slstrWstProductFactory.getBandCacheKey(whateverWeMeasureNadir));

        final Band whateverWeMeasureDual = new Band("whatever_we_measure_DUAL", ProductData.TYPE_UINT32, 3, 4);
        assertEquals("whatever_we_measure", slstrWstProductFactory.getBandCacheKey(whateverWeMeasureDual));

        final Band whateverWeMeasure = new Band("whatever_we_measure", ProductData.TYPE_FLOAT64, 3, 4);
        assertEquals("whatever_we_measure", slstrWstProductFactory.getBandCacheKey(whateverWeMeasure));
    }
}
