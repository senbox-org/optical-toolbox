package eu.esa.opt.dataio.s3;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;

import java.io.IOException;

public interface MetadataProvider {

    MetadataAttribute[] readAttributes(String name) throws IOException;

    MetadataElement[] readElements(String name) throws IOException;
}
