package eu.esa.opt.c2rcc.olci;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.test.LongTestRunner;
import eu.esa.opt.c2rcc.util.TestSample;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.dataop.dem.ElevationModel;
import org.esa.snap.core.gpf.pointop.Sample;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import static org.junit.Assert.*;

@RunWith(LongTestRunner.class)
public class C2rccOlciDEMTest {

    final C2rccOlciOperator operator = initOperator();
    private final GeoPos geoPos = new GeoPos(72.1, 24.34);
    Sample[] sourceSamples;
    final double expectedAltitude = 300;

    @Before
    public void setUp() throws FactoryException, TransformException {
        final int index = C2rccOlciOperator.BAND_COUNT;
        this.sourceSamples = new Sample[index * 3];
        this.sourceSamples[index * 2] = new TestSample(this.expectedAltitude);

        operator.setSourceProduct(OlciTestProduct.create());
        Product targetProduct = operator.getTargetProduct();
    }

    @Test
    @STTM("SNAP-1699")
    public void testUseAltitudeBand() {
        operator.setParameter("demName", C2rccOlciOperator.DEFAULT_ALTITUDE);

        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);
        final double expected = this.sourceSamples[C2rccOlciOperator.BAND_COUNT * 2].getDouble();

        assertNull(elevationModel);
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testDEMCopernicus90() throws Exception {
        operator.setParameter("demName", C2rccOlciOperator.DEM_NAME_COPERNICUS90);
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
        operator.setParameter("demName", C2rccOlciOperator.DEM_NAME_COPERNICUS30);
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
        operator.setParameter("demName", C2rccOlciOperator.DEM_NAME_GETASSE30);
        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);

        assertEquals("GETASSE30ElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }



    private C2rccOlciOperator initOperator() {
        return (C2rccOlciOperator) new C2rccOlciOperator.Spi().createOperator();
    }
}