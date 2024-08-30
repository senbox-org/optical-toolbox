package eu.esa.opt.c2rcc.landsat;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.dataop.dem.ElevationModel;
import org.junit.Test;

import static org.junit.Assert.*;

public class C2rccLandsat8DEMTest {

    final C2rccLandsat8Operator operator = initOperator();
    private GeoPos geoPos = new GeoPos(0.25, 168.4);

    @Test
    @STTM("SNAP-1699")
    public void testDEMCopernicus90() throws Exception {
        operator.setParameter("demName", C2rccLandsat7Operator.DEFAULT_DEM_NAME);
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos);

        assertEquals("Copernicus90mElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testDEMCopernicus30() throws Exception {
        operator.setParameter("demName", C2rccLandsat7Operator.DEM_NAME_COPERNICUS30);
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos);

        assertEquals("Copernicus30mElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testDEMGetasse30() throws Exception {
        operator.setParameter("demName", C2rccLandsat7Operator.DEM_NAME_GETASSE30);
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos);

        assertEquals("GETASSE30ElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testfailedDemInitialisation() throws Exception {
        double elevation = 4.7;
        operator.setParameter("elevation", elevation);

        final double altitude = operator.getAltitude(geoPos);

        assertNull(operator.getElevationModel());
        assertEquals(elevation, altitude, .0000001);
    }

    private C2rccLandsat8Operator initOperator() {
        return (C2rccLandsat8Operator) new C2rccLandsat8Operator.Spi().createOperator();
    }
}