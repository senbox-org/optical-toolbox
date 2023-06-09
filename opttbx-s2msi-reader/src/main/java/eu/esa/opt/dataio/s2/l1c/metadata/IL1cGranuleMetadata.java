package eu.esa.opt.dataio.s2.l1c.metadata;

import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.S2Metadata;
import eu.esa.opt.dataio.s2.S2SpatialResolution;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.Map;

/**
 * Created by obarrile on 30/09/2016.
 */
public interface IL1cGranuleMetadata {

    //To use only if the associated user product metadata is not available
    S2Metadata.ProductCharacteristics getTileProductOrganization(VirtualPath xmlPath);

    Map<S2SpatialResolution, S2Metadata.TileGeometry> getTileGeometries();
    String getTileID();
    String getHORIZONTAL_CS_CODE();
    String getHORIZONTAL_CS_NAME();
    int getAnglesResolution();


    S2Metadata.AnglesGrid getSunGrid();
    S2Metadata.AnglesGrid[] getViewingAnglesGrid();
    S2Metadata.MaskFilename[] getMasks(VirtualPath path);
    MetadataElement getMetadataElement();
    MetadataElement getSimplifiedMetadataElement();
    String getFormat();
}
