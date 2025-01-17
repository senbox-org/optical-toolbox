/*
 * Copyright (C) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;


// import org.opengis.filter.spatial.Equals;

public class SeadasProductReader extends AbstractProductReader {

    private NetcdfFile ncfile;
    private ProductType productType;
    private SeadasFileReader seadasFileReader;

    enum Mission {
        OCI("OCI");

        private String name;
        private Mission(String nm) {
            name = nm;
        }
        public String toString() {
            return name;
        }
    }

    enum ProcessingLevel {
        L1A("L1A"),
        L1B("L1B"),
        L1C("L1C"),
        L2("L2"),
        L3b("L3 Binned"),
        L3m("L3 Mapped");

        private String name;
        private ProcessingLevel(String nm) {
            name = nm;
        }
        public String toString() {
            return name;
        }
    }



    enum ProductType {
        ANCNRT("SeaWiFS Near Real-Time Ancillary Data"),
        ANCNRT2("NCEP Reanalysis 2 Ancillary Data"),
        ANCCLIM("SeaWiFS Climatological Ancillary Data"),
        Bathy("Bathymetry"),
        BrowseFile("Browse Product"),
        Level1A_Aquarius("Aquarius Level 1A"),
        Level2_Aquarius("Aquarius Level 2"),
        Level1A_CZCS("CZCS Level 1A"),
        Level2_CZCS("Level 2"),
        Level1A_Hawkeye("Hawkeye Level 1A"),
        Level1A_OCTS("OCTS Level 1A"),
        Level1A_Seawifs("SeaWiFS Level 1A"),
        Level1B("Generic Level 1B"),
        Level1B_HICO("HICO L1B"),
        Level1B_PACE("PACE L1B"),
        Level1B_Modis("MODIS Level 1B"),
        Level1B_OCM2("OCM2_L1B"),
        Level1B_PaceOCI("PaceOCI_L1B"),
        Level1C_Pace("Pace_L1C"),
        Level2("Level 2"),
        Level2_DscovrEpic("DscovrEpic Level 2"),
        Level2_PaceOCI("OCI Level-2"),
        Level2_PaceOCIS("OCIS Level-2"),
        Level3_Bin("Level 3 Binned"),
        Level3_NSIDC_CDR("Level 3 NSIDC CDR"),
        MEaSUREs("MEaSUREs Mapped"),
        MEaSUREs_Bin("MEaSUREs Binned"),
        OISST("Daily-OI"),
        SeadasMapped("SeaDAS Mapped"),
        SMI("Level 3 Mapped"),
        Level3_SeadasMapped("Level 3 Mapped"),
        VIIRS_IP("VIIRS IP"),
        VIIRS_SDR("VIIRS SDR"),
        VIIRS_EDR("VIIRS EDR"),
        VIIRS_GEO("VIIRS GEO"),
        VIIRS_L1B("VIIRS L1B"),
        UNKNOWN("WHATUTALKINBOUTWILLIS");


        private final String name;

        ProductType(String nm) {
            name = nm;
        }

        public String toString() {
            return name;
        }
    }


    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected SeadasProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {

        try {
//            Product product;
            final File inFile = SeadasHelper.getInputFile(getInput());
            final String path = inFile.getPath();

            ncfile = NetcdfFileOpener.open(path);
            productType = findProductType();

            switch (productType) {
                case Level1A_Aquarius:
                case Level2_Aquarius:
                    seadasFileReader = new AquariusL2FileReader(this);
                    break;
                case Level2:
                case Level1B:
                case Level1A_CZCS:
                case Level2_CZCS:
                case Level2_PaceOCI:
                case Level2_PaceOCIS:
                    seadasFileReader = new L2FileReader(this);
                    break;
                case Level2_DscovrEpic:
                    seadasFileReader = new L2DscovrEpicFileReader(this);
                    break;
                case Level1A_Hawkeye:
                    seadasFileReader = new L1AHawkeyeFileReader(this);
                    break;
                case Level1A_OCTS:
                    seadasFileReader = new L1AOctsFileReader(this);
                    break;
                case Level1A_Seawifs:
                    seadasFileReader = new L1ASeawifsFileReader(this);
                    break;
                case Level1B_Modis:
                    seadasFileReader = new L1BModisFileReader(this);
                    break;
                case Level1B_HICO:
                    seadasFileReader = new L1BHicoFileReader(this);
                    break;
                case Level1B_OCM2:
                    seadasFileReader = new L1BOcm2FileReader(this);
                    break;
                case Level1B_PaceOCI:
                    seadasFileReader = new L1BPaceOciFileReader(this);
                    break;
                case Level1C_Pace:
                    seadasFileReader = new L1CPaceFileReader(this);
                    break;
                case Level3_Bin:
                    seadasFileReader = new L3BinFileReader(this);
                    break;
                case MEaSUREs_Bin:
                    seadasFileReader = new MeasuresL3BinFileReader(this);
                    break;
                case Level3_NSIDC_CDR:
                    seadasFileReader = new NSIDCSeaIceFileReader(this);
                    break;
                case BrowseFile:
                    seadasFileReader = new BrowseProductReader(this);
                    break;
                case SMI:
                case ANCNRT:
                case ANCNRT2:
                case ANCCLIM:
                case OISST:
                case Bathy:
                case MEaSUREs:
                    seadasFileReader = new SMIFileReader(this);
                    break;
                case Level3_SeadasMapped:
                    seadasFileReader = new Level3_SeadasMappedFileReader(this);
                    break;
                case SeadasMapped:
                    seadasFileReader = new SeadasMappedFileReader(this);
                    break;
                case VIIRS_IP:
                case VIIRS_SDR:
                case VIIRS_EDR:
                case VIIRS_GEO:
                    seadasFileReader = new ViirsXDRFileReader(this);
                    break;
                case VIIRS_L1B:
                    seadasFileReader = new L1BViirsFileReader(this);
                    break;
                case UNKNOWN:
                    throw new IOException("Unrecognized product type");
                default:
                    throw new IOException("Unrecognized product type");

            }

            Product product = seadasFileReader.createProduct();

            configurePreferredTileSize(product);
            return product;

        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        if (ncfile != null) {
            ncfile.close();
        }
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {


        try {
            seadasFileReader.readBandData(destBand, sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight,
                    sourceStepX, sourceStepY, destBuffer, pm);
        } catch (Exception e) {
            final ProductIOException exception = new ProductIOException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
    }

    public File getInputFile() {
        return SeadasHelper.getInputFile(getInput());
    }

    public NetcdfFile getNcfile() {
        return ncfile;
    }

    public ProductType getProductType() {
        return productType;
    }

    public boolean checkSeadasMapped() {
        try {
            List<Variable> seadasMappedVariables = ncfile.getVariables();
            return seadasMappedVariables.get(0).findAttribute("Projection_Category").isString();
        } catch (Exception e) {
            return false;
        }
    }

    public ProductType checkMEaSUREs() {
        try {
            Attribute indexes = ncfile.findGlobalAttribute("Data_Bins");
            if (indexes != null) {
                return ProductType.MEaSUREs_Bin;
            } else {
                return ProductType.MEaSUREs;
            }

        } catch (Exception ignored) {
        }
        return ProductType.UNKNOWN;
    }

    public boolean checkModisL1B() {
        Group modisl1bGroup = ncfile.findGroup("MODIS_SWATH_Type_L1B");
        return modisl1bGroup != null;
    }

    public ProductType checkViirsXDR() {
        Attribute platformShortName = ncfile.findGlobalAttribute("Platform_Short_Name");
        try {
            if ("NPP".equals(platformShortName.getStringValue())) {
                Group dataProduct = ncfile.findGroup("Data_Products");
                if (dataProduct.getGroups().get(0).getShortName().matches("VIIRS.*IP")) {
                    return ProductType.VIIRS_IP;
                }
                if (dataProduct.getGroups().get(0).getShortName().matches("VIIRS.*SDR")) {
                    return ProductType.VIIRS_SDR;
                }
                if (dataProduct.getGroups().get(0).getShortName().matches("VIIRS.*EDR")) {
                    return ProductType.VIIRS_EDR;
                }
                if (dataProduct.getGroups().get(0).getShortName().matches("VIIRS.*GEO.*")) {
                    return ProductType.VIIRS_GEO;
                }
            }

        } catch (Exception ignored) {
        }
        return ProductType.UNKNOWN;
    }

    private boolean checkDscoverEpicL2() {
        Attribute scene_title = ncfile.findGlobalAttribute("HDFEOS_ADDITIONAL_FILE_ATTRIBUTES_LocalGranuleID");
        return scene_title != null && scene_title.toString().contains("EPIC-DSCOVR_L2");
    }

    private boolean checkHicoL1B() {
        Attribute hicol1bName = ncfile.findGlobalAttribute("metadata_FGDC_Identification_Information_Platform_and_Instrument_Identification_Instrument_Short_Name");
        if (hicol1bName != null && "hico".equals(hicol1bName.getStringValue(0))) {
            Attribute level = ncfile.findGlobalAttribute("metadata_FGDC_Identification_Information_Processing_Level_Processing_Level_Identifier");
            return level != null && "Level-1B".equals(level.getStringValue(0));
        }
        return false;
    }

    private ProductType checkViirsL1B() {
        Attribute instrumentName = ncfile.findGlobalAttribute("instrument");
        Attribute processingLevel = ncfile.findGlobalAttribute("processing_level");

        if (instrumentName != null) {
            if (processingLevel != null) {
                if ("VIIRS".equals(instrumentName.getStringValue()) && "L1B".equals(processingLevel.getStringValue())) {
                    return ProductType.VIIRS_L1B;
                }
            }
        }
        return ProductType.UNKNOWN;
    }

    public ProductType findProductType() throws ProductIOException {
        Attribute titleAttr = ncfile.findGlobalAttributeIgnoreCase("Title");
        Attribute processing_levelAttr = ncfile.findGlobalAttributeIgnoreCase("processing_level");
        Attribute instrumentAttr = ncfile.findGlobalAttributeIgnoreCase("instrument");

        String title;
        String processing_level = null;
        String instrument;
        ProductType tmp;
        if (titleAttr != null) {
            title = titleAttr.getStringValue().trim();
            processing_level = getStringAttributeValue(processing_levelAttr);
            instrument = getStringAttributeValue(instrumentAttr);
            if ("Oceansat OCM2 Level-1B Data".equals(title)) {
                return ProductType.Level1B_OCM2;
            } else if ("CZCS Level-2 Data".equals(title)) {
                return ProductType.Level2_CZCS;
            } else if (title.contains("Aquarius Level 1A Data")) {
                return ProductType.Level1A_Aquarius;
            } else if (title.contains("Aquarius Level 2 Data")) {
                return ProductType.Level2_Aquarius;
            } else if (title.contains("PACE OCI Level-1B Data")) {
                return ProductType.Level1B_PaceOCI;
            } else if (title.contains("PACE OCI Level-1C Data")
                    || title.contains("PACE SPEXone Level-1C Data")
                    || title.contains("HARP2 Level-1C Data")) {
                return ProductType.Level1C_Pace;
            } else if (processing_level != null && instrument != null && processing_level.toUpperCase().contains("1C")) {
                if (instrument.toUpperCase().contains("OCI")
                        || instrument.toUpperCase().contains("HARP")
                        || instrument.toUpperCase().contains("SPEXONE")) {
                    return ProductType.Level1C_Pace;
                }
            } else if ("OCIS Level-2 Data".equals(title)) {
                return ProductType.Level2_PaceOCIS;
            } else if (title.contains("OCI Level-2 Data")) {
                return ProductType.Level2_PaceOCI;
            } else if (title.contains("Level-1B")) {
                return ProductType.Level1B;
            } else if ("CZCS Level-1A Data".equals(title)) {
                return ProductType.Level1A_CZCS;
            } else if (title.contains("Hawkeye Level-1A Data")) {
                return ProductType.Level1A_Hawkeye;
            } else if ("OCTS Level-1A GAC Data".equals(title)) {
                return ProductType.Level1A_OCTS;
            } else if (title.contains("Browse")) {
                return ProductType.BrowseFile;
            } else if (title.contains("Level-2")) {
                return ProductType.Level2;
            } else if (title.contains("Level 2")) {
                return ProductType.Level2;
            } else if ("SeaWiFS Level-1A Data".equals(title)) {
                return ProductType.Level1A_Seawifs;
            } else if ("NOAA/NSIDC Climate Data Record of Passive Microwave Sea Ice Concentration Version 4".equals(title)) {
                return ProductType.Level3_NSIDC_CDR;
            } else if (title.contains("Daily-OI")) {
                return ProductType.OISST;
            } else if (title.contains("ETOPO")) {
                return ProductType.Bathy;
            } else if ("SeaWiFS Near Real-Time Ancillary Data".equals(title)) {
                return ProductType.ANCNRT;
            } else if ("NCEP Reanalysis 2 Ancillary Data".equals(title)) {
                return ProductType.ANCNRT2;
            } else if ("SeaWiFS Climatological Ancillary Data".equals(title)) {
                return ProductType.ANCCLIM;
            } else if (title.matches("(.*)Level-3 Standard Mapped Image") || title.matches("(.*)Level-3 Equidistant Cylindrical Mapped Image")) {
                return ProductType.SMI;
            } else if (title.contains("Level-3") && title.contains("Mapped Image")) {
                return ProductType.Level3_SeadasMapped;
            } else if (title.contains("Level-3 Binned Data") || title.contains("level-3_binned_data")) {
                return ProductType.Level3_Bin;
            } else if (title.contains("GSM") && (tmp = checkMEaSUREs()) != ProductType.UNKNOWN) {
                return tmp;
            } else if (title.contains("VIIRS") && (tmp = checkViirsL1B()) != ProductType.UNKNOWN) {
                return tmp;
            }
        } else if (checkModisL1B()) {
            return ProductType.Level1B_Modis;
        } else if (checkDscoverEpicL2()) {
            return ProductType.Level2_DscovrEpic;
        } else if (checkHicoL1B()) {
            return ProductType.Level1B_HICO;
        } else if ((tmp = checkViirsXDR()) != ProductType.UNKNOWN) {
            return tmp;
        } else if (checkSeadasMapped()) {
            return ProductType.SeadasMapped;
        }

        throw new ProductIOException("Unrecognized product type");

    }

    private static String getStringAttributeValue(Attribute processing_levelAttr) {
        if (processing_levelAttr != null) {
            final String stringValue = processing_levelAttr.getStringValue();
            if (stringValue != null) {
                return stringValue.trim();
            }
        }
        return null;
    }
}
