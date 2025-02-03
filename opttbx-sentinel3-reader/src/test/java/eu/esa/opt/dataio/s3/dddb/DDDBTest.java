package eu.esa.opt.dataio.s3.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DDDBTest {

    private DDDB dddb;

    @Before
    public void setUp() {
        dddb = DDDB.getInstance();
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testIsSingleton() {
        final DDDB dddb_2 = DDDB.getInstance();

        assertSame(dddb_2, dddb);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetProductDescriptor() throws IOException {
        ProductDescriptor olciL1Descriptor = dddb.getProductDescriptor("OL_1_EFR", "004");
        assertEquals("removedPixelsData", olciL1Descriptor.getExcludedIds());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/columns", olciL1Descriptor.getWidthXPath());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/rows", olciL1Descriptor.getHeightXPath());
        assertEquals("Oa*_radiance:Oa*_radiance_unc:Oa*_radiance_err:atmospheric_temperature_profile:lambda0:FWHM:solar_flux", olciL1Descriptor.getBandGroupingPattern());

        olciL1Descriptor = dddb.getProductDescriptor("OL_1_ERR", "___");
        assertEquals("removedPixelsData", olciL1Descriptor.getExcludedIds());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/columns", olciL1Descriptor.getWidthXPath());

        final ProductDescriptor slstrL1Descriptor = dddb.getProductDescriptor("SL_1_RBT", "004");
        assertEquals("", slstrL1Descriptor.getExcludedIds());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='slstrProductInformation']//metadataWrap/xmlData/slstrProductInformation/nadirImageSize[@grid=\"0.5 km stripe A\"]/columns", slstrL1Descriptor.getWidthXPath());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='slstrProductInformation']//metadataWrap/xmlData/slstrProductInformation/nadirImageSize[@grid=\"0.5 km stripe A\"]/rows", slstrL1Descriptor.getHeightXPath());
        assertEquals("", slstrL1Descriptor.getBandGroupingPattern());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetProductDescriptor_invalidResource() throws IOException {
        try {
            dddb.getProductDescriptor("IN_V_ALI", "D");
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetResourceFileName() {
        assertEquals("OL_1_EFR_004.json", DDDB.getResourceFileName("OL_1_EFR", "004"));
        assertEquals("SL_1_RBT_004.json", DDDB.getResourceFileName("SL_1_RBT", "004"));

        assertEquals("SL_1_RBT.json", DDDB.getResourceFileName("SL_1_RBT", ""));
        assertEquals("OL_1_EFR.json", DDDB.getResourceFileName("OL_1_EFR", null));

        assertEquals("/variables/instrument_data_004.json", DDDB.getResourceFileName("/variables/instrument_data", "004"));
        assertEquals("/variables/instrument_data.json", DDDB.getResourceFileName("/variables/instrument_data", null));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetDDDBResourceName() {
        assertEquals("dddb/SL_1_RBT/heffalump.org", DDDB.getDddbResourceName("SL_1_RBT", "heffalump.org"));
        assertEquals("dddb/OL_1_EFR/variables/geo_coordinates.json", DDDB.getDddbResourceName("OL_1_EFR", "variables/geo_coordinates.json"));

        assertEquals("dddb/SL_1_RBT", DDDB.getDddbResourceName("SL_1_RBT", ""));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetVariableDescriptors() throws IOException {
        VariableDescriptor[] variableDescriptors = dddb.getVariableDescriptors("geo_coordinates.nc", "OL_1_EFR", "004");

        assertEquals(3, variableDescriptors.length);
        assertEquals("altitude", variableDescriptors[0].getName());
        assertEquals("float32", variableDescriptors[1].getDataType());
        assertEquals('v', variableDescriptors[1].getType());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/columns", variableDescriptors[2].getWidthXPath());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/rows", variableDescriptors[0].getHeightXPath());
        assertEquals("degrees_north", variableDescriptors[1].getUnits());
        assertEquals("DEM corrected altitude", variableDescriptors[0].getDescription());

        variableDescriptors = dddb.getVariableDescriptors("instrument_data.nc", "OL_1_EFR", "004");

        assertEquals(6, variableDescriptors.length);
        assertEquals("FWHM", variableDescriptors[0].getName());
        assertEquals("int16", variableDescriptors[1].getDataType());
        assertEquals('v', variableDescriptors[2].getType());
        assertEquals("nm", variableDescriptors[3].getUnits());
        assertEquals("Relative spectral covariance matrix", variableDescriptors[4].getDescription());

        assertNull(variableDescriptors[3].getWidthXPath());
        assertEquals(3700, variableDescriptors[3].getWidth());

        assertNull(variableDescriptors[4].getHeightXPath());
        assertEquals(21, variableDescriptors[4].getHeight());
        assertEquals('m', variableDescriptors[4].getType());

        variableDescriptors = dddb.getVariableDescriptors("Oa07_radiance.nc", "OL_1_EFR", null);
        assertEquals(1, variableDescriptors.length);
        assertEquals("Oa07_radiance", variableDescriptors[0].getName());
        assertEquals("float32", variableDescriptors[0].getDataType());
        assertEquals("!quality_flags.invalid", variableDescriptors[0].getValidExpression());
        assertEquals("mW.m-2.sr-1.nm-1", variableDescriptors[0].getUnits());
        assertEquals("TOA radiance for OLCI acquisition band Oa07", variableDescriptors[0].getDescription());

        variableDescriptors = dddb.getVariableDescriptors("Oa14_radiance_unc.nc", "OL_1_EFR", "003");
        assertEquals(1, variableDescriptors.length);
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/rows", variableDescriptors[0].getHeightXPath());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/columns", variableDescriptors[0].getWidthXPath());
        assertEquals("!quality_flags.invalid", variableDescriptors[0].getValidExpression());
        assertEquals("log10 scaled Radiometric Uncertainty Estimate for OLCI acquisition band Oa14", variableDescriptors[0].getDescription());

        variableDescriptors = dddb.getVariableDescriptors("qualityFlags.nc", "OL_1_EFR", "003");
        assertEquals(1, variableDescriptors.length);
        assertEquals("Classification and quality flags", variableDescriptors[0].getDescription());

        variableDescriptors = dddb.getVariableDescriptors("tie_geo_coordinates.nc", "OL_1_EFR", "004");
        assertEquals(2, variableDescriptors.length);
        assertEquals("TP_latitude", variableDescriptors[0].getName());
        assertEquals("latitude", variableDescriptors[0].getNcVarName());
        assertEquals("Latitude", variableDescriptors[0].getDescription());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/samplingParameters/columnsPerTiePoint", variableDescriptors[0].getTpXSubsamplingXPath());
        assertEquals("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/samplingParameters/rowsPerTiePoint", variableDescriptors[0].getTpYSubsamplingXPath());

        assertEquals('t', variableDescriptors[1].getType());
        assertEquals(-1, variableDescriptors[1].getHeight());
        assertEquals("degrees_north", variableDescriptors[0].getUnits());

        variableDescriptors = dddb.getVariableDescriptors("tie_geometries.nc", "OL_1_EFR", "003");
        assertEquals(4, variableDescriptors.length);
        assertEquals("Sun Zenith Angle", variableDescriptors[3].getDescription());

        variableDescriptors = dddb.getVariableDescriptors("tie_meteo.nc", "OL_1_EFR", null);
        assertEquals(7, variableDescriptors.length);
        assertEquals("Mean sea level pressure", variableDescriptors[4].getDescription());

        variableDescriptors = dddb.getVariableDescriptors("time_coordinates.nc", "OL_1_EFR", null);
        assertEquals(1, variableDescriptors.length);
        assertEquals("Elapsed time since 01 Jan 2000 0h", variableDescriptors[0].getDescription());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetVariableDescriptors_notExisting() {
        try {
            dddb.getVariableDescriptors("now_way_this_exist.ts", "OL_1_EFR", "004");
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }
}
