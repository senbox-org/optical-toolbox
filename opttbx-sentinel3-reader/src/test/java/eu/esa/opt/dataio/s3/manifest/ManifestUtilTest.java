package eu.esa.opt.dataio.s3.manifest;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.VirtualDir;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ManifestUtilTest {

    @Test
    @STTM("SNAP-3711")
    public void testGetManifestInputStream() throws IOException {
        InputStream manifestInputStream = ManifestUtil.getManifestInputStream(getVirtualDir(false));
        assertNotNull(manifestInputStream);
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetManifestInputStream_noManifest() throws IOException {
        InputStream manifestInputStream = ManifestUtil.getManifestInputStream(getVirtualDir(true));
        assertNull(manifestInputStream);
    }

    private static VirtualDir getVirtualDir(boolean skipManifest) {
        return new VirtualDir() {
            @Override
            public String getBasePath() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public File getBaseFile() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public InputStream getInputStream(String path) throws IOException {
                if (path.contains(XfduManifest.MANIFEST_FILE_NAME)) {
                    return new ByteArrayInputStream("whocares".getBytes());
                }
                throw new RuntimeException("not implemented");
            }

            @Override
            public File getFile(String path) throws IOException {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String[] list(String path) throws IOException {
                throw new RuntimeException("not implemented");
            }

            @Override
            public boolean exists(String path) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String[] listAllFiles() throws IOException {
                if (skipManifest) {
                    return new String[]{"Oa_radiance", "popeidience", "latitude", "lon"};
                }else {
                    return new String[]{"Oa_radiance", "popeidience", "latitude", XfduManifest.MANIFEST_FILE_NAME, "lon"};
                }
            }

            @Override
            public void close() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public boolean isCompressed() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public boolean isArchive() {
                throw new RuntimeException("not implemented");
            }
        };
    }
}
