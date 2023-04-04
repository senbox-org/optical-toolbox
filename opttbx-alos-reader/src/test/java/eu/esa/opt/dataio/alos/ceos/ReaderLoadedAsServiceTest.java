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

import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.junit.Test;

import java.util.Iterator;

import static junit.framework.Assert.*;

public class ReaderLoadedAsServiceTest {

    @Test
    public void testReaderIsLoaded() {

        ProductIOPlugInManager plugInManager = ProductIOPlugInManager.getInstance();
        Iterator readerPlugIns;

        readerPlugIns = plugInManager.getReaderPlugIns("PRISM");
        testRegisteredReader(readerPlugIns, 1);

        readerPlugIns = plugInManager.getReaderPlugIns("AVNIR-2");
        testRegisteredReader(readerPlugIns, 1);

    }

    private void testRegisteredReader(Iterator readerPlugIns, int expectedReaderCount) {
        int readerCount = 0;
        while (readerPlugIns.hasNext()) {
            readerCount++;
            ProductReaderPlugIn plugIn = (ProductReaderPlugIn) readerPlugIns.next();
            System.out.println("readerPlugIn.Class = " + plugIn.getClass());
            System.out.println("readerPlugIn.Descr = " + plugIn.getDescription(null));
        }

        assertEquals(expectedReaderCount, readerCount);
    }

}
