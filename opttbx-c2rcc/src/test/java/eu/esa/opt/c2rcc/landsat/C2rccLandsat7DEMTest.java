package eu.esa.opt.c2rcc.landsat;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.dataop.dem.ElevationModel;
import org.junit.Test;

import static org.junit.Assert.*;

public class C2rccLandsat7DEMTest {

    final C2rccLandsat7Operator operator = initOperator();
    private GeoPos geoPos = new GeoPos(23.5511, 68.9937);

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
        double elevation = 2.4;
        operator.setParameter("elevation", elevation);

        final double altitude = operator.getAltitude(geoPos);

        assertNull(operator.getElevationModel());
        assertEquals(elevation, altitude, .0000001);
    }

    private C2rccLandsat7Operator initOperator() {
        return (C2rccLandsat7Operator) new C2rccLandsat7Operator.Spi().createOperator();
    }
}