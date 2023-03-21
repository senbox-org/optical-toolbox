package eu.esa.opt.dataio.s2.l2hf.l2h.metadata;

import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.S2SpatialResolution;
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripDirFilename;
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripFilename;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.Collection;

/**
 * Created by fdouziech
 */
public interface IL2hProductMetadata {
    L2hMetadata.ProductCharacteristics getProductOrganization(VirtualPath path, S2SpatialResolution resolution);
    Collection<String> getTiles();
    String[] getGranules();
    S2DatastripFilename getDatastrip();
    S2DatastripDirFilename getDatastripDir();
    MetadataElement getMetadataElement();
    String getFormat();
}
