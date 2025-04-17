package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeoLocationNamesTest {

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testConstructionAndGetter() {
        final GeoLocationNames geoLocationNames = new GeoLocationNames("franzi", "adele", "horst", "heinz");

        assertEquals("franzi", geoLocationNames.getLongitudeName());
        assertEquals("adele", geoLocationNames.getLatitudeName());
        assertEquals("horst", geoLocationNames.getTpLongitudeName());
        assertEquals("heinz", geoLocationNames.getTpLatitudeName());
    }
}
