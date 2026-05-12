package eu.esa.opt.dataio.flex;

import ucar.nc2.Dimension;
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
    public int resolveDimension(NetcdfFile ncFile, String groupPath, String dimName, int specDefault) {
        final Dimension dimension = ncFile.findDimension(dimName);
        if (dimension != null) {
            return dimension.getLength();
        }
        return specDefault;
    }
}
