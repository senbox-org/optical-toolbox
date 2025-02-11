package eu.esa.opt.dataio.s3.olci;/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.runtime.Config;

import java.util.prefs.Preferences;

import static eu.esa.opt.dataio.s3.util.S3Util.OLCI_L1_CALIBRATION_PATTERN;
import static eu.esa.opt.dataio.s3.util.S3Util.OLCI_L1_CUSTOM_CALIBRATION;

public class OlciLevel1ProductFactory extends OlciProductFactory {

    private final static String validExpression = "!quality_flags.invalid";

    static final String BAND_GROUPING_PATTERN = "Oa*_radiance:Oa*_radiance_unc:Oa*_radiance_err:atmospheric_temperature_profile:" +
            "lambda0:FWHM:solar_flux";

    public OlciLevel1ProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
    }

    @Override
    protected void setAutoGrouping(Product[] sourceProducts, Product targetProduct) {
        targetProduct.setAutoGrouping(BAND_GROUPING_PATTERN);
    }

    @Override
    protected String getValidExpression() {
        return validExpression;
    }

    private boolean applyCustomCalibration() {
        final Preferences preferences = loadPreferences();
        return preferences.getBoolean(OLCI_L1_CUSTOM_CALIBRATION, false);
    }

    private double getCalibrationOffset(String bandName) {
        String calibrationOffsetPropertyName = OLCI_L1_CALIBRATION_PATTERN
                .replace("ID", bandName.toLowerCase())
                .replace("TYPE", "offset");
        Preferences preferences = loadPreferences();
        return preferences.getDouble(calibrationOffsetPropertyName, Double.NaN);
    }

    private Preferences loadPreferences() {
        // @todo 2 tb/tb lazy loading 2025-02-11
        return Config.instance("opttbx").load().preferences();
    }

    protected double getCalibrationFactor(String bandName) {
        String calibrationFactorPropertyName = OLCI_L1_CALIBRATION_PATTERN
                .replace("ID", bandName.toLowerCase())
                .replace("TYPE", "factor");
        Preferences preferences = loadPreferences();
        return preferences.getDouble(calibrationFactorPropertyName, Double.NaN);
    }

    @Override
    protected void applyCustomCalibration(Band targetBand) {
        if (applyCustomCalibration()) {
            final double calibrationOffset = getCalibrationOffset(targetBand.getName());
            if (!Double.isNaN(calibrationOffset)) {
                targetBand.setScalingOffset(calibrationOffset);
            }
            final double calibrationFactor = getCalibrationFactor(targetBand.getName());
            if (!Double.isNaN(calibrationFactor)) {
                targetBand.setScalingFactor(calibrationFactor);
            }
        }
    }
}
