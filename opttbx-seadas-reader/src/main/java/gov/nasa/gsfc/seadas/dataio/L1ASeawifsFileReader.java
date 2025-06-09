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

import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.GeoCodingFactory;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.Dimension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class L1ASeawifsFileReader extends SeadasFileReader {

    static final int[] SEAWIFS_WVL = new int[]{412, 443, 490, 510, 555, 670, 765, 865};

    L1ASeawifsFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int sceneWidth = getDimension("pixels");
        int sceneHeight = getDimension("scans");
        ProductData.UTC utcStart = getUTCAttribute("time_coverage_start");
        ProductData.UTC utcEnd = getUTCAttribute("time_coverage_end");
        if (sceneWidth == -1) {                              // Old format
            sceneWidth = getIntAttribute("Pixels_per_Scan_Line");
            sceneHeight = getIntAttribute("Number_of_Scan_Lines");
            utcStart = getUTCAttribute("Start_Time");
            utcEnd = getUTCAttribute("End_Time");
        }
        String productName = productReader.getInputFile().getName();

        mustFlipX = mustFlipY = getDefaultFlip();
        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        if (utcStart != null) {
            product.setStartTime(utcStart);
        }

        if (utcEnd != null) {
            product.setEndTime(utcEnd);
        }

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
        addScientificMetadata(product);

        variableMap = addSeawifsBands(product, ncFile.getVariables());

        SeaWiFSL1AGeonav geonavCalculator = new SeaWiFSL1AGeonav(ncFile);
        float[] latitudes = flatten2DimArray(geonavCalculator.getLatitudes());
        float[] longitudes = flatten2DimArray(geonavCalculator.getLongitudes());
        Band latBand = new Band("latitude", ProductData.TYPE_FLOAT32, sceneWidth, sceneHeight);
        Band lonBand = new Band("longitude", ProductData.TYPE_FLOAT32, sceneWidth, sceneHeight);
        latBand.setNoDataValue(999.0);
        latBand.setNoDataValueUsed(true);
        lonBand.setNoDataValue(999.0);
        lonBand.setNoDataValueUsed(true);
        product.addBand(latBand);
        product.addBand(lonBand);

        ProductData lats = ProductData.createInstance(latitudes);
        latBand.setData(lats);
        ProductData lons = ProductData.createInstance(longitudes);
        lonBand.setData(lons);
        try {
            final ComponentGeoCoding geoCoding = GeoCodingFactory.createPixelGeoCoding(latBand, lonBand);
            product.setSceneGeoCoding(geoCoding);
        } catch (IOException e) {
            throw new ProductIOException(e.getMessage());
        }

        addFlagsAndMasks(product);

        return product;

    }

    private Map<Band, Variable> addSeawifsBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            int variableRank = variable.getRank();
            if (variableRank == 3) {
                final int[] dimensions = variable.getShape();
                final int bands = dimensions[2];
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = "radiance counts";
                    String description = "Level-1A data";

                    for (int i = 0; i < bands; i++) {
                        final String shortname = "L1A";
                        StringBuilder longname = new StringBuilder(shortname);
                        longname.append("_");
                        longname.append(SEAWIFS_WVL[i]);
                        String name = longname.toString();
                        final int dataType = getProductDataType(variable);
                        final Band band = new Band(name, dataType, width, height);
                        product.addBand(band);

                        final float wavelength = Float.valueOf(SEAWIFS_WVL[i]);
                        band.setSpectralWavelength(wavelength);
                        band.setSpectralBandIndex(spectralBandIndex++);

                        Variable sliced = null;
                        try {
                            sliced = variable.slice(2, i);
                        } catch (InvalidRangeException e) {
                            e.printStackTrace();  //Todo change body of catch statement.
                        }
                        bandToVariableMap.put(band, sliced);
                        band.setUnit(units);
                        band.setDescription(description);

                    }
                }
            }
        }
        return bandToVariableMap;
    }

    private int getDimension(String dimensionName) {
        final List<Dimension> dimensions = ncFile.getDimensions();
        for (Dimension dimension : dimensions) {
            if (dimension.getShortName().equals(dimensionName)) {
                return dimension.getLength();
            }
        }
        return -1;
    }
}