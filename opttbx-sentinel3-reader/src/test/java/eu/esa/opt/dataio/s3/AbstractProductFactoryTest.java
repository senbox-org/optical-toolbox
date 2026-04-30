/*
 * Copyright (c) 2021.  Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.meris.MerisProductFactory;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import org.esa.snap.core.datamodel.Band;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AbstractProductFactoryTest {

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testRemoveLeadingSlash() {
        final ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("radiance.nc");
        fileNames.add("view_angle.nc");
        fileNames.add("./instrument_data.nc");

        final List<String> processedNames = AbstractProductFactory.removeLeadingSlash(fileNames);
        assertEquals(fileNames.size(), processedNames.size());
        assertEquals("radiance.nc", processedNames.get(0));
        assertEquals("view_angle.nc", processedNames.get(1));
        assertEquals("instrument_data.nc", processedNames.get(2));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testRemoveLeadingSlash_emptyInput() {
        final ArrayList<String> fileNames = new ArrayList<>();

        final List<String> processedNames = AbstractProductFactory.removeLeadingSlash(fileNames);
        assertEquals(0, processedNames.size());
    }


    @Test
    @STTM("SNAP-4200")
    public void test_tpgEntry_constructorStoresAllFields() {
        S3NetcdfReader reader = mock(S3NetcdfReader.class);
        Band band = mock(Band.class);

        AbstractProductFactory.TpgEntry entry = new AbstractProductFactory.TpgEntry(
                reader, band, 5, 10, 100, 200, "my_cache_key"
        );

        assertSame("reader", reader, entry.reader);
        assertSame("sourceBand", band, entry.sourceBand);
        assertEquals("dataOffsetX", 5, entry.dataOffsetX);
        assertEquals("dataOffsetY", 10, entry.dataOffsetY);
        assertEquals("gridWidth", 100, entry.gridWidth);
        assertEquals("gridHeight", 200, entry.gridHeight);
        assertEquals("cacheKey", "my_cache_key", entry.cacheKey);
    }

    @Test(expected = UnsupportedOperationException.class)
    @STTM("SNAP-4200")
    public void test_getBandCacheMap_returnsUnmodifiableView() {
        AbstractProductFactory factory = createMinimalFactory();
        factory.getBandCacheMap().put("anyBand", mock(S3NetcdfReader.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    @STTM("SNAP-4200")
    public void test_getTpgReaderMap_returnsUnmodifiableView() {
        AbstractProductFactory factory = createMinimalFactory();
        factory.getTpgReaderMap().put("anyTpg", mock(AbstractProductFactory.TpgEntry.class));
    }


    private static AbstractProductFactory createMinimalFactory() {
        Sentinel3ProductReader mockReader = mock(Sentinel3ProductReader.class);
        return new MerisProductFactory(mockReader);
    }
}
