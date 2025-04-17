package eu.esa.opt.dataio.s3.manifest;

import com.bc.ceres.core.VirtualDir;

import java.io.IOException;
import java.io.InputStream;

public class ManifestUtil {

    public static InputStream getManifestInputStream(VirtualDir virtualDir) throws IOException {
        final String[] list = virtualDir.listAllFiles();
        for (final String entry : list) {
            if (entry.toLowerCase().endsWith(XfduManifest.MANIFEST_FILE_NAME)) {
                return virtualDir.getInputStream(entry);
            }
        }

        return null;
    }
}
