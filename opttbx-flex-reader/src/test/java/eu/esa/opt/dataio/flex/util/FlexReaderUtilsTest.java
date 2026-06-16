package eu.esa.opt.dataio.flex.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.flex.FlexProductReader;
import eu.esa.opt.dataio.flex.compatibility.EarlyProcessorCompatibility;
import eu.esa.opt.dataio.flex.compatibility.StandardFlexCompatibility;
import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import eu.esa.opt.dataio.flex.header.FlexProductHeader;
import org.esa.snap.core.datamodel.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FlexReaderUtilsTest {


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


    @Test
    @STTM("SNAP-4126")
    public void testMapProductType() {
        assertEquals("FLX_L1B_OBS", FlexReaderUtils.mapProductType("L1B_OBS___"));
        assertEquals("FLX_L1C_FLXSYN", FlexReaderUtils.mapProductType("L1C_FLXSYN"));
        assertEquals("FLX_L2_FLXSYN", FlexReaderUtils.mapProductType("L2__FLXSYN"));
    }

    @Test(expected = IllegalArgumentException.class)
    @STTM("SNAP-4126")
    public void testMapProductType_unknownThrows() {
        FlexReaderUtils.mapProductType("UNKNOWN");
    }

    @Test
    @STTM("SNAP-4126")
    public void testFindHeaderFile_inputIsXmlFile() throws Exception {
        final File xml = tempFolder.newFile("header.xml");

        assertEquals(xml.toPath(), FlexReaderUtils.findHeaderFile(xml.toPath()));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFindHeaderFile_inputIsUppercaseXmlFile() throws Exception {
        final File xml = tempFolder.newFile("HEADER.XML");

        assertEquals(xml.toPath(), FlexReaderUtils.findHeaderFile(xml.toPath()));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFindHeaderFile_inputIsNonXmlFileUsesParent() throws Exception {
        final File dir = tempFolder.newFolder("product");
        final File nc = new File(dir, "data.nc");
        final File xml = new File(dir, "header.xml");
        assertTrue(nc.createNewFile());
        assertTrue(xml.createNewFile());

        assertEquals(xml.toPath(), FlexReaderUtils.findHeaderFile(nc.toPath()));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFindHeaderFile_inputIsDirectory() throws Exception {
        final File dir = tempFolder.newFolder("product");
        final File xml = new File(dir, "header.xml");
        assertTrue(xml.createNewFile());

        assertEquals(xml.toPath(), FlexReaderUtils.findHeaderFile(dir.toPath()));
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testFindHeaderFile_noXmlThrows() throws Exception {
        final File dir = tempFolder.newFolder("product");
        assertTrue(new File(dir, "data.nc").createNewFile());

        FlexReaderUtils.findHeaderFile(dir.toPath());
    }

    @Test
    @STTM("SNAP-4126")
    public void testDetectCompatibility_standard() {
        final FlexProductHeader header = new FlexProductHeader();
        header.setDataFileNames(Arrays.asList("data_1.nc", "data_2.nc"));

        assertTrue(FlexReaderUtils.detectCompatibility(header) instanceof StandardFlexCompatibility);
    }

    @Test
    @STTM("SNAP-4126")
    public void testDetectCompatibility_earlyProcessor() {
        final FlexProductHeader header = new FlexProductHeader();
        header.setDataFileNames(Arrays.asList("data_1.nc", "data_2.nc.nc"));

        assertTrue(FlexReaderUtils.detectCompatibility(header) instanceof EarlyProcessorCompatibility);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetScaleAndOffset_bothAttributesPresent() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("scale_factor")).thenReturn(new Attribute("scale_factor", 0.01));
        when(variable.findAttribute("add_offset")).thenReturn(new Attribute("add_offset", 2.5));

        FlexReaderUtils.setScaleAndOffset(band, variable);

        assertEquals(0.01, band.getScalingFactor(), 1.0e-12);
        assertEquals(2.5, band.getScalingOffset(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetScaleAndOffset_noAttributesKeepsDefaults() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("scale_factor")).thenReturn(null);
        when(variable.findAttribute("add_offset")).thenReturn(null);

        FlexReaderUtils.setScaleAndOffset(band, variable);

        assertEquals(1.0, band.getScalingFactor(), 1.0e-12);
        assertEquals(0.0, band.getScalingOffset(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetScaleAndOffset_stringTypedAttributes() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("scale_factor")).thenReturn(new Attribute("scale_factor", "0.01"));
        when(variable.findAttribute("add_offset")).thenReturn(new Attribute("add_offset", "2.5"));

        FlexReaderUtils.setScaleAndOffset(band, variable);

        assertEquals(0.01, band.getScalingFactor(), 1.0e-12);
        assertEquals(2.5, band.getScalingOffset(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetScaleAndOffset_unparseableStringKeepsDefaults() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("scale_factor")).thenReturn(new Attribute("scale_factor", "not_a_number"));
        when(variable.findAttribute("add_offset")).thenReturn(new Attribute("add_offset", "abc"));

        FlexReaderUtils.setScaleAndOffset(band, variable);

        assertEquals(1.0, band.getScalingFactor(), 1.0e-12);
        assertEquals(0.0, band.getScalingOffset(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetFillValue_present() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("_FillValue")).thenReturn(new Attribute("_FillValue", -999.0));

        FlexReaderUtils.setFillValue(band, variable);

        assertTrue(band.isNoDataValueUsed());
        assertEquals(-999.0, band.getNoDataValue(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetFillValue_missing() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("_FillValue")).thenReturn(null);

        FlexReaderUtils.setFillValue(band, variable);

        assertFalse(band.isNoDataValueUsed());
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetFillValue_stringTypedAttribute() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("_FillValue")).thenReturn(new Attribute("_FillValue", "-999.0"));

        FlexReaderUtils.setFillValue(band, variable);

        assertTrue(band.isNoDataValueUsed());
        assertEquals(-999.0, band.getNoDataValue(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetFillValue_unparseableStringKeepsDefault() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final Variable variable = mock(Variable.class);

        when(variable.findAttribute("_FillValue")).thenReturn(new Attribute("_FillValue", "invalid"));

        FlexReaderUtils.setFillValue(band, variable);

        assertFalse(band.isNoDataValueUsed());
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetScaleOffsetAndFillValue_fromDescriptor() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setScaleFactor(0.01);
        descriptor.setAddOffset(-2.5);
        descriptor.setFillValue(0.0);

        FlexReaderUtils.setScaleOffsetAndFillValue(band, descriptor);

        assertEquals(0.01, band.getScalingFactor(), 1.0e-12);
        assertEquals(-2.5, band.getScalingOffset(), 1.0e-12);
        assertTrue(band.isNoDataValueUsed());
        assertEquals(0.0, band.getNoDataValue(), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetScaleOffsetAndFillValue_descriptorDefaultsKeepBandDefaults() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();

        FlexReaderUtils.setScaleOffsetAndFillValue(band, descriptor);

        assertEquals(1.0, band.getScalingFactor(), 1.0e-12);
        assertEquals(0.0, band.getScalingOffset(), 1.0e-12);
        assertFalse(band.isNoDataValueUsed());
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetSpectralWavelength_valid() {
        final Product product = createProductWithSpectralMetadata();
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);

        FlexReaderUtils.setSpectralWavelength(band, product, "wavelength", 2);

        assertEquals(680.0f, band.getSpectralWavelength(), 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetSpectralFwhm_valid() {
        final Product product = createProductWithSpectralMetadata();
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);

        FlexReaderUtils.setSpectralFwhm(band, product, "wavelength", 3);

        assertEquals(740.0f, band.getSpectralBandwidth(), 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetSpectralValue_invalidInputsDoNotChangeBand() {
        final Product product = createProductWithSpectralMetadata();
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);

        FlexReaderUtils.setSpectralWavelength(band, product, null, 1);
        FlexReaderUtils.setSpectralWavelength(band, product, "", 1);
        FlexReaderUtils.setSpectralWavelength(band, product, "wavelength", 0);
        FlexReaderUtils.setSpectralWavelength(band, product, "missing", 1);
        FlexReaderUtils.setSpectralWavelength(band, product, "wavelength", 99);

        assertEquals(0.0f, band.getSpectralWavelength(), 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetSpectralValue_missingNetcdfMetadataDoesNotChangeBand() {
        final Product product = new Product("p", "t", 10, 10);
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);

        FlexReaderUtils.setSpectralWavelength(band, product, "wavelength", 1);

        assertEquals(0.0f, band.getSpectralWavelength(), 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetSpectralValue_missingValueAttributeDoesNotChangeBand() {
        final Product product = new Product("p", "t", 10, 10);
        final MetadataElement netcdf = new MetadataElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        netcdf.addElement(new MetadataElement("wavelength"));
        product.getMetadataRoot().addElement(netcdf);

        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 10, 10);

        FlexReaderUtils.setSpectralWavelength(band, product, "wavelength", 1);

        assertEquals(0.0f, band.getSpectralWavelength(), 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testExtractMetadata_floatData() throws Exception {
        final MetadataElement element = FlexReaderUtils.extractMetadata(
                variableWithData("float_var", DataType.FLOAT, new int[]{2}, new float[]{1.0f, 2.0f})
        );

        final MetadataAttribute value = element.getAttribute("value");
        assertNotNull(value);
        assertEquals("unit", value.getUnit());
        assertEquals("description", value.getDescription());
        assertEquals(2, value.getData().getNumElems());
        assertEquals(1.0, value.getData().getElemDoubleAt(0), 1.0e-12);
    }

    @Test
    @STTM("SNAP-4126")
    public void testExtractMetadata_allNumericValueTypes() throws Exception {
        assertNotNull(FlexReaderUtils.extractMetadata(variableWithData("double_var", DataType.DOUBLE, new int[]{1}, new double[]{1.0})).getAttribute("value"));
        assertNotNull(FlexReaderUtils.extractMetadata(variableWithData("byte_var", DataType.BYTE, new int[]{1}, new byte[]{1})).getAttribute("value"));
        assertNotNull(FlexReaderUtils.extractMetadata(variableWithData("short_var", DataType.SHORT, new int[]{1}, new short[]{1})).getAttribute("value"));
        assertNotNull(FlexReaderUtils.extractMetadata(variableWithData("int_var", DataType.INT, new int[]{1}, new int[]{1})).getAttribute("value"));
        assertNotNull(FlexReaderUtils.extractMetadata(variableWithData("long_var", DataType.LONG, new int[]{1}, new long[]{1L})).getAttribute("value"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testExtractMetadata_stringVariableDoesNotReadValue() throws Exception {
        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("string_var");
        when(variable.getAttributes()).thenReturn(Collections.emptyList());
        when(variable.getDataType()).thenReturn(DataType.STRING);

        final MetadataElement element = FlexReaderUtils.extractMetadata(variable);

        assertNull(element.getAttribute("value"));
        verify(variable, never()).read();
    }

    @Test
    @STTM("SNAP-4126")
    public void testExtractMetadata_attributes() throws Exception {
        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("var");
        when(variable.getAttributes()).thenReturn(Arrays.asList(
                attribute("string_attr", "hello"),
                attribute("byte_attr", new byte[]{1}),
                attribute("short_attr", new short[]{2}),
                attribute("int_attr", new int[]{3}),
                attribute("long_attr", new long[]{4L}),
                attribute("float_attr", new float[]{5.0f}),
                attribute("double_attr", new double[]{6.0})
        ));
        when(variable.getDataType()).thenReturn(DataType.STRING);

        final MetadataElement element = FlexReaderUtils.extractMetadata(variable);

        assertNotNull(element.getAttribute("string_attr"));
        assertNotNull(element.getAttribute("byte_attr"));
        assertNotNull(element.getAttribute("short_attr"));
        assertNotNull(element.getAttribute("int_attr"));
        assertNotNull(element.getAttribute("long_attr"));
        assertNotNull(element.getAttribute("float_attr"));
        assertNotNull(element.getAttribute("double_attr"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testExtractMetadata_ignoresAttributeWithNullValues() throws Exception {
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getValues()).thenReturn(null);

        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("var");
        when(variable.getAttributes()).thenReturn(Collections.singletonList(attribute));
        when(variable.getDataType()).thenReturn(DataType.STRING);

        final MetadataElement element = FlexReaderUtils.extractMetadata(variable);

        assertEquals(0, element.getNumAttributes());
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testExtractMetadata_readThrowsIOException() throws Exception {
        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("var");
        when(variable.getAttributes()).thenReturn(Collections.emptyList());
        when(variable.getDataType()).thenReturn(DataType.FLOAT);
        when(variable.read()).thenThrow(new IOException("read failed"));

        FlexReaderUtils.extractMetadata(variable);
    }

    @Test
    @STTM("SNAP-4126")
    public void testAddStringAttribute() {
        final MetadataElement element = new MetadataElement("metadata");

        FlexReaderUtils.addStringAttribute(element, "name", "value");

        assertEquals("value", element.getAttribute("name").getData().getElemString());
    }

    private Product createProductWithSpectralMetadata() {
        final Product product = new Product("p", "t", 10, 10);

        final MetadataElement netcdf = new MetadataElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        final MetadataElement wavelength = new MetadataElement("wavelength");
        wavelength.addAttribute(new MetadataAttribute(
                "value",
                ProductData.createInstance(new float[]{550.0f, 680.0f, 740.0f}),
                true
        ));

        netcdf.addElement(wavelength);
        product.getMetadataRoot().addElement(netcdf);

        return product;
    }

    private Variable variableWithData(String name, DataType dataType, int[] shape, Object javaArray) throws IOException {
        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn(name);
        when(variable.getAttributes()).thenReturn(Collections.emptyList());
        when(variable.getDataType()).thenReturn(dataType);
        when(variable.getUnitsString()).thenReturn("unit");
        when(variable.getDescription()).thenReturn("description");
        when(variable.read()).thenReturn(Array.factory(dataType, shape, javaArray));
        return variable;
    }

    private Attribute attribute(String name, String value) {
        return new Attribute(name, value);
    }

    private Attribute attribute(String name, byte[] values) {
        return new Attribute(name, Array.factory(DataType.BYTE, new int[]{values.length}, values));
    }

    private Attribute attribute(String name, short[] values) {
        return new Attribute(name, Array.factory(DataType.SHORT, new int[]{values.length}, values));
    }

    private Attribute attribute(String name, int[] values) {
        return new Attribute(name, Array.factory(DataType.INT, new int[]{values.length}, values));
    }

    private Attribute attribute(String name, long[] values) {
        return new Attribute(name, Array.factory(DataType.LONG, new int[]{values.length}, values));
    }

    private Attribute attribute(String name, float[] values) {
        return new Attribute(name, Array.factory(DataType.FLOAT, new int[]{values.length}, values));
    }

    private Attribute attribute(String name, double[] values) {
        return new Attribute(name, Array.factory(DataType.DOUBLE, new int[]{values.length}, values));
    }
}
