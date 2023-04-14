/*
 *
 *  * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package eu.esa.opt.olci.radiometry.gasabsorption;

import com.bc.ceres.core.Assert;
import eu.esa.opt.olci.radiometry.smilecorr.SmileCorrectionUtils;

import java.util.ArrayList;

/**
 * @author muhammad.bc.
 */
public class GaseousAbsorptionAlgo {

    private float getAtmosphericGas(String bandName) {
        return 1;
    }

    private float getNormalizedConcentration(String bandName) {
        return 1;
    }

    float getExponential(float atmosphericGas, float normConcentration, float massAir) {
        final float calValue = -atmosphericGas * normConcentration * massAir;
        return (float) Math.exp(calValue);
    }

    String[] gasToComputeForBand(String bandName) {
        GasToCompute gasToCompute = GasToCompute.valueOf(bandName);
        return gasToCompute.getGasBandToCompute();
    }

    float[] getMassAir(float[] sza, float[] oza) {
        Assert.notNull(sza, "The sun zenith angel most not be null.");
        Assert.notNull(oza);
        float[] szaRad = SmileCorrectionUtils.convertDegreesToRadians(sza);
        float[] ozaRad = SmileCorrectionUtils.convertDegreesToRadians(oza);

        float[] massAirs = new float[sza.length];
        for (int i = 0; i < sza.length; i++) {
            massAirs[i] = getMassAir(szaRad[i], ozaRad[i]);
        }
        return massAirs;
    }

    private static float getMassAir(float szaRad, float ozaRad) {
        return (float) (1 / Math.cos(szaRad) + 1 / Math.cos(ozaRad));
    }

    float[] getTransmissionGas(String bandName, float[] sza, float[] oza) {
        float[] calMassAirs = getMassAir(sza, oza);
        String[] gasesToCompute = gasToComputeForBand(bandName);

        final ArrayList<float[]> arrayListExponential = new ArrayList<>();

        for (String gas : gasesToCompute) {
            final float calAtmosphericGas = getAtmosphericGas(gas);
            final float normalizedConcentration = getNormalizedConcentration(gas);
            final float[] calExponential = new float[oza.length];

            for (int i = 0; i < sza.length; i++) {
                calExponential[i] = getExponential(calAtmosphericGas, normalizedConcentration, calMassAirs[i]);
            }
            arrayListExponential.add(calExponential);
        }

        final int size = arrayListExponential.size();
        float[] transmissionGas = new float[0];
        if (size == 1) {
            transmissionGas = arrayListExponential.get(0);
        } else if (size == 2) {
            transmissionGas = SmileCorrectionUtils.multiple2ArrayFloat(arrayListExponential.get(0), arrayListExponential.get(1));
        } else if (size == 3) {
            transmissionGas = SmileCorrectionUtils.multiple3ArrayFloat(arrayListExponential.get(0), arrayListExponential.get(1), arrayListExponential.get(2));
        }
        return transmissionGas;
    }
}
