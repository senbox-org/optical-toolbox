/*
 * Copyright (C) 2015 Brockmann Consult GmbH (info@brockmann-consult.de)
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
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGeoCoding;
import org.esa.snap.core.datamodel.TiePointGrid;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.Dimension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class L1AOctsFileReader extends SeadasFileReader {

    static final int[] OCTS_WVL = new int[]{412, 443, 490, 520, 560, 670, 765, 865};

    L1AOctsFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int sceneWidth = getDimension("nsamp");
        int sceneHeight = getDimension("lines");
        ProductData.UTC utcStart = getUTCAttribute("time_coverage_start");
        ProductData.UTC utcEnd = getUTCAttribute("time_coverage_end");
        if (sceneHeight == 0) {
            sceneWidth = getIntAttribute("Pixels_per_Scan_Line");
            sceneHeight = getIntAttribute("Number_of_Scan_Lines") * 2;
            utcStart = getUTCAttribute("Start_Time");
            utcEnd = getUTCAttribute("End_Time");
        }
        String productName = productReader.getInputFile().getName();

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

        variableMap = addOctsBands(product, ncFile.getVariables());
        // todo: OCTS L1 uses the same orbit vector geolocation as SeaWiFS - make the SeaWiFSL1aGeonav code generic
        addGeocoding(product);

//        addFlagsAndMasks(product);

        return product;

    }

    public void addGeocoding(final Product product) throws ProductIOException {
        String navGroup = "Scan-Line_Attributes";
        final String longitude = "lon";
        final String latitude = "lat";
        int susampX = 16;
        int subsampY = 2;


        Variable lats = ncFile.findVariable(navGroup + "/" +latitude);
        Variable lons = ncFile.findVariable(navGroup + "/" +longitude);
        if (lats == null) {
            lats = ncFile.findVariable(latitude);
            lons = ncFile.findVariable(longitude);
        }

        int[] dims = lats.getShape();

        float[] latTiePoints;
        float[] lonTiePoints;

        try {
            Array latarr = lats.read();
            Array lonarr = lons.read();

            latTiePoints = (float[]) latarr.getStorage();
            lonTiePoints = (float[]) lonarr.getStorage();

            final TiePointGrid latGrid = new TiePointGrid("latitude", dims[1], dims[0], 0, 0,
                    susampX, subsampY, latTiePoints);

            product.addTiePointGrid(latGrid);

            final TiePointGrid lonGrid = new TiePointGrid("longitude", dims[1], dims[0], 0, 0,
                    susampX, subsampY, lonTiePoints, TiePointGrid.DISCONT_AT_180);

            product.addTiePointGrid(lonGrid);

            product.setSceneGeoCoding(new TiePointGeoCoding(latGrid, lonGrid));

        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
    }

    private Map<Band, Variable> addOctsBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            int variableRank = variable.getRank();
            if (variableRank == 3) {
                final int[] dimensions = variable.getShape();
                final int bands = dimensions[0];
                final int height = dimensions[1];
                final int width = dimensions[2];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    final List<Attribute> list = variable.getAttributes();

                    String units = "radiance counts";
                    String description = "Level-1A data";

                    for (int i = 0; i < bands; i++) {
                        final String shortname = "L1A";
                        StringBuilder longname = new StringBuilder(shortname);
                        longname.append("_");
                        longname.append(OCTS_WVL[i]);
                        String name = longname.toString();
                        final int dataType = getProductDataType(variable);
                        final Band band = new Band(name, dataType, width, height);
                        product.addBand(band);

                        final float wavelength = Float.valueOf(OCTS_WVL[i]);
                        band.setSpectralWavelength(wavelength);
                        band.setSpectralBandIndex(spectralBandIndex++);

                        Variable sliced = null;
                        try {
                            sliced = variable.slice(0, i);
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
