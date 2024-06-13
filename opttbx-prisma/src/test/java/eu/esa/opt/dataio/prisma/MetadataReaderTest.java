package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import io.jhdf.api.Attribute;
import io.jhdf.api.Node;

import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class MetadataReaderTest {

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromString() {
        // given
        String[] data = {"Test1", "Test2", "Test3"};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.createArrayDataFromString(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(3));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElemString(), is("Test1"));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElemString(), is("Test2"));
        assertThat(arrayAttElem.getAttributeAt(2).getData().getElemString(), is("Test3"));
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromFloat() throws IllegalFileFormatException {
        // given
        float[] data = {1.1f, 2.2f, 3.3f};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.createArrayDataFromFloat(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromFloat_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        float[][] data = {{1.1f, 2.2f}, {3.3f, 4.4f}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        // when
        MetadataReader.createArrayDataFromFloat(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromFloat_illegalNumberOfDimensions() {
        // given
        float[][][] data = {{{1.1f, 2.2f}, {3.3f, 4.4f}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        try {
            // when
            MetadataReader.createArrayDataFromFloat(arrayAttElem, "Test", data);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 3"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromLong() throws IllegalFileFormatException {
        // given
        long[] data = {1L, 2L, 3L};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        int[] dimensions = {data.length};

        // when
        MetadataReader.createArrayDataFromLong(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromLong_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        long[][] data = {{1L, 2L}, {3L, 4L}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        int[] dimensions = {data.length, data[0].length};

        // when
        MetadataReader.createArrayDataFromLong(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromLong_illegalNumberOfDimensions() {
        // given
        long[][][][] data = {{{{1L, 2L}, {3L, 4L}}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        try {
            // when
            MetadataReader.createArrayDataFromLong(arrayAttElem, "Test", data);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 4"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromInt() throws IllegalFileFormatException {
        // given
        int[] data = {1, 2, 3};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        int[] dimensions = {data.length};

        // when
        MetadataReader.createArrayDataFromInt(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromInt_forMultidimensionalData() throws IllegalFileFormatException {
        // given
        int[][] data = {{1, 2}, {3, 4}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");
        int[] dimensions = {data.length, data[0].length};

        // when
        MetadataReader.createArrayDataFromInt(arrayAttElem, "Test", data);

        // then
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateArrayDataFromInt_illegalNumberOfDimensions() {
        // given
        int[][][][][] data = {{{{{1, 2}, {3, 4}}}}};
        MetadataElement arrayAttElem = new MetadataElement("Leaf");

        try {
            // when
            MetadataReader.createArrayDataFromInt(arrayAttElem, "Test", data);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported number of dimensions: 5"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataForInt() throws Exception {
        // given
        final int[] data = {1, 2, 3};
        final Attribute mockedAtt = new JHdfMockAttribute(false, int.class, data);
        MetadataElement element = new MetadataElement("Leaf");
        String attName = "Test";
        // when
        MetadataReader.handleArrayData(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataForLong() throws Exception {
        // given
        final long[] data = {10, 20, 30};
        final Attribute mockedAtt = new JHdfMockAttribute(false, long.class, data);
        MetadataElement element = new MetadataElement("Leaf");
        String attName = "Test";
        // when
        MetadataReader.handleArrayData(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(1));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataForMultidimensionalFloat() throws Exception {
        // given
        final float[][] data = {{1.1f, 2.2f}, {3.3f, 4.4f}};
        final JHdfMockAttribute mockedAtt = new JHdfMockAttribute(false, float.class, data);
        MetadataElement element = new MetadataElement("Leaf");
        String attName = "Test";
        // when
        MetadataReader.handleArrayData(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(2));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElems(), is(data[0]));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElems(), is(data[1]));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayDataForScalarString() throws Exception {
        // given
        final String[] data = {"Test1", "Test2", "Test3"};
        Attribute mockedAtt = new JHdfMockAttribute(false, String.class, data);
        MetadataElement element = new MetadataElement("Leaf");
        String attName = "Test";
        // when
        MetadataReader.handleArrayData(element, attName, mockedAtt);
        // then
        assertThat(element.getNumElements(), is(1));
        MetadataElement arrayAttElem = element.getElementAt(0);
        assertThat(arrayAttElem.getName(), is(attName));
        assertThat(arrayAttElem.getNumAttributes(), is(3));
        assertThat(arrayAttElem.getAttributeAt(0).getData().getElemString(), is("Test1"));
        assertThat(arrayAttElem.getAttributeAt(1).getData().getElemString(), is("Test2"));
        assertThat(arrayAttElem.getAttributeAt(2).getData().getElemString(), is("Test3"));
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleArrayData_UnsupportedDataType() {
        // given
        final Double[] data = {3.3, 4.4, 5.5};
        Attribute mockedAtt = new JHdfMockAttribute(false, Double.class, data);
        MetadataElement element = new MetadataElement("Leaf");
        String attName = "Test";
        try {
            // when
            MetadataReader.handleArrayData(element, attName, mockedAtt);
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            // then
            assertThat(expected.getMessage(), is("Unsupported data type: java.lang.Double[]"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testCreateProductDataInstanceForScalar() throws IllegalFileFormatException {
        ProductData pd;

        pd = MetadataReader.createProductDataInstanceForScalarData((short) 11);
        assertThat(pd, is(instanceOf(ProductData.Short.class)));
        assertThat(pd.getElems(), is(new short[]{11}));

        pd = MetadataReader.createProductDataInstanceForScalarData((int) 42);
        assertThat(pd, is(instanceOf(ProductData.Int.class)));
        assertThat(pd.getElems(), is(new int[]{42}));

        pd = MetadataReader.createProductDataInstanceForScalarData((long) 82);
        assertThat(pd, is(instanceOf(ProductData.Long.class)));
        assertThat(pd.getElems(), is(new long[]{82}));

        pd = MetadataReader.createProductDataInstanceForScalarData((float) 4.2);
        assertThat(pd, is(instanceOf(ProductData.Float.class)));
        assertThat(pd.getElems(), is(new float[]{4.2f}));

        pd = MetadataReader.createProductDataInstanceForScalarData("What?");
        assertThat(pd, is(instanceOf(ProductData.ASCII.class)));
        assertThat(pd.getElemString(), is("What?"));

        try {
            MetadataReader.createProductDataInstanceForScalarData(new HashMap());
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            assertThat(expected.getMessage(), is("Unsupported data type: java.util.HashMap"));
        }
    }

    @STTM("SNAP-3445")
    @Test
    public void testHandleScalarData() throws IllegalFileFormatException {
        final MetadataElement elem = new MetadataElement("elem");

        try {
            MetadataReader.handleScalarData(elem, "AttName", new ArrayList());
            fail("IllegalFileFormatException expected");
        } catch (IllegalFileFormatException expected) {
            assertThat(expected.getMessage(), is("Unsupported data type: java.util.ArrayList"));
        }
        assertThat(elem.getNumElements(), is(0));
        assertThat(elem.getNumAttributes(), is(0));

        MetadataReader.handleScalarData(elem, "AttName", 23.4f);
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
                elem,
                new JHdfMockAttribute("SomeName", true, int.class, 6));

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
                new JHdfMockAttribute("SomeOtherName", false, long.class, new long[]{4, 5, 6, 7}));

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

    private static class JHdfMockAttribute implements Attribute {
        private final String name;
        private final boolean scalar;
        private final Class<?> aClass;
        private final Object data;

        public JHdfMockAttribute(boolean scalar, Class<?> aClass, Object data) {
            this("attName", scalar, aClass, data);
        }

        public JHdfMockAttribute(String name, boolean scalar, Class<?> aClass, Object data) {
            this.name = name;
            this.scalar = scalar;
            this.aClass = aClass;
            this.data = data;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public long getSizeInBytes() {
            return 0;
        }

        @Override
        public int[] getDimensions() {
            return null;
        }

        @Override
        public Object getData() {
            return data;
        }

        @Override
        public Class<?> getJavaType() {
            return aClass;
        }

        @Override
        public boolean isScalar() {
            return scalar;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ByteBuffer getBuffer() {
            return null;
        }
    }
}
