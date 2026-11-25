package eu.esa.opt.dataio;

import com.bc.ceres.core.VirtualDir;
import com.bc.ceres.core.runtime.internal.IOHelper;
import com.bc.ceres.util.CleanUpState;
import com.bc.ceres.util.CleanerRegistry;
import eu.esa.opt.commons.FilePath;
import eu.esa.opt.commons.FilePathInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of a virtual directory representing the contents of a tar or a tar-gz file.
 * @deprecated since 10.0.0, use {@link org.esa.snap.engine_utilities.dataio.VirtualDirTgz} instead
 */
public class VirtualDirTgz extends VirtualDirEx {

    private static final int TRANSFER_BUFFER_SIZE = 1024 * 1024;

    private final Path archiveFile;
    private final VirtualDirTgzState state;

    public VirtualDirTgz(Path tgz) {
        if (tgz == null) {
            throw new IllegalArgumentException("Input file shall not be null");
        }
        this.archiveFile = tgz;
        this.state = new VirtualDirTgzState();
        CleanerRegistry.getInstance().register(this, state);
    }

    public static String getFilenameFromPath(String path) {
        int lastSepIndex = path.lastIndexOf("/");
        if (lastSepIndex == -1) {
            lastSepIndex = path.lastIndexOf("\\");
            if (lastSepIndex == -1) {
                return path;
            }
        }

        return path.substring(lastSepIndex + 1);
    }

