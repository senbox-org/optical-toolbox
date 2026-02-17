package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.enmap.EnmapMetadata;
import org.esa.snap.core.datamodel.ProductData;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;


class L1BSpectrumImageReader extends EnmapImageReader {


    private final EnmapImageReader vnirImageReader;
    private final EnmapImageReader swirImageReader;


    public L1BSpectrumImageReader(VirtualDir dataDir, EnmapMetadata meta, String vnirImageKey, String swirImageKey) throws IOException {
        vnirImageReader = EnmapImageReader.createImageReader(dataDir, meta, vnirImageKey);
        swirImageReader = EnmapImageReader.createImageReader(dataDir, meta, swirImageKey);
    }


    @Override
    public Dimension getTileDimension() throws IOException {
        return vnirImageReader.getTileDimension();
    }

    @Override
    public int getNumImages() throws IOException {
        return getNumVnirImages() + getNumSwirImages();
    }

    public int getNumVnirImages() throws IOException {
        return vnirImageReader.getNumImages();
    }

    public int getNumSwirImages() throws IOException {
        return swirImageReader.getNumImages();
    }

    @Override
    public RenderedImage getImageAt(int index) throws IOException {
        int vnirImages = getNumVnirImages();
        int swirImages = getNumSwirImages();
        int maxImages = vnirImages + swirImages;
        if (index < vnirImages) {
            return vnirImageReader.getImageAt(index);
        } else if (index < maxImages) {
            return swirImageReader.getImageAt(index - vnirImages);
        } else {
            throw new IllegalArgumentException(String.format("Image index must be between 0 and %d", maxImages - 1));
        }
    }

    @Override
    public void readLayerBlock(int startLayer, int numLayers, int x, int y, int width, int height, ProductData targetData) throws IOException {
        int remainingLayers = numLayers;
        int currentLayer = startLayer;
        int copiedLayers = 0;
        final int layerSize = width * height;
        final int vnirImages = getNumVnirImages();

        while (remainingLayers > 0) {
            final EnmapImageReader sourceReader;
            final int localStartLayer;
            final int layersToRead;

            if (currentLayer < vnirImages) {
                sourceReader = vnirImageReader;
                localStartLayer = currentLayer;
                layersToRead = Math.min(remainingLayers, vnirImages - currentLayer);
            } else {
                sourceReader = swirImageReader;
                localStartLayer = currentLayer - vnirImages;
                layersToRead = remainingLayers;
            }

            final int targetSize = layersToRead * layerSize;
            final ProductData blockData = ProductData.createInstance(targetData.getType(), targetSize);
            sourceReader.readLayerBlock(localStartLayer, layersToRead, x, y, width, height, blockData);
            System.arraycopy(blockData.getElems(), 0, targetData.getElems(), copiedLayers * layerSize, targetSize);

            copiedLayers += layersToRead;
            currentLayer += layersToRead;
            remainingLayers -= layersToRead;
        }
    }

    @Override
    public boolean isInterleavedReadOptimized() {
        return vnirImageReader.isInterleavedReadOptimized() && swirImageReader.isInterleavedReadOptimized();
    }

    @Override
    public void close() {
        vnirImageReader.close();
        swirImageReader.close();
    }
}
