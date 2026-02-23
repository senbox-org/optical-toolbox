package eu.esa.opt.dataio.enmap;

import eu.esa.opt.dataio.enmap.imgReader.EnmapImageReader;
import eu.esa.snap.core.dataio.cache.CacheDataProvider;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import org.esa.snap.core.datamodel.ProductData;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class EnmapMultiCubeCacheProvider implements CacheDataProvider {


    private static final int INTERLEAVED_TILE_LAYERS = 32;
    private final Map<String, CubeSpec> cubes = new HashMap<>();


    public void addCube(String variableName, EnmapImageReader reader, int sceneWidth, int sceneHeight, int numLayers, int dataType) throws IOException {
        addCube(variableName, reader, sceneWidth, sceneHeight, numLayers, dataType, INTERLEAVED_TILE_LAYERS);
    }

    public void addCube(String variableName, EnmapImageReader reader, int sceneWidth, int sceneHeight, int numLayers, int dataType, int tileLayers) throws IOException {
        if (cubes.containsKey(variableName)) {
            throw new IOException("Duplicate cache variable: " + variableName);
        }

        VariableDescriptor d = new VariableDescriptor();
        d.name = variableName;
        d.dataType = dataType;
        d.width = sceneWidth;
        d.height = sceneHeight;
        d.layers = numLayers;

        Dimension td = reader.getTileDimension();
        d.tileWidth = td.width;
        d.tileHeight = td.height;

        int nl = Math.max(1, numLayers);
        int tl = Math.max(1, tileLayers);
        d.tileLayers = Math.min(nl, tl);

        cubes.put(variableName, new CubeSpec(reader, d));
    }


    @Override
    public VariableDescriptor getVariableDescriptor(String variableName) throws IOException {
        return cube(variableName).descriptor;
    }


    @Override
    public DataBuffer readCacheBlock(String variableName, int[] offsets, int[] shapes, ProductData targetData) throws IOException {
        validateShape(offsets, shapes);
        CubeSpec c = cube(variableName);

        final int startLayer, numLayers, x, y, w, h, elems;

        if (offsets.length == 2) {
            startLayer = 0;
            numLayers = 1;
            y = offsets[0];
            x = offsets[1];
            h = shapes[0];
            w = shapes[1];
            elems = h * w;
        } else {
            startLayer = offsets[0];
            y = offsets[1];
            x = offsets[2];
            numLayers  = shapes[0];
            h = shapes[1];
            w = shapes[2];
            elems = numLayers * h * w;
        }

        if (targetData == null) {
            targetData = ProductData.createInstance(c.descriptor.dataType, elems);
        }

        synchronized (c.readLock) {
            c.reader.readLayerBlock(startLayer, numLayers, x, y, w, h, targetData);
        }
        return new DataBuffer(targetData, offsets, shapes);
    }

    private CubeSpec cube(String variableName) throws IOException {
        CubeSpec c = cubes.get(variableName);
        if (c == null) {
            throw new IOException("Unknown cache variable: " + variableName);
        }
        return c;
    }

    private static void validateShape(int[] offsets, int[] shapes) throws IOException {
        if (offsets == null || shapes == null) {
            throw new IOException("Expected 2D/3D offsets and shapes");
        }

        boolean ok2 = offsets.length == 2 && shapes.length == 2;
        boolean ok3 = offsets.length == 3 && shapes.length == 3;

        if (!ok2 && !ok3) {
            throw new IOException("Expected 2D/3D offsets and shapes");
        }
    }

    public void clear() {
        cubes.clear();
    }


    private static final class CubeSpec {
        final EnmapImageReader reader;
        final VariableDescriptor descriptor;
        final Object readLock = new Object();

        CubeSpec(EnmapImageReader reader, VariableDescriptor descriptor) {
            this.reader = reader;
            this.descriptor = descriptor;
        }
    }
}
