package eu.esa.opt.dataio.flex.metadata;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class FlexMetadataElementLazyTest {


    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnGetNumElements() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);

        assertEquals(1, lazy.getNumElements());
        assertEquals("child", lazy.getElementAt(0).getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnGetNumAttributes() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);

        assertEquals(2, lazy.getNumAttributes());
        assertEquals("units", lazy.getAttributeAt(0).getName());
        assertEquals("nm", lazy.getAttributeAt(0).getData().getElemString());
        assertEquals("value", lazy.getAttributeAt(1).getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnGetElements() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);

        final MetadataElement[] elements = lazy.getElements();
        assertEquals(1, elements.length);
        assertEquals("child", elements[0].getName());
    }

    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnGetAttributes() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);

        final MetadataAttribute[] attributes = lazy.getAttributes();
        assertEquals(2, attributes.length);
    }

    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnContainsElement() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        assertTrue(lazy.containsElement("child"));
        assertFalse(lazy.containsElement("nonexistent"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnContainsAttribute() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        assertTrue(lazy.containsAttribute("units"));
        assertFalse(lazy.containsAttribute("nonexistent"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnGetElement() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        assertNotNull(lazy.getElement("child"));
        assertNull(lazy.getElement("nonexistent"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testLazyLoadingOnGetAttribute() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        assertNotNull(lazy.getAttribute("units"));
        assertNull(lazy.getAttribute("nonexistent"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testProviderReturnsNull() {
        final FlexMetadataProvider provider = name -> null;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("missing_var", provider);
        assertEquals(0, lazy.getNumAttributes());
        assertEquals(0, lazy.getNumElements());
    }

    @Test
    @STTM("SNAP-4126")
    public void testProviderCalledOnlyOnce() {
        final int[] callCount = {0};
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> {
            callCount[0]++;
            return inner;
        };

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        lazy.getNumAttributes();
        lazy.getNumElements();
        lazy.getAttributes();
        lazy.getElements();
        assertEquals(1, callCount[0]);
    }

    @Test
    @STTM("SNAP-4126")
    public void testProviderReceivesCorrectName() {
        final String[] receivedName = {null};
        final FlexMetadataProvider provider = name -> {
            receivedName[0] = name;
            return new MetadataElement(name);
        };

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("my_variable", provider);
        lazy.getNumAttributes();
        assertEquals("my_variable", receivedName[0]);
    }

    @Test(expected = RuntimeException.class)
    @STTM("SNAP-4126")
    public void testProviderIOExceptionWrappedInRuntimeException() {
        final FlexMetadataProvider provider = name -> {
            throw new IOException("test error");
        };

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        lazy.getNumAttributes();
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetElementNames() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        final String[] names = lazy.getElementNames();
        assertEquals(1, names.length);
        assertEquals("child", names[0]);
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetAttributeNames() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        final String[] names = lazy.getAttributeNames();
        assertEquals(2, names.length);
        assertEquals("units", names[0]);
        assertEquals("value", names[1]);
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetElementGroup() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);

        assertNotNull(lazy.getElementGroup());
        assertEquals(1, lazy.getElementGroup().getNodeCount());
    }

    @Test
    @STTM("SNAP-4126")
    public void testRemoveElement() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        final MetadataElement child = lazy.getElement("child");

        assertTrue(lazy.removeElement(child));
        assertFalse(lazy.containsElement("child"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetElementIndex() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        final MetadataElement child = lazy.getElement("child");

        assertEquals(0, lazy.getElementIndex(child));
    }

    @Test
    @STTM("SNAP-4126")
    public void testGetAttributeIndex() {
        final MetadataElement inner = createTestElement();
        final FlexMetadataProvider provider = name -> inner;

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);
        final MetadataAttribute attr = lazy.getAttribute("units");

        assertEquals(0, lazy.getAttributeIndex(attr));
    }

    @Test
    @STTM("SNAP-4126")
    public void testProviderIOExceptionWrappedWithCause() {
        final IOException ioException = new IOException("test error");
        final FlexMetadataProvider provider = name -> {
            throw ioException;
        };

        final FlexMetadataElementLazy lazy = new FlexMetadataElementLazy("test_var", provider);

        try {
            lazy.getNumAttributes();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertSame(ioException, e.getCause());
        }
    }


    private static MetadataElement createTestElement() {
        final MetadataElement element = new MetadataElement("test_var");
        element.addAttribute(new MetadataAttribute("units", ProductData.createInstance("nm"), true));
        element.addAttribute(new MetadataAttribute("value",
                ProductData.createInstance(new double[]{550.0, 680.0, 740.0}), true));

        final MetadataElement child = new MetadataElement("child");
        child.addAttribute(new MetadataAttribute("info", ProductData.createInstance("some info"), true));
        element.addElement(child);
        return element;
    }
}
