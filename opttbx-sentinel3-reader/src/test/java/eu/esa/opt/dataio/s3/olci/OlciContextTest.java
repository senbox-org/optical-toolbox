package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.util.GeoLocationNames;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OlciContextTest {

    private OlciContext olciContext;

    @Before
    public void setUp() {
        olciContext = new OlciContext();
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetInversePixelGeoCodingKey() {
        assertEquals("opttbx.reader.olci.pixelGeoCoding.inverse", olciContext.getInversePixelGeoCodingKey());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetTiePointForwardGeoCodingKey() {
        assertEquals("opttbx.reader.olci.tiePointGeoCoding.forward", olciContext.getTiePointForwardGeoCodingKey());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testUsePixelGeoCodingKey() {
        assertEquals("opttbx.reader.olci.pixelGeoCoding", olciContext.getUsePixelGeoCodingKey());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetCustomCalibrationKey() {
        assertEquals("opttbx.reader.olcil1.applyCustomCalibration", olciContext.getCustomCalibrationKey());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetCalibrationPatternKey() {
        assertEquals("opttbx.reader.olcil1.ID.calibration.TYPE", olciContext.getCalibrationPatternKey());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetGeoLocationNames() {
        final GeoLocationNames geoLocationNames = olciContext.getGeoLocationNames();
        assertEquals("longitude", geoLocationNames.getLongitudeName());
        assertEquals("latitude", geoLocationNames.getLatitudeName());
        assertEquals("TP_longitude", geoLocationNames.getTpLongitudeName());
        assertEquals("TP_latitude", geoLocationNames.getTpLatitudeName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testBandNameToKey() {
        assertEquals("Oa01", olciContext.bandNameToKey("Oa01_radiance"));
        assertEquals("Oa02", olciContext.bandNameToKey("Oa02_radiance_unc"));
    }
}
