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

        /*
        TODO: 13.02.2025 SE/TB -- Thoughts
            Instead of removing the owner and then setting it again to interrupt the informing of the listeners, we
            should be able to set a LazyLoading state in the respective nodes (via java try with resources block),
            which temporarily stops the firing of listener events of these nodes and below.
            After the work is done (try with resource block), only 1 event should be fired to the topmost inserted or
            changed nodes.
            So if a complete node tree has been inserted or modified, then only one event should be fired to the root
            element of the modified or added tree.
        */
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

        /*
        TODO: 13.02.2025 SE/TB -- Thoughts
            Instead of removing the owner and then setting it again to interrupt the informing of the listeners, we
            should be able to set a LazyLoading state in the respective nodes (via java try with resources block),
            which temporarily stops the firing of listener events of these nodes and below.
            After the work is done (try with resource block), only 1 event should be fired to the topmost inserted or
            changed nodes.
            So if a complete node tree has been inserted or modified, then only one event should be fired to the root
            element of the modified or added tree.
        */
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
