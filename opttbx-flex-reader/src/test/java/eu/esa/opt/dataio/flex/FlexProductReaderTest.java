package eu.esa.opt.dataio.flex;

import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.lang.reflect.Field;
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

    private Object getField(FlexProductReader reader, String name) throws Exception {
        final Field field = FlexProductReader.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(reader);
    }
}