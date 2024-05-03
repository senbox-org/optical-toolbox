package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SlstrFrpProductFactoryTest {

    @Test
    @STTM("SNAP-1684")
    public void testGetTargetBandName() {
        assertEquals("wannachange_in", SlstrFrpProductFactory.getTargetBandName("in", "wannachange"));
        assertEquals("staythesame_an", SlstrFrpProductFactory.getTargetBandName("an", "staythesame_an"));
    }

    @Test
    @STTM("SNAP-1684")
    public void testUpdateFlagCodingNamesFRP() {
        final Product frpInProduct = new Product("FRP_in", "dontcare");
        Band flags = createFlagsBand();
        frpInProduct.addBand(flags);

        SlstrFrpProductFactory.updateFlagCodingNamesFRP(flags);
        assertEquals("flags_in", flags.getFlagCoding().getName());

        final Product frpAnProduct = new Product("FRP_an", "dontcare");
        flags = createFlagsBand();
        frpAnProduct.addBand(flags);

        SlstrFrpProductFactory.updateFlagCodingNamesFRP(flags);
        assertEquals("flags_an_bn", flags.getFlagCoding().getName());

        final Product frpBnProduct = new Product("FRP_bn", "dontcare");
        flags = createFlagsBand();
        frpBnProduct.addBand(flags);

        SlstrFrpProductFactory.updateFlagCodingNamesFRP(flags);
        assertEquals("flags_an_bn", flags.getFlagCoding().getName());

        final Product otherProduct = new Product("other", "dontcare");
        flags = createFlagsBand();
        otherProduct.addBand(flags);

        SlstrFrpProductFactory.updateFlagCodingNamesFRP(flags);
        assertEquals("flags", flags.getFlagCoding().getName());

    }

    private static Band createFlagsBand() {
        final Band flags = new Band("flags", ProductData.TYPE_UINT8, 3, 3);
        flags.setSampleCoding(new FlagCoding("flags"));
        return flags;
    }
}
