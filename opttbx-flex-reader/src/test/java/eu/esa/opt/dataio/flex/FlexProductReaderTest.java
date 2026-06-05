package eu.esa.opt.dataio.flex;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import eu.esa.opt.dataio.flex.dddb.FlexFlagMask;
import eu.esa.opt.dataio.flex.dddb.FlexProductDescriptor;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductSpectralAxis;
import org.esa.snap.runtime.Config;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Section;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FlexProductReaderTest {


    @Test
    @STTM("SNAP-4126")
    public void testIsSubsetReadingFullySupported() {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertTrue(reader.isSubsetReadingFullySupported());
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadGeoCoding_withoutLatitudeLongitudeBands_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);

        final GeoCoding geoCoding = reader.readGeoCoding(product);

        assertNull(geoCoding);
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadGeoCoding_withOnlyLongitudeBand_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("longitude", ProductData.TYPE_FLOAT32);

        assertNull(reader.readGeoCoding(product));
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadGeoCoding_withOnlyPrefixedLongitudeBand_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("HRE1_longitude", ProductData.TYPE_FLOAT32);

        assertNull(reader.readGeoCoding(product));
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadGeoCoding_usesCachedL1bGridGeoCoding() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "FLX_L1B_OBS", 10, 10);
        final GeoCoding geoCoding = mock(GeoCoding.class);
        geoCodingMap(reader).put("lres", geoCoding);

        assertSame(geoCoding, reader.readGeoCoding(product));
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadElement_descriptorMissing_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertNull(reader.readElement("missing"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadElement_variableMissing_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        final FlexVariableDescriptor descriptor = mockDescriptor(
                "metadata_var",
                "Group/metadata_var",
                "",
                "Group",
                "metadata_var"
        );

        metadataMap(reader).put("metadata_var", descriptor);

        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findVariable("Group/metadata_var")).thenReturn(null);
        ncFilesMap(reader).put("data.nc", ncFile);

        assertNull(reader.readElement("metadata_var"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadElement_variableFoundByFullPath_returnsMetadataElement() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        final FlexVariableDescriptor descriptor = mockDescriptor(
                "metadata_var",
                "Group/metadata_var",
                "",
                "Group",
                "metadata_var"
        );

        metadataMap(reader).put("metadata_var", descriptor);

        final Variable variable = mockStringVariable("Group/metadata_var");

        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findVariable("Group/metadata_var")).thenReturn(variable);
        ncFilesMap(reader).put("data.nc", ncFile);

        final MetadataElement element = reader.readElement("metadata_var");

        assertNotNull(element);
        assertEquals("Group/metadata_var", element.getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadElement_variableFoundByAlternativeGroupPath_returnsMetadataElement() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        final FlexVariableDescriptor descriptor = mockDescriptor(
                "metadata_var",
                "Quality_flags/metadata_var",
                "",
                "Quality_flags",
                "metadata_var"
        );

        metadataMap(reader).put("metadata_var", descriptor);

        final Variable variable = mockStringVariable("Quality flags/metadata_var");

        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findVariable("Quality_flags/metadata_var")).thenReturn(null);
        when(ncFile.findVariable("Quality flags/metadata_var")).thenReturn(variable);
        ncFilesMap(reader).put("data.nc", ncFile);

        final MetadataElement element = reader.readElement("metadata_var");

        assertNotNull(element);
        assertEquals("Quality flags/metadata_var", element.getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadElement_respectsNcDataFileFilter() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        final FlexVariableDescriptor descriptor = mockDescriptor(
                "metadata_var",
                "Group/metadata_var",
                "target",
                "Group",
                "metadata_var"
        );

        metadataMap(reader).put("metadata_var", descriptor);

        final NetcdfFile skippedFile = mock(NetcdfFile.class);
        final NetcdfFile targetFile = mock(NetcdfFile.class);
        final Variable variable = mockStringVariable("Group/metadata_var");

        when(targetFile.findVariable("Group/metadata_var")).thenReturn(variable);

        ncFilesMap(reader).put("other.nc", skippedFile);
        ncFilesMap(reader).put("target.nc", targetFile);

        final MetadataElement element = reader.readElement("metadata_var");

        assertNotNull(element);
        verify(skippedFile, never()).findVariable(anyString());
        verify(targetFile).findVariable("Group/metadata_var");
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadElement_usesVariableCache() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        final FlexVariableDescriptor descriptor = mockDescriptor(
                "metadata_var",
                "Group/metadata_var",
                "",
                "Group",
                "metadata_var"
        );

        metadataMap(reader).put("metadata_var", descriptor);

        final Variable variable = mockStringVariable("cached_var");
        ncVariablesCache(reader).put("metadata_var", variable);

        final NetcdfFile ncFile = mock(NetcdfFile.class);
        ncFilesMap(reader).put("data.nc", ncFile);

        final MetadataElement element = reader.readElement("metadata_var");

        assertNotNull(element);
        assertEquals("cached_var", element.getName());
        verify(ncFile, never()).findVariable(anyString());
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetGridId_fromDescriptorToFileMap() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        descriptorToFileMap(reader).put("FLORIS_HR1B_1_radiance", "measurement_data_hre1");
        descriptorToFileMap(reader).put("FLORIS_HR2B_1_radiance", "measurement_data_hre2");
        descriptorToFileMap(reader).put("FLORIS_LRB_1_radiance", "measurement_data_lres");

        assertEquals("hre1", reader.getGridId("FLORIS_HR1B_1_radiance"));
        assertEquals("hre2", reader.getGridId("FLORIS_HR2B_1_radiance"));
        assertEquals("lres", reader.getGridId("FLORIS_LRB_1_radiance"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetGridId_fromAnnotationBandPrefix() {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertEquals("hre1", reader.getGridId("HRE1_latitude"));
        assertEquals("hre1", reader.getGridId("HRE1_longitude"));
        assertEquals("hre1", reader.getGridId("HRE1_SZA"));
        assertEquals("hre2", reader.getGridId("HRE2_latitude"));
        assertEquals("hre2", reader.getGridId("HRE2_OAA"));
        assertEquals("lres", reader.getGridId("LRES_latitude"));
        assertEquals("lres", reader.getGridId("LRES_common_quality_flags"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetGridId_fromMeasurementBandPrefix() {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertEquals("hre1", reader.getGridId("FLORIS_HR1B_1_radiance"));
        assertEquals("hre1", reader.getGridId("FLORIS_HR1B_42_radiance"));
        assertEquals("hre2", reader.getGridId("FLORIS_HR2B_1_radiance"));
        assertEquals("lres", reader.getGridId("FLORIS_LRB_1_radiance"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetGridId_unknownBand_returnsNull() {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertNull(reader.getGridId("unknown_band"));
        assertNull(reader.getGridId("some_other_variable"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetGridId_descriptorToFileMapTakesPrecedence() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        // Band name looks like HRE1 prefix but is mapped to lres file
        descriptorToFileMap(reader).put("HRE1_special", "annotation_data_lres");

        assertEquals("lres", reader.getGridId("HRE1_special"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testAssignPerBandGeoCoding_reusesSameGridGeoCodingForBands() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        setField(reader, "dddbProductType", "FLX_L1B_OBS");
        final Product product = new Product("p", "FLX_L1B_OBS", 10, 10);
        product.addBand("HRE1_latitude", ProductData.TYPE_FLOAT32);
        product.addBand("HRE1_longitude", ProductData.TYPE_FLOAT32);
        final Band firstBand = product.addBand("FLORIS_HR1B_1_radiance", ProductData.TYPE_FLOAT32);
        final Band secondBand = product.addBand("FLORIS_HR1B_2_radiance", ProductData.TYPE_FLOAT32);

        final GeoCoding geoCoding = mock(GeoCoding.class);
        geoCodingMap(reader).put("hre1", geoCoding);

        invokeAssignPerBandGeoCoding(reader, product);

        assertSame(geoCoding, firstBand.getGeoCoding());
        assertSame(geoCoding, secondBand.getGeoCoding());
    }

    @Test
    @STTM("SNAP-4126")
    public void testClose_clearsStateAndClosesNcFiles() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        final NetcdfFile ncFile = mock(NetcdfFile.class);
        ncFilesMap(reader).put("data.nc", ncFile);
        ncVariablesCache(reader).put("cached", mock(Variable.class));
        descriptorToFileMap(reader).put("band", "file");
        setField(reader, "nativeNetcdfEnabled", false);

        reader.close();

        verify(ncFile).close();
        assertTrue(ncFilesMap(reader).isEmpty());
        assertTrue(ncVariablesCache(reader).isEmpty());
        assertTrue(descriptorToFileMap(reader).isEmpty());
        assertTrue(geoCodingMap(reader).isEmpty());
        assertEquals(FlexProductReader.NATIVE_NETCDF_ENABLED_DEFAULT, getField(reader, "nativeNetcdfEnabled"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testInitializeOperationMode_falsePreferenceDoesNotCreateOperationModeObjects() throws Exception {
        final Preferences preferences = Config.instance("opttbx").load().preferences();
        final String oldValue = preferences.get(FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, null);
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        try {
            preferences.putBoolean(FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, false);

            invokeInitializeOperationMode(reader);

            assertFalse((Boolean) getField(reader, "cacheEnabled"));
            assertNull(getField(reader, "cacheDataProvider"));
            assertNull(getField(reader, "productCache"));
        } finally {
            restorePreference(preferences, oldValue);
            reader.close();
        }
    }

    @Test
    @STTM("SNAP-4126")
    public void testInitializeOperationMode_readsNativeNetcdfPreference() throws Exception {
        final Preferences preferences = Config.instance("opttbx").load().preferences();
        final String oldCacheValue = preferences.get(FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, null);
        final String oldNativeValue = preferences.get(FlexProductReader.PREFERENCE_KEY_ENABLE_NATIVE_NETCDF, null);
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        try {
            preferences.putBoolean(FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, false);
            preferences.putBoolean(FlexProductReader.PREFERENCE_KEY_ENABLE_NATIVE_NETCDF, false);

            invokeInitializeOperationMode(reader);

            assertFalse((Boolean) getField(reader, "cacheEnabled"));
            assertFalse((Boolean) getField(reader, "nativeNetcdfEnabled"));
            assertNull(getField(reader, "cacheDataProvider"));
            assertNull(getField(reader, "productCache"));
        } finally {
            restorePreference(preferences, FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, oldCacheValue);
            restorePreference(preferences, FlexProductReader.PREFERENCE_KEY_ENABLE_NATIVE_NETCDF, oldNativeValue);
            reader.close();
        }
    }

    @Test
    @STTM("SNAP-4126")
    public void testInitializeOperationMode_defaultsNativeNetcdfPreferenceToEnabled() throws Exception {
        final Preferences preferences = Config.instance("opttbx").load().preferences();
        final String oldCacheValue = preferences.get(FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, null);
        final String oldNativeValue = preferences.get(FlexProductReader.PREFERENCE_KEY_ENABLE_NATIVE_NETCDF, null);
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        try {
            preferences.putBoolean(FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, false);
            preferences.remove(FlexProductReader.PREFERENCE_KEY_ENABLE_NATIVE_NETCDF);

            invokeInitializeOperationMode(reader);

            assertFalse((Boolean) getField(reader, "cacheEnabled"));
            assertTrue((Boolean) getField(reader, "nativeNetcdfEnabled"));
            assertNull(getField(reader, "cacheDataProvider"));
            assertNull(getField(reader, "productCache"));
        } finally {
            restorePreference(preferences, FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, oldCacheValue);
            restorePreference(preferences, FlexProductReader.PREFERENCE_KEY_ENABLE_NATIVE_NETCDF, oldNativeValue);
            reader.close();
        }
    }

    @Test
    @STTM("SNAP-4126")
    public void testOpenFlexNetcdfFile_l1cWithoutCacheUsesNativeOpen() throws Exception {
        final OpenTrackingFlexProductReader reader = new OpenTrackingFlexProductReader();
        setField(reader, "dddbProductType", "FLX_L1C_FLXSYN");
        setField(reader, "cacheEnabled", false);
        setField(reader, "nativeNetcdfEnabled", true);

        final NetcdfFile opened = invokeOpenFlexNetcdfFile(reader);

        assertSame(reader.nativeFile, opened);
        assertEquals(1, reader.nativeOpenCalls);
        assertEquals(0, reader.defaultOpenCalls);
    }

    @Test
    @STTM("SNAP-4126")
    public void testOpenFlexNetcdfFile_l2WithoutCacheUsesNativeOpen() throws Exception {
        final OpenTrackingFlexProductReader reader = new OpenTrackingFlexProductReader();
        setField(reader, "dddbProductType", "FLX_L2_FLEX");
        setField(reader, "cacheEnabled", false);
        setField(reader, "nativeNetcdfEnabled", true);

        final NetcdfFile opened = invokeOpenFlexNetcdfFile(reader);

        assertSame(reader.nativeFile, opened);
        assertEquals(1, reader.nativeOpenCalls);
        assertEquals(0, reader.defaultOpenCalls);
    }

    @Test
    @STTM("SNAP-4126")
    public void testOpenFlexNetcdfFile_lowercaseL1cProductTypeUsesNativeOpen() throws Exception {
        final OpenTrackingFlexProductReader reader = new OpenTrackingFlexProductReader();
        setField(reader, "dddbProductType", "flx_l1c_flxsyn");
        setField(reader, "cacheEnabled", false);
        setField(reader, "nativeNetcdfEnabled", true);

        final NetcdfFile opened = invokeOpenFlexNetcdfFile(reader);

        assertSame(reader.nativeFile, opened);
        assertEquals(1, reader.nativeOpenCalls);
        assertEquals(0, reader.defaultOpenCalls);
    }

    @Test
    @STTM("SNAP-4126")
    public void testOpenFlexNetcdfFile_l1bNeverUsesNativeOpen() throws Exception {
        final OpenTrackingFlexProductReader reader = new OpenTrackingFlexProductReader();
        setField(reader, "dddbProductType", "FLX_L1B_FLXSYN");
        setField(reader, "cacheEnabled", false);
        setField(reader, "nativeNetcdfEnabled", true);

        final NetcdfFile opened = invokeOpenFlexNetcdfFile(reader);

        assertSame(reader.defaultFile, opened);
        assertEquals(0, reader.nativeOpenCalls);
        assertEquals(1, reader.defaultOpenCalls);
    }

    @Test
    @STTM("SNAP-4126")
    public void testOpenFlexNetcdfFile_cacheEnabledL1cUsesNativeOpen() throws Exception {
        final OpenTrackingFlexProductReader reader = new OpenTrackingFlexProductReader();
        setField(reader, "dddbProductType", "FLX_L1C_FLXSYN");
        setField(reader, "cacheEnabled", true);
        setField(reader, "nativeNetcdfEnabled", true);

        final NetcdfFile opened = invokeOpenFlexNetcdfFile(reader);

        assertSame(reader.nativeFile, opened);
        assertEquals(1, reader.nativeOpenCalls);
        assertEquals(0, reader.defaultOpenCalls);
    }

    @Test
    @STTM("SNAP-4126")
    public void testOpenFlexNetcdfFile_nativePreferenceDisabledUsesDefaultOpen() throws Exception {
        final OpenTrackingFlexProductReader reader = new OpenTrackingFlexProductReader();
        setField(reader, "dddbProductType", "FLX_L1C_FLXSYN");
        setField(reader, "cacheEnabled", false);
        setField(reader, "nativeNetcdfEnabled", false);

        final NetcdfFile opened = invokeOpenFlexNetcdfFile(reader);

        assertSame(reader.defaultFile, opened);
        assertEquals(0, reader.nativeOpenCalls);
        assertEquals(1, reader.defaultOpenCalls);
    }

    @Test
    @STTM("SNAP-4126")
    public void testOpenFlexNetcdfFile_nativeOpenFailureFallsBackToDefaultOpen() throws Exception {
        final OpenTrackingFlexProductReader reader = new OpenTrackingFlexProductReader();
        setField(reader, "dddbProductType", "FLX_L1C_FLXSYN");
        setField(reader, "cacheEnabled", false);
        setField(reader, "nativeNetcdfEnabled", true);
        reader.nativeOpenException = new IOException("native unavailable");

        final NetcdfFile opened = invokeOpenFlexNetcdfFile(reader);

        assertSame(reader.defaultFile, opened);
        assertEquals(1, reader.nativeOpenCalls);
        assertEquals(1, reader.defaultOpenCalls);
    }

    @Test
    @STTM("SNAP-4126")
    public void testReadGeoData_noCacheUsesDirectVariableRead() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("latitude");
        when(variable.getRank()).thenReturn(2);
        when(variable.getDimensions()).thenReturn(Arrays.asList(
                new Dimension("number_of_along_track_samples", 2),
                new Dimension("number_of_across_track_samples", 2)
        ));
        when(variable.read(any(Section.class))).thenReturn(
                Array.factory(DataType.DOUBLE, new int[]{2, 2}, new double[]{1.0, 2.0, 3.0, 4.0}));

        setField(reader, "cacheEnabled", false);
        bandToVariableMap(reader).put("latitude", variable);

        final Band band = new Band("latitude", ProductData.TYPE_FLOAT64, 2, 2);
        band.setScalingFactor(2.0);
        band.setScalingOffset(10.0);

        final double[] geoData = invokeReadGeoData(reader, "latitude", 2, 2, band);

        assertArrayEquals(new double[]{12.0, 14.0, 16.0, 18.0}, geoData, 1.0e-8);
        verify(variable).read(any(Section.class));
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddBand_usesDescriptorScaleOffsetAndFillValueWithoutReadingNcAttributes() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 4, 3);

        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setName("FLORIS_HR1B_1_radiance");
        descriptor.setNcVarName("FLORIS_HR1B_1_radiance");
        descriptor.setNcGroupPath("Measurement_data");
        descriptor.setDataType("uint16");
        descriptor.setScaleFactor(0.01338472);
        descriptor.setAddOffset(-0.02064825);
        descriptor.setFillValue(0.0);

        final Variable variable = mock(Variable.class);
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findVariable(descriptor.getFullNcPath())).thenReturn(variable);
        ncFilesMap(reader).put("sample_hre1.nc", ncFile);

        invokeAddBand(reader, product, descriptor);

        final Band band = product.getBand("FLORIS_HR1B_1_radiance");
        assertNotNull(band);
        assertEquals(0.01338472, band.getScalingFactor(), 1.0e-12);
        assertEquals(-0.02064825, band.getScalingOffset(), 1.0e-12);
        assertTrue(band.isNoDataValueUsed());
        assertEquals(0.0, band.getNoDataValue(), 1.0e-12);
        assertSame(variable, bandToVariableMap(reader).get("FLORIS_HR1B_1_radiance"));
        verify(variable, never()).findAttribute(anyString());
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddFlagMask_usesDirectMaskCreation() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = spy(new Product("p", "t", 10, 10));
        product.addBand("quality", ProductData.TYPE_UINT8);
        final FlexFlagMask flagMask = new FlexFlagMask("quality", "bad", 1, "Bad pixel", true);

        invokeAddFlagMask(reader, product, "quality", flagMask, 0);

        verify(product, never()).addMask(eq("quality_bad"), anyString(), anyString(), any(Color.class), anyDouble());
        verify(product).addMask(any(Mask.class));
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddFlagMask_preservesBitmaskExpression() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("quality", ProductData.TYPE_UINT8);
        final FlexFlagMask flagMask = new FlexFlagMask("quality", "bad", 4, "Bad pixel", true);

        invokeAddFlagMask(reader, product, "quality", flagMask, 0);

        final Mask mask = product.getMaskGroup().get("quality_bad");
        assertNotNull(mask);
        assertEquals("quality & 4 != 0", Mask.BandMathsType.getExpression(mask));
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddFlagMask_preservesIndexExpression() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("classification", ProductData.TYPE_UINT8);
        final FlexFlagMask flagMask = new FlexFlagMask("classification", "land", 2, "Land pixel", false);

        invokeAddFlagMask(reader, product, "classification", flagMask, 0);

        final Mask mask = product.getMaskGroup().get("classification_land");
        assertNotNull(mask);
        assertEquals("classification == 2", Mask.BandMathsType.getExpression(mask));
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddFlagMask_usesReferencedBandGeometry() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 20, 30);
        product.addBand(new Band("quality", ProductData.TYPE_UINT8, 4, 5));
        final FlexFlagMask flagMask = new FlexFlagMask("quality", "bad", 1, "Bad pixel", true);

        invokeAddFlagMask(reader, product, "quality", flagMask, 0);

        final Mask mask = product.getMaskGroup().get("quality_bad");
        assertNotNull(mask);
        assertEquals(4, mask.getRasterWidth());
        assertEquals(5, mask.getRasterHeight());
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddFlagMasks_expandsChannelQualityMasksForAllLayerBands() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("HRE1_channel_quality_flags_1", ProductData.TYPE_UINT8);
        product.addBand("HRE1_channel_quality_flags_2", ProductData.TYPE_UINT8);
        product.addBand("HRE1_channel_quality_flags_summary", ProductData.TYPE_UINT8);
        product.addBand("HRE1_channel_quality_flags", ProductData.TYPE_UINT8);
        product.addBand("HRE2_channel_quality_flags_1", ProductData.TYPE_UINT8);
        product.addBand("HRE2_channel_quality_flags_summary", ProductData.TYPE_UINT8);
        product.addBand("LRES_channel_quality_flags_1", ProductData.TYPE_UINT8);
        product.addBand("HRE1_common_quality_flags", ProductData.TYPE_UINT8);

        final FlexProductDescriptor descriptor = new FlexProductDescriptor();
        descriptor.setFlagMasks(new FlexFlagMask[]{
                new FlexFlagMask("HRE1_channel_quality_flags", "bad", 1, "Bad pixel", true),
                new FlexFlagMask("HRE1_channel_quality_flags", "dead", 2, "Dead pixel", true),
                new FlexFlagMask("HRE2_channel_quality_flags", "bad", 1, "Bad pixel", true),
                new FlexFlagMask("LRES_channel_quality_flags", "bad", 1, "Bad pixel", true)
        });

        invokeAddFlagMasks(reader, product, descriptor);

        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_1_bad"));
        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_2_bad"));
        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_1_dead"));
        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_2_dead"));
        assertNotNull(product.getMaskGroup().get("HRE2_channel_quality_flags_1_bad"));
        assertNotNull(product.getMaskGroup().get("LRES_channel_quality_flags_1_bad"));
        assertNull(product.getMaskGroup().get("HRE1_channel_quality_flags_summary_bad"));
        assertNull(product.getMaskGroup().get("HRE1_channel_quality_flags_bad"));
        assertNull(product.getMaskGroup().get("HRE2_channel_quality_flags_summary_bad"));
        assertEquals(6, product.getMaskGroup().getNodeCount());
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddSpecialBands_usesProductDescriptorFlagMasksForChannelQualityFlagCoding() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 4, 3);

        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setName("HRE2_channel_quality_flags");
        descriptor.setNcVarName("channel_quality_flags");
        descriptor.setNcGroupPath("Annotation_data/Quality_flags");
        descriptor.setNcDataFile("hre2");
        descriptor.setType('s');
        descriptor.setDataType("uint8");
        descriptor.setDepth(2);
        descriptor.setDepthPrefixToken("_");
        descriptor.setDescription("Channel quality");
        descriptor.setScaleFactor(2.0);
        descriptor.setAddOffset(3.0);
        descriptor.setFillValue(0.0);
        specialsMap(reader).put(descriptor.getName(), descriptor);

        final Variable variable = mock(Variable.class);
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findVariable(descriptor.getFullNcPath())).thenReturn(variable);
        ncFilesMap(reader).put("sample_hre2.nc", ncFile);

        final FlexProductDescriptor productDescriptor = new FlexProductDescriptor();
        productDescriptor.setFlagMasks(new FlexFlagMask[]{
                new FlexFlagMask("HRE2_channel_quality_flags", "bad", 1, "DDDB bad sample", true),
                new FlexFlagMask("HRE2_channel_quality_flags", "dead", 2, "DDDB dead sample", true),
                new FlexFlagMask("LRES_channel_quality_flags", "bad", 1, "Wrong base", true)
        });

        invokeAddSpecialBands(reader, product, productDescriptor);

        final Band layerBand = product.getBand("HRE2_channel_quality_flags_1");
        assertNotNull(layerBand);
        assertTrue(layerBand.getSampleCoding() instanceof FlagCoding);
        assertEquals(2.0, layerBand.getScalingFactor(), 1.0e-12);
        assertEquals(3.0, layerBand.getScalingOffset(), 1.0e-12);
        assertTrue(layerBand.isNoDataValueUsed());
        assertEquals(0.0, layerBand.getNoDataValue(), 1.0e-12);
        verify(variable, never()).findAttribute(anyString());

        final FlagCoding flagCoding = (FlagCoding) layerBand.getSampleCoding();
        assertEquals(2, flagCoding.getNumAttributes());
        assertEquals(1, flagCoding.getFlagMask("bad"));
        assertEquals("DDDB bad sample", flagCoding.getFlag("bad").getDescription());
        assertEquals(2, flagCoding.getFlagMask("dead"));
        assertEquals("DDDB dead sample", flagCoding.getFlag("dead").getDescription());
        assertNull(flagCoding.getFlag("saturated"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testAssignSpectralAxes_l1cUsesDescriptorSeriesAndMergesSlstrRadiance() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        setField(reader, "dddbProductType", "FLX_L1C_FLXSYN");
        final Product product = new Product("p", "FLX_L1C_FLXSYN", 4, 3);
        addSpectralBand(product, "floris_toa_radiance_ch_1", 500.0f);
        addSpectralBand(product, "floris_toa_radiance_ch_2", 501.0f);
        addSpectralBand(product, "floris_toa_radiance_noise_uncertainty_ch_1", 500.0f);
        addSpectralBand(product, "olci_toa_radiance_ch_1", 600.0f);
        addSpectralBand(product, "olci_toa_radiance_ch_2", 601.0f);
        addSpectralBand(product, "slstr_nadir_toa_radiance_ch_1", 800.0f);
        addSpectralBand(product, "slstr_nadir_toa_radiance_ch_2", 801.0f);
        addSpectralBand(product, "slstr_nadir_tir_toa_radiance_ch_1", 11000.0f);
        addSpectralBand(product, "slstr_nadir_brightness_temperature_ch_1", 11000.0f);
        addSpectralBand(product, "slstr_oblique_toa_radiance_ch_1", 800.0f);
        addSpectralBand(product, "temperature_profile_level_1", 0.0f);

        specialsMap(reader).put("floris_toa_radiance",
                specialDescriptor("floris_toa_radiance", 2, "_ch_", "floris_spectral_channel_central_wavelengths"));
        specialsMap(reader).put("floris_toa_radiance_noise_uncertainty",
                specialDescriptor("floris_toa_radiance_noise_uncertainty", 1, "_ch_", "floris_spectral_channel_central_wavelengths"));
        specialsMap(reader).put("olci_toa_radiance",
                specialDescriptor("olci_toa_radiance", 2, "_ch_", "olci_spectral_channel_central_wavelengths"));
        specialsMap(reader).put("slstr_nadir_toa_radiance",
                specialDescriptor("slstr_nadir_toa_radiance", 2, "_ch_", "slstr_vswir_spectral_channel_central_wavelengths"));
        specialsMap(reader).put("slstr_nadir_tir_toa_radiance",
                specialDescriptor("slstr_nadir_tir_toa_radiance", 1, "_ch_", "slstr_tir_spectral_channel_central_wavelengths"));
        specialsMap(reader).put("slstr_nadir_brightness_temperature",
                specialDescriptor("slstr_nadir_brightness_temperature", 1, "_ch_", "slstr_tir_spectral_channel_central_wavelengths"));
        specialsMap(reader).put("slstr_oblique_toa_radiance",
                specialDescriptor("slstr_oblique_toa_radiance", 1, "_ch_", "slstr_vswir_spectral_channel_central_wavelengths"));

        invokeAssignSpectralAxes(reader, product);

        final List<ProductSpectralAxis> axes = product.getSpectralAxes();
        assertEquals(6, axes.size());
        assertAxis(axes.get(0), "floris_toa_radiance", "FLORIS TOA Radiance",
                "floris_toa_radiance_ch_1", "floris_toa_radiance_ch_2");
        assertAxis(axes.get(1), "floris_toa_radiance_noise_uncertainty", "FLORIS TOA Radiance Noise Uncertainty",
                "floris_toa_radiance_noise_uncertainty_ch_1");
        assertAxis(axes.get(2), "olci_toa_radiance", "OLCI TOA Radiance",
                "olci_toa_radiance_ch_1", "olci_toa_radiance_ch_2");
        assertAxis(axes.get(3), "slstr_nadir_toa_radiance", "SLSTR Nadir TOA Radiance",
                "slstr_nadir_toa_radiance_ch_1", "slstr_nadir_toa_radiance_ch_2", "slstr_nadir_tir_toa_radiance_ch_1");
        assertAxis(axes.get(4), "slstr_nadir_brightness_temperature", "SLSTR Nadir Brightness Temperature",
                "slstr_nadir_brightness_temperature_ch_1");
        assertAxis(axes.get(5), "slstr_oblique_toa_radiance", "SLSTR Oblique TOA Radiance",
                "slstr_oblique_toa_radiance_ch_1");
    }

    @Test
    @STTM("SNAP-4126")
    public void testAssignSpectralAxes_l2UsesAllWavelengthReferenceSeries() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        setField(reader, "dddbProductType", "FLX_L2_FLXSYN");
        final Product product = new Product("p", "FLX_L2_FLXSYN", 4, 3);

        addL2Series(reader, product, "floris_apparent_reflectance", "floris_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "floris_apparent_reflectance_uncertainty", "floris_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "olci_apparent_reflectance", "olci_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "slstr_apparent_reflectance", "slstr_vswir_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "direct_irradiance", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "direct_irradiance_uncertainty", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "diffuse_irradiance", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "diffuse_irradiance_uncertainty", "floris_olci_slstr_spectral_channel_central_wavelengths");
        addL2Series(reader, product, "sif_emission_spectrum", "sif_spectral_grid");
        addL2Series(reader, product, "sif_emission_spectrum_uncertainty", "sif_spectral_grid");
        addL2Series(reader, product, "floris_real_reflectance", "floris_real_reflectance_spectral_grid");
        addL2Series(reader, product, "floris_real_reflectance_uncertainty", "floris_real_reflectance_spectral_grid");
        addL2Series(reader, product, "fluorescence_escape_probability", "sif_spectral_grid");
        addL2Series(reader, product, "fluorescence_escape_probability_uncertainty", "sif_spectral_grid");

        addSpectralBand(product, "sif_peak_values_peak_1", 760.0f);
        specialsMap(reader).put("sif_peak_values",
                specialDescriptor("sif_peak_values", 1, "_peak_", ""));

        invokeAssignSpectralAxes(reader, product);

        final List<ProductSpectralAxis> axes = product.getSpectralAxes();
        assertEquals(14, axes.size());
        assertAxis(axes.get(0), "floris_apparent_reflectance", "FLORIS Apparent Reflectance",
                "floris_apparent_reflectance_ch_1");
        assertAxis(axes.get(4), "direct_irradiance", "Direct Irradiance",
                "direct_irradiance_ch_1");
        assertAxis(axes.get(8), "sif_emission_spectrum", "SIF Emission Spectrum",
                "sif_emission_spectrum_ch_1");
        assertAxis(axes.get(10), "floris_real_reflectance", "FLORIS Real Reflectance",
                "floris_real_reflectance_ch_1");
        assertAxis(axes.get(12), "fluorescence_escape_probability", "Fluorescence Escape Probability",
                "fluorescence_escape_probability_ch_1");
        for (final ProductSpectralAxis axis : axes) {
            assertFalse(axis.getId().startsWith("sif_peak"));
        }
    }

    @Test
    @STTM("SNAP-4126")
    public void testAssignSpectralAxes_l1bCreatesSeparatedAxesAndSetsWavelengths() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        setField(reader, "dddbProductType", "FLX_L1B_OBS");
        final Product product = new Product("p", "FLX_L1B_OBS", 4, 3);

        addL1bRadianceBands(product, "HR1", 140, false);
        addL1bRadianceBands(product, "HR1", 140, true);
        addL1bRadianceBands(product, "HR2", 269, false);
        addL1bRadianceBands(product, "LR", 235, false);
        addL1bSpectralMetadata(product, "HRE1_spectral_channel_centre_wavelength", 140, 500.0f);
        addL1bSpectralMetadata(product, "HRE1_FWHM", 140, 0.1f);
        addL1bSpectralMetadata(product, "HRE2_spectral_channel_centre_wavelength", 269, 640.0f);
        addL1bSpectralMetadata(product, "HRE2_FWHM", 269, 0.2f);
        addL1bSpectralMetadata(product, "LRES_spectral_channel_centre_wavelength", 235, 700.0f);
        addL1bSpectralMetadata(product, "LRES_FWHM", 235, 0.3f);

        invokeAssignSpectralAxes(reader, product);

        final List<ProductSpectralAxis> axes = product.getSpectralAxes();
        assertEquals(4, axes.size());
        assertEquals("floris_hr1_radiance", axes.get(0).getId());
        assertEquals("FLORIS HR1 Radiance", axes.get(0).getName());
        assertEquals(140, axes.get(0).getBandNames().size());
        assertEquals("floris_hr1_radiance_uncertainty", axes.get(1).getId());
        assertEquals(140, axes.get(1).getBandNames().size());
        assertEquals("floris_hr2_radiance", axes.get(2).getId());
        assertEquals(269, axes.get(2).getBandNames().size());
        assertEquals("floris_lr_radiance", axes.get(3).getId());
        assertEquals(235, axes.get(3).getBandNames().size());

        assertEquals(500.0f, product.getBand("FLORIS_HR1B_1_radiance").getSpectralWavelength(), 1.0e-6f);
        assertEquals(639.0f, product.getBand("FLORIS_HR1B_140_radiance").getSpectralWavelength(), 1.0e-6f);
        assertEquals(0.1f, product.getBand("FLORIS_HR1B_1_radiance").getSpectralBandwidth(), 1.0e-6f);
        assertEquals(640.0f, product.getBand("FLORIS_HR2B_1_radiance").getSpectralWavelength(), 1.0e-6f);
        assertEquals(700.0f, product.getBand("FLORIS_LRB_1_radiance").getSpectralWavelength(), 1.0e-6f);
    }

    private Variable mockStringVariable(String fullName) {
        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn(fullName);
        when(variable.getAttributes()).thenReturn(Collections.emptyList());
        when(variable.getDataType()).thenReturn(DataType.STRING);
        return variable;
    }

    private FlexVariableDescriptor mockDescriptor(String name, String fullNcPath, String ncDataFile, String ncGroupPath, String ncVarName) {
        final FlexVariableDescriptor descriptor = mock(FlexVariableDescriptor.class);

        when(descriptor.getName()).thenReturn(name);
        when(descriptor.getFullNcPath()).thenReturn(fullNcPath);
        when(descriptor.getNcDataFile()).thenReturn(ncDataFile);
        when(descriptor.getNcGroupPath()).thenReturn(ncGroupPath);
        when(descriptor.getNcVarName()).thenReturn(ncVarName);

        return descriptor;
    }

    @SuppressWarnings("unchecked")
    private Map<String, FlexVariableDescriptor> metadataMap(FlexProductReader reader) throws Exception {
        return (Map<String, FlexVariableDescriptor>) getField(reader, "metadataMap");
    }

    @SuppressWarnings("unchecked")
    private Map<String, NetcdfFile> ncFilesMap(FlexProductReader reader) throws Exception {
        return (Map<String, NetcdfFile>) getField(reader, "ncFilesMap");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Variable> ncVariablesCache(FlexProductReader reader) throws Exception {
        return (Map<String, Variable>) getField(reader, "ncVariablesCache");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> descriptorToFileMap(FlexProductReader reader) throws Exception {
        return (Map<String, String>) getField(reader, "descriptorToFileMap");
    }

    @SuppressWarnings("unchecked")
    private Map<String, GeoCoding> geoCodingMap(FlexProductReader reader) throws Exception {
        return (Map<String, GeoCoding>) getField(reader, "geoCodingMap");
    }

    @SuppressWarnings("unchecked")
    private Map<String, FlexVariableDescriptor> specialsMap(FlexProductReader reader) throws Exception {
        return (Map<String, FlexVariableDescriptor>) getField(reader, "specialsMap");
    }

    private void invokeAddFlagMask(FlexProductReader reader, Product product, String bandName,
                                   FlexFlagMask mask, int colorIndex) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod(
                "addFlagMask", Product.class, String.class, FlexFlagMask.class, int.class);
        method.setAccessible(true);
        method.invoke(reader, product, bandName, mask, colorIndex);
    }

    private void invokeAddFlagMasks(FlexProductReader reader, Product product,
                                    FlexProductDescriptor productDescriptor) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod(
                "addFlagMasks", Product.class, FlexProductDescriptor.class);
        method.setAccessible(true);
        method.invoke(reader, product, productDescriptor);
    }

    private void invokeAddSpecialBands(FlexProductReader reader, Product product,
                                       FlexProductDescriptor productDescriptor) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod(
                "addSpecialBands", Product.class, FlexProductDescriptor.class);
        method.setAccessible(true);
        method.invoke(reader, product, productDescriptor);
    }

    private void invokeAssignSpectralAxes(FlexProductReader reader, Product product) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod("assignSpectralAxes", Product.class);
        method.setAccessible(true);
        method.invoke(reader, product);
    }

    private void invokeAssignPerBandGeoCoding(FlexProductReader reader, Product product) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod("assignPerBandGeoCoding", Product.class);
        method.setAccessible(true);
        method.invoke(reader, product);
    }

    private void invokeAddBand(FlexProductReader reader, Product product, FlexVariableDescriptor descriptor) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod(
                "addBand", Product.class, FlexVariableDescriptor.class);
        method.setAccessible(true);
        method.invoke(reader, product, descriptor);
    }

    private void invokeInitializeOperationMode(FlexProductReader reader) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod("initializeOperationMode");
        method.setAccessible(true);
        method.invoke(reader);
    }

    private NetcdfFile invokeOpenFlexNetcdfFile(FlexProductReader reader) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod("openFlexNetcdfFile", File.class, String.class);
        method.setAccessible(true);
        return (NetcdfFile) method.invoke(reader, new File("dummy.nc"), "dummy.nc");
    }

    private double[] invokeReadGeoData(FlexProductReader reader, String bandName, int width, int height,
                                       Band band) throws Exception {
        final Method method = FlexProductReader.class.getDeclaredMethod(
                "readGeoData", String.class, int.class, int.class, Band.class);
        method.setAccessible(true);
        return (double[]) method.invoke(reader, bandName, width, height, band);
    }

    private void restorePreference(Preferences preferences, String oldValue) {
        restorePreference(preferences, FlexProductReader.PREFERENCE_KEY_ENABLE_CACHE, oldValue);
    }

    private void restorePreference(Preferences preferences, String key, String oldValue) {
        if (oldValue == null) {
            preferences.remove(key);
        } else {
            preferences.put(key, oldValue);
        }
    }

    private Object getField(FlexProductReader reader, String name) throws Exception {
        final Field field = FlexProductReader.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(reader);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Variable> bandToVariableMap(FlexProductReader reader) throws Exception {
        return (Map<String, Variable>) getField(reader, "bandToVariableMap");
    }

    private void setField(FlexProductReader reader, String name, Object value) throws Exception {
        final Field field = FlexProductReader.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(reader, value);
    }

    private static void addSpectralBand(Product product, String name, float wavelength) {
        final Band band = new Band(name, ProductData.TYPE_FLOAT32, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        band.setSpectralWavelength(wavelength);
        product.addBand(band);
    }

    private void addL2Series(FlexProductReader reader, Product product, String descriptorName,
                             String wavelengthReference) throws Exception {
        addSpectralBand(product, descriptorName + "_ch_1", 500.0f);
        specialsMap(reader).put(descriptorName, specialDescriptor(descriptorName, 1, "_ch_", wavelengthReference));
    }

    private static FlexVariableDescriptor specialDescriptor(String name, int depth, String depthPrefixToken,
                                                            String wavelengthReference) {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setName(name);
        descriptor.setType('s');
        descriptor.setDepth(depth);
        descriptor.setDepthPrefixToken(depthPrefixToken);
        descriptor.setWavelengthReference(wavelengthReference);
        descriptor.setDataType("float32");
        return descriptor;
    }

    private static void addL1bRadianceBands(Product product, String family, int channelCount, boolean uncertainty) {
        final String detector = uncertainty ? "U" : "B";
        final String suffix = uncertainty ? "_unc" : "";
        for (int channel = 1; channel <= channelCount; channel++) {
            product.addBand("FLORIS_" + family + detector + "_" + channel + "_radiance" + suffix, ProductData.TYPE_FLOAT32);
        }
    }

    private static void addL1bSpectralMetadata(Product product, String name, int channelCount, float firstValue) {
        MetadataElement baseElement = product.getMetadataRoot().getElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        if (baseElement == null) {
            baseElement = new MetadataElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
            product.getMetadataRoot().addElement(baseElement);
        }
        final float[] values = new float[channelCount];
        for (int i = 0; i < channelCount; i++) {
            values[i] = firstValue + i;
        }
        final MetadataElement element = new MetadataElement(name);
        element.addAttribute(new MetadataAttribute("value", ProductData.createInstance(values), true));
        baseElement.addElement(element);
    }

    private static void assertAxis(ProductSpectralAxis axis, String id, String name, String... bandNames) {
        assertEquals(id, axis.getId());
        assertEquals(name, axis.getName());
        assertEquals(Arrays.asList(bandNames), axis.getBandNames());
    }

    private static class OpenTrackingFlexProductReader extends FlexProductReader {
        private final NetcdfFile nativeFile = mock(NetcdfFile.class);
        private final NetcdfFile defaultFile = mock(NetcdfFile.class);
        private int nativeOpenCalls;
        private int defaultOpenCalls;
        private IOException nativeOpenException;

        OpenTrackingFlexProductReader() {
            super(mock(ProductReaderPlugIn.class));
        }

        @Override
        NetcdfFile openNativeNetcdfFile(File file) throws IOException {
            nativeOpenCalls++;
            if (nativeOpenException != null) {
                throw nativeOpenException;
            }
            return nativeFile;
        }

        @Override
        NetcdfFile openDefaultNetcdfFile(File file) {
            defaultOpenCalls++;
            return defaultFile;
        }
    }
}
