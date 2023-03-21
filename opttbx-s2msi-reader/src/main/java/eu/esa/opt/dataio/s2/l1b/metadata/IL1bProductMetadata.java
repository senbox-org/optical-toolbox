package eu.esa.opt.dataio.s2.l1b.metadata;

import eu.esa.opt.dataio.s2.S2Metadata;
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripDirFilename;
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripFilename;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.Collection;

/**
 * Created by obarrile on 07/10/2016.
 */
public interface IL1bProductMetadata {
    S2Metadata.ProductCharacteristics getProductOrganization();
    Collection<String> getTiles();
    S2DatastripFilename getDatastrip();
    S2DatastripDirFilename getDatastripDir();
    MetadataElement getMetadataElement();
    String getFormat();
}
