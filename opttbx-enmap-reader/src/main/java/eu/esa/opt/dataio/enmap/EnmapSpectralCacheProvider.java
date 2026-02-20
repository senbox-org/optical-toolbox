package eu.esa.opt.dataio.enmap;

import eu.esa.opt.dataio.enmap.imgReader.EnmapImageReader;
import eu.esa.snap.core.dataio.cache.CacheDataProvider;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import org.esa.snap.core.datamodel.ProductData;

import java.awt.Dimension;
import java.io.IOException;


class EnmapSpectralCacheProvider implements CacheDataProvider {


    private static final int INTERLEAVED_TILE_LAYERS = 32;

    private final String variableName;
    private final EnmapImageReader spectralImageReader;
    private final VariableDescriptor variableDescriptor;
    private final Object readLock = new Object();


    EnmapSpectralCacheProvider(String variableName, EnmapImageReader spectralImageReader, int sceneWidth, int sceneHeight, int numSpectralLayers, int dataType) throws IOException {
        this.variableName = variableName;
        this.spectralImageReader = spectralImageReader;
        this.variableDescriptor = createVariableDescriptor(sceneWidth, sceneHeight, numSpectralLayers, dataType, spectralImageReader);
    }


    @Override
    public VariableDescriptor getVariableDescriptor(String variableName) throws IOException {
        ensureKnownVariable(variableName);
        return variableDescriptor;
    }

    @Override
    public DataBuffer readCacheBlock(String variableName, int[] offsets, int[] shapes, ProductData targetData) throws IOException {
        ensureKnownVariable(variableName);
        validateShape(offsets, shapes);

        if (targetData == null) {
            targetData = ProductData.createInstance(variableDescriptor.dataType, shapes[0] * shapes[1] * shapes[2]);
        }

        final int startLayer = offsets[0];
        final int startY = offsets[1];
        final int startX = offsets[2];
        final int numLayers = shapes[0];
        final int height = shapes[1];
        final int width = shapes[2];
        synchronized (readLock) {
            spectralImageReader.readLayerBlock(startLayer, numLayers, startX, startY, width, height, targetData);
        }

        return new DataBuffer(targetData, offsets, shapes);
    }

    private VariableDescriptor createVariableDescriptor(int sceneWidth, int sceneHeight, int numLayers, int dataType,
                                                        EnmapImageReader imageReader) throws IOException {
        final Dimension tileDimension = imageReader.getTileDimension();

        final VariableDescriptor descriptor = new VariableDescriptor();
        descriptor.name = variableName;
        descriptor.dataType = dataType;
        descriptor.width = sceneWidth;
        descriptor.height = sceneHeight;
        descriptor.layers = numLayers;
        descriptor.tileWidth = tileDimension.width;
        descriptor.tileHeight = tileDimension.height;
        descriptor.tileLayers = Math.min(numLayers, INTERLEAVED_TILE_LAYERS);
        return descriptor;
    }

    private void ensureKnownVariable(String variableName) throws IOException {
        if (!this.variableName.equals(variableName)) {
            throw new IOException("Variable not known: " + variableName);
        }
    }

    private static void validateShape(int[] offsets, int[] shapes) throws IOException {
        if (offsets == null || shapes == null || offsets.length != 3 || shapes.length != 3) {
            throw new IOException("Expected 3D offsets and shapes");
        }
    }
}