    public static boolean isTgz(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".tgz") || lowerCaseFilename.endsWith(".tar.gz");
    }

    public static boolean isTbz(String filename) {
        String lcName = filename.toLowerCase();
        return lcName.endsWith(".tar.bz") || lcName.endsWith(".tbz") ||
                lcName.endsWith(".tar.bz2") || lcName.endsWith(".tbz2");
    }

    public static boolean isTar(String filename) {
        return ".tar".equals(FileUtils.getExtension(filename));
    }

    @Override
    public String getBasePath() {
        return this.archiveFile.toString();
    }

    @Override
    public Path getBaseFile() {
        return this.archiveFile;
    }

    @Override
    public Path buildPath(String first, String... more) {
        FileSystem fileSystem = this.archiveFile.getFileSystem();
        return fileSystem.getPath(first, more);
    }

    @Override
    public String getFileSystemSeparator() {
        FileSystem fileSystem = this.archiveFile.getFileSystem();
        return fileSystem.getSeparator();
    }

    @Override
    public FilePathInputStream getInputStream(String childRelativePath) throws IOException {
        Path file = getFile(childRelativePath);
        InputStream inputStream = Files.newInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        return new FilePathInputStream(file, bufferedInputStream, null);
    }

    @Override
    public FilePath getFilePath(String childRelativePath) throws IOException {
        Path file = getFile(childRelativePath);
        return new FilePath(file, null);
    }

    @Override
    public Path getFile(String childRelativePath) throws IOException {
        ensureUnpacked(null);
        final Path file = state.extractDir.resolve(childRelativePath);
        if (!(Files.isRegularFile(file) || Files.isDirectory(file))) {
            throw new FileNotFoundException("The path '"+childRelativePath+"' does not exist in the folder '"+state.extractDir.toAbsolutePath()+"'.");
        }
        return file;
    }

    @Override
    public String[] list(String path) throws IOException {
        final Path file = getFile(path);
        return IOHelper.list(file).toArray(new String[0]);
    }

    @Override
    public String[] listAllFiles() throws IOException {
        try (TarArchiveInputStream tarStream = buildTarInputStream()) {

            TarArchiveEntry entry;
            List<String> entryNames = new ArrayList<>();
            while ((entry = tarStream.getNextTarEntry()) != null) {
                if (!entry.isDirectory()) {
                    entryNames.add(entry.getName());
                }
            }
            return entryNames.toArray(new String[0]);
        }
    }

    public boolean exists(String s) {
        return Files.exists(this.archiveFile);
    }

    @Override
    public void close() {
        CleanerRegistry.getInstance().cleanup(this);
    }

    @Override
    public boolean isCompressed() {
        final String fileName = this.archiveFile.getFileName().toString();
        return isTgz(fileName) || isTbz(fileName);
    }

    @Override
    public boolean isArchive() {
        return true;
    }


    @Override
    public Path getTempDir() {
        return state.extractDir;
    }

    @Override
    public Path makeLocalTempFolder() throws IOException {
        if (state.extractDir == null) {
            state.extractDir = VirtualDir.createUniqueTempDir();
        }
        return state.extractDir;
    }

    public void ensureUnpacked(Path unpackFolder) throws IOException {
        if (state.extractDir == null) {
            state.extractDir = (unpackFolder == null) ? VirtualDir.createUniqueTempDir() : unpackFolder;

            try (TarArchiveInputStream tarStream = buildTarInputStream()) {
                byte[] data = new byte[TRANSFER_BUFFER_SIZE];
                TarArchiveEntry entry;
                String longLink = null;
                while ((entry = tarStream.getNextTarEntry()) != null) {
                    String entryName = entry.getName();
                    boolean entryIsLink = entry.isLink() || entry.isSymbolicLink();
                    if (longLink != null && longLink.startsWith(entryName)) {
                        entryName = longLink;
                        longLink = null;
                    }
                    if (entry.isDirectory()) {
                        final Path directory = state.extractDir.resolve(entryName);
                        ensureDirectory(directory);
                        continue;
                    }

                    final String fileNameFromPath = getFilenameFromPath(entryName);
                    final int pathIndex = entryName.indexOf(fileNameFromPath);
                    String tarPath = null;
                    if (pathIndex > 0) {
                        tarPath = entryName.substring(0, pathIndex - 1);
                    }

                    final Path targetDir;
                    if (tarPath == null) {
                        targetDir = state.extractDir;
                    } else {
                        targetDir = state.extractDir.resolve(tarPath);
                    }

                    ensureDirectory(targetDir);
                    final Path targetFile = targetDir.resolve(fileNameFromPath);
                    if (!entryIsLink && Files.isRegularFile(targetFile)) {
                        continue;
                    }

                    if (!entryIsLink && !IOHelper.createFile(targetFile)) {
                        throw new IOException("Unable to create file: " + targetFile.toAbsolutePath());
                    }

                    try (OutputStream fileOutputStream = ProductUtils.getProductOutputStream(targetFile);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                        int count;
                        while ((count = tarStream.read(data)) != -1) {
                            bufferedOutputStream.write(data, 0, count);
                            //if the entry is a link, must be saved, since the name of the next entry depends on this
                            if (entryIsLink) {
                                longLink = (longLink == null ? "" : longLink) + new String(data, 0, count);
                            } else {
                                longLink = null;
                            }
                        }
                        // the last character is \u0000, so it must be removed
                        if (longLink != null) {
                            longLink = longLink.substring(0, longLink.length() - 1);
                        }
                        bufferedOutputStream.flush();
                    }
                }
            }
        }
    }

    private TarArchiveInputStream buildTarInputStream() throws IOException {
        final CompressorStreamFactory compressorStreamFactory = new CompressorStreamFactory();
        CompressorInputStream compressorInputStream = null;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(Files.newInputStream(this.archiveFile));
            compressorInputStream = compressorStreamFactory.createCompressorInputStream(stream);
            return new TarArchiveInputStream(compressorInputStream);
        } catch (IOException | CompressorException | IllegalArgumentException ex) {
            if (stream == null) {
                throw new IOException("Cannot open file");
            }
            // maybe it's just a simple tar
            return new TarArchiveInputStream(stream);
        }
    }

    private void ensureDirectory(Path targetDir) throws IOException {
        if (!Files.isDirectory(targetDir)) {
            if (!IOHelper.createDirectories(targetDir)) {
                throw new IOException("unable to create directory: " + targetDir.toAbsolutePath());
            }
        }
    }

    @Override
    public String[] listAll(Pattern...patterns) {
        List<String> fileNames;
        try (TarArchiveInputStream tis = buildTarInputStream()) {

            fileNames = new ArrayList<>();
            byte[] data = new byte[TRANSFER_BUFFER_SIZE];
            TarArchiveEntry entry;
            String longLink = null;
            while ((entry = tis.getNextTarEntry()) != null) {
                String entryName = entry.getName();
                boolean entryIsLink = entry.isLink() || entry.isSymbolicLink();
                if (longLink != null && longLink.startsWith(entryName)) {
                    entryName = longLink;
                    longLink = null;
                }
                // if the entry is a link, must be saved, since the name of the next entry depends on this
                if (entryIsLink) {
                    int count;
                    while ((count = tis.read(data)) != -1) {
                        longLink = (longLink == null ? "" : longLink) + new String(data, 0, count);
                    }
                } else {
                    longLink = null;
                    fileNames.add(entryName);
                }
                // the last character is \u0000, so it must be removed
                if (longLink != null) {
                    longLink = longLink.substring(0, longLink.length() - 1);
                }
            }
        } catch (IOException e) {
            // cannot open/read tar, list will be empty
            fileNames = new ArrayList<>();
        }
        return fileNames.toArray(new String[0]);
    }


    private static final class VirtualDirTgzState implements CleanUpState {
        private Path extractDir;

        @Override
        public synchronized void run() {
            if (extractDir != null) {
                FileUtils.deleteTree(extractDir);
                extractDir = null;
            }
        }
    }
}
