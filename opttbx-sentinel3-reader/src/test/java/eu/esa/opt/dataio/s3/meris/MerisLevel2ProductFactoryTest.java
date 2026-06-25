package eu.esa.opt.dataio.s3.meris;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MerisLevel2ProductFactoryTest {

    @Test
    @STTM("SNAP-4215")
    public void testGetBandCacheKey() {
        final MerisLevel2ProductFactory merisLevel2ProductFactory = new MerisLevel2ProductFactory(null);

        final Band wIwv = new Band("W_IWV", ProductData.TYPE_INT8, 2, 2);
        assertEquals("IWV", merisLevel2ProductFactory.getBandCacheKey(wIwv));

        final Band lIwv = new Band("L_IWV", ProductData.TYPE_INT8, 2, 2);
        assertEquals("IWV", merisLevel2ProductFactory.getBandCacheKey(lIwv));

        final Band whatever = new Band("whatever", ProductData.TYPE_INT8, 2, 2);
        assertEquals("whatever", merisLevel2ProductFactory.getBandCacheKey(whatever));
    }
}
