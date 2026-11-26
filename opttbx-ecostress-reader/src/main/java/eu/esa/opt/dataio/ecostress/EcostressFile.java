package eu.esa.opt.dataio.ecostress;

import hdf.object.FileFormat;
import org.esa.snap.core.util.io.FileUtils;

import java.nio.file.Path;

/**
 * Ecostress Product File class
 *
 * @author adraghici
 */
public class EcostressFile implements AutoCloseable {

    /**
     * the HDF5 object
     */
    private final FileFormat h5File;

    /**
     * Creates a new instance of this class
     * @param inputFile the ECOSTRESS file path object
     */
    public EcostressFile(Path inputFile) {
        final Path cachedFile = FileUtils.getCachedFile(inputFile);
        try {
            final FileFormat h5FileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
            this.h5File = h5FileFormat.createInstance(cachedFile.toString(), FileFormat.READ);
        } catch (Exception e) {
            throw new IllegalStateException("Error initialising ECOSTRESS product '" + this.getFileName() + "': " + e.getMessage());
        }
    }

    /**
     * Gets the HDF5 object of ECOSTRESS file
     * @return the HDF5 object of ECOSTRESS file
     */
    public FileFormat getH5File() {
        open();
        return h5File;
    }

    /**
     * Gets the file name of ECOSTRESS file
     * @return the file name of ECOSTRESS file
     */
    public String getFileName() {
        return this.h5File.getAbsoluteFile().toPath().getFileName().toString();
    }

    /**
     * Gets the file path of ECOSTRESS file
     * @return the file path of ECOSTRESS file
     */
    public Path getFileLocation() {
        return this.h5File.getAbsoluteFile().toPath();
    }

    /**
     * Opens the HDF5 object of ECOSTRESS file
     */
    private void open() {
        try {
            if (this.h5File != null) {
                this.h5File.open();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error opening ECOSTRESS product '" + this.getFileName() + "': " + e.getMessage());
        }
    }

    /**
     * Closes the HDF5 object of ECOSTRESS file
     * @throws Exception if an error occurs
     */
    @Override
    public void close() throws Exception {
        if (this.h5File != null) {
            this.h5File.close();
        }
    }
}
