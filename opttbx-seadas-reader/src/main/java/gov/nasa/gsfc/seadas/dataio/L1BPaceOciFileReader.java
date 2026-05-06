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

import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import eu.esa.snap.core.datamodel.band.BandUsingReaderDirectly;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.GeoCodingFactory;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.runtime.Config;
import org.jspecify.annotations.NonNull;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import static org.esa.snap.dataio.netcdf.util.DataTypeUtils.getRasterDataType;

public class L1BPaceOciFileReader extends SeadasFileReader {

    private boolean applyScaling;

    L1BPaceOciFileReader(SeadasProductReader productReader) {
        super(productReader);

        final Preferences preferences = Config.instance("seadas").preferences();
        wantsCaching = preferences.getBoolean("seadas.reader.enable.cache", true);
        applyScaling = preferences.getBoolean("seadas.reader.apply.scaling", true);
    }

    enum WvlType {
        RED("red_wavelengths"),
        BLUE("blue_wavelengths"),
        SWIR("swir_wavelengths");

        private final String name;

        WvlType(String nm) {
            name = nm;
        }

        public String toString() {
            return name;
        }
    }

    Array blue_wavlengths = null;
    Array red_wavlengths = null;
    Array swir_wavlengths = null;


    @Override
    public Product createProduct() throws ProductIOException {

        int[] shape;
        int sceneWidth, sceneHeight;
        String productName;

        try {
            shape = ncFile.findVariable("geolocation_data/latitude").getShape();
            sceneWidth = shape[1];
            sceneHeight = shape[0];
        } catch (Exception e) {
            throw new ProductIOException(e.getMessage(), e);
        }

        try {
            productName = getStringAttribute("product_name");
        } catch (Exception ignored) {
            productName = productReader.getInputFile().getName();
        }

        readWvlVariables();

        if (SeadasReaderDefaults.FlIP_YES.equals(getBandFlipXL1BPace())) {
            mustFlipX = true;
        } else if (SeadasReaderDefaults.FlIP_NO.equals(getBandFlipXL1BPace())) {
            mustFlipX = false;
        } else {
            mustFlipX = false; // default flipX
        }

        // default flipY
        if (SeadasReaderDefaults.FlIP_YES.equals(getBandFlipYL1BPace())) {
            mustFlipY = true;
        } else mustFlipY = !SeadasReaderDefaults.FlIP_NO.equals(getBandFlipYL1BPace());

        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        List<Variable> variables = ncFile.getVariables();
        if (wantsCaching) {
            variableMap = addCachedOciBands(product, variables);
        } else {
            variableMap = addOciBands(product, variables);
        }

        addGeocoding(product);
        addMetadata(product, "products", "Band_Metadata");
        addMetadata(product, "navigation", "Navigation_Metadata");

//        product.setAutoGrouping("rhot_blue:rhot_red:rhot_SWIR:qual_blue:qual_red:qual_SWIR:Lt_blue:Lt_red:Lt_SWIR");
        product.setAutoGrouping(getBandGroupingL1BPace());

        return product;
    }

