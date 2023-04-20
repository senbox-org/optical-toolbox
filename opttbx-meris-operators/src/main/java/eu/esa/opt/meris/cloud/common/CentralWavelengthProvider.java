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
package eu.esa.opt.meris.cloud.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides central wavelengths for meris channel 11.
 * MER_RR could be read from an auxfile. MER_FR is interpolated from them.
 */
public class CentralWavelengthProvider {
    private static final String CENTRAL_WAVELENGTH_FILE_NAME = "central_wvl_rr.txt";
    private static final int DETECTOR_LENGTH_RR = 925;
    private static final int DETECTOR_LENGTH_FR = 3700;

    private final float[] centralWavelengthRr = new float[DETECTOR_LENGTH_RR];

    /**
     * Read the RR central-wavelength from a file in the given directory.
     *
     * @param auxDataDir the auxdata directory from which the cw are read.
     * @throws IOException in case of an IO error
     */
    public void readAuxData(Path auxDataDir) throws IOException {
        try (InputStream inputStream = Files.newInputStream(auxDataDir.resolve(CENTRAL_WAVELENGTH_FILE_NAME))) {
            readCW(inputStream);
        }
    }

    /**
     * Read the RR central-wavelength from a file in the given directory.
     *
     * @param auxDataDir the auxdata directory from which the cw are read.
     * @throws IOException in case of an IO error
     * @deprecated since OPTTBX 10.0; use {@link #readAuxData(Path)} instead
     */
    @Deprecated
    public void readAuxData(File auxDataDir) throws IOException {
        readAuxData(auxDataDir.toPath());
    }

    void readCW(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        for (int i = 0; i < centralWavelengthRr.length; i++) {
            String line = bufferedReader.readLine();
            line = line.trim();
            centralWavelengthRr[i] = Float.parseFloat(line);
        }
    }

    /**
     * Returns an float array with central wavelength. Depending on the
     * product type this is either RR or FR.
     *
     * @param productType A textual representation of the product type.
     * @return float array with central wavelengths.
     */
    public float[] getCentralWavelength(final String productType) {
        if (productType.startsWith("MER_RR")) {
            return centralWavelengthRr;
        } else if (productType.startsWith("MER_F")) {
            return generateCentralWavelengthFr();
        } else {
            throw new IllegalArgumentException("'The product has an unsupported product type: " + productType);
        }
    }

    /**
     * Generates the central wavelength for FR by interpolating
     * from the RR wavelength.
     *
     * @return FR central wavelength
     */
    private float[] generateCentralWavelengthFr() {
        float[] cwFr = new float[DETECTOR_LENGTH_FR];
        for (int camera = 0; camera < 5; camera++) {
            final int frCameraOffset = camera * 740;
            final int rrCameraOffset = camera * 185;
            for (int frIdx = 0; frIdx < 740; frIdx++) {
                final float vector = frIdx / 4f + 0.125f;
                final float rrVector = 0.5f + vector;
                int rrIndex = (int) rrVector;
                float weight = rrVector % 1;
                if (rrIndex == 0) {
                    rrIndex += 1;
                    weight -= 1;
                }
                if (rrIndex >= 185) {
                    rrIndex = 184;
                    weight += 1;
                }
                final float x1 = centralWavelengthRr[rrCameraOffset + rrIndex - 1];
                final float x2 = centralWavelengthRr[rrCameraOffset + rrIndex];
                cwFr[frIdx + frCameraOffset] = x1 + (x2 - x1) * weight;
            }
        }
        return cwFr;
    }
}
