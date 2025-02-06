package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductNodeGroup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class MetadataElementLazyTest {

    private MetadataElementLazy metadataElementLazy;

    @Before
    public void setUp() {
        metadataElementLazy = new MetadataElementLazy("Anna", new MockAttributeProvider());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetNumAttributes() {
        assertEquals(4, metadataElementLazy.getNumAttributes());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testAddAttribute() {
        metadataElementLazy.addAttribute(new MetadataAttribute("a_new one", ProductData.TYPE_INT32));
        assertEquals(5, metadataElementLazy.getNumAttributes());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetAttributeAt() {
        final MetadataAttribute attribute = metadataElementLazy.getAttributeAt(1);
        assertEquals("two", attribute.getName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetAttributeNames() {
        final String[] attributeNames = metadataElementLazy.getAttributeNames();
        assertEquals(4, attributeNames.length);
        assertEquals("three", attributeNames[2]);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetAttributs() {
        MetadataAttribute[] attributes = metadataElementLazy.getAttributes();
        assertEquals(4, attributes.length);
        assertEquals("four", attributes[3].getName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetAttribute() {
        MetadataAttribute attribute = metadataElementLazy.getAttribute("one");
        assertEquals("one", attribute.getName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testContainsAttribute() {
        assertTrue(metadataElementLazy.containsAttribute("two"));
        assertFalse(metadataElementLazy.containsAttribute("eight"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetAttributeIndex() {
       assertEquals(-1, metadataElementLazy.getAttributeIndex(new MetadataAttribute("sure_not", ProductData.createInstance(new float[]{13}), true)));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetElementGroup() {
        final ProductNodeGroup<MetadataElement> elementGroup = metadataElementLazy.getElementGroup();
        assertEquals(2, elementGroup.getNodeCount());
        assertEquals("Himpelchen", elementGroup.get(0).getName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetNumElements() {
        assertEquals(2, metadataElementLazy.getNumElements());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetElementAt() {
        final MetadataElement element = metadataElementLazy.getElementAt(1);
        assertEquals("Pimpelchen", element.getName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetElementNames() {
        final String[] expected = new String[]{"Himpelchen", "Pimpelchen"};
        final String[] elementNames = metadataElementLazy.getElementNames();
        assertArrayEquals(expected, elementNames);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetElements() {
        final MetadataElement[] elements = metadataElementLazy.getElements();
        assertEquals(2, elements.length);
        assertEquals("Himpelchen", elements[0].getName());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetElement() {
        MetadataElement element = metadataElementLazy.getElement("Pimpelchen");
        assertEquals("Pimpelchen", element.getName());

        element = metadataElementLazy.getElement("NasenWutz");
        assertNull(element);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testContainsElement() {
        assertTrue(metadataElementLazy.containsElement("Pimpelchen"));
        assertFalse(metadataElementLazy.containsElement("Häschen"));
    }

    private static class MockAttributeProvider implements MetadataProvider {

        @Override
        public MetadataElement[] readElements(String name) throws IOException {
            final MetadataElement[] metadataElements = new MetadataElement[2];

            metadataElements[0] = new MetadataElement("Himpelchen");
            metadataElements[1] = new MetadataElement("Pimpelchen");
            return metadataElements;
        }

        @Override
        public MetadataAttribute[] readAttributes(String name) throws IOException {
            final MetadataAttribute[] metadataAttributes = new MetadataAttribute[4];

            metadataAttributes[0] = new MetadataAttribute("one", ProductData.createInstance(new int[]{1}), true);
            metadataAttributes[1] = new MetadataAttribute("two", ProductData.createInstance(new short[]{2, 3}), true);
            metadataAttributes[2] = new MetadataAttribute("three", ProductData.createInstance(new float[]{4, 5, 6}), true);
            metadataAttributes[3] = new MetadataAttribute("four", ProductData.createInstance(new double[]{7, 8, 9, 10}), true);
            return metadataAttributes;
        }
    }
}
