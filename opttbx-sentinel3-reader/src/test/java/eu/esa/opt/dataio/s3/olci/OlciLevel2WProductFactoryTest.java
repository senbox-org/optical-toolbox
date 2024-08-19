package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.binding.PropertyContainer;
import eu.esa.snap.core.datamodel.group.BandGroup;
import eu.esa.snap.core.datamodel.group.BandGroupImpl;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNodeGroup;
import org.junit.Test;

import static org.esa.snap.core.datamodel.Mask.BandMathsType.PROPERTY_NAME_EXPRESSION;
import static org.junit.Assert.*;

public class OlciLevel2WProductFactoryTest {

    @Test
    @STTM("SNAP-3642")
    public void testCreateRecommendedMaskName() {
        assertEquals("WQSF_heffalump_RECOM", OlciLevel2WProductFactory.createRecommendedMaskName("heffalump"));
    }

    @Test
    @STTM("SNAP-3642")
    public void testSetMasks() {
        final OlciLevel2WProductFactory factory = new OlciLevel2WProductFactory(null);

        final Product product = new Product("test", "me", 2, 2);
        factory.setMasks(product);

        final ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
        Mask mask = maskGroup.get("WQSF_REFLECTANCE_RECOM");
        assertNotNull(mask);
        PropertyContainer imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE) and not (WQSF_lsb.AC_FAIL or WQSF_lsb.WHITECAPS or WQSF_lsb.ADJAC or WQSF_msb.RWNEG_O2 or WQSF_msb.RWNEG_O3 or WQSF_msb.RWNEG_O4 or WQSF_msb.RWNEG_O5 or WQSF_msb.RWNEG_O6 or WQSF_msb.RWNEG_O7 or WQSF_msb.RWNEG_O8))", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Water Leaving Reflectances. Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_CHL_OC4ME_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE) and not (WQSF_lsb.AC_FAIL or WQSF_lsb.WHITECAPS or WQSF_lsb.ADJAC or WQSF_msb.RWNEG_O2 or WQSF_msb.RWNEG_O3 or WQSF_msb.RWNEG_O4 or WQSF_msb.RWNEG_O5 or WQSF_msb.RWNEG_O6 or WQSF_msb.RWNEG_O7 or WQSF_msb.RWNEG_O8)) or WQSF_lsb.OC4ME_FAIL", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Algal Pigment Concentration. Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_KD490_M07_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE) and not (WQSF_lsb.AC_FAIL or WQSF_lsb.WHITECAPS or WQSF_lsb.ADJAC or WQSF_msb.RWNEG_O2 or WQSF_msb.RWNEG_O3 or WQSF_msb.RWNEG_O4 or WQSF_msb.RWNEG_O5 or WQSF_msb.RWNEG_O6 or WQSF_msb.RWNEG_O7 or WQSF_msb.RWNEG_O8)) or WQSF_lsb.KDM_FAIL", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Diffuse Attenuation Coefficient. Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_PAR_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE) and not (WQSF_lsb.AC_FAIL or WQSF_lsb.WHITECAPS or WQSF_lsb.ADJAC or WQSF_msb.RWNEG_O2 or WQSF_msb.RWNEG_O3 or WQSF_msb.RWNEG_O4 or WQSF_msb.RWNEG_O5 or WQSF_msb.RWNEG_O6 or WQSF_msb.RWNEG_O7 or WQSF_msb.RWNEG_O8)) or WQSF_lsb.PAR_FAIL", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Photosynthetically Active Radiation. Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_W_AER_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE) and not (WQSF_lsb.AC_FAIL or WQSF_lsb.WHITECAPS or WQSF_lsb.ADJAC or WQSF_msb.RWNEG_O2 or WQSF_msb.RWNEG_O3 or WQSF_msb.RWNEG_O4 or WQSF_msb.RWNEG_O5 or WQSF_msb.RWNEG_O6 or WQSF_msb.RWNEG_O7 or WQSF_msb.RWNEG_O8))", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Aerosol Optical Thickness (T865) and Angstrom exponent (A865). Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_CHL_NN_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE)) or WQSF_lsb.OCNN_FAIL", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Algal Pigment Concentration. Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_TSM_NN_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE)) or WQSF_lsb.OCNN_FAIL", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Total Suspended Matter Concentration. Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_ADG443_NN_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("not ((WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER) and not (WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE)) or WQSF_lsb.OCNN_FAIL", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Coloured Detrital and Dissolved Material Absorption. Flag recommended by QWG.", mask.getDescription());

        mask = maskGroup.get("WQSF_IWV_RECOM");
        assertNotNull(mask);
        imageConfig = mask.getImageConfig();
        assertEquals("WQSF_lsb.MEGLINT or WQSF_lsb.WV_FAIL", imageConfig.getValue(PROPERTY_NAME_EXPRESSION));
        assertEquals("Excluding pixels that are deemed unreliable for Integrated Water Vapour Column. Flag recommended by QWG.", mask.getDescription());
    }

    @Test
    @STTM("SNAP-3728")
    public void testSetAutogrouping() {
        final OlciLevel2WProductFactory factory = new OlciLevel2WProductFactory(null);
        final Product product = new Product("test", "me", 2, 2);

        factory.setAutoGrouping(null, product);

        final BandGroupImpl autoGrouping = (BandGroupImpl) product.getAutoGrouping();
        assertEquals(14, autoGrouping.size());

        final String[] iopGrouping = autoGrouping.get(13);
        assertEquals(1, iopGrouping.length);
        assertEquals("IOP", iopGrouping[0]);
    }
}
