package eu.esa.opt.meris.sdr.cloud;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlueBandOpTest {

    @Test
    public void testIsSnowPlausible() {
        float dont_care = 99.f;
        int any_month = 99;

        assertFalse(BlueBandOp.isSnowPlausible(dont_care, dont_care, any_month, false ));

        assertTrue(BlueBandOp.isSnowPlausible(62.f, dont_care, any_month, true ));
        assertTrue(BlueBandOp.isSnowPlausible(-62.f, dont_care, any_month, true ));

        assertTrue(BlueBandOp.isSnowPlausible(38.f, dont_care, any_month, true ));
        assertTrue(BlueBandOp.isSnowPlausible(38.f, 2000, any_month, true ));
        assertTrue(BlueBandOp.isSnowPlausible(38.f, 400, any_month, true ));
        assertTrue(BlueBandOp.isSnowPlausible(38.f, 2000, 7, true ));
        assertFalse(BlueBandOp.isSnowPlausible(38.f, 400, 8, true ));

        assertFalse(BlueBandOp.isSnowPlausible(-40.f, dont_care, any_month, true ));
        assertTrue(BlueBandOp.isSnowPlausible(-40.f, dont_care, 7, true ));
        assertTrue(BlueBandOp.isSnowPlausible(-40.f, 1600, 12, true ));
        assertTrue(BlueBandOp.isSnowPlausible(-40.f, 1600, 2, true ));

        assertTrue(BlueBandOp.isSnowPlausible(-20.f, 3000, any_month, true ));
        assertTrue(BlueBandOp.isSnowPlausible(17.f, 3000, any_month, true ));
    }
}
