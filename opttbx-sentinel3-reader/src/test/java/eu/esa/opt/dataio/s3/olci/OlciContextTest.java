package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.manifest.XfduManifest;
import eu.esa.opt.dataio.s3.util.GeoLocationNames;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
