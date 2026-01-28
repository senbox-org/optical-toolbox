package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.manifest.XfduManifest;
import eu.esa.opt.dataio.s3.util.GeoLocationNames;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.runtime.Config;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.prefs.Preferences;

import static org.junit.Assert.*;

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
    public void testGetGeoLocationNames() {
        final GeoLocationNames geoLocationNames = olciContext.getGeoLocationNames();
        assertEquals("longitude", geoLocationNames.getLongitudeName());
        assertEquals("latitude", geoLocationNames.getLatitudeName());
        assertEquals("TP_longitude", geoLocationNames.getTpLongitudeName());
        assertEquals("TP_latitude", geoLocationNames.getTpLatitudeName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711,SNAP-4149")
    public void testBandNameToKey() {
        assertEquals("Oa01", olciContext.bandNameToKey("Oa01_radiance"));
        assertEquals("Oa02", olciContext.bandNameToKey("Oa02_radiance_unc"));
        assertEquals("IWV", olciContext.bandNameToKey("IWV"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetBandDescriptionsElement() {
        final Manifest manifest = getOlciManifest();

        final MetadataElement bandDescriptionsElement = olciContext.getBandDescriptionsElement(manifest);
        assertNotNull(bandDescriptionsElement);
        assertEquals("bandDescriptions", bandDescriptionsElement.getName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetResolutionInKm() {
        assertEquals(1.2, olciContext.getResolutionInKm("OL_1_ERR"), 1e-8);
        assertEquals(0.3, olciContext.getResolutionInKm("OL_1_EFR"), 1e-8);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetResolutionInKm_invalid() {
        try {
            olciContext.getResolutionInKm("strange_type");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddDescriptionAndUnit_default() {
        final Band band = new Band("Oa04_radiance", ProductData.TYPE_UINT16, 3, 4);
        final VariableDescriptor descriptor = new VariableDescriptor();
        descriptor.setDescription("the description we want");
        descriptor.setUnits("squareinchperminute");

        olciContext.addDescriptionAndUnit(band, descriptor);

        assertEquals("the description we want", band.getDescription());
        assertEquals("squareinchperminute", band.getUnit());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddDescriptionAndUnit_uncertaintyVariable() {
        final Band band = new Band("Oa04_radiance_unc", ProductData.TYPE_UINT16, 3, 4);
        final VariableDescriptor descriptor = new VariableDescriptor();
        descriptor.setDescription("log10 scaled the description we want");
        descriptor.setUnits("lg(squareinchperminute)");

        olciContext.addDescriptionAndUnit(band, descriptor);

        assertEquals("the description we want", band.getDescription());
        assertEquals("squareinchperminute", band.getUnit());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddDescriptionAndUnit_logGeophysicalData() {
        final Band band = new Band("aphy_443", ProductData.TYPE_UINT16, 3, 4);
        final VariableDescriptor descriptor = new VariableDescriptor();
        descriptor.setDescription("log10 scaled the description we want");
        descriptor.setUnits("lg(squareinchperminute)");

        olciContext.addDescriptionAndUnit(band, descriptor);

        assertEquals("the description we want", band.getDescription());
        assertEquals("squareinchperminute", band.getUnit());
    }

    @Test
    @STTM("SNAP-3728,SNAP-1696,SNAP-3711")
    public void testIsLogScaledGeophysicalData() {
        assertTrue(OlciContext.isLogScaledGeophysicalData("anw_443"));
        assertTrue(OlciContext.isLogScaledGeophysicalData("aphy_443"));
        assertTrue(OlciContext.isLogScaledGeophysicalData("bbp_443"));
        assertTrue(OlciContext.isLogScaledGeophysicalData("bbp_slope"));
        assertTrue(OlciContext.isLogScaledGeophysicalData("ADG443_NN"));
        assertTrue(OlciContext.isLogScaledGeophysicalData("CHL_OC4ME_unc"));
        assertTrue(OlciContext.isLogScaledGeophysicalData("KD490_M07"));
        assertTrue(OlciContext.isLogScaledGeophysicalData("TSM_NN_unc"));

        assertFalse(OlciContext.isLogScaledGeophysicalData("Oa03_radiance_unc"));
        assertFalse(OlciContext.isLogScaledGeophysicalData("Oa11_radiance"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testApplyCalibration_notSet() {
        final Preferences preferences = Config.instance("opttbx").preferences();
        boolean oldValue = preferences.getBoolean("opttbx.reader.olcil1.applyCustomCalibration", false);

        Band band = new Band("whatever", ProductData.TYPE_UINT16, 3, 4);
        band.setScalingFactor(100.0);
        band.setScalingOffset(-20.0);

        try {
            preferences.putBoolean("opttbx.reader.olcil1.applyCustomCalibration", false);
            olciContext.applyCalibration(band);

            assertEquals(100.0, band.getScalingFactor(), 1e-8);
            assertEquals(-20.0, band.getScalingOffset(), 1e-8);

        } finally {
            preferences.putBoolean("opttbx.reader.olcil1.applyCustomCalibration", oldValue);
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testApplyCalibration() {
        final Preferences preferences = Config.instance("opttbx").preferences();
        final boolean oldValue = preferences.getBoolean("opttbx.reader.olcil1.applyCustomCalibration", false);
        final double oldScaleFactor = preferences.getDouble("opttbx.reader.olcil1.to_scale.calibration.factor", Double.NaN);
        final double oldScaleOffset = preferences.getDouble("opttbx.reader.olcil1.to_scale.calibration.offset", Double.NaN);

        Band band = new Band("to_scale", ProductData.TYPE_UINT16, 3, 4);
        band.setScalingFactor(100.0);
        band.setScalingOffset(-20.0);

        try {
            preferences.putBoolean("opttbx.reader.olcil1.applyCustomCalibration", true);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.factor", 1.87);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.offset", -106.3);
            olciContext.applyCalibration(band);

            assertEquals(1.87, band.getScalingFactor(), 1e-8);
            assertEquals(-106.3, band.getScalingOffset(), 1e-8);

        } finally {
            preferences.putBoolean("opttbx.reader.olcil1.applyCustomCalibration", oldValue);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.factor", oldScaleFactor);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.offset", oldScaleOffset);
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testApplyCalibration_unscaledBandname() {
        final Preferences preferences = Config.instance("opttbx").preferences();
        final boolean oldValue = preferences.getBoolean("opttbx.reader.olcil1.applyCustomCalibration", false);
        final double oldScaleFactor = preferences.getDouble("opttbx.reader.olcil1.to_scale.calibration.factor", Double.NaN);
        final double oldScaleOffset = preferences.getDouble("opttbx.reader.olcil1.to_scale.calibration.offset", Double.NaN);

        Band band = new Band("not_to_scale", ProductData.TYPE_UINT16, 3, 4);
        band.setScalingFactor(100.0);
        band.setScalingOffset(-20.0);

        try {
            preferences.putBoolean("opttbx.reader.olcil1.applyCustomCalibration", true);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.factor", 1.87);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.offset", -106.3);
            olciContext.applyCalibration(band);

            assertEquals(100, band.getScalingFactor(), 1e-8);
            assertEquals(-20, band.getScalingOffset(), 1e-8);

        } finally {
            preferences.putBoolean("opttbx.reader.olcil1.applyCustomCalibration", oldValue);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.factor", oldScaleFactor);
            preferences.putDouble("opttbx.reader.olcil1.to_scale.calibration.offset", oldScaleOffset);
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetCalibrationProperty() {
        assertEquals("opttbx.reader.olcil1.bella.calibration.offset", OlciContext.getCalibrationProperty("bella", "offset"));
        assertEquals("opttbx.reader.olcil1.ciao.calibration.factor", OlciContext.getCalibrationProperty("Ciao", "factor"));
    }

    private Manifest getOlciManifest() {
        return new Manifest() {
            @Override
            public String getProductName() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String getProductType() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String getBaselineCollection() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String getDescription() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public ProductData.UTC getStartTime() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public ProductData.UTC getStopTime() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public List<String> getFileNames(String schema) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public List<String> getFileNames(String[] excluded) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public MetadataElement getMetadata() {
                final MetadataElement root = new MetadataElement("root");
                final MetadataElement metadataSection = new MetadataElement("metadataSection");
                final MetadataElement olciProductInformation = new MetadataElement("olciProductInformation");
                final MetadataElement bandDescriptions = new MetadataElement("bandDescriptions");

                olciProductInformation.addElement(bandDescriptions);
                metadataSection.addElement(olciProductInformation);
                root.addElement(metadataSection);
                return root;
            }

            @Override
            public String getXPathString(String xPath) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public int getXPathInt(String xPath) {
                throw new RuntimeException("not implemented");
            }
        };
    }
}
