package eu.esa.opt.dataio.enmap;

import org.junit.Test;

import static eu.esa.opt.dataio.enmap.ProductFormat.BIP_Metadata;
import static eu.esa.opt.dataio.enmap.ProductFormat.JPEG2000_Metadata;
import static org.junit.Assert.assertEquals;

public class ProductFormatTest {

    @Test
    public void testToEnumName() {
        assertEquals("GeoTIFF_Metadata", ProductFormat.toEnumName("GeoTIFF+Metadata"));
        assertEquals("BSQ_Metadata", ProductFormat.toEnumName("BSQ+Metadata"));
    }

    @Test
    public void testAsEnmapFormatName() {
        assertEquals("BIP+Metadata", BIP_Metadata.asEnmapFormatName());
        assertEquals("JPEG2000+Metadata", JPEG2000_Metadata.asEnmapFormatName());
    }
}
