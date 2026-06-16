package eu.esa.opt.dataio.flex.compatibility;

import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;

/**
 * Handles known deviations in test products from the FLEX processor team:
 * - Double ".nc.nc" file extension in header references
 * - Dimensions defined per-group instead of at root level
 *
 * @deprecated Remove once processor delivers spec-conformant products
 */
@Deprecated
public class EarlyProcessorCompatibility implements FlexProductCompatibility {

    @Override
    public String resolveDataFilePath(String headerReferencedPath) {
        String path = headerReferencedPath;
        if (path.startsWith("./")) {
            path = path.substring(2);
        }
        if (path.endsWith(".nc.nc")) {
            path = path.substring(0, path.length() - 3);
        }
        return path;
    }

    @Override
    public int resolveDimension(NetcdfFile ncFile, String groupPath, String dimName, int specDefault) {
        final Dimension rootDim = ncFile.findDimension(dimName);
        if (rootDim != null) {
            return rootDim.getLength();
        }

        final Group group = ncFile.findGroup(groupPath);
        if (group != null) {
            final Dimension groupDim = group.findDimensionLocal(dimName);
            if (groupDim != null) {
                return groupDim.getLength();
            }
        }

        return specDefault;
    }
}
