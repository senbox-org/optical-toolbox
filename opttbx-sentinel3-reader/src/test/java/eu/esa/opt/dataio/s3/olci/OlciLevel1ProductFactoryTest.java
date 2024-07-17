package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.annotation.STTM;
import eu.esa.snap.core.datamodel.group.BandGroup;
import org.esa.snap.core.datamodel.Product;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class OlciLevel1ProductFactoryTest {

    @STTM("SNAP-3519")
    @Test
    public void testBandGroupingPattern() {
        String bandGroupingPattern = OlciLevel1ProductFactory.BAND_GROUPING_PATTERN;
        Product dummy = new Product("name", "type", 10, 10);
        dummy.setAutoGrouping(bandGroupingPattern);
        BandGroup autoGrouping = dummy.getAutoGrouping();

        assertNotEquals(-1, autoGrouping.indexOf("Oa02_radiance"));
        assertNotEquals(-1, autoGrouping.indexOf("Oa20_radiance"));
        assertNotEquals(-1, autoGrouping.indexOf("Oa13_radiance_unc"));
        assertNotEquals(-1, autoGrouping.indexOf("Oa01_radiance_unc"));
        assertNotEquals(-1, autoGrouping.indexOf("atmospheric_temperature_profile_pressure_level_3"));
        assertNotEquals(-1, autoGrouping.indexOf("lambda0_band_7"));
        assertNotEquals(-1, autoGrouping.indexOf("FWHM_band_3"));
        assertNotEquals(-1, autoGrouping.indexOf("solar_flux_band_19"));
        assertEquals(-1, autoGrouping.indexOf("not_handled"));

        // err kept because it is not causing problems, just in case if somewhere exists a product with err bands
        assertNotEquals(-1, autoGrouping.indexOf("Oa13_radiance_err"));

    }
}