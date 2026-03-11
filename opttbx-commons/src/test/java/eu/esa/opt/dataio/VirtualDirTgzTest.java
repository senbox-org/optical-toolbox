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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * @deprecated since 10.0.0, use {@link org.esa.snap.engine_utilities.dataio.VirtualDirTgzTest} instead
 */
public class VirtualDirTgzTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


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
        File tarFile = tempFolder.newFile("test.tar");
        createTarWithSingleFile(tarFile.toPath(), "folder/a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile.toPath());
        virtualDir.ensureUnpacked(null);
        File extractDir = virtualDir.getTempDir();

        assertNotNull(extractDir);
        assertTrue(extractDir.exists());
        assertTrue(new File(extractDir, "folder/a.txt").isFile());

        virtualDir.close();
        assertFalse(extractDir.exists());
        assertNull(virtualDir.getTempDir());
    }

    @Test
    @STTM("SNAP-4105")
    public void test_closeIsIdempotent() throws Exception {
        File tarFile = tempFolder.newFile("test.tar");
        createTarWithSingleFile(tarFile.toPath(), "a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile.toPath());
        virtualDir.ensureUnpacked(null); File extractDir = virtualDir.getTempDir();

        assertNotNull(extractDir);
        assertTrue(extractDir.exists());
        virtualDir.close();
        virtualDir.close();

        assertFalse(extractDir.exists());
        assertNull(virtualDir.getTempDir());
    }

    @Test
    @STTM("SNAP-4105")
    public void test_closeWithoutUnpackDoesNothing() throws Exception {
        File tarFile = tempFolder.newFile("test.tar");
        createTarWithSingleFile(tarFile.toPath(), "a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile.toPath());

        assertNull(virtualDir.getTempDir()); virtualDir.close();
        assertNull(virtualDir.getTempDir());
    }

    @Test
    @STTM("SNAP-4105")
    public void test_makeLocalTempFolderThenCloseDeletesTempDir() throws Exception {
        File tarFile = tempFolder.newFile("test.tar");
        createTarWithSingleFile(tarFile.toPath(), "a.txt", "hello");
        VirtualDirTgz virtualDir = new VirtualDirTgz(tarFile.toPath());
        Path tempDirPath = virtualDir.makeLocalTempFolder();

        assertNotNull(tempDirPath); assertTrue(tempDirPath.toFile().exists());
        assertEquals(tempDirPath.toFile(), virtualDir.getTempDir());

        virtualDir.close();
        assertFalse(tempDirPath.toFile().exists());
        assertNull(virtualDir.getTempDir());
    }


    private static void createTarWithSingleFile(Path tarPath, String entryName, String content) throws Exception {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        try (OutputStream fos = new FileOutputStream(tarPath.toFile());
             TarArchiveOutputStream tos = new TarArchiveOutputStream(fos)) {
            File parentDir = tarPath.getParent().toFile();
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