    private void readWvlVariables() throws ProductIOException {
        Variable blueWvl = ncFile.findVariable("sensor_band_parameters/blue_wavelength");
        Variable redWvl = ncFile.findVariable("sensor_band_parameters/red_wavelength");
        Variable swirWvl = ncFile.findVariable("sensor_band_parameters/SWIR_wavelength");
        try {
            blue_wavlengths = blueWvl.read();
            red_wavlengths = redWvl.read();
            swir_wavlengths = swirWvl.read();
        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
    }


    public void addMetadata(Product product, String groupname, String meta_element) throws ProductIOException {
        Group group = ncFile.findGroup(groupname);

        if (group != null) {
            final MetadataElement bandAttributes = new MetadataElement(meta_element);
            List<Variable> variables = group.getVariables();
            for (Variable variable : variables) {
                final String name = variable.getShortName();
                final MetadataElement sdsElement = new MetadataElement(name + ".attributes");
                final int dataType = getProductDataType(variable);
                final MetadataAttribute prodtypeattr = new MetadataAttribute("data_type", dataType);

                sdsElement.addAttribute(prodtypeattr);
                bandAttributes.addElement(sdsElement);

                final List<Attribute> list = variable.getAttributes();
                for (Attribute varAttribute : list) {
                    addAttributeToElement(sdsElement, varAttribute);
                }
            }
            final MetadataElement metadataRoot = product.getMetadataRoot();
            metadataRoot.addElement(bandAttributes);
        }
    }

    private Map<String, Variable> addCachedOciBands(Product product, List<Variable> variables) {
        final Map<String, Variable> bandToVariableMap = new HashMap<String, Variable>();
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        for (Variable variable : variables) {
            final Group parentGroup = variable.getParentGroup();
            final String groupName = parentGroup.getShortName();
            if ("sensor_band_parameters".equals(groupName) || "scan_line_attributes".equals(groupName)) {
                continue;
            }
            final String variableName = variable.getShortName();
            if (("latitude".equals(variableName)) || ("longitude".equals(variableName))) {
                continue;
            }

            final int variableRank = variable.getRank();
            if (variableRank == 2) {
                add2DBand(product, variable, sceneRasterHeight, sceneRasterWidth, bandToVariableMap);
            } else if (variableRank == 3) {
                add3DVariables_forCaching(product, variable, sceneRasterHeight, sceneRasterWidth, bandToVariableMap);
            }
        }

        return bandToVariableMap;
    }

    private void add3DVariables_forCaching(Product product, Variable variable, int sceneRasterHeight, int sceneRasterWidth, Map<String, Variable> bandToVariableMap) {
        final int[] dimensions = variable.getShape();
        final int bands = dimensions[0];
        final int height = dimensions[1];
        final int width = dimensions[2];

        if (height != sceneRasterHeight || width != sceneRasterWidth) {
            return;
        }

        final String ncVariableName = variable.getShortName();
        final WvlType wvlType = getWvlType(variable.getShortName());
        int spectralBandIndex = 0;
        for (int i = 0; i < bands; i++) {
            final float wavelength = getOciWvl(i, wvlType);
            final String variableName = getVariableName(ncVariableName, wavelength);
            final int dataType = getProductDataType(variable);
            final Band band = new BandUsingReaderDirectly(variableName, dataType, width, height);

            product.addBand(band);

            band.setSpectralWavelength(wavelength);
            band.setSpectralBandIndex(spectralBandIndex++);
            addAttributes(variable, band, applyScaling);
            band.setDescription(ncVariableName);
        }
        bandToVariableMap.put(ncVariableName, variable);
    }

    private Map<String, Variable> addOciBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        Map<String, Variable> bandToVariableMap = new HashMap<String, Variable>();
        for (Variable variable : variables) {
            if (variable.getParentGroup().equals("sensor_band_parameters") || variable.getParentGroup().equals("scan_line_attributes"))
                continue;
            if ((variable.getShortName().equals("latitude")) || (variable.getShortName().equals("longitude")))
                continue;
            int variableRank = variable.getRank();

            if (variableRank == 2) {
                add2DBand(product, variable, sceneRasterHeight, sceneRasterWidth, bandToVariableMap);
            } else if (variableRank == 3) {
                add3DBands(product, variable, sceneRasterHeight, sceneRasterWidth, bandToVariableMap);
            }
        }
        return bandToVariableMap;
    }

    private void add3DBands(Product product, Variable variable, int sceneRasterHeight, int sceneRasterWidth, Map<String, Variable> bandToVariableMap) {
        int spectralBandIndex = 0;
        final int[] dimensions = variable.getShape();
        final int bands = dimensions[0];
        final int height = dimensions[1];
        final int width = dimensions[2];

        if (height != sceneRasterHeight || width != sceneRasterWidth) {
            return;
        }

        final String units = variable.getUnitsString();
        final String description = variable.getShortName();

        for (int i = 0; i < bands; i++) {
            final float wavelength = getOciWvl(i, getWvlType(variable.getShortName()));
            final String variableName = getVariableName(description, wavelength);
            final int dataType = getProductDataType(variable);
            final Band band = new Band(variableName, dataType, width, height);
            product.addBand(band);

            band.setSpectralWavelength(wavelength);
            band.setSpectralBandIndex(spectralBandIndex++);

            Variable sliced = null;
            try {
                sliced = variable.slice(0, i);
            } catch (InvalidRangeException e) {
                e.printStackTrace();  //Todo change body of catch statement.
            }

            addAttributes(variable, band, applyScaling);
            bandToVariableMap.put(band.getName(), sliced);
            // the following two are duplicated, already covered by addAttributes() tb 2026-04-28
            band.setUnit(units);
            band.setDescription(description);
        }
    }

