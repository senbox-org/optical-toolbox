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
package eu.esa.opt.meris.smac;

import org.esa.snap.core.util.Guardian;
import org.esa.snap.dataio.envisat.EnvisatConstants;

import java.util.regex.Pattern;

class SmacUtils {

    private static Pattern AATSR_L1_TOA_TYPE_PATTERN = Pattern.compile("ATS_TOA_1P");

    /**
     * Converts the sensor type given by the request to a string that can be understood by the
     * <code>SensorCoefficientManager</code>.
     *
     * @param productType the request type string, or <code>null</code> on unsupported input product type
     */
    public static String getSensorType(String productType) {
        Guardian.assertNotNull("productType", productType);

        if (isSupportedAatsrProductType(productType)) {
            return SensorCoefficientManager.AATSR_NAME;
        } else if (isSupportedMerisProductType(productType)) {
            return SensorCoefficientManager.MERIS_NAME;
        }
        return null;
    }

    public static boolean isSupportedMerisProductType(String productType) {
        return EnvisatConstants.MERIS_L1_TYPE_PATTERN.matcher(productType).matches();
    }

    public static boolean isSupportedAatsrProductType(String productType) {
        return AATSR_L1_TOA_TYPE_PATTERN.matcher(productType).matches();
    }

    public static boolean isSupportedProductType(String productType) {
        Guardian.assertNotNull("productType", productType);
        return isSupportedAatsrProductType(productType) || isSupportedMerisProductType(productType);
    }
}
