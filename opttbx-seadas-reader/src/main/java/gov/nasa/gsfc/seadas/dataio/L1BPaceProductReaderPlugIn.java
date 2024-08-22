package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.util.Debug;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.dataio.netcdf.GenericNetCdfReaderPlugIn;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.iosp.hdf5.H5iosp;
import ucar.nc2.util.DebugFlags;
import ucar.nc2.util.DebugFlagsImpl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class L1BPaceProductReaderPlugIn extends GenericNetCdfReaderPlugIn {

    private static final String DEFAULT_FILE_EXTENSION = ".nc";

    public static final String READER_DESCRIPTION = "PACE OCI L1B Products";
    public static final String FORMAT_NAME = "PaceOCI_L1B";

    static {
        L1BPaceRgbProfiles.registerRGBProfiles();
    }

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     */
    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final File file = SeadasProductReader.getInputFile(input);
        if (file == null) {
            return DecodeQualification.UNABLE;
        }
        if (!file.exists()) {
            Debug.trace("# File not found: " + file);
            return DecodeQualification.UNABLE;
        }
        if (!file.isFile()) {
            Debug.trace("# Not a file: " + file);
            return DecodeQualification.UNABLE;
        }
        NetcdfFile ncfile = null;
        H5iosp.setDebugFlags(new DebugFlagsImpl("HdfEos/turnOff"));

        try {
            ncfile = NetcdfFileOpener.open(file.getPath());
            if (ncfile != null) {
                Attribute scene_title = ncfile.findGlobalAttribute("title");

                if (scene_title != null) {
                    if (scene_title.toString().contains("PACE OCIS Level-1B Data") ||
                            scene_title.toString().contains("PACE OCI Level-1B Data")) {
                        Debug.trace(file.toString());
                        ncfile.close();
                        DebugFlags debugFlags = new DebugFlagsImpl("HdfEos/turnOff");
                        debugFlags.set("HdfEos/turnOff", false);
                        H5iosp.setDebugFlags(debugFlags);
                        return DecodeQualification.INTENDED;
                    } else {
                        Debug.trace("# Unrecognized scene title =[" + scene_title + "]: " + file);
                    }
                } else {
                    Debug.trace("# Missing scene title attribute': " + file);
                }
            } else {
                Debug.trace("# Can't open as NetCDF: " + file);
            }
        } catch (Exception ignore) {
            Debug.trace("# I/O exception caught: " + file);
        } finally {
            DebugFlags debugFlags = new DebugFlagsImpl("HdfEos/turnOff");
            debugFlags.set("HdfEos/turnOff", false);
            H5iosp.setDebugFlags(debugFlags);
            if (ncfile != null) {
                try {
                    ncfile.close();
                } catch (IOException ignore) {
                }
            }
        }
        return DecodeQualification.UNABLE;
    }

    /**
     * Returns an array containing the classes that represent valid input types for this reader.
     * <p> Intances of the classes returned in this array are valid objects for the {@code setInput} method of the
     * <code>ProductReader</code> interface (the method will not throw an {@code InvalidArgumentException} in this
     * case).
     *
     * @return an array containing valid input types, never {@code null}
     */
    @Override
    public Class[] getInputTypes() {
        return new Class[]{String.class, File.class};
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return {@code null}.
     *
     * @return a new reader instance, never {@code null}
     */
    @Override
    public ProductReader createReaderInstance() {
        return new SeadasProductReader(this);
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        String[] formatNames = getFormatNames();
        String formatName = "";
        if (formatNames.length > 0) {
            formatName = formatNames[0];
        }
        return new SnapFileFilter(formatName, getDefaultFileExtensions(), getDescription(null));
    }

    /**
     * Gets the default file extensions associated with each of the format names returned by the <code>{@link
     * #getFormatNames}</code> method. <p>The string array returned shall always have the same length as the array
     * returned by the <code>{@link #getFormatNames}</code> method. <p>The extensions returned in the string array shall
     * always include a leading colon ('.') character, e.g. {@code ".hdf"}
     *
     * @return the default file extensions for this product I/O plug-in, never {@code null}
     */
    @Override
    public String[] getDefaultFileExtensions() {
        // todo: return regular expression to clean up the extensions.
        return new String[]{
                DEFAULT_FILE_EXTENSION
        };
    }

    /**
     * Gets a short description of this plug-in. If the given locale is set to {@code null} the default locale is
     * used.
     * <p> In a GUI, the description returned could be used as tool-tip text.
     *
     * @param locale the local for the given decription string, if {@code null} the default locale is used
     * @return a textual description of this product reader/writer
     */
    @Override
    public String getDescription(Locale locale) {
        return READER_DESCRIPTION;
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never {@code null}
     */
    @Override
    public String[] getFormatNames() {
        return new String[]{FORMAT_NAME};
    }
}
