package eu.esa.opt.dataio.s3;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.datamodel.ProductNodeGroup;

import java.io.IOException;

public class MetadataElementLazy extends MetadataElement {

    private final MetadataProvider provider;
    private boolean attributesLoaded;
    private boolean elementsLoaded;

    /**
     * Constructs a new metadata element.
     *
     * @param name the element name
     */
    public MetadataElementLazy(String name, MetadataProvider provider) {
        super(name);

        this.provider = provider;
        attributesLoaded = false;
        elementsLoaded = false;
    }

    public ProductNodeGroup<MetadataElement> getElementGroup() {
        ensureElementsLoaded();
        return super.getElementGroup();
    }

    public boolean removeElement(MetadataElement element) {
        ensureElementsLoaded();
        return super.removeElement(element);
    }

    public int getNumElements() {
        ensureElementsLoaded();
        return super.getNumElements();
    }

    public MetadataElement getElementAt(int index) {
        ensureElementsLoaded();
        return super.getElementAt(index);
    }

    public String[] getElementNames() {
        ensureElementsLoaded();
        return super.getElementNames();
    }

    public MetadataElement[] getElements() {
        ensureElementsLoaded();
        return super.getElements();
    }

    public MetadataElement getElement(String name) {
        ensureElementsLoaded();
        return super.getElement(name);
    }

    public boolean containsElement(String name) {
        ensureElementsLoaded();
        return super.containsElement(name);
    }

    public int getElementIndex(MetadataElement element) {
        ensureElementsLoaded();
        return super.getElementIndex(element);
    }

    @Override
    public int getNumAttributes() {
        ensureAttributesLoaded();
        return super.getNumAttributes();
    }

    @Override
    public MetadataAttribute getAttributeAt(int index) {
        ensureAttributesLoaded();
        return super.getAttributeAt(index);
    }

    @Override
    public String[] getAttributeNames() {
        ensureAttributesLoaded();
        return super.getAttributeNames();
    }

    @Override
    public MetadataAttribute[] getAttributes() {
        ensureAttributesLoaded();
        return super.getAttributes();
    }

    @Override
    public MetadataAttribute getAttribute(String name) {
        ensureAttributesLoaded();
        return super.getAttribute(name);
    }

    @Override
    public boolean containsAttribute(String name) {
        ensureAttributesLoaded();
        return super.containsAttribute(name);
    }

    @Override
    public int getAttributeIndex(MetadataAttribute attribute) {
        ensureAttributesLoaded();
        return super.getAttributeIndex(attribute);
    }

    private void ensureAttributesLoaded() {
        if (attributesLoaded) {
            return;
        }

        final ProductNode owner = getOwner();
        try {
            // disable firing of NodeAdded events. Leads to infinite recursions ... tb 2025-02-07
            setOwner(null);

            final MetadataAttribute[] metadataAttributes = provider.readAttributes(getName());
            for (MetadataAttribute attribute : metadataAttributes) {
                addAttribute(attribute);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            setOwner(owner);
        }
        attributesLoaded = true;
    }

    private void ensureElementsLoaded() {
        if (elementsLoaded) {
            return;
        }

        final ProductNode owner = getOwner();
        try {
            // disable firing of NodeAdded events. Leads to infinite recursions ... tb 2025-02-07
            setOwner(null);

            final MetadataElement[] metadataElements = provider.readElements(getName());
            for (MetadataElement element : metadataElements) {
                MetadataElement[] containedElements = element.getElements();
                for (MetadataElement containedElement : containedElements) {
                    addElement(containedElement);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            setOwner(owner);
        }

        elementsLoaded = true;
    }
}
