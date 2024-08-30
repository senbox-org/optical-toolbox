package eu.esa.opt.c2rcc.meris;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.c2rcc.util.TestSample;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.dataop.dem.ElevationModel;
import org.esa.snap.core.gpf.pointop.Sample;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class C2rccMerisDEMTest {

    final C2rccMerisOperator operator = initOperator();
    private final GeoPos geoPos = new GeoPos(12.5511, 123.321);
    Sample[] sourceSamples;

    @Before
    public void setUp() {
        final double expectedAltitude = 123.32;
        final int index = C2rccMerisOperator.BAND_COUNT;
        this.sourceSamples = new Sample[index + 1];
        this.sourceSamples[index] = new TestSample(expectedAltitude);
    }

    @Test
    @STTM("SNAP-1699")
    public void testDEMCopernicus90() throws Exception {
        operator.setParameter("demName", C2rccMerisOperator.DEM_NAME_COPERNICUS90);
        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);

        assertEquals("Copernicus90mElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testDEMCopernicus30() throws Exception {
        operator.setParameter("demName", C2rccMerisOperator.DEM_NAME_COPERNICUS30);
        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);

        assertEquals("Copernicus30mElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testDEMGetasse30() throws Exception {
        operator.setParameter("demName", C2rccMerisOperator.DEM_NAME_GETASSE30);
        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);

        assertEquals("GETASSE30ElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testUseAltitudeBand() throws Exception {
        operator.setParameter("demName", C2rccMerisOperator.DEFAULT_ALTITUDE);

        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);
        final double expected = this.sourceSamples[C2rccMerisOperator.BAND_COUNT].getDouble();

        assertNull(elevationModel);
        assertEquals(expected, actual, .0001);
    }

    private C2rccMerisOperator initOperator() {
        return (C2rccMerisOperator) new C2rccMerisOperator.Spi().createOperator();
    }
}