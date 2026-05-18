package eu.esa.opt.dataio.s3;/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.manifest.XfduManifest;
import eu.esa.opt.dataio.s3.olci.OlciLevel2LProductFactory;
import eu.esa.opt.dataio.s3.slstr.*;
import eu.esa.opt.dataio.s3.synergy.SynAodProductFactory;
import eu.esa.opt.dataio.s3.synergy.SynL1CProductFactory;
import eu.esa.opt.dataio.s3.synergy.SynLevel2ProductFactory;
import eu.esa.opt.dataio.s3.synergy.VgtProductFactory;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import eu.esa.opt.dataio.s3.util.S3Util;
import eu.esa.snap.core.dataio.cache.CachedSubsamplingReader;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.util.ProductUtils;

import java.io.File;
import java.io.IOException;

public class Sentinel3ProductReader extends AbstractProductReader {

    private ProductFactory factory;
    private VirtualDir virtualDir;

    public Sentinel3ProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();
        ensureVirtualDir(inputFile);

        final String basePath = virtualDir.getBasePath();
        final File productDir = new File(basePath);

        factory = getProductFactory(productDir.getName());

        // set manifest file - better, open stream to manifest and proagate this. It must be read anyways. tb 2024-05-29
        return createProduct();
    }

    @Override
    public boolean isSubsetReadingFullySupported() {
        return true;
    }

    protected void ensureVirtualDir(File inputFile) {
        virtualDir = ProductUtils.getProductVirtualDir(inputFile);
    }

    public VirtualDir getVirtualDir() {
        return virtualDir;
    }

    ProductFactory getProductFactory(String dirName) {
        ProductFactory factory = null;
        if (dirName.matches("S3.?_OL_2_(L[FR]R)_.*(.SEN3)?")) { // OLCI L2 L -
            factory = new OlciLevel2LProductFactory(this);
        }
        /*
        disabled - using new reader now tb 2026-01-28
        else if (dirName.matches("S3.?_OL_2_(W[FR]R)_.*(.SEN3)?")) { // OLCI L2 W -
            factory = new OlciLevel2WProductFactory(this);
        }

         */
        else if (dirName.matches("S3.?_SL_1_RBT.*(.SEN3)?")) { // SLSTR L1b
            final ProductReaderPlugIn readerPlugIn = getReaderPlugIn();
            if (readerPlugIn instanceof SlstrLevel1B1kmProductReaderPlugIn) {
                factory = new SlstrLevel1B1kmProductFactory(this);
            } else if (readerPlugIn instanceof SlstrLevel1B500mProductReaderPlugIn) {
                factory = new SlstrLevel1B500mProductFactory(this);
            } else {
                factory = new SlstrLevel1ProductFactory(this);
            }
        } else if (dirName.matches("S3.?_SL_2_LST_.*(.SEN3)?")) { // SLSTR L2 LST
            factory = new SlstrLstProductFactory(this);
        } else if (dirName.matches("S3.?_SL_2_WST_.*(.SEN3)?")) { // SLSTR L2 WST
            factory = new SlstrWstProductFactory(this);
        } else if (dirName.matches("S3.?_SL_2_WCT_.*(.SEN3)?")) { // SLSTR L2 WCT
            factory = new SlstrSstProductFactory(this);
        } else if (dirName.matches("S3.?_SL_2_FRP_.*(.SEN3)?")) { // SLSTR L2 FRP
            factory = new SlstrFrpProductFactory(this);
        } else if (dirName.matches("S3.?_SL_2_AOD_.*(.SEN3)?")) { // SLSTR L2 AOD
            factory = new SlstrAodProductFactory(this);
        } else if (dirName.matches("S3.?_SY_1_SYN_.*(.SEN3)?")) { // SYN L1
            factory = new SynL1CProductFactory(this);
        } else if (dirName.matches("S3.?_SY_2_SYN_.*(.SEN3)?")) { // SYN L2
            factory = new SynLevel2ProductFactory(this);
        } else if (dirName.matches("S3.?_SY_2_AOD_.*(.SEN3)?")) { // SYN AOD
            factory = new SynAodProductFactory(this);
        } else if (dirName.matches("S3.?_SY_(2_VGP|[23]_VG1|2_V10)_.*(.SEN3)?")) { // SYN VGT
            factory = new VgtProductFactory(this);
        }

        return factory;
    }

    protected void setFactory(ProductFactory factory) {
        this.factory = factory;
    }

    protected Product createProduct() throws IOException {
        if (factory == null) {
            throw new IOException("Cannot read product file '" + getInputFile() + "'.");
        }

        return factory.createProduct(virtualDir);
    }

    protected void setInput(Object input) {
        if (input instanceof File && ((File) input).isDirectory()) {
            super.setInput(((File) input).toPath().resolve(XfduManifest.MANIFEST_FILE_NAME).toFile());
        } else {
            super.setInput(input);
        }
    }

    @Override
    protected final void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                                int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                                int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                                ProgressMonitor pm) throws IOException {
        final S3NetcdfReader s3Reader = ((AbstractProductFactory) factory).getBandCacheMap().get(destBand.getName());
        if (s3Reader == null) {
            throw new IOException("No cache reader registered for band '" + destBand.getName() + "'. " +
                    "The band may require a JAI source image that was not set up correctly.");
        }

        final String cacheKey = getBandCacheKey(destBand);
        CachedSubsamplingReader.read(s3Reader.getProductCache(), cacheKey, destBand.getDataType(),
                sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight,
                sourceStepX, sourceStepY, destWidth, destHeight, destBuffer
        );

    }

    protected String getBandCacheKey(Band destBand) {
        return destBand.getName();
    }



    @Override
    public void readTiePointGridRasterData(TiePointGrid tpg,
                                           int destOffsetX, int destOffsetY,
                                           int destWidth, int destHeight,
                                           ProductData destBuffer,
                                           ProgressMonitor pm) throws IOException {
        final AbstractProductFactory.TpgEntry entry =
                ((AbstractProductFactory) factory).getTpgReaderMap().get(tpg.getName());

        if (entry == null) {
            throw new IOException("No cache reader registered for tie-point grid '" + tpg.getName() + "'.");
        }

        final int srcOffsetX = entry.dataOffsetX + destOffsetX;
        final int srcOffsetY = entry.dataOffsetY + destOffsetY;
        final int[] offsets = new int[]{srcOffsetY, srcOffsetX};
        final int[] shapes = new int[]{destHeight, destWidth};

        String cacheKey = entry.cacheKey;
        final ProductData sourceData;
        final int sourceType = entry.sourceBand.getDataType();

        if (sourceType == destBuffer.getType()) {
            entry.reader.getProductCache().read(cacheKey, offsets, shapes, new DataBuffer(destBuffer, offsets, shapes));
            sourceData = destBuffer;
        } else {
            final DataBuffer intermediateBuffer = new DataBuffer(sourceType, offsets, shapes);
            entry.reader.getProductCache().read(cacheKey, offsets, shapes, intermediateBuffer);
            sourceData = intermediateBuffer.getData();
        }


        final int count = destWidth * destHeight;
        for (int ii = 0; ii < count; ii++) {
            final double rawValue = sourceData.getElemDoubleAt(ii);
            final double geoValue = S3Util.getGeophysicalValue(entry.sourceBand, rawValue);
            destBuffer.setElemDoubleAt(ii, geoValue);
        }
    }

    @Override
    public final void close() throws IOException {
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        if (virtualDir != null) {
            virtualDir.close();
            virtualDir = null;
        }
        super.close();
    }

    public final File getInputFile() {
        return getProductFile();
    }

    public final File getInputFileParentDirectory() {
        return getInputFile().getParentFile();
    }
}
