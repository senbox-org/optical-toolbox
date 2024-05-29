package eu.esa.opt.dataio;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TarUtils {

    public static void decompress(Path archive, Path targetFolder, boolean deleteAfterDecompress) throws IOException {
        if (archive == null || !archive.toString().endsWith(".tar")
                || !archive.toString().endsWith(".tar.gz") || !archive.toString().endsWith(".tgz")) {
            throw new IOException("Not a tar/tgz file!");
        }
        final CompressorStreamFactory compressorStreamFactory = new CompressorStreamFactory();
        Files.createDirectories(targetFolder);
        try (InputStream is = Files.newInputStream(archive);
             TarArchiveInputStream tarStream = new TarArchiveInputStream(compressorStreamFactory.createCompressorInputStream(is))) {
            TarArchiveEntry entry;
            while ((entry = tarStream.getNextTarEntry()) != null) {
                final Path currentPath = targetFolder.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(currentPath);
                } else {
                    int count;
                    final byte[] buffer = new byte[65536];
                    Files.createDirectories(currentPath.getParent());
                    try (OutputStream outputStream = Files.newOutputStream(currentPath)) {
                        while ((count = tarStream.read(buffer, 0, buffer.length)) != -1) {
                            outputStream.write(buffer, 0, count);
                        }
                    }
                }
            }
        } catch (CompressorException e) {
            throw new IOException(e);
        }
        if (deleteAfterDecompress) {
            Files.delete(archive);
        }
    }

    public static boolean isTar(Path path) {
        final String lcFileName = path.getFileName().toString().toLowerCase();
        return lcFileName.endsWith("tar.gz") || lcFileName.endsWith("tgz") || lcFileName.endsWith("tar");
    }
}