    // package access for testing only tb 2026-04-28
    static void addAttributes(Variable variable, Band band, boolean applyScaling) {
        Attribute attribute = variable.findAttribute("units");
        if (attribute != null) {
            band.setUnit(attribute.getStringValue());
        }
        attribute = variable.findAttribute("long_name");
        if (attribute != null) {
            band.setDescription(attribute.getStringValue());
        }
        if (applyScaling) {
            attribute = variable.findAttribute("slope");
            if (attribute != null) {
                band.setScalingFactor(attribute.getNumericValue().doubleValue());
            }
            attribute = variable.findAttribute("scale_factor");
            if (attribute != null) {
                band.setScalingFactor(attribute.getNumericValue().doubleValue());
            }
            attribute = variable.findAttribute("intercept");
            if (attribute != null) {
                band.setScalingOffset(attribute.getNumericValue().doubleValue());
            }
            attribute = variable.findAttribute("add_offset");
            if (attribute != null) {
                band.setScalingOffset(attribute.getNumericValue().doubleValue());
            }
            attribute = variable.findAttribute("bad_value_scaled");
            if (attribute != null) {
                band.setNoDataValue(attribute.getNumericValue().doubleValue());
                band.setNoDataValueUsed(true);
            }
        }
    }

    // package access for testing only tb 2026-04-28
    static @NonNull String getVariableName(String description, float wavelength) {
        return description + "_" + wavelength;
    }

    @Override
    String getNcVariableName(String variableName) {
        return stripWavelengthFromName(variableName);
    }

    static @NonNull String stripWavelengthFromName(String variableName) {
        final int underscoreIdx = variableName.lastIndexOf("_");
        return variableName.substring(0, underscoreIdx);
    }

    private void add2DBand(Product product, Variable variable, int sceneRasterHeight, int sceneRasterWidth, Map<String, Variable> bandToVariableMap) {
        final int[] dimensions = variable.getShape();
        final int height = dimensions[0];
        final int width = dimensions[1];

        if (height != sceneRasterHeight || width != sceneRasterWidth) {
            return;
        }

        final String units = variable.getUnitsString();
        final String name = variable.getShortName();
        final int dataType = getProductDataType(variable);
        final Band band;
        if (wantsCaching) {
            band = new BandUsingReaderDirectly(name, dataType, width, height);
        } else {
            band = new Band(name, dataType, width, height);
        }
        product.addBand(band);

        addAttributes(variable, band, applyScaling);

        bandToVariableMap.put(band.getName(), variable);
        band.setUnit(units);
        band.setDescription(variable.getDescription());
    }

    private WvlType getWvlType(String productName) {
        WvlType wvltype = null;
        if (productName.equals("Lt_blue") || productName.equals("rhot_blue") || productName.equals("qual_blue")) {
            wvltype = WvlType.BLUE;
        } else if (productName.equals("Lt_red") || productName.equals("rhot_red") || productName.equals("qual_red")) {
            wvltype = WvlType.RED;
        } else if (productName.equals("Lt_SWIR") || productName.equals("rhot_SWIR") || productName.equals("qual_SWIR")) {
            wvltype = WvlType.SWIR;
        }
        return wvltype;
    }

    private float getOciWvl(int index, WvlType wvlEnum) {
        float wvl;
        switch (wvlEnum) {
            case RED:
                wvl = red_wavlengths.getFloat(index);
                break;
            case BLUE:
                wvl = blue_wavlengths.getFloat(index);
                break;
            case SWIR:
                wvl = swir_wavlengths.getFloat(index);
                break;
            default:
                wvl = 0;
        }
        return wvl;
    }

