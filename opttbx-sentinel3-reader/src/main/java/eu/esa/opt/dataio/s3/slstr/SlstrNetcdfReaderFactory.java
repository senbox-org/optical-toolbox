package eu.esa.opt.dataio.s3.slstr;

import eu.esa.opt.dataio.s3.Manifest;
import eu.esa.opt.dataio.s3.util.MetTxReader;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;

import java.io.File;
import java.io.IOException;

/**
 * @author Tonio Fincke
 */
class SlstrNetcdfReaderFactory {

    static S3NetcdfReader createSlstrNetcdfReader(File file, Manifest manifest) {
        String productType = manifest.getProductType();
        final String fileName = file.getName();
        if (productType.startsWith("SL_2_FRP")) {
            return new SlstrFRPReader();
        } else if (productType.startsWith("SL_2_WST")) {
            return new SlstrL2WSTL2PReader();
        } else if ("LST_ancillary_ds.nc".equals(fileName)) {
            return new SlstrLSTAncillaryDsReader();
        } else if ("met_tx.nc".equals(fileName)) {
            return new MetTxReader();
        } else {
            return new S3NetcdfReader();
        }
    }
}
