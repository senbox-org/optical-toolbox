package eu.esa.opt.dataio.flex;

import ucar.nc2.NetcdfFile;

public interface FlexProductCompatibility {

    String resolveDataFilePath(String headerReferencedPath);

    int resolveDimension(NetcdfFile ncFile, String groupPath, String dimName, int specDefault);
}
