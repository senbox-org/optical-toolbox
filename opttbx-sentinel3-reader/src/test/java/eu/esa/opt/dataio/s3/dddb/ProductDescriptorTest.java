package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ProductDescriptorTest {

    @Test
    @STTM("SNAP-3711")
    public void testGetExcludedIdsAsArray() {
        final ProductDescriptor productDescriptor = new ProductDescriptor();

        // empty case
        assertArrayEquals(new String[0], productDescriptor.getExcludedIdsAsArray());

        productDescriptor.setExcludedIds("hasenfuss");
        assertArrayEquals(new String[] {"hasenfuss"}, productDescriptor.getExcludedIdsAsArray());

        productDescriptor.setExcludedIds("himpelchen, pimpelchen");
        assertArrayEquals(new String[] {"himpelchen", "pimpelchen"}, productDescriptor.getExcludedIdsAsArray());
    }

}
