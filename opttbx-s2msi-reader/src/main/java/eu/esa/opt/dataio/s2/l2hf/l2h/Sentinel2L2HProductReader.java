/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2013-2015 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.dataio.s2.l2hf.l2h;

import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.l2hf.l2h.metadata.S2L2hProductMetadataReader;
import eu.esa.opt.dataio.s2.masks.MaskInfo;
import eu.esa.opt.dataio.s2.ortho.Sentinel2OrthoProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;

import java.io.IOException;

/**
 * <p>
 * This product reader can currently read single L2H tiles (also called L2H granules) and entire L2H scenes composed of
 * multiple L2H tiles.
 * </p>
 * <p>
 * To read single tiles, select any tile image file (IMG_*.jp2) within a product package. The reader will then
 * collect other band images for the selected tile and wiull also try to read the metadata file (MTD_*.xml).
 * </p>
 * <p>To read an entire scene, select the metadata file (MTD_*.xml) within a product package. The reader will then
 * collect other tile/band images and create a mosaic on the fly.
 * </p>
 *
 * @author Florian Douziech
 */
public class Sentinel2L2HProductReader extends Sentinel2OrthoProductReader {

    static final String L2H_CACHE_DIR = "l2h-reader";

    public Sentinel2L2HProductReader(ProductReaderPlugIn readerPlugIn, String epsgCode) {
        super(readerPlugIn, epsgCode);
    }

    @Override
    protected S2L2hProductMetadataReader buildMetadataReader(VirtualPath virtualPath) throws IOException {
        return new S2L2hProductMetadataReader(virtualPath, this.epsgCode);
    }

    @Override
    protected String getReaderCacheDir() {
        return L2H_CACHE_DIR;
    }

    @Override
    protected int getMaskLevel() {
        return MaskInfo.L2A;
    }
}
