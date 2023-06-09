/*
 * =========================================================================
 * This file is part of NITRO
 * =========================================================================
 * 
 * (C) Copyright 2004 - 2010, General Dynamics - Advanced Information Systems
 * 
 * NITRO is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, If not,
 * see <http://www.gnu.org/licenses/>.
 */

package nitf.imageio;

import nitf.DownSampler;
import nitf.IOHandle;
import nitf.ImageSubheader;
import nitf.NITFException;
import nitf.NITFResourceManager;
import nitf.PixelSkipDownSampler;
import nitf.Reader;
import nitf.Record;
import nitf.SubWindow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NITFReader extends ImageReader {
    private static final Log log = LogFactory.getLog(NITFReader.class);

    private IOHandle handle = null;

    private Reader reader = null;

    private Record record = null;

    private final Map<Integer, nitf.ImageReader> imageReaderMap = Collections
            .synchronizedMap(new HashMap<>());

    public NITFReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public void setInput(Object input) {
        if (input instanceof File) {
            File file = (File) input;
            try {
                this.handle = new IOHandle(file.getAbsolutePath());
            } catch (NITFException e) {
                throw new IllegalArgumentException("Invalid file: "
                                                           + file.getAbsolutePath(), e);
            }
        } else {
            throw new IllegalArgumentException(
                    "Currently, the input must be a File");
        }
    }

    @Override
    public Object getInput() {
        return this.handle;
    }

    public synchronized void readHeader() throws IOException {
        if (reader != null)
            return;

        if (handle == null) {
            throw new IllegalStateException("No input handle");
        }

        try {
            reader = new Reader();
            record = reader.read(handle);
        } catch (NITFException e) {
            log.error(e.getMessage(), e);
            throw new IIOException("NITF Exception", e);
        }
    }

    private void checkIndex(int imageIndex) throws IOException {
        readHeader();
        int numImages = getNumImages(true);

        if (imageIndex < 0 || imageIndex >= numImages) {
            throw new IndexOutOfBoundsException("bad index: " + imageIndex);
        }
    }

    private synchronized nitf.ImageReader getImageReader(int imageIndex)
            throws IOException {
        checkIndex(imageIndex);

//        Integer key = new Integer(imageIndex);
        try {
            if (!imageReaderMap.containsKey(imageIndex))
                imageReaderMap.put(imageIndex, reader.getNewImageReader(imageIndex));
            return imageReaderMap.get(imageIndex);
        } catch (NITFException e) {
            log.error(e.getMessage(), e);
            throw new IIOException("NITF Exception", e);
        }
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        readHeader();
        try {
            return record.getHeader().getNumImages().getIntData();
        } catch (NITFException e) {
            log.error(e.getMessage(), e);
            throw new IIOException("NITF Exception", e);
        }
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        try {
            return record.getImages()[imageIndex].getSubheader().getNumCols()
                    .getIntData();
        } catch (NITFException e) {
            log.error(e.getMessage(), e);
            throw new IIOException("NITF Exception", e);
        }
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        try {
            return record.getImages()[imageIndex].getSubheader().getNumRows()
                    .getIntData();
        } catch (NITFException e) {
            log.error(e.getMessage(), e);
            throw new IIOException("NITF Exception", e);
        }
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        checkIndex(imageIndex);

        List<ImageTypeSpecifier> l = new ArrayList<>();

        try {
            ImageSubheader subheader = record.getImages()[imageIndex]
                    .getSubheader();
            String irep = subheader.getImageRepresentation().getStringData()
                    .trim();
            String pvType = subheader.getPixelValueType().getStringData()
                    .trim();
            int bandCount = subheader.getBandCount();
            int nbpp = subheader.getNumBitsPerPixel().getIntData();

            int nBytes = ((nbpp - 1) / 8) + 1;
            if (nBytes == 1 || nBytes == 2
                    || (nBytes == 4 && pvType.equals("R"))
                    || (nBytes == 8 && pvType.equals("R"))) {
                if (nBytes == 1 && bandCount == 3 && irep.equals("RGB")) {
                    ColorSpace rgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                    int[] bandOffsets = new int[3];
                    for (int i = 0; i < bandOffsets.length; ++i)
                        bandOffsets[i] = i;
                    l.add(ImageTypeSpecifier.createInterleaved(rgb,
                                                               bandOffsets, DataBuffer.TYPE_BYTE, false, false));
                }
                l.add(ImageTypeSpecifier.createGrayscale(8,
                                                         DataBuffer.TYPE_BYTE, false));
            } else {
                throw new IllegalStateException(String.format("Support for pixels of size %d bytes has not been implemented yet", nbpp));
            }
        } catch (NITFException e) {
            log.error(e.getMessage(), e);
        }
        return l.iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * Returns the ACTUAL data from the image. Note: for anything other than
     * byte or int, this is NOT viewable. This is used for getting the actual
     * data. (non-Javadoc)
     * 
     * @see javax.imageio.ImageReader#readRaster(int,
     * javax.imageio.ImageReadParam)
     */
    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param)
            throws IOException {
        checkIndex(imageIndex);

        Rectangle sourceRegion = new Rectangle();
        Rectangle destRegion = new Rectangle();
        computeRegions(param, getWidth(imageIndex), getHeight(imageIndex),
                       null, sourceRegion, destRegion);

        // Set everything to default values
        int sourceXSubsampling = param != null ? param.getSourceXSubsampling()
                : 1;
        int sourceYSubsampling = param != null ? param.getSourceYSubsampling()
                : 1;
        Point destinationOffset = param != null ? param.getDestinationOffset()
                : new Point(0, 0);

        ImageSubheader subheader;
        try {
            subheader = record.getImages()[imageIndex].getSubheader();
        } catch (NITFException e) {
            throw new IOException(e);
        }
//        String irep = subheader.getImageRepresentation().getStringData().trim();
        String pvType = subheader.getPixelValueType().getStringData().trim();
        int nbpp = subheader.getNumBitsPerPixel().getIntData();
        int bandCount = subheader.getBandCount();

        // make the band offsets array, for the output
        int[] bandOffsets;
        int[] sourceBands = param != null ? param.getSourceBands() : null;
        if (param != null && param.getDestinationBands() != null)
            bandOffsets = param.getDestinationBands();
        else if (param != null && sourceBands != null) {
            bandOffsets = new int[sourceBands.length];
            System.arraycopy(sourceBands, 0, bandOffsets, 0, bandOffsets.length);
        } else {
            // Setup band offsets -- TODO should we really read ALL bands by
            // default?
            bandOffsets = new int[bandCount];
            for (int i = 0; i < bandOffsets.length; i++)
                bandOffsets[i] = i;
        }

        int nBytes = ((nbpp - 1) / 8) + 1;

        int bufType;

        // byte
        if (nBytes == 1) {
            bufType = DataBuffer.TYPE_BYTE;
        }
        // short
        else if (nBytes == 2) {
            bufType = DataBuffer.TYPE_USHORT;
        }
        // float
        else if (nBytes == 4 && pvType.equals("R")) {
            bufType = DataBuffer.TYPE_FLOAT;
        }
        // double
        else if (nBytes == 8 && pvType.equals("R")) {
            bufType = DataBuffer.TYPE_DOUBLE;
        } else {
            throw new IllegalStateException("not yet implemented");
        }

        WritableRaster ras = ImageIOUtils
                .makeGenericPixelInterleavedWritableRaster(destRegion.width,
                                                           destRegion.height, bandOffsets.length, bufType);
        checkReadParamBandSettings(param, bandCount, ras.getSampleModel()
                .getNumBands());
        readRaster(imageIndex, sourceRegion, destRegion, sourceXSubsampling,
                   sourceYSubsampling, bandOffsets, nBytes, destinationOffset, ras);
        return ras;
    }

    /**
     * Optimization to read the entire image in one fell swoop... This is most
     * likely the common use case for this codec, so we hope this optimization
     * will be helpful.
     */
    protected void readFullImage(int imageIndex, Rectangle destRegion,
                                 int sourceXSubsampling,
                                 int sourceYSubsampling, int[] bandOffsets,
                                 int pixelSize, WritableRaster imRas)
            throws IOException {
        try {
            int numCols = destRegion.width;
            int numRows = destRegion.height;

            int bufSize = numCols * numRows * pixelSize;
            byte[][] imageBuf = new byte[bandOffsets.length][bufSize];

            // make a SubWindow from the params
            // TODO may want to read by blocks or rows to make faster and more
            // memory efficient
            SubWindow window;
            window = new SubWindow();
            window.setNumBands(bandOffsets.length);
            window.setBandList(bandOffsets);
            window.setNumCols(numCols);
            window.setNumRows(numRows);
            window.setStartCol(0);
            window.setStartRow(0);

            // the NITRO library can do the subsampling for us
            if (sourceYSubsampling != 1 || sourceXSubsampling != 1) {
                DownSampler downSampler = new PixelSkipDownSampler(
                        sourceYSubsampling, sourceXSubsampling);
                window.setDownSampler(downSampler);
            }

            nitf.ImageReader imageReader = getImageReader(imageIndex);
            imageReader.read(window, imageBuf);

            List<ByteBuffer> bandBufs = new ArrayList<>();

            for (int i = 0; i < bandOffsets.length; ++i) {
                ByteBuffer bandBuf = ByteBuffer.wrap(imageBuf[i]);
                bandBufs.add(bandBuf);
            }

            // optimization for 1 band case... just dump the whole thing
            if (bandOffsets.length == 1) {
                ByteBuffer bandBuf = bandBufs.get(0);

                switch (pixelSize) {
                    case 1:
                        ByteBuffer rasterByteBuf = ByteBuffer
                                .wrap(((DataBufferByte) imRas.getDataBuffer())
                                              .getData());
                        rasterByteBuf.put(bandBuf);
                        break;
                    case 2:
                        ShortBuffer rasterShortBuf = ShortBuffer
                                .wrap(((DataBufferUShort) imRas.getDataBuffer())
                                              .getData());
                        rasterShortBuf.put(bandBuf.asShortBuffer());
                        break;
                    case 4:
                        FloatBuffer rasterFloatBuf = FloatBuffer
                                .wrap(((DataBufferFloat) imRas.getDataBuffer())
                                              .getData());
                        rasterFloatBuf.put(bandBuf.asFloatBuffer());
                        break;
                    case 8:
                        DoubleBuffer rasterDoubleBuf = DoubleBuffer
                                .wrap(((DataBufferDouble) imRas.getDataBuffer())
                                              .getData());
                        rasterDoubleBuf.put(bandBuf.asDoubleBuffer());
                        break;
                }
            } else {
                // for multi-band case, we need to iterate over each pixel...
                // TODO -- optimize this!... somehow

                for (int srcY = 0, srcX = 0; srcY < numRows; srcY++) {
                    // Copy each (subsampled) source pixel into imRas
                    for (int dstX = 0; dstX < numCols; srcX += pixelSize, dstX++) {
                        for (int i = 0; i < bandOffsets.length; ++i) {
                            ByteBuffer bandBuf = bandBufs.get(i);

                            switch (pixelSize) {
                                case 1:
                                    imRas.setSample(dstX, srcY, i, bandBuf.get(srcX));
                                    break;
                                case 2:
                                    imRas.setSample(dstX, srcY, i, bandBuf.getShort(srcX));
                                    break;
                                case 4:
                                    imRas.setSample(dstX, srcY, i, bandBuf.getFloat(srcX));
                                    break;
                                case 8:
                                    imRas.setSample(dstX, srcY, i, bandBuf.getDouble(srcX));
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (NITFException e1) {
            throw new IOException(e1);
        }
    }

    /**
     * Reads image data as bytes for the given region, and writes it to the
     * given writable raster
     *
     */
    protected void readRaster(int imageIndex, Rectangle sourceRegion,
                              Rectangle destRegion, int sourceXSubsampling,
                              int sourceYSubsampling, int[] bandOffsets,
                              int pixelSize, Point destinationOffset,
                              WritableRaster imRas) throws IOException {
        checkIndex(imageIndex);

        try {
            ImageSubheader subheader = record.getImages()[imageIndex]
                    .getSubheader();
            int numCols = subheader.getNumCols().getIntData();
            int numRows = subheader.getNumRows().getIntData();

            // try to optimize the read call by reading in the entire
            // image at once
            if ((destRegion.height * sourceYSubsampling) == numRows
                    && (destRegion.width * sourceXSubsampling) == numCols) {
                readFullImage(imageIndex, destRegion, sourceXSubsampling,
                              sourceYSubsampling, bandOffsets, pixelSize, imRas);
            }
            // the general purpose case
            else {
                int colBytes = destRegion.width * pixelSize;

                int dstMinX = imRas.getMinX();
                int dstMaxX = dstMinX + imRas.getWidth() - 1;
                int dstMinY = imRas.getMinY();
                int dstMaxY = dstMinY + imRas.getHeight() - 1;
                // int swap = 0;

                int nBands = subheader.getBandCount();

                /*
                 * NOTE: This is a "fix" that will be removed once the
                 * underlying NITRO library gets patched. Currently, if you make
                 * a request of a single band, it doesn't matter which band you
                 * request - the data from the first band will be returned
                 * regardless. This is obviously wrong. To thwart this, we will
                 * read all bands, then scale down what we return to the user
                 * based on their actual request.
                 */
                int[] requestBands = new int[nBands];
                for (int i = 0; i < nBands; ++i)
                    requestBands[i] = i;

                byte[][] rowBuf = new byte[requestBands.length][colBytes];

                // make a SubWindow from the params
                // TODO may want to read by blocks or rows to make faster and
                // more
                // memory efficient
                SubWindow window;
                window = new SubWindow();
                window.setNumBands(requestBands.length);
                window.setBandList(requestBands);
                window.setNumCols(destRegion.width);
                window.setNumRows(1);
                window.setStartCol(sourceRegion.x);
                window.setStartRow(sourceRegion.y);

                // the NITRO library can do the subsampling for us
                if (sourceYSubsampling != 1 || sourceXSubsampling != 1) {
                    DownSampler downSampler = new PixelSkipDownSampler(
                            sourceYSubsampling, sourceXSubsampling);
                    window.setDownSampler(downSampler);
                }

                List<ByteBuffer> bandBufs = new ArrayList<>();
                for (int i = 0; i < requestBands.length; ++i) {
                    ByteBuffer bandBuf = ByteBuffer.wrap(rowBuf[i]);
                    bandBufs.add(bandBuf);
                }

                nitf.ImageReader imageReader = getImageReader(imageIndex);
                for (int srcY = 0; srcY < sourceRegion.height; srcY++) {
                    if (sourceYSubsampling != 1
                            && (srcY % sourceYSubsampling) != 0)
                        continue;

                    window.setStartRow(sourceRegion.y + srcY);

                    // Read the row
                    try {
                        imageReader.read(window, rowBuf);
                    } catch (NITFException e) {
                        throw new IIOException("Error reading line " + srcY, e);
                    }

                    // Determine where the row will go in the destination
                    int dstY = destinationOffset.y + srcY / sourceYSubsampling;
                    if (dstY < dstMinY) {
                        continue; // The row is above imRas
                    }
                    if (dstY > dstMaxY) {
                        break; // We're done with the image
                    }

                    // Copy each (subsampled) source pixel into imRas
                    for (int srcX = 0, dstX = destinationOffset.x; srcX < colBytes; srcX += pixelSize, dstX++) {
                        if (dstX < dstMinX) {
                            continue;
                        }
                        if (dstX > dstMaxX) {
                            break;
                        }

                        for (int i = 0; i < bandOffsets.length; ++i) {
                            ByteBuffer bandBuf = bandBufs.get(bandOffsets[i]);

                            switch (pixelSize) {
                                case 1:
                                    imRas.setSample(dstX, dstY, i, bandBuf.get(srcX));
                                    break;
                                case 2:
                                    imRas.setSample(dstX, dstY, i, bandBuf.getShort(srcX));
                                    break;
                                case 4:
                                    imRas.setSample(dstX, dstY, i, bandBuf.getFloat(srcX));
                                    break;
                                case 8:
                                    imRas.setSample(dstX, dstY, i, bandBuf.getDouble(srcX));
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (NITFException e1) {
            throw new IOException(e1);
        }
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        readHeader();
        Raster raster = readRaster(imageIndex, param);

        // get the requested number of destination bands (or 0 for all)
        int numDestBands = param != null ? (param.getDestinationBands() != null ? param
                .getDestinationBands().length
                : param.getSourceBands() != null ? param.getSourceBands().length
                : 0)
                : 0;

        // try to find a good match for the specifier
        ImageTypeSpecifier imageType = null, firstType = null;
        Iterator<ImageTypeSpecifier> imageTypes = getImageTypes(imageIndex);
        while (imageTypes.hasNext() && imageType == null) {
            ImageTypeSpecifier currentImageType = imageTypes.next();
            if (firstType == null)
                firstType = currentImageType;

            if (currentImageType.getNumBands() == numDestBands)
                imageType = currentImageType;
        }

        if (imageType == null) {
            if (firstType == null)
                throw new IOException(
                        "Unable to determine the ImageTypeSpecifier");
            else
                imageType = firstType;
        }

        try {
            ImageSubheader subheader = record.getImages()[imageIndex]
                    .getSubheader();
            String pvType = subheader.getPixelValueType().getStringData()
                    .trim();
            int nbpp = subheader.getNumBitsPerPixel().getIntData();
            int nBytes = ((nbpp - 1) / 8) + 1;

            if (nBytes == 1 || nBytes == 2
                    || (nBytes == 4 && pvType.equals("R"))
                    || (nBytes == 8 && pvType.equals("R"))) {
                return ImageIOUtils.rasterToBufferedImage(raster, imageType);
            }
        } catch (NITFException e) {
            throw new IOException(e);
        }
        throw new IllegalStateException("Image pixel type or bits per pixel not yet supported");
    }

    @Override
    public boolean canReadRaster() {
        return true;
    }

    /**
     * @return returns the underlying Record
     */
    public Record getRecord() throws IOException {
        readHeader();
        return record;
    }

    @Override
    public void dispose() {
        try {
            if (this.handle != null) {
                this.handle.close();
                NITFResourceManager.getInstance().destroy(handle);
            }
            if (reader != null) {
                NITFResourceManager.getInstance().destroy(reader);
            }
        } catch (NITFException ignored) {
        }
        super.dispose();
    }
}
