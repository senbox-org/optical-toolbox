package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.dataio.netcdf.remote.RemoteFileAccess;

import java.io.File;
import java.util.Locale;

public class SeadasHelper {

    // Set to "true" to output debugging information.
    // Don't forget to set back to "false" in production code!
    //
    private static final boolean DEBUG = false;

    static DecodeQualification checkInputObject(File inputFile) {
        if (inputFile == null) {
            return DecodeQualification.UNABLE;
        }
        if (!inputFile.isFile()) {
            if (DEBUG) {
                System.out.println("# Not a file: " + inputFile);
            }
            return DecodeQualification.UNABLE;
        }

        return DecodeQualification.SUITABLE;
    }

    /**
     * Like {@link #checkInputObject(File)}, but also accepts an http(s) location of a NetCDF/HDF5 file
     * as returned by {@link #getNetcdfInput(Object)}.
     */
    static DecodeQualification checkNetcdfInput(Object netcdfInput) {
        if (netcdfInput instanceof String location) {
            String name = RemoteFileAccess.getFileName(location).toLowerCase(Locale.ROOT);
            boolean netcdfName = name.endsWith(".nc") || name.endsWith(".nc4") || name.endsWith(".h5") || name.endsWith(".hdf5");
            return netcdfName ? DecodeQualification.SUITABLE : DecodeQualification.UNABLE;
        }
        return checkInputObject(netcdfInput instanceof File file ? file : null);
    }

    /**
     * @return the local file, or null for a remote location or an unsupported input type
     */
    static File getInputFile(Object input) {
        if (isRemote(input)) {
            return null;
        }
        File inputFile;
        if (input instanceof File) {
            inputFile = (File) input;
        } else if (input instanceof String) {
            inputFile = new File((String) input);
        } else {
            return null;
        }
        return inputFile;
    }

    /**
     * @return what to pass to {@code NetcdfFileOpener.open}: the trimmed URL for a remote location,
     * otherwise the local file (or null)
     */
    static Object getNetcdfInput(Object input) {
        if (isRemote(input)) {
            return input.toString().trim();
        }
        return getInputFile(input);
    }

    static boolean isRemote(Object input) {
        return RemoteFileAccess.isRemoteLocation(input);
    }
}
