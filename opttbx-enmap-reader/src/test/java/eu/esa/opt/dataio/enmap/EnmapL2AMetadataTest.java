package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EnmapL2AMetadataTest {

    private static EnmapMetadata meta;

    @BeforeClass
    public static void beforeClass() throws Exception {
        URI resource = Objects.requireNonNull(EnmapL2AMetadataTest.class.getResource("enmap_L2A_gtif_qualification.zip")).toURI();
        try (ZipFile zip = new ZipFile(new File(resource))) {
            ZipEntry entry = zip.getEntry("enmap_L2A_gtif_qualification/ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-METADATA.XML");
            meta = EnmapMetadata.create(zip.getInputStream(entry));
        }
    }

    @Test
    public void testSchemaVersion() throws IOException {
        assertEquals("00.02.00", meta.getSchemaVersion());
    }

    @Test
    public void testProcessingVersion() throws IOException {
        assertEquals("00.02.04", meta.getProcessingVersion());
    }

    @Test
    public void testL0ProcessingVersion() throws IOException {
        assertEquals("00.02.04", meta.getL0ProcessingVersion());
    }

    @Test
    public void testProductFormat() throws IOException {
        assertEquals("GeoTIFF+Metadata", meta.getProductFormat());
    }

    @Test
    public void testBasicProductInfo() throws Exception {
        ProductData.UTC startTime = meta.getStartTime();
        Calendar startCal = startTime.getAsCalendar();
        Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // expected 2017-06-26T10:20:20.999936Z
        assertEquals(2017, startCal.get(Calendar.YEAR));
        assertEquals(5, startCal.get(Calendar.MONTH));
        assertEquals(26, startCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, startCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, startCal.get(Calendar.MINUTE));
        assertEquals(21, startCal.get(Calendar.SECOND));        // time is rounded from microseconds to milliseconds
        assertEquals(0, startCal.get(Calendar.MILLISECOND));    // time is rounded from microseconds to milliseconds
        assertEquals(TimeZone.getTimeZone("UTC"), startCal.getTimeZone());

        ProductData.UTC stopTime = meta.getStopTime();
        Calendar stopCal = stopTime.getAsCalendar();
        // expected 2017-06-26T10:20:25.545157Z
        assertEquals(2017, stopCal.get(Calendar.YEAR));
        assertEquals(5, stopCal.get(Calendar.MONTH));
        assertEquals(26, stopCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, stopCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, stopCal.get(Calendar.MINUTE));
        assertEquals(25, stopCal.get(Calendar.SECOND));
        assertEquals(545, stopCal.get(Calendar.MILLISECOND));
        assertEquals(TimeZone.getTimeZone("UTC"), stopCal.getTimeZone());

        assertEquals("ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z", meta.getProductName());
        assertEquals("ENMAP_L2A", meta.getProductType());
        assertEquals(new Dimension(1128, 1212), meta.getSceneDimension());
        assertEquals(218, meta.getNumSpectralBands());
        assertEquals(88, meta.getNumVnirBands());
        assertEquals(130, meta.getNumSwirBands());
        assertEquals("VNIR surface reflectance @823.46", meta.getSpectralBandDescription(67));
        assertEquals("SWIR surface reflectance @2217.8", meta.getSpectralBandDescription(189));
    }

    @Test
    public void testSpatialCoverage() throws IOException {
        Geometry spatialCoverage = meta.getSpatialCoverage();
        Coordinate[] coordinates = spatialCoverage.getCoordinates();
        assertEquals(new Coordinate(10.7960234, 47.7875252), coordinates[0]);
        assertEquals(new Coordinate(10.7108641, 47.5192797), coordinates[1]);
        assertEquals(new Coordinate(11.0813528, 47.4554646), coordinates[2]);
        assertEquals(new Coordinate(11.1687202, 47.7235135), coordinates[3]);
        assertEquals(new Coordinate(10.7960234, 47.7875252), coordinates[4]);
    }

    @Test
    public void testSpatialOrthoCoverage() throws IOException {
        Geometry orthoCoverage = meta.getSpatialOrthoCoverage();
        Coordinate[] coordinates = orthoCoverage.getCoordinates();
        assertEquals(new Coordinate(4.5113521, 4.31E-4), coordinates[0]);
        assertEquals(new Coordinate(4.511352, 4.281E-4), coordinates[1]);
        assertEquals(new Coordinate(4.5113561, 4.28E-4), coordinates[2]);
        assertEquals(new Coordinate(4.5113562, 4.31E-4), coordinates[3]);
        assertEquals(new Coordinate(4.5113521, 4.31E-4), coordinates[4]);
    }

    @Test
    public void testGeoReferencing() throws IOException {
        GeoReferencing geoReferencing = meta.getGeoReferencing();
        assertEquals("UTM_Zone32_North", geoReferencing.projection);
        assertEquals(30, geoReferencing.resolution, 1.0e-8);
        assertEquals(0, geoReferencing.easting, 1.0e-8);
        assertEquals(0, geoReferencing.northing, 1.0e-8);
        assertEquals(0, geoReferencing.refX, 1.0e-8);
        assertEquals(0, geoReferencing.refY, 1.0e-8);
    }

    @Test
    public void testAngles() throws IOException {
        assertArrayEquals(new double[]{62.843017, 63.025996, 63.048052, 63.232013}, meta.getSunElevationAngles(), 1.0e-6f);
        assertEquals(63.038384f, meta.getSunElevationAngleCenter(), 1.0e-6);

        assertArrayEquals(new double[]{148.98072, 149.614311, 148.59262, 149.224083}, meta.getSunAzimuthAngles(), 1.0e-6f);
        assertEquals(149.106702, meta.getSunAzimuthAngleCenter(), 1.0e-6f);

        assertArrayEquals(new double[]{1.27641076292, -1.35358251158, 1.17928885285, -1.4507597324}, meta.getAcrossOffNadirAngles(), 1.0e-6f);
        assertEquals(-0.0871606570525, meta.getAcrossOffNadirAngleCenter(), 1.0e-6);

        assertArrayEquals(new double[]{-0.0692040225255, -0.0695301149423, -0.16870825914, -0.167785438271}, meta.getAlongOffNadirAngles(), 1.0e-6f);
        assertEquals(-0.11880695872, meta.getAlongOffNadirAngleCenter(), 1.0e-6);

        assertArrayEquals(new double[]{14.2906888082, 14.2906888082, 14.2149804324, 14.2149804324}, meta.getSceneAzimuthAngles(), 1.0e-6f);
        assertEquals(14.2528346203, meta.getSceneAzimuthAngleCenter(), 1.0e-6);
    }

    @Test
    public void testFileMap() {
    }
}