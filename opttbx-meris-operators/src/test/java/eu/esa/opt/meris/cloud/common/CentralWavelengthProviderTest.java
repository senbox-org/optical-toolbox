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

import eu.esa.opt.meris.ModuleActivator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class CentralWavelengthProviderTest {

    private static final float DIFF = 0.000001f;
    private CentralWavelengthProvider provider;

    @BeforeClass
    public static void beforeClass() {
        ModuleActivator.activate();
    }

    @Before
    public void setUp() {
        provider = new CentralWavelengthProvider();
    }

    @Test
    public void testInit() {
        final float[] cwRR = provider.getCentralWavelength("MER_RR");
        assertEquals("925 members", 925, cwRR.length);
        assertEquals("value 0", 0f, cwRR[0], DIFF);
        assertEquals("value 1", 0f, cwRR[1], DIFF);
        assertEquals("value 924", 0f, cwRR[924], DIFF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalProductType() {
        provider.getCentralWavelength("MER_TT");
    }

    @Test
    public void testReadCW() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < 925; i++) {
            final String line = String.format("%d\n", i);
            outputStream.write(line.getBytes());
        }
        final byte[] outArray = outputStream.toByteArray();
        final InputStream inputStream = new ByteArrayInputStream(outArray);
        provider.readCW(inputStream);

        final float[] cwRR = provider.getCentralWavelength("MER_RR");
        assertEquals("925 members", 925, cwRR.length);
        assertEquals("value 0", 0f, cwRR[0], DIFF);
        assertEquals("value 1", 1f, cwRR[1], DIFF);
        assertEquals("value 2", 2f, cwRR[2], DIFF);
        assertEquals("value 924", 924f, cwRR[924], DIFF);
    }

    @Test
    public void testFr() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        float value = 1.5f;
        for (int i = 0; i < 925; i++) {
            final String line = String.valueOf(value) + "\n";
            outputStream.write(line.getBytes());
            value += 4;
        }
        final byte[] outArray = outputStream.toByteArray();
        final InputStream inputStream = new ByteArrayInputStream(outArray);
        provider.readCW(inputStream);
        final float[] cwFR = provider.getCentralWavelength("MER_FR");

        assertEquals("3700 members", 3700, cwFR.length);
        for (int i = 0; i < 3700; i++) {
            assertEquals("value " + i, i, cwFR[i], DIFF);
        }
    }

    @Test
    public void testFrDiscontinuiti() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int camera = 0; camera < 5; camera++) {
            float value = 1.5f;
            for (int i = 0; i < 185; i++) {
                final String line = String.valueOf(value) + "\n";
                outputStream.write(line.getBytes());
                value += 4;
            }
        }
        final byte[] outArray = outputStream.toByteArray();
        final InputStream inputStream = new ByteArrayInputStream(outArray);
        provider.readCW(inputStream);
        final float[] cwFR = provider.getCentralWavelength("MER_FR");

        assertEquals("3700 members", 3700, cwFR.length);
        int index=0;
        for (int camera = 0; camera < 5; camera++) {
            for (int value = 0; value < 740; value++) {
                assertEquals("value " + index, value, cwFR[index], DIFF);
                index++;
            }
        }
    }
}
