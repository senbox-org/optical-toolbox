package eu.esa.opt.dataio.flex.metadata;

import org.esa.snap.core.datamodel.MetadataElement;

import java.io.IOException;

public interface FlexMetadataProvider {

    MetadataElement readElement(String name) throws IOException;
}
