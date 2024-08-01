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
import org.esa.snap.core.util.StringUtils;

import static org.esa.snap.dataio.envisat.EnvisatConstants.AATSR_L1B_TOA_PRODUCT_TYPE_NAME;
import static org.esa.snap.dataio.envisat.EnvisatConstants.MERIS_L1_TYPE_PATTERN;

class SmacUtils {

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
        final String pattern = extractMerisPattern(productType);
        if (StringUtils.isNullOrEmpty(pattern)) {
            return false;
        }
        return MERIS_L1_TYPE_PATTERN.matcher(pattern).matches();
    }

    static String extractMerisPattern(String productType) {
        if (StringUtils.isNullOrEmpty(productType)) {
            return null;
        }

        final int patternIndex = productType.indexOf("MER_");
        if (patternIndex < 0) {
            return null;
        }

        return productType.substring(patternIndex, patternIndex + 10);
    }

    public static boolean isSupportedAatsrProductType(String productType) {
        return productType.contains(AATSR_L1B_TOA_PRODUCT_TYPE_NAME);
    }

    public static boolean isSupportedProductType(String productType) {
        Guardian.assertNotNull("productType", productType);
        return isSupportedAatsrProductType(productType) || isSupportedMerisProductType(productType);
    }
}
