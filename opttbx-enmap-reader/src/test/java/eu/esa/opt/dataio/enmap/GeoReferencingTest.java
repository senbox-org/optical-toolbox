package eu.esa.opt.dataio.enmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeoReferencingTest {

    @Test
    public void testConstruction() {
        final GeoReferencing geoReferencing = new GeoReferencing("schief", 6.098);

        assertEquals("schief", geoReferencing.projection);
        assertEquals(6.098, geoReferencing.resolution, 1e-8);
        assertEquals(0.0, geoReferencing.northing, 1e-8);
        assertEquals(0.0, geoReferencing.easting, 1e-8);
        assertEquals(0.0, geoReferencing.refX, 1e-8);
        assertEquals(0.0, geoReferencing.refY, 1e-8);
    }
}
