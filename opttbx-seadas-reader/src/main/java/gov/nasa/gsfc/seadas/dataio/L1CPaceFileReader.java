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
import org.esa.snap.core.datamodel.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class L1CPaceFileReader extends SeadasFileReader {

    L1CPaceFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int sceneHeight = ncFile.findDimension("bins_along_track").getLength();
        int sceneWidth = ncFile.findDimension("bins_across_track").getLength();

        String productName = productReader.getInputFile().getName();

        mustFlipY = mustFlipX = false;
        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        Attribute startTime = findAttribute("time_coverage_start");
//        ProductData.UTC utcStart = getUTCAttribute("time_coverage_start");
//        ProductData.UTC utcEnd = getUTCAttribute("time_coverage_end");
//        if (startTime == null) {
//            utcStart = getUTCAttribute("Start_Time");
//            utcEnd = getUTCAttribute("End_Time");
//        }
//        // only needed as a stop-gap to handle an intermediate version of l2gen metadata
//        if (utcEnd == null) {
//            utcEnd = getUTCAttribute("time_coverage_stop");
//        }

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        addGlobalMetadata(product);
//        Attribute scene_title = ncFile.findGlobalAttributeIgnoreCase("Title");
        Attribute instrument = ncFile.findGlobalAttributeIgnoreCase("instrument");

        if (instrument != null && instrument.toString().toUpperCase().contains("OCI")) {
            mustFlipY = true;
            variableMap = addOciBands(product, ncFile.getVariables());
        } else if (instrument != null && instrument.toString().toUpperCase().contains("HARP")) {
            mustFlipY = true;
            variableMap = addHarpBands(product, ncFile.getVariables());
        } else if (instrument != null && instrument.toString().toUpperCase().contains("SPEXONE")) {
            mustFlipY = true;
            variableMap = addSPEXoneBands(product, ncFile.getVariables());
        }


        addGeocoding(product);
        addMetadata(product, "products", "Band_Metadata");
        addMetadata(product, "navigation", "Navigation_Metadata");
        addBandMetadata(product);

        if (instrument != null) {
            if (instrument.toString().toUpperCase().contains("OCI")) {
                product.setAutoGrouping("I_-20:I_20:obs_per_view:");
            } else if (instrument.toString().toUpperCase().contains("HARP")) {
                product.setAutoGrouping("I_*_549:I_*_669:I_*_867:I_*_441:Q_*_549:Q_*_669:Q_*_867:Q_*_441:" +
                        "U_*_549:U_*_669:U_*_867:U_*_441:DOLP_*_549:DOLP_*_669:DOLP_*_867:DOLP_*_441:" +
                        "I_noise_*_549:I_noise_*_669:I_noise_*_867:I_noise_*_441:Q_noise_*_549:Q_noise_*_669:Q_noise_*_867:Q_noise_*_441:" +
                        "U_noise_*_549:U_noise_*_669:U_noise_*_867:U_noise_*_441:DOLP_noise_*_549:DOLP_noise_*_669:DOLP_noise_*_867:DOLP_noise_*_441:" +
                        "Sensor_Zenith:Sensor_Azimuth:Solar_Zenith:Solar_Azimuth:obs_per_view:view_time_offsets");
            } else if (instrument.toString().toUpperCase().contains("SPEXONE")) {
                product.setAutoGrouping("I:I_58_*:I_22_*:I_4_*:I_-22_*:I_-58_*:" +
                        "AOLP:AOLP_58_*:AOLP_22_*:AOLP_4_*:AOLP_-22_*:AOLP_-58_*:" +
                        "DOLP:DOLP_58_*:DOLP_22_*:DOLP_4_*:DOLP_-22_*:DOLP_-58_*:" +
                        "QC:QC_58_*:QC_22_*:QC_4_*:QC_-22_*:QC_-58_*:" +
                        "I_57_*:I_20_*:I_0_*:I_-20_*:I_-57_*:" +
                        "AOLP_57_*:AOLP_20_*:AOLP_0_*:AOLP_-20_*:AOLP_-57_*:" +
                        "DOLP_57_*:DOLP_20_*:DOLP_0_*:DOLP_-20_*:DOLP_-57_*:" +
                        "QC_57_*:QC_20_*:QC_0_*:QC_-22_*:QC_-57_*:" +
                        "QC_bitwise:QC_polsample_bitwise:QC_polsample:" +
                        "I_noise:I_noisefree:I_polsample:I_polsample_noise:I_noisefree_polsample:" +
                        "DOLP_noise:DOLP_noisefree:AOLP_noise:AOLP_noisefree:" +
                        "Q_over_I:Q_over_I_noise:Q_over_I_noisefree:" +
                        "U_over_I:U_over_I_noise:U_over_I_noisefree:scattering_angle:" +
                        "sensor_azimuth:sensor_zenith:solar_azimuth:solar_zenith:" +
                        "obs_per_view:view_time_offsets");
            }
        }
        return product;
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

    private Map<Band, Variable> addHarpBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band;

        Array wavelengths = null;
        Array view_angles = null;

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            if (variable.getParentGroup().equals("sensor_views_bands"))
                continue;
            if ((variable.getShortName().equals("latitude")) || (variable.getShortName().equals("longitude")))
                continue;
            int variableRank = variable.getRank();

            if (variableRank == 2) {
                final int[] dimensions = variable.getShape();
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String name = variable.getShortName();
                    final int dataType = getProductDataType(variable);
                    band = new Band(name, dataType, width, height);
                    product.addBand(band);

                    final List<Attribute> list = variable.getAttributes();
                    double[] validMinMax = {0.0, 0.0};
                    for (Attribute hdfAttribute : list) {
                        final String attribName = hdfAttribute.getShortName();
                        if ("units".equals(attribName)) {
                            band.setUnit(hdfAttribute.getStringValue());
                        } else if ("long_name".equals(attribName)) {
                            band.setDescription(hdfAttribute.getStringValue());
                        } else if ("slope".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("intercept".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("scale_factor".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("add_offset".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("bad_value_scaled".equals(attribName)) {
                            band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                            band.setNoDataValueUsed(true);
                        } else if ("_FillValue".equals(attribName)) {
                            band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                            band.setNoDataValueUsed(true);
                        } else if (attribName.startsWith("valid_")) {
                            if ("valid_min".equals(attribName)) {
                                if (hdfAttribute.getDataType().isUnsigned()) {
                                    validMinMax[0] = getUShortAttribute(hdfAttribute);
                                } else {
                                    validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_max".equals(attribName)) {
                                if (hdfAttribute.getDataType().isUnsigned()) {
                                    validMinMax[1] = getUShortAttribute(hdfAttribute);
                                } else {
                                    validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_range".equals(attribName)) {
                                validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                            }
                        }
                    }
                    if (validMinMax[0] != validMinMax[1]) {
                        String validExp;
                        if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                            validExp = format("%s >= %.05f && %s <= %.05f", name, validMinMax[0], name, validMinMax[1]);

                        } else {
                            double[] minmax = {0.0, 0.0};
                            minmax[0] = validMinMax[0];
                            minmax[1] = validMinMax[1];

                            if (band.getScalingFactor() != 1.0) {
                                minmax[0] *= band.getScalingFactor();
                                minmax[1] *= band.getScalingFactor();
                            }
                            if (band.getScalingOffset() != 0.0) {
                                minmax[0] += band.getScalingOffset();
                                minmax[1] += band.getScalingOffset();
                            }
                            validExp = format("%s >= %.05f && %s <= %.05f", name, minmax[0], name, minmax[1]);
                        }
                        band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                    }
                    bandToVariableMap.put(band, variable);
                    band.setUnit(units);
                    band.setDescription(variable.getDescription());
                }
            } else if (variableRank == 3) {
                int angularBandIndex = 0;
                spectralBandIndex = -1;
                final int[] dimensions = variable.getShape();
                final int views = dimensions[0];
                final int height = dimensions[1];
                final int width = dimensions[2];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String description = variable.getShortName();

                    Variable view_angle = ncFile.findVariable("sensor_views_bands/view_angles");
                    Variable wvl = ncFile.findVariable("sensor_views_bands/intensity_wavelengths");
                    Variable wvl_sliced = null;

                    if (view_angle != null && wvl != null) {
                        try {
                            view_angles = view_angle.read();
                            try {
                                wvl_sliced = wvl.slice(0,0);
                            } catch (InvalidRangeException e) {
                                e.printStackTrace();  //Todo change body of catch statement.
                            }
                            wavelengths = wvl_sliced.read();
                        } catch (IOException e) {
                        }
                        ArrayList wavelength_list = new ArrayList();
                        for (int i = 0; i < views; i++) {
                            StringBuilder longname = new StringBuilder(description);
                            longname.append("_");
                            longname.append(view_angles.getInt(i));
                            longname.append("_");
                            longname.append(wavelengths.getInt(i));
                            String name = longname.toString();
                            String safeName = (name != null && name.contains("-")) ? "'" + name + "'" : name;

                            final int dataType = getProductDataType(variable);
                            band = new Band(name, dataType, width, height);
                            product.addBand(band);

                            band.setSpectralWavelength(wavelengths.getFloat(i));
                            if (!wavelength_list.contains(wavelengths.getInt(i))) {
                                wavelength_list.add(wavelengths.getInt(i));
                                angularBandIndex = 0;
                                spectralBandIndex++;
                            }
                            band.setSpectralBandIndex(spectralBandIndex);

                            band.setAngularValue(view_angles.getFloat(i));
                            band.setAngularBandIndex(angularBandIndex++); // should angularBandIndex be 0 - 89 or 0 - 9 (0 -59)?

                            Variable sliced = null;
                            try {
                                sliced = variable.slice(0, i);
                            } catch (InvalidRangeException e) {
                                e.printStackTrace();  //Todo change body of catch statement.
                            }

                            final List<Attribute> list = variable.getAttributes();
                            double[] validMinMax = {0.0, 0.0};
                            for (Attribute hdfAttribute : list) {
                                final String attribName = hdfAttribute.getShortName();
                                if ("units".equals(attribName)) {
                                    band.setUnit(hdfAttribute.getStringValue());
                                } else if ("long_name".equals(attribName)) {
                                    band.setDescription(hdfAttribute.getStringValue());
                                } else if ("slope".equals(attribName)) {
                                    band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("intercept".equals(attribName)) {
                                    band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("scale_factor".equals(attribName)) {
                                    band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("add_offset".equals(attribName)) {
                                    band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("bad_value_scaled".equals(attribName)) {
                                    band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                    band.setNoDataValueUsed(true);
                                } else if ("_FillValue".equals(attribName)) {
                                band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                band.setNoDataValueUsed(true);
                                } else if (attribName.startsWith("valid_")) {
                                    if ("valid_min".equals(attribName)) {
                                        if (hdfAttribute.getDataType().isUnsigned()) {
                                            validMinMax[0] = getUShortAttribute(hdfAttribute);
                                        } else {
                                            validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                        }
                                    } else if ("valid_max".equals(attribName)) {
                                        if (hdfAttribute.getDataType().isUnsigned()) {
                                            validMinMax[1] = getUShortAttribute(hdfAttribute);
                                        } else {
                                            validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                        }
                                    } else if ("valid_range".equals(attribName)) {
                                        validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                        validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                                    }
                                }
                            }
                            if (validMinMax[0] != validMinMax[1]) {
                                String validExp;
                                if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                                    validExp = format("%s >= %.05f && %s <= %.05f", safeName, validMinMax[0], safeName, validMinMax[1]);

                                } else {
                                    double[] minmax = {0.0, 0.0};
                                    minmax[0] = validMinMax[0];
                                    minmax[1] = validMinMax[1];

                                    if (band.getScalingFactor() != 1.0) {
                                        minmax[0] *= band.getScalingFactor();
                                        minmax[1] *= band.getScalingFactor();
                                    }
                                    if (band.getScalingOffset() != 0.0) {
                                        minmax[0] += band.getScalingOffset();
                                        minmax[1] += band.getScalingOffset();
                                    }
                                    validExp = format("%s >= %.05f && %s <= %.05f", safeName, minmax[0], safeName, minmax[1]);
                                }
                                band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                            }
                            bandToVariableMap.put(band, sliced);
                            band.setUnit(units);
                            band.setDescription(description);
                        }
                    }
                }
            }


        }
        return bandToVariableMap;
    }

    private Map<Band, Variable> addOciBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band;

        Array wavelengths = null;
        Array view_angles = null;

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            if (variable.getParentGroup().equals("sensor_views_bands"))
                continue;
            if ((variable.getShortName().equals("latitude")) || (variable.getShortName().equals("longitude")))
                continue;
            int variableRank = variable.getRank();

            if (variableRank == 2) {
                final int[] dimensions = variable.getShape();
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String name = variable.getShortName();
                    final int dataType = getProductDataType(variable);
                    band = new Band(name, dataType, width, height);
                    product.addBand(band);

                    final List<Attribute> list = variable.getAttributes();
                    double[] validMinMax = {0.0, 0.0};
                    for (Attribute hdfAttribute : list) {
                        final String attribName = hdfAttribute.getShortName();
                        if ("units".equals(attribName)) {
                            band.setUnit(hdfAttribute.getStringValue());
                        } else if ("long_name".equals(attribName)) {
                            band.setDescription(hdfAttribute.getStringValue());
                        } else if ("slope".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("intercept".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("scale_factor".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("add_offset".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("bad_value_scaled".equals(attribName)) {
                            band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                            band.setNoDataValueUsed(true);
                        } else if ("_FillValue".equals(attribName)) {
                            band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                            band.setNoDataValueUsed(true);
                        } else if (attribName.startsWith("valid_")) {
                            if ("valid_min".equals(attribName)) {
                                if (hdfAttribute.getDataType().isUnsigned()) {
                                    validMinMax[0] = getUShortAttribute(hdfAttribute);
                                } else {
                                    validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_max".equals(attribName)) {
                                if (hdfAttribute.getDataType().isUnsigned()) {
                                    validMinMax[1] = getUShortAttribute(hdfAttribute);
                                } else {
                                    validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_range".equals(attribName)) {
                                validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                            }
                        }
                    }
                    if (validMinMax[0] != validMinMax[1]) {
                        String validExp;
                        if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                            validExp = format("%s >= %.05f && %s <= %.05f", name, validMinMax[0], name, validMinMax[1]);

                        } else {
                            double[] minmax = {0.0, 0.0};
                            minmax[0] = validMinMax[0];
                            minmax[1] = validMinMax[1];

                            if (band.getScalingFactor() != 1.0) {
                                minmax[0] *= band.getScalingFactor();
                                minmax[1] *= band.getScalingFactor();
                            }
                            if (band.getScalingOffset() != 0.0) {
                                minmax[0] += band.getScalingOffset();
                                minmax[1] += band.getScalingOffset();
                            }
                            validExp = format("%s >= %.05f && %s <= %.05f", name, minmax[0], name, minmax[1]);
                        }
                        band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                    }
                    bandToVariableMap.put(band, variable);
                    band.setUnit(units);
                    band.setDescription(variable.getDescription());
                }
            } else if (variableRank == 3) {
                int angularBandIndex = 0;

                final int[] dimensions = variable.getShape();
                final int views = dimensions[2];
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String description = variable.getShortName();

                    Variable view_angle = ncFile.findVariable("sensor_views_bands/view_angles");

                    if (view_angle != null) {
                        try {
                            view_angles = view_angle.read();
                        } catch (IOException e) {
                        }

                        for (int i = 0; i < views; i++) {
                            StringBuilder longname = new StringBuilder(description);
                            longname.append("_");
                            longname.append(view_angles.getInt(i));
                            String name = longname.toString();
                            String safeName = (name != null && name.contains("-")) ? "'" + name + "'" : name;

                            final int dataType = getProductDataType(variable);
                            band = new Band(name, dataType, width, height);
                            product.addBand(band);

                            band.setAngularValue(view_angles.getFloat(i));
                            band.setAngularBandIndex(angularBandIndex++);

                            Variable sliced = null;
                            try {
                                sliced = variable.slice(2, i);
                            } catch (InvalidRangeException e) {
                                e.printStackTrace();  //Todo change body of catch statement.
                            }

                            final List<Attribute> list = variable.getAttributes();
                            double[] validMinMax = {0.0, 0.0};
                            for (Attribute hdfAttribute : list) {
                                final String attribName = hdfAttribute.getShortName();
                                if ("units".equals(attribName)) {
                                    band.setUnit(hdfAttribute.getStringValue());
                                } else if ("long_name".equals(attribName)) {
                                    band.setDescription(hdfAttribute.getStringValue());
                                } else if ("slope".equals(attribName)) {
                                    band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("intercept".equals(attribName)) {
                                    band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("scale_factor".equals(attribName)) {
                                    band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("add_offset".equals(attribName)) {
                                    band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("bad_value_scaled".equals(attribName)) {
                                    band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                    band.setNoDataValueUsed(true);
                                } else if ("_FillValue".equals(attribName)) {
                                    band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                    band.setNoDataValueUsed(true);
                                } else if (attribName.startsWith("valid_")) {
                                    if ("valid_min".equals(attribName)) {
                                        if (hdfAttribute.getDataType().isUnsigned()) {
                                            validMinMax[0] = getUShortAttribute(hdfAttribute);
                                        } else {
                                            validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                        }
                                    } else if ("valid_max".equals(attribName)) {
                                        if (hdfAttribute.getDataType().isUnsigned()) {
                                            validMinMax[1] = getUShortAttribute(hdfAttribute);
                                        } else {
                                            validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                        }
                                    } else if ("valid_range".equals(attribName)) {
                                        validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                        validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                                    }
                                }
                            }
                            if (validMinMax[0] != validMinMax[1]) {
                                String validExp;
                                if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                                    validExp = format("%s >= %.05f && %s <= %.05f", safeName, validMinMax[0], safeName, validMinMax[1]);

                                } else {
                                    double[] minmax = {0.0, 0.0};
                                    minmax[0] = validMinMax[0];
                                    minmax[1] = validMinMax[1];

                                    if (band.getScalingFactor() != 1.0) {
                                        minmax[0] *= band.getScalingFactor();
                                        minmax[1] *= band.getScalingFactor();
                                    }
                                    if (band.getScalingOffset() != 0.0) {
                                        minmax[0] += band.getScalingOffset();
                                        minmax[1] += band.getScalingOffset();
                                    }
                                    validExp = format("%s >= %.05f && %s <= %.05f", safeName, minmax[0], safeName, minmax[1]);
                                }
                                band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                            }
                            bandToVariableMap.put(band, sliced);
                            band.setUnit(units);
                            band.setDescription(description);
                        }
                    }
                }
            } else if (variableRank == 4) {
                final int[] dimensions = variable.getShape();
                final int views = dimensions[2];
                final int height = dimensions[0];
                final int width = dimensions[1];
                final int bands = dimensions[3];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {

                    String units = variable.getUnitsString();
                    String description = variable.getShortName();

                    Variable view_angle = ncFile.findVariable("sensor_views_bands/view_angles");
                    Variable wvl = ncFile.findVariable("sensor_views_bands/intensity_wavelengths");
                    Variable wvl_sliced = null;

                    if (view_angle != null && wvl != null) {
                        try {
                            view_angles = view_angle.read();
                            try {
                                wvl_sliced = wvl.slice(0,1);
                            } catch (InvalidRangeException e) {
                                e.printStackTrace();  //Todo change body of catch statement.
                            }
                            wavelengths = wvl_sliced.read();
                            wavelengths.setInt(120, wavelengths.getInt(120) + 1);
                            wavelengths.setInt(121, wavelengths.getInt(121) + 1);
                            wavelengths.setInt(122, wavelengths.getInt(122) + 1);
                            wavelengths.setInt(123, wavelengths.getInt(123) + 1);
                            wavelengths.setInt(243, wavelengths.getInt(243) + 1);
                            wavelengths.setInt(246, wavelengths.getInt(246) + 1);

                        } catch (IOException e) {
                        }


                        for (int i = 0; i < views; i++) {
                            for (int j = 0; j < bands; j++) {
                                StringBuilder longname = new StringBuilder(description);
                                longname.append("_");
                                longname.append(view_angles.getInt(i));
                                longname.append("_");
                                longname.append(wavelengths.getInt(j));
                                String name = longname.toString();
                                String safeName = (name != null && name.contains("-")) ? "'" + name + "'" : name;

                                final int dataType = getProductDataType(variable);
                                band = new Band(name, dataType, width, height);
                                product.addBand(band);

                                band.setSpectralWavelength(wavelengths.getFloat(j));
                                band.setSpectralBandIndex(j);

                                band.setAngularValue(view_angles.getFloat(i));
                                band.setAngularBandIndex(i);

                                Variable sliced1 = null;
                                Variable sliced = null;
                                try {
                                    sliced1 = variable.slice(2, i);
                                    sliced = sliced1.slice(2, j);
                                } catch (InvalidRangeException e) {
                                    e.printStackTrace();  //Todo change body of catch statement.
                                }

                                final List<Attribute> list = variable.getAttributes();
                                double[] validMinMax = {0.0, 0.0};
                                for (Attribute hdfAttribute : list) {
                                    final String attribName = hdfAttribute.getShortName();
                                    if ("units".equals(attribName)) {
                                        band.setUnit(hdfAttribute.getStringValue());
                                    } else if ("long_name".equals(attribName)) {
                                        band.setDescription(hdfAttribute.getStringValue());
                                    } else if ("slope".equals(attribName)) {
                                        band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("intercept".equals(attribName)) {
                                        band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("scale_factor".equals(attribName)) {
                                        band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("add_offset".equals(attribName)) {
                                        band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("bad_value_scaled".equals(attribName)) {
                                        band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                        band.setNoDataValueUsed(true);
                                    } else if ("_FillValue".equals(attribName)) {
                                        band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                        band.setNoDataValueUsed(true);
                                    }  else if (attribName.startsWith("valid_")) {
                                        if ("valid_min".equals(attribName)) {
                                            if (hdfAttribute.getDataType().isUnsigned()) {
                                                validMinMax[0] = getUShortAttribute(hdfAttribute);
                                            } else {
                                                validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                            }
                                        } else if ("valid_max".equals(attribName)) {
                                            if (hdfAttribute.getDataType().isUnsigned()) {
                                                validMinMax[1] = getUShortAttribute(hdfAttribute);
                                            } else {
                                                validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                            }
                                        } else if ("valid_range".equals(attribName)) {
                                            validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                            validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                                        }
                                    }
                                }
                                if (validMinMax[0] != validMinMax[1]) {
                                    String validExp;
                                    if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                                        validExp = format("%s >= %.05f && %s <= %.05f", safeName, validMinMax[0], safeName, validMinMax[1]);

                                    } else {
                                        double[] minmax = {0.0, 0.0};
                                        minmax[0] = validMinMax[0];
                                        minmax[1] = validMinMax[1];

                                        if (band.getScalingFactor() != 1.0) {
                                            minmax[0] *= band.getScalingFactor();
                                            minmax[1] *= band.getScalingFactor();
                                        }
                                        if (band.getScalingOffset() != 0.0) {
                                            minmax[0] += band.getScalingOffset();
                                            minmax[1] += band.getScalingOffset();
                                        }
                                        validExp = format("%s >= %.05f && %s <= %.05f", safeName, minmax[0], safeName, minmax[1]);
                                    }
                                    band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                                }
                                bandToVariableMap.put(band, sliced);
                                band.setUnit(units);
                                band.setDescription(description);
                            }
                        }
                    }
                }
            }


        }
        return bandToVariableMap;
    }

    private Map<Band, Variable> addSPEXoneBands(Product product, List<Variable> variables) {
        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        Band band;

        Array wavelengths = null;
        Array wavelengths_pol = null;
        Array view_angles = null;

        Map<Band, Variable> bandToVariableMap = new HashMap<Band, Variable>();
        int spectralBandIndex = 0;
        for (Variable variable : variables) {
            if (variable.getParentGroup().equals("sensor_views_bands"))
                continue;
            if ((variable.getShortName().equals("latitude")) || (variable.getShortName().equals("longitude")))
                continue;
            int variableRank = variable.getRank();

            if (variableRank == 2) {
                final int[] dimensions = variable.getShape();
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String name = variable.getShortName();
                    final int dataType = getProductDataType(variable);
                    band = new Band(name, dataType, width, height);
                    product.addBand(band);

                    final List<Attribute> list = variable.getAttributes();
                    double[] validMinMax = {0.0, 0.0};
                    for (Attribute hdfAttribute : list) {
                        final String attribName = hdfAttribute.getShortName();
                        if ("units".equals(attribName)) {
                            band.setUnit(hdfAttribute.getStringValue());
                        } else if ("long_name".equals(attribName)) {
                            band.setDescription(hdfAttribute.getStringValue());
                        } else if ("slope".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("intercept".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("scale_factor".equals(attribName)) {
                            band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("add_offset".equals(attribName)) {
                            band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                        } else if ("bad_value_scaled".equals(attribName)) {
                            band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                            band.setNoDataValueUsed(true);
                        } else if ("_FillValue".equals(attribName)) {
                            band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                            band.setNoDataValueUsed(true);
                        } else if (attribName.startsWith("valid_")) {
                            if ("valid_min".equals(attribName)) {
                                if (hdfAttribute.getDataType().isUnsigned()) {
                                    validMinMax[0] = getUShortAttribute(hdfAttribute);
                                } else {
                                    validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_max".equals(attribName)) {
                                if (hdfAttribute.getDataType().isUnsigned()) {
                                    validMinMax[1] = getUShortAttribute(hdfAttribute);
                                } else {
                                    validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                }
                            } else if ("valid_range".equals(attribName)) {
                                validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                            }
                        }
                    }
                    if (validMinMax[0] != validMinMax[1]) {
                        String validExp;
                        if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                            validExp = format("%s >= %.05f && %s <= %.05f", name, validMinMax[0], name, validMinMax[1]);

                        } else {
                            double[] minmax = {0.0, 0.0};
                            minmax[0] = validMinMax[0];
                            minmax[1] = validMinMax[1];

                            if (band.getScalingFactor() != 1.0) {
                                minmax[0] *= band.getScalingFactor();
                                minmax[1] *= band.getScalingFactor();
                            }
                            if (band.getScalingOffset() != 0.0) {
                                minmax[0] += band.getScalingOffset();
                                minmax[1] += band.getScalingOffset();
                            }
                            validExp = format("%s >= %.05f && %s <= %.05f", name, minmax[0],name, minmax[1]);
                        }
                        band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                    }
                    bandToVariableMap.put(band, variable);
                    band.setUnit(units);
                    band.setDescription(variable.getDescription());
                }
            } else if (variableRank == 3) {
                int angularBandIndex = 0;

                final int[] dimensions = variable.getShape();
                final int views = dimensions[2];
                final int height = dimensions[0];
                final int width = dimensions[1];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {
                    // final List<Attribute> list = variable.getAttributes();

                    String units = variable.getUnitsString();
                    String description = variable.getShortName();

                    Variable view_angle = ncFile.findVariable("sensor_views_bands/view_angles");

                    if (view_angle != null) {
                        try {
                            view_angles = view_angle.read();
                        } catch (IOException e) {
                        }

                        for (int i = 0; i < views; i++) {
                            StringBuilder longname = new StringBuilder(description);
                            longname.append("_");
                            if ((i > views / 2) && (view_angles.getInt(i) == view_angles.getInt(views - 1 - i))) {
                                longname.append(-view_angles.getInt(i));
                            } else {
                                longname.append(view_angles.getInt(i));
                            }
                            String name = longname.toString();
                            String safeName = (name != null && name.contains("-")) ? "'" + name + "'" : name;
                            final int dataType = getProductDataType(variable);
                            band = new Band(name, dataType, width, height);
                            product.addBand(band);

                            if ((i > views / 2) && (view_angles.getInt(i) == view_angles.getInt(views - 1 - i))) {
                                band.setAngularValue(-view_angles.getFloat(i));
                            } else {
                                band.setAngularValue(view_angles.getFloat(i));
                            }
                            band.setAngularBandIndex(angularBandIndex++);

                            Variable sliced = null;
                            try {
                                sliced = variable.slice(2, i);
                            } catch (InvalidRangeException e) {
                                e.printStackTrace();  //Todo change body of catch statement.
                            }

                            final List<Attribute> list = variable.getAttributes();
                            double[] validMinMax = {0.0, 0.0};
                            for (Attribute hdfAttribute : list) {
                                final String attribName = hdfAttribute.getShortName();
                                if ("units".equals(attribName)) {
                                    band.setUnit(hdfAttribute.getStringValue());
                                } else if ("long_name".equals(attribName)) {
                                    band.setDescription(hdfAttribute.getStringValue());
                                } else if ("slope".equals(attribName)) {
                                    band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("intercept".equals(attribName)) {
                                    band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("scale_factor".equals(attribName)) {
                                    band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("add_offset".equals(attribName)) {
                                    band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                } else if ("bad_value_scaled".equals(attribName)) {
                                    band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                    band.setNoDataValueUsed(true);
                                } else if ("_FillValue".equals(attribName)) {
                                    band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                    band.setNoDataValueUsed(true);
                                } else if (attribName.startsWith("valid_")) {
                                    if ("valid_min".equals(attribName)) {
                                        if (hdfAttribute.getDataType().isUnsigned()) {
                                            validMinMax[0] = getUShortAttribute(hdfAttribute);
                                        } else {
                                            validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                        }
                                    } else if ("valid_max".equals(attribName)) {
                                        if (hdfAttribute.getDataType().isUnsigned()) {
                                            validMinMax[1] = getUShortAttribute(hdfAttribute);
                                        } else {
                                            validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                        }
                                    } else if ("valid_range".equals(attribName)) {
                                        validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                        validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                                    }
                                }
                            }
                            if (validMinMax[0] != validMinMax[1]) {
                                String validExp;
                                if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                                    validExp = format("%s >= %.05f && %s <= %.05f", safeName, validMinMax[0], safeName, validMinMax[1]);

                                } else {
                                    double[] minmax = {0.0, 0.0};
                                    minmax[0] = validMinMax[0];
                                    minmax[1] = validMinMax[1];

                                    if (band.getScalingFactor() != 1.0) {
                                        minmax[0] *= band.getScalingFactor();
                                        minmax[1] *= band.getScalingFactor();
                                    }
                                    if (band.getScalingOffset() != 0.0) {
                                        minmax[0] += band.getScalingOffset();
                                        minmax[1] += band.getScalingOffset();
                                    }
                                    validExp = format("%s >= %.05f && %s <= %.05f", safeName, minmax[0], safeName, minmax[1]);
                                }
                                band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                            }
                            bandToVariableMap.put(band, sliced);
                            band.setUnit(units);
                            band.setDescription(description);
                        }
                    }
                }
            } else if (variableRank == 4) {
                final int[] dimensions = variable.getShape();
                final int views = dimensions[2];
                final int height = dimensions[0];
                final int width = dimensions[1];
                final int bands = dimensions[3];

                if (height == sceneRasterHeight && width == sceneRasterWidth) {

                    String units = variable.getUnitsString();
                    String description = variable.getShortName();

                    Variable view_angle = ncFile.findVariable("sensor_views_bands/view_angles");
                    Variable wvl = ncFile.findVariable("sensor_views_bands/intensity_wavelengths");
                    Variable wvl_sliced = null;
                    Variable wvl_pol = ncFile.findVariable("sensor_views_bands/polarization_wavelengths");
                    Variable wvl_sliced_pol = null;

                    if (view_angle != null && wvl != null) {
                        try {
                            view_angles = view_angle.read();
                            try {
                                wvl_sliced = wvl.slice(0,1);
                                wvl_sliced_pol = wvl_pol.slice(0,1);
                            } catch (InvalidRangeException e) {
                                e.printStackTrace();  //Todo change body of catch statement.
                            }
                            wavelengths = wvl_sliced.read();
                            wavelengths_pol = wvl_sliced_pol.read();
//                            wavelengths.setInt(120, wavelengths.getInt(120) + 1);
//                            wavelengths.setInt(121, wavelengths.getInt(121) + 1);
//                            wavelengths.setInt(122, wavelengths.getInt(122) + 1);
//                            wavelengths.setInt(123, wavelengths.getInt(123) + 1);
//                            wavelengths.setInt(243, wavelengths.getInt(243) + 1);
//                            wavelengths.setInt(246, wavelengths.getInt(246) + 1);

                        } catch (IOException e) {
                        }


                        for (int i = 0; i < views; i++) {
                            for (int j = 0; j < bands; j++) {
//                                spectralBandIndex = 0;
                                StringBuilder longname = new StringBuilder(description);
                                longname.append("_");
                                if ((i > views / 2) && (view_angles.getInt(i) == view_angles.getInt(views - 1 - i))) {
                                    longname.append(-view_angles.getInt(i));
                                } else {
                                    longname.append(view_angles.getInt(i));
                                }
                                longname.append("_");
                                if (bands == 400) {
                                    longname.append(wavelengths.getInt(j));
                                } else {
                                    longname.append(wavelengths_pol.getInt(j));
                                }
                                String name = longname.toString();
                                String safeName = (name != null && name.contains("-")) ? "'" + name + "'" : name;
                                final int dataType = getProductDataType(variable);
                                band = new Band(name, dataType, width, height);
                                product.addBand(band);

                                if (bands == 400) {
                                    band.setSpectralWavelength(wavelengths.getFloat(j));
                                } else {
                                    band.setSpectralWavelength(wavelengths_pol.getFloat(j));
                                }
                                band.setSpectralBandIndex(j);

                                if ((i > views / 2) && (view_angles.getInt(i) == view_angles.getInt(views - 1 - i))) {
                                    band.setAngularValue(-view_angles.getFloat(i));
                                } else {
                                    band.setAngularValue(view_angles.getFloat(i));
                                }
                                band.setAngularBandIndex(i);

                                Variable sliced1 = null;
                                Variable sliced = null;
                                try {
                                    sliced1 = variable.slice(2, i);
                                    sliced = sliced1.slice(2, j);
                                } catch (InvalidRangeException e) {
                                    e.printStackTrace();  //Todo change body of catch statement.
                                }

                                final List<Attribute> list = variable.getAttributes();
                                double[] validMinMax = {0.0, 0.0};
                                for (Attribute hdfAttribute : list) {
                                    final String attribName = hdfAttribute.getShortName();
                                    if ("units".equals(attribName)) {
                                        band.setUnit(hdfAttribute.getStringValue());
                                    } else if ("long_name".equals(attribName)) {
                                        band.setDescription(hdfAttribute.getStringValue());
                                    } else if ("slope".equals(attribName)) {
                                        band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("intercept".equals(attribName)) {
                                        band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("scale_factor".equals(attribName)) {
                                        band.setScalingFactor(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("add_offset".equals(attribName)) {
                                        band.setScalingOffset(hdfAttribute.getNumericValue(0).doubleValue());
                                    } else if ("bad_value_scaled".equals(attribName)) {
                                        band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                        band.setNoDataValueUsed(true);
                                    } else if ("_FillValue".equals(attribName)) {
                                        band.setNoDataValue(hdfAttribute.getNumericValue(0).doubleValue());
                                        band.setNoDataValueUsed(true);
                                    } else if (attribName.startsWith("valid_")) {
                                        if ("valid_min".equals(attribName)) {
                                            if (hdfAttribute.getDataType().isUnsigned()) {
                                                validMinMax[0] = getUShortAttribute(hdfAttribute);
                                            } else {
                                                validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                            }
                                        } else if ("valid_max".equals(attribName)) {
                                            if (hdfAttribute.getDataType().isUnsigned()) {
                                                validMinMax[1] = getUShortAttribute(hdfAttribute);
                                            } else {
                                                validMinMax[1] = hdfAttribute.getNumericValue(0).doubleValue();
                                            }
                                        } else if ("valid_range".equals(attribName)) {
                                            validMinMax[0] = hdfAttribute.getNumericValue(0).doubleValue();
                                            validMinMax[1] = hdfAttribute.getNumericValue(1).doubleValue();
                                        }
                                    }
                                }
                                if (validMinMax[0] != validMinMax[1]) {
                                    String validExp;
                                    if (ncFile.getFileTypeId().equalsIgnoreCase("HDF4")) {
                                        validExp = format("%s >= %.05f && %s <= %.05f", safeName, validMinMax[0],safeName, validMinMax[1]);

                                    } else {
                                        double[] minmax = {0.0, 0.0};
                                        minmax[0] = validMinMax[0];
                                        minmax[1] = validMinMax[1];

                                        if (band.getScalingFactor() != 1.0) {
                                            minmax[0] *= band.getScalingFactor();
                                            minmax[1] *= band.getScalingFactor();
                                        }
                                        if (band.getScalingOffset() != 0.0) {
                                            minmax[0] += band.getScalingOffset();
                                            minmax[1] += band.getScalingOffset();
                                        }
                                        validExp = format("%s >= %.05f && %s <= %.05f", safeName, minmax[0], safeName, minmax[1]);
                                    }
                                    band.setValidPixelExpression(validExp);//.format(name, validMinMax[0], name, validMinMax[1]));
                                }
                                bandToVariableMap.put(band, sliced);
                                band.setUnit(units);
                                band.setDescription(description);
                            }
                        }
                    }
                }
            }


        }
        return bandToVariableMap;
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

    public void addGeocoding(final Product product) throws ProductIOException {
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

            product.setSceneGeoCoding(GeoCodingFactory.createPixelGeoCoding(latBand, lonBand, null, 5));

        }
    }
}