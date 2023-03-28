/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class L2ProductReaderPlugIn implements ProductReaderPlugIn {

    // Set to "true" to output debugging information.
    // Don't forget to setback to "false" in production code!
    //
    private static final boolean DEBUG = false;

    private static final String DEFAULT_FILE_EXTENSION = ".nc";

    private static final String DEFAULT_FILE_EXTENSION_L2 = ".L2";
    private static final String DEFAULT_FILE_EXTENSION_L2_GAC = DEFAULT_FILE_EXTENSION_L2 + "_GAC";
    private static final String DEFAULT_FILE_EXTENSION_L2_GAC_OC = DEFAULT_FILE_EXTENSION_L2_GAC + "_OC";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC = DEFAULT_FILE_EXTENSION_L2 + "_LAC";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC_OC = DEFAULT_FILE_EXTENSION_L2_LAC + "_OC";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC_SST = DEFAULT_FILE_EXTENSION_L2_LAC + "_SST";
    private static final String DEFAULT_FILE_EXTENSION_L2_LAC_SST4 = DEFAULT_FILE_EXTENSION_L2_LAC + "_SST4";
    private static final String DEFAULT_FILE_EXTENSION_L2_MLAC = DEFAULT_FILE_EXTENSION_L2 + "_MLAC";
    private static final String DEFAULT_FILE_EXTENSION_L2_MLAC_OC = DEFAULT_FILE_EXTENSION_L2_MLAC + "_OC";

    public static final String READER_DESCRIPTION = "SeaDAS-Supported Level 2 Products";
    public static final String FORMAT_NAME = "SeaDAS-L2";

    private static final String[] supportedProductTypes = {
            "AVHRR Level-2 Data",
            "CZCS Level-2 Data",
            "HMODISA Level-2 Data",
            "HMODIST Level-2 Data",
            "MERIS Level-2 Data",
            "MODISA Level-2 Data",
            "MODIST Level-2 Data",
            "MOS Level-2 Data",
            "OSMI Level-2 Data",
            "OCTS Level-2 Data",
            "SeaWiFS Level-2 Data",
            "VIIRSN Level-2 Data",
            "VIIRSJ1 Level-2 Data",
            "OCM2 Level-2 Data",
            "OCM Level-2 Data",
            "HICO Level-2 Data",
            "GOCI Level-2 Data",
            "OLI Level-2 Data",
            "OLCI Level-2 Data",
    };
    private static final Set<String> supportedProductTypeSet = new HashSet<String>(Arrays.asList(supportedProductTypes));


    static{
        registerRGBProfiles();
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
            if (DEBUG) {
                System.out.println("# File not found: " + file);
            }
            return DecodeQualification.UNABLE;
        }
        if (!file.isFile()) {
            if (DEBUG) {
                System.out.println("# Not a file: " + file);
            }
            return DecodeQualification.UNABLE;
        }
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFileOpener.open(file.getPath());
            if (ncfile != null) {
                String titleattr = "title";
                Attribute titleAttribute = ncfile.findGlobalAttributeIgnoreCase(titleattr);

                if (titleAttribute != null) {
                    Attribute instrumentAttribute = ncfile.findGlobalAttribute("instrument");
                    Attribute platformAttribute = ncfile.findGlobalAttribute("platform");
                    Attribute levelAttribute = ncfile.findGlobalAttribute("processing_level");
                    if(instrumentAttribute != null && platformAttribute != null  && levelAttribute != null) {
                        final String level = levelAttribute.getStringValue();
                        if(level.equals("L2")) {
                            if (DEBUG) {
                                System.out.println(file);
                            }
                            ncfile.close();
                            return DecodeQualification.INTENDED;
                        } else {
                            ncfile.close();
                            return DecodeQualification.UNABLE;
                        }

                    } else {
                    final String title = titleAttribute.getStringValue();
                    if (title != null) {
                        if (supportedProductTypeSet.contains(title.trim())) {
                            if (DEBUG) {
                                System.out.println(file);
                            }
                            ncfile.close();
                            return DecodeQualification.INTENDED;
                        } else {
                            if (DEBUG) {
                                System.out.println("# Unrecognized attribute Title=[" + title + "]: " + file);
                            }
                        }
                    }
                    }
                } else {
                    if (DEBUG) {
                        System.out.println("# Missing attribute 'Title': " + file);
                    }
                }
            } else {
                if (DEBUG) {
                    System.out.println("# Can't open as NetCDF: " + file);
                }
            }
        } catch (Exception ignore) {
            if (DEBUG) {
                System.out.println("# I/O exception caught: " + file);
            }
        } finally {
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
     * <p/>
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
                DEFAULT_FILE_EXTENSION,
                DEFAULT_FILE_EXTENSION_L2_GAC_OC,
                DEFAULT_FILE_EXTENSION_L2_LAC_OC,
                DEFAULT_FILE_EXTENSION_L2_LAC_SST,
                DEFAULT_FILE_EXTENSION_L2_LAC_SST4,
                DEFAULT_FILE_EXTENSION_L2_MLAC_OC
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

    private static void registerRGBProfiles() {
        RGBImageProfileManager manager = RGBImageProfileManager.getInstance();
        manager.addProfile(new RGBImageProfile("NASA SeaWiFS L2",
                                               new String[]{
                                                       "Rrs_670",
                                                       "Rrs_510",
                                                       "Rrs_443"
                                               }));
        manager.addProfile(new RGBImageProfile("NASA MODIS L2",
                                               new String[]{
                                                       "Rrs_667",
                                                       "Rrs_531",
                                                       "Rrs_443"
                                               }));
        manager.addProfile(new RGBImageProfile("NASA OCTS/CZCS L2",
                                               new String[]{
                                                       "Rrs_670",
                                                       "Rrs_520",
                                                       "Rrs_443"
                                               }));
        manager.addProfile(new RGBImageProfile("NASA SeaWiFS L2 Tristimulus",
                                               new String[]{
                                                       "log(1 + 0.2*nLw_443 + 1.1*nLw_670)",
                                                       "log(1 + 0.2*nLw_443 + 0.4*nLw_510 + 0.4*nLw_555)",
                                                       "log(1 + 1.85*nLw_443)"
                                               }));
        manager.addProfile(new RGBImageProfile("NASA MODIS L2 Tristimulus",
                                               new String[]{
                                                       "log(1 + 0.2*nLw_443 + 1.1*nLw_670)",
                                                       "log(1 + 0.2*nLw_443 + 0.4*nLw_531 + 0.4*nLw_551)",
                                                       "log(1 + 1.85*nLw_443)"
                                               }));
        manager.addProfile(new RGBImageProfile("NASA OCTS L2 Tristimulus",
                                               new String[]{
                                                       "log(1 + 0.2*nLw_443 + 1.1*nLw_670)",
                                                       "log(1 + 0.2*nLw_443 + 0.4*nLw_520 + 0.4*nLw_565)",
                                                       "log(1 + 1.85*nLw_443)"
                                               }));
        manager.addProfile(new RGBImageProfile("NASA CZCS L2 Tristimulus",
                                               new String[]{
                                                       "log(1 + 0.2*nLw_443 + 1.1*nLw_670)",
                                                       "log(1 + 0.2*nLw_443 + 0.4*nLw_520 + 0.4*nLw_550)",
                                                       "log(1 + 1.85*nLw_443)"
                                               }));
    }
}