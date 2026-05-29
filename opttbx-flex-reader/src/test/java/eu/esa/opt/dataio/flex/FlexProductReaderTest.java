package eu.esa.opt.dataio.flex;

import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import eu.esa.opt.dataio.flex.dddb.FlexFlagMask;
import eu.esa.opt.dataio.flex.dddb.FlexProductDescriptor;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FlexProductReaderTest {


    @Test
    public void testIsSubsetReadingFullySupported() {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertTrue(reader.isSubsetReadingFullySupported());
    }

    @Test
    public void testReadGeoCoding_withoutLatitudeLongitudeBands_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);

        final GeoCoding geoCoding = reader.readGeoCoding(product);

        assertNull(geoCoding);
    }

    @Test
    public void testReadGeoCoding_withOnlyLongitudeBand_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("longitude", ProductData.TYPE_FLOAT32);

        assertNull(reader.readGeoCoding(product));
    }

    @Test
    public void testReadGeoCoding_withOnlyPrefixedLongitudeBand_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("HRE1_longitude", ProductData.TYPE_FLOAT32);

        assertNull(reader.readGeoCoding(product));
    }

    @Test
    public void testReadElement_descriptorMissing_returnsNull() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertNull(reader.readElement("missing"));
    }

    @Test
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
    public void testGetGridId_fromMeasurementBandPrefix() {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertEquals("hre1", reader.getGridId("FLORIS_HR1B_1_radiance"));
        assertEquals("hre1", reader.getGridId("FLORIS_HR1B_42_radiance"));
        assertEquals("hre2", reader.getGridId("FLORIS_HR2B_1_radiance"));
        assertEquals("lres", reader.getGridId("FLORIS_LRB_1_radiance"));
    }

    @Test
    public void testGetGridId_unknownBand_returnsNull() {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        assertNull(reader.getGridId("unknown_band"));
        assertNull(reader.getGridId("some_other_variable"));
    }

    @Test
    public void testGetGridId_descriptorToFileMapTakesPrecedence() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        // Band name looks like HRE1 prefix but is mapped to lres file
        descriptorToFileMap(reader).put("HRE1_special", "annotation_data_lres");

        assertEquals("lres", reader.getGridId("HRE1_special"));
    }

    @Test
    public void testClose_clearsStateAndClosesNcFiles() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));

        final NetcdfFile ncFile = mock(NetcdfFile.class);
        ncFilesMap(reader).put("data.nc", ncFile);
        ncVariablesCache(reader).put("cached", mock(Variable.class));
        descriptorToFileMap(reader).put("band", "file");

        reader.close();

        verify(ncFile).close();
        assertTrue(ncFilesMap(reader).isEmpty());
        assertTrue(ncVariablesCache(reader).isEmpty());
        assertTrue(descriptorToFileMap(reader).isEmpty());
        assertTrue(geoCodingMap(reader).isEmpty());
    }

    @Test
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
    public void testAddFlagMasks_expandsChannelQualityMasksForAllLayerBands() throws Exception {
        final FlexProductReader reader = new FlexProductReader(mock(ProductReaderPlugIn.class));
        final Product product = new Product("p", "t", 10, 10);
        product.addBand("HRE1_channel_quality_flags_1", ProductData.TYPE_UINT8);
        product.addBand("HRE1_channel_quality_flags_2", ProductData.TYPE_UINT8);
        product.addBand("HRE1_common_quality_flags", ProductData.TYPE_UINT8);

        final FlexProductDescriptor descriptor = new FlexProductDescriptor();
        descriptor.setFlagMasks(new FlexFlagMask[]{
                new FlexFlagMask("HRE1_channel_quality_flags", "bad", 1, "Bad pixel", true),
                new FlexFlagMask("HRE1_channel_quality_flags", "dead", 2, "Dead pixel", true)
        });

        invokeAddFlagMasks(reader, product, descriptor);

        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_1_bad"));
        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_2_bad"));
        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_1_dead"));
        assertNotNull(product.getMaskGroup().get("HRE1_channel_quality_flags_2_dead"));
        assertEquals(4, product.getMaskGroup().getNodeCount());
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

    private Object getField(FlexProductReader reader, String name) throws Exception {
        final Field field = FlexProductReader.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(reader);
    }
}
