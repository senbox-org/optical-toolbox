package eu.esa.opt.dataio.s3;

import org.esa.snap.core.datamodel.MetadataElement;

import java.io.IOException;

public interface MetadataProvider {

    MetadataElement readElement(String name) throws IOException;
}
