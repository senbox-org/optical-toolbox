package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
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

public class L1CPaceProductReaderPlugIn implements ProductReaderPlugIn {

    private static final String DEFAULT_FILE_EXTENSION = ".nc";

    public static final String READER_DESCRIPTION = "PACE OCI L1C Products";
    public static final String FORMAT_NAME = "PaceOCI_L1C";

    static {
        L1CPaceRgbProfiles.registerRGBProfiles();
    }

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     */
    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final File inputFile = SeadasHelper.getInputFile(input);

        final DecodeQualification decodeQualification = SeadasHelper.checkInputObject(inputFile);
        if (decodeQualification == DecodeQualification.UNABLE) {
            return decodeQualification;
        }

        H5iosp.setDebugFlags(new DebugFlagsImpl("HdfEos/turnOff"));

        try (NetcdfFile ncfile = NetcdfFileOpener.open(inputFile.getPath())) {
            if (ncfile != null) {
                Attribute instrument = ncfile.findGlobalAttributeIgnoreCase("instrument");
                Attribute processing_level = ncfile.findGlobalAttributeIgnoreCase("processing_level");

                if (processing_level != null && instrument != null) {
                    if (processing_level.toString().toUpperCase().contains("1C") &&
                            (instrument.toString().toUpperCase().contains("OCI")
                                    || instrument.toString().toUpperCase().contains("HARP")
                                    || instrument.toString().toUpperCase().contains("SPEXONE"))) {
                        Debug.trace(inputFile.toString());
                        ncfile.close();
                        DebugFlags debugFlags = new DebugFlagsImpl("HdfEos/turnOff");
                        debugFlags.set("HdfEos/turnOff", false);
                        H5iosp.setDebugFlags(debugFlags);
                        return DecodeQualification.INTENDED;
                    } else {
                        Debug.trace("# Unrecognized instrument =[" + instrument + "]: " + inputFile);
                    }
                } else {
                    Debug.trace("# Missing processing_level or instrument attribute': " + inputFile);
                }
            } else {
                Debug.trace("# Can't open as NetCDF: " + inputFile);
            }
        } catch (Exception ignore) {
            Debug.trace("# I/O exception caught: " + inputFile);
        } finally {
            DebugFlags debugFlags = new DebugFlagsImpl("HdfEos/turnOff");
            debugFlags.set("HdfEos/turnOff", false);
            H5iosp.setDebugFlags(debugFlags);
        }
        return DecodeQualification.UNABLE;
    }

    /**
     * Returns an array containing the classes that represent valid input types for this reader.
     * <p> Intances of the classes returned in this array are valid objects for the <code>setInput</code> method of the
     * <code>ProductReader</code> interface (the method will not throw an <code>InvalidArgumentException</code> in this
     * case).
     *
     * @return an array containing valid input types, never <code>null</code>
     */
    @Override
    public Class[] getInputTypes() {
        return new Class[]{String.class, File.class};
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
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
     * always include a leading colon ('.') character, e.g. <code>".hdf"</code>
     *
     * @return the default file extensions for this product I/O plug-in, never <code>null</code>
     */
    @Override
    public String[] getDefaultFileExtensions() {
        // todo: return regular expression to clean up the extensions.
        return new String[]{
                DEFAULT_FILE_EXTENSION
        };
    }

    /**
     * Gets a short description of this plug-in. If the given locale is set to <code>null</code> the default locale is
     * used.
     * <p/>
     * <p> In a GUI, the description returned could be used as tool-tip text.
     *
     * @param locale the local for the given decription string, if <code>null</code> the default locale is used
     * @return a textual description of this product reader/writer
     */
    @Override
    public String getDescription(Locale locale) {
        return READER_DESCRIPTION;
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never <code>null</code>
     */
    @Override
    public String[] getFormatNames() {
        return new String[]{FORMAT_NAME};
    }
}
