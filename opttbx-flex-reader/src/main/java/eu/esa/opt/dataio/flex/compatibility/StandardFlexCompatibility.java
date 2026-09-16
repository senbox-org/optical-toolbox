package eu.esa.opt.dataio.flex.compatibility;

import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;

public class StandardFlexCompatibility implements FlexProductCompatibility {

    @Override
    public String resolveDataFilePath(String headerReferencedPath) {
        if (headerReferencedPath.startsWith("./")) {
            return headerReferencedPath.substring(2);
        }
        return headerReferencedPath;
    }

    @Override
    public int resolveDimension(NetcdfFile ncFile, String groupPath, String dimName, int defaultValue) {
        final Dimension dimension = ncFile.findDimension(dimName);
        if (dimension != null) {
            return dimension.getLength();
        }
        final Dimension groupDimension = findGroupDimension(ncFile, groupPath, dimName);
        if (groupDimension != null) {
            return groupDimension.getLength();
        }
        return defaultValue;
    }

    private static Dimension findGroupDimension(NetcdfFile ncFile, String groupPath, String dimName) {
        if (groupPath == null || groupPath.isEmpty()) {
            return null;
        }

        final Group group = ncFile.findGroup(groupPath);
        if (group == null) {
            return null;
        }
        return group.findDimensionLocal(dimName);
    }
}
