package eu.esa.opt.c2rcc.meris4;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.c2rcc.util.TestSample;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.dataop.dem.ElevationModel;
import org.esa.snap.core.gpf.pointop.Sample;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.util.Date;

import static org.junit.Assert.*;

public class C2rccMeris4DEMTest {

    final C2rccMeris4Operator operator = initOperator();
    private final GeoPos geoPos = new GeoPos(12.5511, 123.321);
    Sample[] sourceSamples;
    final double expectedAltitude = 500;

    @Before
    public void setUp() throws FactoryException, TransformException {
        final int index = C2rccMeris4Operator.BAND_COUNT;
        this.sourceSamples = new Sample[index * 3];
        this.sourceSamples[index * 2] = new TestSample(this.expectedAltitude);

        operator.setSourceProduct(createMeris4TestProduct());
        Product targetProduct = operator.getTargetProduct();
    }

    @Test
    @STTM("SNAP-1699")
    public void testUseAltitudeBand() {
        operator.setParameter("demName", C2rccMeris4Operator.DEFAULT_ALTITUDE);

        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);
        final double expected = this.sourceSamples[C2rccMeris4Operator.BAND_COUNT * 2].getDouble();

        assertNull(elevationModel);
        assertEquals(expected, actual, .0001);
    }

    @Test
    @STTM("SNAP-1699")
    public void testDEMCopernicus90() throws Exception {
        operator.setParameter("demName", C2rccMeris4Operator.DEM_NAME_COPERNICUS90);
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
        operator.setParameter("demName", C2rccMeris4Operator.DEM_NAME_COPERNICUS30);
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
        operator.setParameter("demName", C2rccMeris4Operator.DEM_NAME_GETASSE30);
        operator.initialiseUseSnapDEM();
        operator.initialiseElevationModel();

        final ElevationModel elevationModel = operator.getElevationModel();
        final double expected = elevationModel.getElevation(geoPos);
        final double actual = operator.getAltitude(geoPos, this.sourceSamples);

        assertEquals("GETASSE30ElevationModel", elevationModel.getClass().getSimpleName());
        assertEquals(expected, actual, .0001);
    }


    private C2rccMeris4Operator initOperator() {
        return (C2rccMeris4Operator) new C2rccMeris4Operator.Spi().createOperator();
    }

    private Product createMeris4TestProduct() throws FactoryException, TransformException {
        Product product = new Product("test-olci", "t", 1, 1);
        for (int i = 1; i <= C2rccMeris4Operator.BAND_COUNT; i++) {
            String expression = String.valueOf(i);
            product.addBand(String.format("M%02d_radiance", i), expression);
            product.addBand("solar_flux_band_" + i, expression);
        }

        Date time = new Date();
        product.setStartTime(ProductData.UTC.create(time, 0));
        product.setEndTime(ProductData.UTC.create(time, 500));


        product.addBand(C2rccMeris4Operator.RASTER_NAME_ALTITUDE, String.valueOf(this.expectedAltitude));
        product.addBand(C2rccMeris4Operator.RASTER_NAME_SUN_AZIMUTH, "42");
        product.addBand(C2rccMeris4Operator.RASTER_NAME_SUN_ZENITH, "42");
        product.addBand(C2rccMeris4Operator.RASTER_NAME_VIEWING_AZIMUTH, "42");
        product.addBand(C2rccMeris4Operator.RASTER_NAME_VIEWING_ZENITH, "42");
        product.addBand(C2rccMeris4Operator.RASTER_NAME_SEA_LEVEL_PRESSURE, "999");
        product.addBand(C2rccMeris4Operator.RASTER_NAME_TOTAL_OZONE, "0.004");
        Band flagBand = product.addBand(C2rccMeris4Operator.RASTER_NAME_QUALITY_FLAGS, ProductData.TYPE_INT8);
        FlagCoding l1FlagsCoding = new FlagCoding(C2rccMeris4Operator.RASTER_NAME_QUALITY_FLAGS);
        product.getFlagCodingGroup().add(l1FlagsCoding);
        flagBand.setSampleCoding(l1FlagsCoding);

        product.setSceneGeoCoding(new CrsGeoCoding(DefaultGeographicCRS.WGS84, 1, 1, 10, 50, 1, 1));

        return product;
    }
}