    public ProductData readDataFlip(Variable variable) throws ProductIOException {
        final int dataType = getProductDataType(variable);
        Array array;
        Object storage;
        try {
            array = variable.read();
            storage = array.flip(0).copyTo1DJavaArray();
        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
        return ProductData.createInstance(dataType, storage);
    }

    private void addGeocoding(final Product product) throws ProductIOException {
        final String longitude = "longitude";
        final String latitude = "latitude";
        String navGroup = "geolocation_data";

        Variable latVar = ncFile.findVariable(navGroup + "/" + latitude);
        Variable lonVar = ncFile.findVariable(navGroup + "/" + longitude);

        if (latVar != null && lonVar != null) {
            final ProductData lonRawData;
            final ProductData latRawData;
            if (mustFlipY) {
                lonRawData = readDataFlip(lonVar);
                latRawData = readDataFlip(latVar);
            } else {
                lonRawData = readData(lonVar);
                latRawData = readData(latVar);
            }

            Band latBand = product.addBand(latVar.getShortName(), ProductData.TYPE_FLOAT32);
            Band lonBand = product.addBand(lonVar.getShortName(), ProductData.TYPE_FLOAT32);
            latBand.setNoDataValue(-999.);
            lonBand.setNoDataValue(-999.);
            latBand.setNoDataValueUsed(true);
            lonBand.setNoDataValueUsed(true);
            latBand.setData(latRawData);
            lonBand.setData(lonRawData);

            try {
                final ComponentGeoCoding geoCoding = GeoCodingFactory.createPixelGeoCoding(latBand, lonBand);
                product.setSceneGeoCoding(geoCoding);
            } catch (IOException e) {
                throw new ProductIOException(e.getMessage());
            }
        }
    }

    @Override
    public VariableDescriptor getVariableDescriptor(String variableName) throws IOException {
        final Variable netcdVariable = variableMap.get(variableName);
        if (netcdVariable == null) {
            throw new IOException("Variable not known: " + variableName);
        }

        final VariableDescriptor variableDescriptor = new VariableDescriptor();
        variableDescriptor.name = variableName;
        variableDescriptor.dataType = getRasterDataType(netcdVariable.getDataType(), false);

        int[] shape = netcdVariable.getShape();

        final Array chunkSizesValues;
        final Attribute chunkSizes = netcdVariable.findAttribute("_ChunkSizes");
        if (chunkSizes != null) {
            chunkSizesValues = chunkSizes.getValues();
        } else {
            chunkSizesValues = Array.factory(DataType.INT, new int[]{shape.length}, shape);
        }

        if (shape.length == 2) {
            variableDescriptor.width = shape[1];
            variableDescriptor.height = shape[0];
            variableDescriptor.layers = -1;

            variableDescriptor.tileWidth = chunkSizesValues.getInt(1);
            variableDescriptor.tileHeight = chunkSizesValues.getInt(0);
            variableDescriptor.tileLayers = -1;
        } else if (shape.length == 3) {
            variableDescriptor.width = shape[2];
            variableDescriptor.height = shape[1];
            variableDescriptor.layers = shape[0];

            variableDescriptor.tileWidth = chunkSizesValues.getInt(2);
            variableDescriptor.tileHeight = chunkSizesValues.getInt(1);
            variableDescriptor.tileLayers = chunkSizesValues.getInt(0);
        }

        return variableDescriptor;
    }

    @Override
    public DataBuffer readCacheBlock(String variableName, int[] offsets, int[] shapes, ProductData targetData) throws IOException {
        final Variable netcdfVariable = variableMap.get(variableName);
        int rasterDataType = getRasterDataType(netcdfVariable);

        Array rawBuffer;
        synchronized (ncFile) {
            try {
                rawBuffer = netcdfVariable.read(offsets, shapes);
            } catch (InvalidRangeException e) {
                throw new IOException(e);
            }

            if (targetData == null) {
                targetData = createTargetDataBuffer(shapes, rasterDataType);
            }

            switch (rasterDataType) {
                case ProductData.TYPE_FLOAT32:
                    targetData.setElems(rawBuffer.get1DJavaArray(DataType.FLOAT));
                    break;
                case ProductData.TYPE_FLOAT64:
                    targetData.setElems(rawBuffer.get1DJavaArray(DataType.DOUBLE));
                    break;
                case ProductData.TYPE_INT16:
                    targetData.setElems(rawBuffer.get1DJavaArray(DataType.SHORT));
                    break;
                case ProductData.TYPE_UINT8:
                    targetData.setElems(rawBuffer.get1DJavaArray(DataType.BYTE));
                    break;
                default:
                    throw new IOException("Unknown data type: " + rasterDataType);
            }
        }

        return new DataBuffer(targetData, offsets, shapes);
    }

    private static @NonNull ProductData createTargetDataBuffer(int[] shapes, int rasterDataType) throws IOException {
        ProductData targetData;
        if (shapes.length == 2) {
            targetData = ProductData.createInstance(rasterDataType, shapes[0] * shapes[1]);
        } else if (shapes.length == 3) {
            targetData = ProductData.createInstance(rasterDataType, shapes[0] * shapes[1] * shapes[2]);
        } else {
            throw new IOException("Illegally shaped variable");
        }
        return targetData;
    }
}