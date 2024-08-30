package eu.esa.opt.c2rcc.msi;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.test.LongTestRunner;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.dataop.dem.ElevationModel;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(LongTestRunner.class)
public class C2rccMsiDEMTest {

    final C2rccMsiOperator operator = initOperator();
    private GeoPos geoPos = new GeoPos(53.5511, 9.9937);

    @Test
    @STTM("SNAP-1699")
    public void testDEMCopernicus90() throws Exception {
        operator.setParameter("demName", C2rccMsiOperator.DEFAULT_DEM_NAME);
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
        operator.setParameter("demName", C2rccMsiOperator.DEM_NAME_COPERNICUS30);
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
        operator.setParameter("demName", C2rccMsiOperator.DEM_NAME_GETASSE30);
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

        operator.setParameter("demName", C2rccMsiOperator.DEM_NAME_GETASSE30);
        operator.setParameter("elevation", elevation);

        final double altitude = operator.getAltitude(geoPos);

        assertNull(operator.getElevationModel());
        assertEquals(elevation, altitude, .0000001);
    }

    private C2rccMsiOperator initOperator() {
        return (C2rccMsiOperator) new C2rccMsiOperator.Spi().createOperator();
    }
}