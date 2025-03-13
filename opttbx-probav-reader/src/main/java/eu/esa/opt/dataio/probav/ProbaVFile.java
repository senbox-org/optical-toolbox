package eu.esa.opt.dataio.probav;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.object.FileFormat;

import java.io.File;

/**
 * ProbaV Product File class
 *
 * @author adraghici
 */
public class ProbaVFile extends File implements AutoCloseable {

    /**
     * the HDF5 object
     */
    private final FileFormat h5File;

    /**
     * Creates a new instance of this class
     * @param inputFilePath the ProbaV file path string
     */
    public ProbaVFile(String inputFilePath) {
        super(inputFilePath);
        try {
            final FileFormat h5FileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
            this.h5File = h5FileFormat.createInstance(this.getAbsolutePath(), FileFormat.READ);
        } catch (Exception e) {
            throw new IllegalStateException("Error initialising ProbaV product '" + this.getName() + "': " + e.getMessage());
        }
    }

    /**
     * Creates a new instance of this class
     * @param inputFile the ProbaV file path object
     */
    public ProbaVFile(File inputFile) {
        this(inputFile.getAbsolutePath());
    }

    /**
     * Gets the HDF5 object of ProbaV file
     * @return the HDF5 object of ProbaV file
     */
    public FileFormat getH5File() {
        open();
        return h5File;
    }

    public long getFileId() throws Exception {
        return H5.H5Fopen(h5File.getAbsolutePath(),   // Name of the file to access.
                HDF5Constants.H5F_ACC_RDONLY,  // File access flag
                HDF5Constants.H5P_DEFAULT);
    }

    /**
     * Opens the HDF5 object of ProbaV file
     */
    private void open() {
        try {
            if (this.h5File != null) {
                this.h5File.open();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error opening ProbaV product '" + this.getName() + "': " + e.getMessage());
        }
    }

    /**
     * Closes the HDF5 object of ProbaV file
     * @throws Exception if an error occurs
     */
    @Override
    public void close() throws Exception {
        if (this.h5File != null) {
            this.h5File.close();
        }
    }
}
