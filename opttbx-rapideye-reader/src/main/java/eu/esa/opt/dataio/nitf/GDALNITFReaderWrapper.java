/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2014-2015 CS-Romania (office@c-s.ro)
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

package eu.esa.opt.dataio.nitf;

import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.dataio.gdal.reader.plugins.NITFDriverProductReaderPlugIn;
import java.io.File;
import java.io.IOException;

/**
 * Wrapper class over the nitro-nitf reader.
 *
 * @author CSRO
 */
public class GDALNITFReaderWrapper {

    private static final NITFDriverProductReaderPlugIn readerPlugin = new NITFDriverProductReaderPlugIn();

    private final ProductReader reader;

    private final Product bandProduct;

    public GDALNITFReaderWrapper(File file) throws IOException {
        reader = readerPlugin.createReaderInstance();
        bandProduct =  reader.readProductNodes(file, null);
    }

    public ProductReader getReader(){
        return reader;
    }

    public Product getBandProduct(){
        return bandProduct;
    }
    public NITFMetadata getMetadata() throws IOException {
        NITFMetadata metadata = new NITFMetadata();
        metadata.setRootElement(bandProduct.getMetadataRoot());
        metadata.getMetadataRoot().setName("NITF Metadata");

        // metadata.setRootElement(AbstractMetadata.getAbstractedMetadata(bandProduct));
        return metadata;
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
