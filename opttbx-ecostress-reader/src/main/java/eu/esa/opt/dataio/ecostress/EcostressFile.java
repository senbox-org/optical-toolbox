package eu.esa.opt.dataio.ecostress;

import ncsa.hdf.object.FileFormat;

import java.io.File;

/**
 * Ecostress Product File class
 *
 * @author adraghici
 */
public class EcostressFile extends File implements AutoCloseable {

    /**
     * the HDF5 object
     */
    private final FileFormat h5File;

    /**
     * Creates a new instance of this class
     * @param inputFilePath the ECOSTRESS file path string
     */
    public EcostressFile(String inputFilePath) {
        super(inputFilePath);
        try {
            final FileFormat h5FileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
            this.h5File = h5FileFormat.createInstance(this.getAbsolutePath(), FileFormat.READ);
        } catch (Exception e) {
            throw new IllegalStateException("Error initialising ECOSTRESS product '" + this.getName() + "': " + e.getMessage());
        }
    }

    /**
     * Creates a new instance of this class
     * @param inputFile the ECOSTRESS file path object
     */
    public EcostressFile(File inputFile) {
        this(inputFile.getAbsolutePath());
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
     * Opens the HDF5 object of ECOSTRESS file
     */
    private void open() {
        try {
            if (this.h5File != null) {
                this.h5File.open();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error opening ECOSTRESS product '" + this.getName() + "': " + e.getMessage());
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
