package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;

import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import org.mockito.Mockito;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.VariableDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class MetadataReaderTest {

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromString() {
        // given
        String[] data = {"Test1", "Test2", "Test3"};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.addMetaAttributesFromString(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(3));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElemString(), is("Test1"));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElemString(), is("Test2"));
        assertThat(arrayAttElem.getAttributeAt(2).getData().getElemString(), is("Test3"));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromFloat() throws IllegalFileFormatException {
        // given
        float[] data = {1.1f, 2.2f, 3.3f};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.addMetaAttributesFromFloat(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromFloat_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        float[][] data = {{1.1f, 2.2f}, {3.3f, 4.4f}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.addMetaAttributesFromFloat(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromFloat_illegalNumberOfDimensions() {
        // given
        float[][][] data = {{{1.1f, 2.2f}, {3.3f, 4.4f}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        try {
            // when
            MetadataReader.addMetaAttributesFromFloat(arrayAttElem, "Test", data);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 3"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromDouble() throws IllegalFileFormatException {
        // given
        double[] data = {1.1, 2.2, 3.3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.addMetaAttributesFromDouble(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromDouble_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        double[][] data = {{1.1, 2.2}, {3.3, 4.4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.addMetaAttributesFromDouble(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromDouble_illegalNumberOfDimensions() {
        // given
        double[][][] data = {{{1.1, 2.2}, {3.3, 4.4}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        try {
            // when
            MetadataReader.addMetaAttributesFromDouble(arrayAttElem, "Test", data);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 3"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromByte() throws IllegalFileFormatException {
        // given
        byte[] data = {1, 2, 3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromByte(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        final MetadataAttribute attribute = arrayAttElem.getAttributeAt(0);
        assertNotNull(attribute);
        assertThat(attribute.getDataType(), is(ProductData.TYPE_INT8));
        assertThat(attribute.getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromByte_Unsigned() throws IllegalFileFormatException {
        // given
        byte[] data = {-1, -2, -3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = true;

        // when
        MetadataReader.addMetaAttributesFromByte(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        final MetadataAttribute attribute = arrayAttElem.getAttributeAt(0);
        assertNotNull(attribute);
        assertThat(attribute.getDataType(), is(ProductData.TYPE_UINT8));
        assertThat(attribute.getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromByte_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        byte[][] data = {{1, 2}, {3, 4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromByte(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));

        final MetadataAttribute attributeAt0 = arrayAttElem.getAttributeAt(0);
        assertNotNull(attributeAt0);
        assertThat(attributeAt0.getDataType(), is(ProductData.TYPE_INT8));
        assertThat(attributeAt0.getData().getElems(), is(data[0]));

        final MetadataAttribute attributeAt1 = arrayAttElem.getAttributeAt(1);
        assertNotNull(attributeAt1);
        assertThat(attributeAt1.getDataType(), is(ProductData.TYPE_INT8));
        assertThat(attributeAt1.getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromByte_Unsigned_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        byte[][] data = {{1, 2}, {3, 4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = true;

        // when
        MetadataReader.addMetaAttributesFromByte(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));

        final MetadataAttribute attributeAt0 = arrayAttElem.getAttributeAt(0);
        assertNotNull(attributeAt0);
        assertThat(attributeAt0.getDataType(), is(ProductData.TYPE_UINT8));
        assertThat(attributeAt0.getData().getElems(), is(data[0]));

        final MetadataAttribute attributeAt1 = arrayAttElem.getAttributeAt(1);
        assertNotNull(attributeAt1);
        assertThat(attributeAt1.getDataType(), is(ProductData.TYPE_UINT8));
        assertThat(attributeAt1.getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromByte_illegalNumberOfDimensions() {
        // given
        byte[][][][] data = {{{{1, 2}, {3, 4}}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        try {
            // when
            MetadataReader.addMetaAttributesFromByte(arrayAttElem, "Test", data, unsigned);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 4"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromShort() throws IllegalFileFormatException {
        // given
        short[] data = {1, 2, 3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromShort(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        final MetadataAttribute attribute = arrayAttElem.getAttributeAt(0);
        assertNotNull(attribute);
        assertThat(attribute.getDataType(), is(ProductData.TYPE_INT16));
        assertThat(attribute.getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromShort_Unsigned() throws IllegalFileFormatException {
        // given
        short[] data = {-1, -2, -3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = true;

        // when
        MetadataReader.addMetaAttributesFromShort(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        final MetadataAttribute attribute = arrayAttElem.getAttributeAt(0);
        assertNotNull(attribute);
        assertThat(attribute.getDataType(), is(ProductData.TYPE_UINT16));
        assertThat(attribute.getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromShort_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        short[][] data = {{1, 2}, {3, 4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromShort(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));

        final MetadataAttribute attributeAt0 = arrayAttElem.getAttributeAt(0);
        assertNotNull(attributeAt0);
        assertThat(attributeAt0.getDataType(), is(ProductData.TYPE_INT16));
        assertThat(attributeAt0.getData().getElems(), is(data[0]));

        final MetadataAttribute attributeAt1 = arrayAttElem.getAttributeAt(1);
        assertNotNull(attributeAt1);
        assertThat(attributeAt1.getDataType(), is(ProductData.TYPE_INT16));
        assertThat(attributeAt1.getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromShort_Unsigned_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        short[][] data = {{1, 2}, {3, 4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = true;

        // when
        MetadataReader.addMetaAttributesFromShort(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));

        final MetadataAttribute attributeAt0 = arrayAttElem.getAttributeAt(0);
        assertNotNull(attributeAt0);
        assertThat(attributeAt0.getDataType(), is(ProductData.TYPE_UINT16));
        assertThat(attributeAt0.getData().getElems(), is(data[0]));

        final MetadataAttribute attributeAt1 = arrayAttElem.getAttributeAt(1);
        assertNotNull(attributeAt1);
        assertThat(attributeAt1.getDataType(), is(ProductData.TYPE_UINT16));
        assertThat(attributeAt1.getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromShort_illegalNumberOfDimensions() {
        // given
        short[][][][] data = {{{{1, 2}, {3, 4}}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        try {
            // when
            MetadataReader.addMetaAttributesFromShort(arrayAttElem, "Test", data, unsigned);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 4"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromInt() throws IllegalFileFormatException {
        // given
        int[] data = {1, 2, 3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromInt(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromInt_Unsigned() throws IllegalFileFormatException {
        // given
        int[] data = {-1, -2, -3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = true;

        // when
        MetadataReader.addMetaAttributesFromInt(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        final MetadataAttribute attribute = arrayAttElem.getAttributeAt(0);
        assertNotNull(attribute);
        assertThat(attribute.getDataType(), is(ProductData.TYPE_UINT32));
        assertThat(attribute.getData().getElems(), is(data));
        assertThat(attribute.getData().getElemUIntAt(0), is(data[0] & 0xffffffffL));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromInt_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        int[][] data = {{1, 2}, {3, 4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromInt(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromInt_Unsigned_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        int[][] data = {{1, 2}, {3, 4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = true;

        // when
        MetadataReader.addMetaAttributesFromInt(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromInt_illegalNumberOfDimensions() {
        // given
        int[][][][][] data = {{{{{1, 2}, {3, 4}}}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        try {
            // when
            MetadataReader.addMetaAttributesFromInt(arrayAttElem, "Test", data, unsigned);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 5"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromLong() throws IllegalFileFormatException {
        // given
        long[] data = {1L, 2L, 3L};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromLong(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromLong_Unsigned_NotSupported() {
        // given
        long[] data = {1L, 2L, 3L};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = true;

        // when
        try {
            MetadataReader.addMetaAttributesFromLong(arrayAttElem, "Test", data, unsigned);
        } catch (IllegalFileFormatException e) {
            assertThat(e.getMessage(), is("Unsigned long is not supported for attribute: 'Test'."));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromLong_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        long[][] data = {{1L, 2L}, {3L, 4L}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        // when
        MetadataReader.addMetaAttributesFromLong(arrayAttElem, "Test", data, unsigned);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributesFromLong_illegalNumberOfDimensions() {
        // given
        long[][][][] data = {{{{1L, 2L}, {3L, 4L}}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        final boolean unsigned = false;

        try {
            // when
            MetadataReader.addMetaAttributesFromLong(arrayAttElem, "Test", data, unsigned);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 4"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfAttributesForByte() throws Exception {
        // given
        String attName = "Test";
        final byte[] data = {1, 2, 3};
        final boolean isUnsigned = false;
        Array array = Array.makeFromJavaArray(data, isUnsigned);
        final Attribute mockedAtt = new Attribute(attName, array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfAttributesForShort() throws Exception {
        // given
        String attName = "Test";
        final short[] data = {1, 2, 3};
        final boolean isUnsigned = false;
        Array array = Array.makeFromJavaArray(data, isUnsigned);
        final Attribute mockedAtt = new Attribute(attName, array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfAttributesForInt() throws Exception {
        // given
        String attName = "Test";
        final int[] data = {1, 2, 3};
        final boolean isUnsigned = false;
        Array array = Array.makeFromJavaArray(data, isUnsigned);
        final Attribute mockedAtt = new Attribute(attName, array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfAttributesForLong() throws Exception {
        // given
        String attName = "Test";
        final long[] data = {10, 20, 30};
        final boolean isUnsigned = false;
        Array dataArray = Array.makeFromJavaArray(data, isUnsigned);
        final Attribute mockedAtt = new Attribute(attName, dataArray);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfAttributesForFloat() throws Exception {
        // given
        String attName = "Test";
        final float[] data = {10, 20, 30};
        final boolean isUnsigned = false;
        Array dataArray = Array.makeFromJavaArray(data, isUnsigned);
        final Attribute mockedAtt = new Attribute(attName, dataArray);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfAttributesForDouble() throws Exception {
        // given
        String attName = "Test";
        final double[] data = {10, 20, 30};
        final boolean isUnsigned = false;
        Array dataArray = Array.makeFromJavaArray(data, isUnsigned);
        final Attribute mockedAtt = new Attribute(attName, dataArray);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfAttributesForScalarString() throws Exception {
        // given
        String attName = "Test";
        final String[] data = {"Test1", "Test2", "Test3"};
        final Attribute mockedAtt = new Attribute(attName, List.of(data));
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(3));
        final String[] expectedAttNames = {"Test.1", "Test.2", "Test.3"};
        final String[] expectedValues = data.clone();
        for (int i = 0; i < arrayAttElem.getNumAttributes(); i++) {
            final MetadataAttribute att = arrayAttElem.getAttributeAt(i);
            assertThat("Invalid name at pos " + i, att.getName(), is(expectedAttNames[i]));
            assertThat("Invalid value at pos " + i, att.getData().getElemString(), is(expectedValues[i]));
        }
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElemString(), is("Test2"));
        assertThat(arrayAttElem.getAttributeAt(2).getData().getElemString(), is("Test3"));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_ByteAndUByte_2D() throws Exception {
        // given
        String varName = "Test";
        final byte[][] data = {{-1, -2}, {-3, -4}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.UBYTE);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2, 2});
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfVariable(element, variable);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(varName));
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_ShortAndUShort_2D() throws Exception {
        // given
        String varName = "Test";
        final short[][] data = {{-1, -2}, {-3, -4}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.USHORT);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2, 2});
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfVariable(element, variable);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(varName));
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_IntAndUInt_2D() throws Exception {
        // given
        String varName = "Test";
        final int[][] data = {{-1, -2}, {-3, -4}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.UINT);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2, 2});
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfVariable(element, variable);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(varName));
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_Long_2D() throws Exception {
        // given
        String varName = "Test";
        final long[][] data = {{1, 2}, {3, 4}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.LONG);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2, 2});
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfVariable(element, variable);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(varName));
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }


    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_ULong_2D_NotSupported() throws Exception {
        // given
        String varName = "Test";
        final long[][] data = {{-1, -2}, {-3, -4}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.ULONG);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2, 2});
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        try {
            MetadataReader.handleArrayDataOfVariable(element, variable);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException e) {
            // then
            assertThat(e.getMessage(), is("Unsupported data type: ulong"));
        } catch (IOException e) {
            fail("IllegalFileFormatException expected");
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_Float_2D() throws Exception {
        // given
        String varName = "Test";
        final float[][] data = {{1.1f, 2.2f}, {3.3f, 4.4f}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.FLOAT);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2, 2});
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfVariable(element, variable);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(varName));
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_Double_2D() throws Exception {
        // given
        String varName = "Test";
        final double[][] data = {{1.1, 2.2}, {3.3, 4.4}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.DOUBLE);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2, 2});
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfVariable(element, variable);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(varName));
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataOfVariable_Char() throws Exception {
        // given
        String varName = "Test";
        final char[] data = ("a char array containing tabs\tand\tdifferent type of line separators" +
                             "\nn\rr\r\nrn and\n\rnr").toCharArray();
        final Array array = Array.makeFromJavaArray(data);
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.CHAR);
        Mockito.when(variable.read()).thenReturn(array);
        MetadataElement element = new MetadataElement("Leaf");
        // when
        MetadataReader.handleArrayDataOfVariable(element, variable);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(varName));
        assertThat(arrayAttElem.getNumAttributes(), is(5));
        final MetadataAttribute[] attributes = arrayAttElem.getAttributes();
        final String[] expected = {
                "a char array containing tabs    and    different type of line separators",
                "n", "r", "rn and", "nr"
        };
        for (int i = 0; i < attributes.length; i++) {
            MetadataAttribute attribute = attributes[i];
            final String reason = "At position: " + i;
            assertThat(reason, attribute.getDataType(), is(ProductData.TYPE_ASCII));
            assertThat(reason, attribute.getData().getElemString(), is(expected[i]));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayData_UnsupportedDataOfAttributesType() {
        // given
        String attName = "Test";
        final boolean[] data = {true, false, true};
        final boolean isUnsigned = false;
        final Array array = Array.makeFromJavaArray(data, isUnsigned);
        final Attribute mockedAtt = new Attribute(attName, array);
        MetadataElement element = new MetadataElement("Leaf");
        try {
            // when
            MetadataReader.handleArrayDataOfAttributes(element, attName, mockedAtt);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported data type: boolean"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateProductDataInstanceForScalarData() throws IllegalFileFormatException {
        ProductData pd;
        boolean isUnsigned;
        Attribute att;

        isUnsigned = true;
        att = new Attribute("n", Array.makeFromJavaArray(new byte[]{-1}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.UByte.class)));
        assertThat(pd.getElems(), is(new byte[]{-1}));
        assertThat(pd.isUnsigned(), is(true));
        assertThat(pd.getElemInt(), is(255));

        isUnsigned = false;
        att = new Attribute("n", Array.makeFromJavaArray(new byte[]{-1}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.Byte.class)));
        assertThat(pd.getElems(), is(new byte[]{-1}));
        assertThat(pd.isUnsigned(), is(false));
        assertThat(pd.getElemInt(), is(-1));

        isUnsigned = true;
        att = new Attribute("n", Array.makeFromJavaArray(new short[]{-1}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.UShort.class)));
        assertThat(pd.getElems(), is(new short[]{-1}));
        assertThat(pd.isUnsigned(), is(true));
        assertThat(pd.getElemInt(), is(65535));

        isUnsigned = false;
        att = new Attribute("n", Array.makeFromJavaArray(new short[]{-1}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.Short.class)));
        assertThat(pd.getElems(), is(new short[]{-1}));
        assertThat(pd.isUnsigned(), is(false));
        assertThat(pd.getElemInt(), is(-1));

        isUnsigned = true;
        att = new Attribute("n", Array.makeFromJavaArray(new int[]{-1}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.UInt.class)));
        assertThat(pd.getElems(), is(new int[]{-1}));
        assertThat(pd.isUnsigned(), is(true));
        assertThat(pd.getElemLong(), is(-1L));

        isUnsigned = false;
        att = new Attribute("n", Array.makeFromJavaArray(new int[]{42}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.Int.class)));
        assertThat(pd.getElems(), is(new int[]{42}));

        isUnsigned = true;
        att = new Attribute("n", Array.makeFromJavaArray(new long[]{-1}, isUnsigned));
        try {
            pd = MetadataReader.createProductDataInstanceForScalarData(att);
            fail("Exception expected.");
        } catch (IllegalFileFormatException e) {
            assertThat(e.getMessage(), is("Unsupported data type: ulong"));
        } catch (Throwable e) {
            fail("Unexpected exception: " + e);
        }

        isUnsigned = false;
        att = new Attribute("n", Array.makeFromJavaArray(new long[]{82}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.Long.class)));
        assertThat(pd.getElems(), is(new long[]{82}));

        att = new Attribute("n", Array.makeFromJavaArray(new float[]{4.2f}));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.Float.class)));
        assertThat(pd.getElems(), is(new float[]{4.2f}));

        att = new Attribute("n", Array.makeFromJavaArray(new double[]{8.2}));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.Double.class)));
        assertThat(pd.getElems(), is(new double[]{8.2}));

        att = new Attribute("n", Array.makeArray(DataType.STRING, new String[]{"What?"}));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.ASCII.class)));
        assertThat(pd.getElemString(), is("What?"));

        try {
            att = new Attribute("n", Array.makeFromJavaArray(new boolean[]{false}));
            MetadataReader.createProductDataInstanceForScalarData(att);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            assertThat(expected.getMessage(), is("Unsupported data type: boolean"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleScalarData() throws IllegalFileFormatException {
        final MetadataElement elem = new MetadataElement("elem");
        Attribute att;

        try {
            att = new Attribute("n", Array.factory(DataType.BOOLEAN, new int[]{1}, new boolean[]{false}));
            MetadataReader.handleScalarData(elem, "AttName", att);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            assertThat(expected.getMessage(), is("Unsupported data type: boolean"));
        }
        assertThat(elem.getNumElements(), is(0));
        assertThat(elem.getNumAttributes(), is(0));

        att = new Attribute("n", Array.makeFromJavaArray(new float[]{23.4f}));
        MetadataReader.handleScalarData(elem, "AttName", att);
        assertThat(elem.getNumElements(), is(0));
        assertThat(elem.getNumAttributes(), is(1));
        final MetadataAttribute attr = elem.getAttributeAt(0);
        assertThat(attr.getName(), is("AttName"));
        assertThat(attr.getData().getElems(), is(new float[]{23.4f}));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleAttributeData_scalar() throws IllegalFileFormatException {
        final MetadataElement elem = new MetadataElement("elem");
        MetadataReader.handleAttributeData(
                elem, new Attribute("SomeName", Array.makeFromJavaArray(new int[]{6})));

        assertThat(elem.getNumElements(), is(0));
        assertThat(elem.getNumAttributes(), is(1));
        final MetadataAttribute attrAt0 = elem.getAttributeAt(0);
        assertThat(attrAt0.getName(), is("SomeName"));
        assertThat(attrAt0.getData().getElems(), is(new int[]{6}));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleAttributeData_arrayData() throws IllegalFileFormatException {
        final MetadataElement elem = new MetadataElement("elem");
        MetadataReader.handleAttributeData(
                elem,
                new Attribute("SomeOtherName", Array.makeFromJavaArray(new long[]{4, 5, 6, 7})));

        assertThat(elem.getNumElements(), is(1));
        assertThat(elem.getNumAttributes(), is(0));
        final MetadataElement elemAt0 = elem.getElementAt(0);
        assertThat(elemAt0.getName(), is("SomeOtherName"));
        assertThat(elemAt0.getNumElements(), is(0));
        assertThat(elemAt0.getNumAttributes(), is(1));
        final MetadataAttribute attrAt0 = elemAt0.getAttributeAt(0);
        assertThat(attrAt0.getName(), is("SomeOtherName"));
        assertThat(attrAt0.getData().getElems(), is(new long[]{4, 5, 6, 7}));
    }

    @STTM("SNAP-3445")
    @Test
    public void testReadMetadata_withGlobalAttributes() throws IOException {
        final Product testProduct = new Product("Name", "Type");
        final NetcdfFile mockedHdfFile = Mockito.mock(NetcdfFile.class);
        final Group mockRootGroup = Mockito.mock(Group.class);

        Mockito.when(mockedHdfFile.getRootGroup()).thenReturn(mockRootGroup);
        Mockito.when(mockedHdfFile.getGlobalAttributes()).thenReturn(List.of(
                new Attribute("Att ints", Array.makeFromJavaArray(new int[]{2, 5})),
                new Attribute("Att String", "# 5% --==>> Any Characters")
        ));

        //execution
        MetadataReader.readMetadata(mockedHdfFile, testProduct);

        //verification
        final MetadataElement globalAttributes = testProduct.getMetadataRoot().getElement("GlobalAttributes");
        assertThat(globalAttributes, is(notNullValue()));
        assertThat(globalAttributes.getNumElements(), is(1));
        assertThat(globalAttributes.getNumAttributes(), is(1));

        final MetadataElement meInts = globalAttributes.getElementAt(0);
        assertThat(meInts.getName(), is("Att_ints")); // underscore ... a metadata name must not contain blank characters.
        assertThat(meInts.getNumElements(), is(0));
        assertThat(meInts.getNumAttributes(), is(1));
        assertThat(meInts.getAttributeAt(0).getName(), is("Att_ints"));
        assertThat(meInts.getAttributeAt(0).getDataElems(), is(new int[]{2, 5}));

        final MetadataAttribute strAtt = globalAttributes.getAttributeAt(0);
        assertThat(strAtt.getName(), is("Att_String")); // underscore ... a metadata name must not contain blank characters.
        assertThat(strAtt.getData().getElemString(), is("# 5% --==>> Any Characters"));
    }

    @STTM("SNAP-3445")
    @Test
    public void testReadMetadata_addAttributeTreeTo() throws IOException {
        final MetadataElement metadataRoot = new MetadataElement("metadataRoot");
        final Group mockRootGroup = Mockito.mock(Group.class);
        final Group mockNestedGroup1 = Mockito.mock(Group.class);
        final Group mockNestedGroup2 = Mockito.mock(Group.class);

        Mockito.when(mockRootGroup.getGroups()).thenReturn(List.of(mockNestedGroup1));
        Mockito.when(mockNestedGroup1.getGroups()).thenReturn(List.of(mockNestedGroup2));

        Mockito.when(mockNestedGroup1.getShortName()).thenReturn("nested1");
        Mockito.when(mockNestedGroup1.getAttributes()).thenReturn(List.of(
                new Attribute("att1", Array.makeFromJavaArray(new int[]{2, 5}))));

        Mockito.when(mockNestedGroup2.getShortName()).thenReturn("nested2");
        Mockito.when(mockNestedGroup2.getAttributes()).thenReturn(List.of(
                new Attribute("att2", Array.makeFromJavaArray(new int[]{4, 8}))));

        //execution
        MetadataReader.addAttributeTreeTo(metadataRoot, mockRootGroup);

        //verification
        assertThat(metadataRoot.getNumElements(), is(1));
        assertThat(metadataRoot.getNumAttributes(), is(0));

        final MetadataElement nested1 = metadataRoot.getElement("nested1");
        assertThat(nested1, is(notNullValue()));
        assertThat(nested1.getNumElements(), is(2));
        assertThat(nested1.getNumAttributes(), is(0));
        final MetadataElement elAtt1 = nested1.getElement("att1");
        assertThat(elAtt1, is(notNullValue()));
        assertThat(elAtt1.getNumElements(), is(0));
        assertThat(elAtt1.getNumAttributes(), is(1));
        final MetadataAttribute att1 = elAtt1.getAttribute("att1");
        assertThat(att1, is(notNullValue()));
        assertThat(att1.getDataElems(), is(new int[]{2, 5}));

        final MetadataElement nested2 = nested1.getElement("nested2");
        assertThat(nested2, is(notNullValue()));
        assertThat(nested2.getNumElements(), is(1));
        assertThat(nested2.getNumAttributes(), is(0));
        final MetadataElement elAtt2 = nested2.getElement("att2");
        assertThat(elAtt2, is(notNullValue()));
        assertThat(elAtt2.getNumElements(), is(0));
        assertThat(elAtt2.getNumAttributes(), is(1));
        final MetadataAttribute att2 = elAtt2.getAttribute("att2");
        assertThat(att2, is(notNullValue()));
        assertThat(att2.getDataElems(), is(new int[]{4, 8}));
    }

    @STTM("SNAP-3445")
    @Test
    public void testReadMetadata_addAuxVariableTreeTo() throws IOException {
        final Group mockRootGroup = Mockito.mock(Group.class);
        final Group mockNestedGroup1 = Mockito.mock(Group.class);
        final Group mockNestedGroup2 = Mockito.mock(Group.class);

        Mockito.when(mockNestedGroup1.getShortName()).thenReturn("nested1");
        Mockito.when(mockNestedGroup2.getShortName()).thenReturn("HDFEOS");

        Mockito.when(mockRootGroup.getGroups()).thenReturn(List.of(mockNestedGroup1));
        Mockito.when(mockNestedGroup1.getGroups()).thenReturn(List.of(mockNestedGroup2));

        final Variable var1 = Variable.builder()
                .setName("var1")
                .setDataType(DataType.INT)
                .setDimensionsAnonymous(new int[]{3, 2})
                .setCachedData(Array.makeFromJavaArray(new int[][]{{1, 2}, {3, 4}, {5, 6}}), false)
                .setGroup(mockNestedGroup1)
                .build();
        final Variable var2 = Variable.builder()
                .setName("var2")
                .setDataType(DataType.INT)
                .setDimensionsAnonymous(new int[]{3, 1})
                .setCachedData(Array.makeFromJavaArray(new int[]{7, 8, 9}), false)
                .setGroup(mockNestedGroup2)
                .build();

        Mockito.when(mockNestedGroup1.getVariables()).thenReturn(List.of(var1));
        Mockito.when(mockNestedGroup2.getVariables()).thenReturn(List.of(var2));

        final MetadataElement metadataRoot = new MetadataElement("metadataRoot");

        //execution
        MetadataReader.addAuxVariableTreeTo(metadataRoot, mockRootGroup, false);

        //verification
        assertThat(metadataRoot.getNumElements(), is(1));
        assertThat(metadataRoot.getNumAttributes(), is(0));

        final MetadataElement nested1 = metadataRoot.getElement("nested1");
        assertThat(nested1, is(notNullValue()));
        assertThat(nested1.getNumElements(), is(2));
        assertThat(nested1.getNumAttributes(), is(0));
        final MetadataElement elVar1 = nested1.getElement("var1");
        assertThat(elVar1, is(notNullValue()));
        assertThat(elVar1.getNumElements(), is(0));
        assertThat(elVar1.getNumAttributes(), is(3));
        final MetadataAttribute var1_1 = elVar1.getAttribute("var1.1");
        assertThat(var1_1, is(notNullValue()));
        assertThat(var1_1.getDataElems(), is(new int[]{1, 2}));
        final MetadataAttribute var1_2 = elVar1.getAttribute("var1.2");
        assertThat(var1_2, is(notNullValue()));
        assertThat(var1_2.getDataElems(), is(new int[]{3, 4}));
        final MetadataAttribute var1_3 = elVar1.getAttribute("var1.3");
        assertThat(var1_3, is(notNullValue()));
        assertThat(var1_3.getDataElems(), is(new int[]{5, 6}));

        final MetadataElement nested2 = nested1.getElement("HDFEOS");
        assertThat(nested2, is(notNullValue()));
        assertThat(nested2.getNumElements(), is(1));
        assertThat(nested2.getNumAttributes(), is(0));
        final MetadataElement elVar2 = nested2.getElement("var2");
        assertThat(elVar2, is(notNullValue()));
        assertThat(elVar2.getNumElements(), is(0));
        assertThat(elVar2.getNumAttributes(), is(1));
        final MetadataAttribute var2_1 = elVar2.getAttribute("var2");
        assertThat(var2_1, is(notNullValue()));
        assertThat(var2_1.getDataElems(), is(new int[]{7,8,9}));
    }
}

