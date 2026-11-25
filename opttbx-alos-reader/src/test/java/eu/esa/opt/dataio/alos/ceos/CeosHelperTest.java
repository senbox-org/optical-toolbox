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

package eu.esa.opt.dataio.alos.ceos;

import com.bc.ceres.core.runtime.internal.IOHelper;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
public class CeosHelperTest {

    @Test
    public void testGetFileFromInput() {
        String inputString = "testFile";

        Path result = CeosHelper.getFileFromInput(inputString);
        assertNotNull(result);
        assertEquals(inputString, result.getFileName().toString());

        final Path inputFile = IOHelper.createATempDir().resolve("anotherTest");
        result = CeosHelper.getFileFromInput(inputFile);
        assertNotNull(result);
        assertEquals(inputFile.getFileName(), result.getFileName());

        result = CeosHelper.getFileFromInput(9.0);
        assertNull(result);
    }

}
