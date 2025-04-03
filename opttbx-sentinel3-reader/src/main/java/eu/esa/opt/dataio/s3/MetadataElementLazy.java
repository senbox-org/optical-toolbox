package eu.esa.opt.dataio.s3;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.datamodel.ProductNodeGroup;

import java.io.IOException;

public class MetadataElementLazy extends MetadataElement {

    private final MetadataProvider provider;
    private final boolean attributesLoaded;
    private final boolean elementsLoaded;
    private MetadataElement element;

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
        element = null;
    }

    public ProductNodeGroup<MetadataElement> getElementGroup() {
        ensureMetaElementLoaded();
        return super.getElementGroup();
    }

    public boolean removeElement(MetadataElement element) {
        ensureMetaElementLoaded();
        return super.removeElement(element);
    }

    public int getNumElements() {
        ensureMetaElementLoaded();
        return super.getNumElements();
    }

    public MetadataElement getElementAt(int index) {
        ensureMetaElementLoaded();
        return super.getElementAt(index);
    }

    public String[] getElementNames() {
        ensureMetaElementLoaded();
        return super.getElementNames();
    }

    public MetadataElement[] getElements() {
        ensureMetaElementLoaded();
        return super.getElements();
    }

    public MetadataElement getElement(String name) {
        ensureMetaElementLoaded();
        return super.getElement(name);
    }

    public boolean containsElement(String name) {
        ensureMetaElementLoaded();
        return super.containsElement(name);
    }

    public int getElementIndex(MetadataElement element) {
        ensureMetaElementLoaded();
        return super.getElementIndex(element);
    }

    @Override
    public int getNumAttributes() {
        ensureMetaElementLoaded();
        return super.getNumAttributes();
    }

    @Override
    public MetadataAttribute getAttributeAt(int index) {
        ensureMetaElementLoaded();
        return super.getAttributeAt(index);
    }

    @Override
    public String[] getAttributeNames() {
        ensureMetaElementLoaded();
        return super.getAttributeNames();
    }

    @Override
    public MetadataAttribute[] getAttributes() {
        ensureMetaElementLoaded();
        return super.getAttributes();
    }

    @Override
    public MetadataAttribute getAttribute(String name) {
        ensureMetaElementLoaded();
        return super.getAttribute(name);
    }

    @Override
    public boolean containsAttribute(String name) {
        ensureMetaElementLoaded();
        return super.containsAttribute(name);
    }

    @Override
    public int getAttributeIndex(MetadataAttribute attribute) {
        ensureMetaElementLoaded();
        return super.getAttributeIndex(attribute);
    }

    private void ensureMetaElementLoaded() {
        if (element == null) {
            final ProductNode owner = getOwner();
            try {
                // disable firing of NodeAdded events. Leads to infinite recursions ... tb 2025-02-07
                setOwner(null);

                element = provider.readElement(getName());
                final MetadataElement[] elements = element.getElements();
                for (final MetadataElement element : elements) {
                    addElement(element);
                }

                final MetadataAttribute[] attributes = element.getAttributes();
                for (final MetadataAttribute attribute : attributes) {
                    addAttribute(attribute);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                setOwner(owner);
            }
        }
    }
}
