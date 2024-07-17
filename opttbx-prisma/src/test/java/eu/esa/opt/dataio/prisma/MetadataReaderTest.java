package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;

import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import org.mockito.Mockito;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.ProxyReader;
import ucar.nc2.Variable;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.util.CancelTask;

import java.io.IOException;
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
    public void testAddMetaAttributes_forMultidimensionslData() throws IllegalFileFormatException {
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
    public void testAddMetaAttributes_forMultidimensionalDataFromLong() throws IllegalFileFormatException {
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
        assertThat(attribute.getData().getElemUIntAt(0) , is(data[0]& 0xffffffffL));
    }

    @STTM("SNAP-3445")
    @Test
    public void testAddMetaAttributes_forMultidimensionalDataFromInt() throws IllegalFileFormatException {
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
    public void testHandleArrayDataOfVariableForMultidimensionalFloat() throws Exception {
        // given
        String varName = "Test";
        final float[][] data = {{1.1f, 2.2f}, {3.3f, 4.4f}};
        final Array array = Array.makeFromJavaArray(data).reshape(new int[]{2, 2});
        final VariableDS variable = Mockito.mock(VariableDS.class);
        Mockito.when(variable.getShortName()).thenReturn(varName);
        Mockito.when(variable.getDataType()).thenReturn(DataType.FLOAT);
        Mockito.when(variable.getShape()).thenReturn(new int[]{2,2});
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
    public void testCreateProductDataInstanceForScalar() throws IllegalFileFormatException {
        ProductData pd;

        boolean isUnsigned = true;
        Attribute att = new Attribute("n", Array.makeFromJavaArray(new short[]{-1}, isUnsigned));
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

        isUnsigned = false;
        att = new Attribute("n", Array.makeFromJavaArray(new int[]{42}, isUnsigned));
        pd = MetadataReader.createProductDataInstanceForScalarData(att);
        assertThat(pd, is(instanceOf(ProductData.Int.class)));
        assertThat(pd.getElems(), is(new int[]{42}));

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
}
