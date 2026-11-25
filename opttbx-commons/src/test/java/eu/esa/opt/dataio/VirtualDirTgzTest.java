/*
 *
 *  Copyright (c) 2022.
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package eu.esa.opt.dataio;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.runtime.internal.IOHelper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @deprecated since 10.0.0, use {@link org.esa.snap.engine_utilities.dataio.VirtualDirTgzTest} instead
 */
public class VirtualDirTgzTest {

    public Path tempFolder = IOHelper.createATempDir();


    @Test
    public void testIsTgz() {
        assertFalse(VirtualDirTgz.isTgz("xxxxx.nc.gz"));
        assertFalse(VirtualDirTgz.isTgz("xxxxx.ppt.gz"));
        assertFalse(VirtualDirTgz.isTgz("xxxxx.gz"));
        assertFalse(VirtualDirTgz.isTgz("xxxxx.tar"));
        assertFalse(VirtualDirTgz.isTgz("xxxxx.geotiff"));

        assertTrue(VirtualDirTgz.isTgz("xxxxx.tgz"));
        assertTrue(VirtualDirTgz.isTgz("xxxxx.tGz"));
        assertTrue(VirtualDirTgz.isTgz("xxxxx.tar.gz"));
        assertTrue(VirtualDirTgz.isTgz("xxxxx.TAR.gz"));
    }


    @Test
    @STTM("SNAP-4105")
    public void test_closeDeletesExtractedTempDir() throws Exception {
        final Path tarFile = tempFolder.resolve("test.tar");
        createTarWithSingleFile(tarFile, "folder/a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile);
        virtualDir.ensureUnpacked(null);
        final Path extractDir = virtualDir.getTempDir();

        assertNotNull(extractDir);
        assertTrue(Files.exists(extractDir));
        assertTrue(Files.isRegularFile(extractDir.resolve("folder/a.txt")));

        virtualDir.close();
        assertFalse(Files.exists(extractDir));
        assertNull(virtualDir.getTempDir());
    }

    @Test
    @STTM("SNAP-4105")
    public void test_closeIsIdempotent() throws Exception {
        final Path tarFile = tempFolder.resolve("test.tar");
        createTarWithSingleFile(tarFile, "a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile);
        virtualDir.ensureUnpacked(null); Path extractDir = virtualDir.getTempDir();

        assertNotNull(extractDir);
        assertTrue(Files.exists(extractDir));
        virtualDir.close();
        virtualDir.close();

        assertFalse(Files.exists(extractDir));
        assertNull(virtualDir.getTempDir());
    }

    @Test
    @STTM("SNAP-4105")
    public void test_closeWithoutUnpackDoesNothing() throws Exception {
        final Path tarFile = tempFolder.resolve("test.tar");
        createTarWithSingleFile(tarFile, "a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile);

        assertNull(virtualDir.getTempDir()); virtualDir.close();
        assertNull(virtualDir.getTempDir());
    }

    @Test
    @STTM("SNAP-4105")
    public void test_makeLocalTempFolderThenCloseDeletesTempDir() throws Exception {
        final Path tarFile = tempFolder.resolve("test.tar");
        createTarWithSingleFile(tarFile, "a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile);
        final Path tempDirPath = virtualDir.makeLocalTempFolder();

        assertNotNull(tempDirPath); assertTrue(Files.exists(tempDirPath));
        assertEquals(tempDirPath, virtualDir.getTempDir());

        virtualDir.close();
        assertFalse(Files.exists(tempDirPath));
        assertNull(virtualDir.getTempDir());
    }


    private static void createTarWithSingleFile(Path tarPath, String entryName, String content) throws Exception {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        try (OutputStream fos = Files.newOutputStream(tarPath);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(fos)) {
            final Path parentDir = tarPath.getParent();
            assertNotNull(parentDir);

            String normalizedEntry = Objects.requireNonNull(entryName);
            TarArchiveEntry entry = new TarArchiveEntry(normalizedEntry);

            entry.setSize(bytes.length);
            tos.putArchiveEntry(entry);
            tos.write(bytes);
            tos.closeArchiveEntry();
            tos.finish();
        }
    }
}
