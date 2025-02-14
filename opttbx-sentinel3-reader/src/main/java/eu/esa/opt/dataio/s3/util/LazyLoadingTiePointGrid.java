package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;

import java.io.IOException;

public class LazyLoadingTiePointGrid extends TiePointGrid {

    private final ProductReader reader;


    public LazyLoadingTiePointGrid(ProductReader reader, String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY);
        this.reader = reader;
    }

    public LazyLoadingTiePointGrid(ProductReader reader, String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints);
        this.reader = reader;
    }

    public LazyLoadingTiePointGrid(ProductReader reader, String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints, boolean containsAngles) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints, containsAngles);
        this.reader = reader;
    }

    public LazyLoadingTiePointGrid(ProductReader reader, String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints, int discontinuity) {
        super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints, discontinuity);
        this.reader = reader;
    }

    @Override
    public int[] readPixels(int x, int y, int w, int h, int[] pixels, ProgressMonitor pm) throws IOException {
        reader.readTiePointGridRasterData(this, x, y, w, h, ProductData.createInstance(pixels), pm);
        return super.readPixels(x, y, w, h, pixels, pm);
    }

    @Override
    public float[] readPixels(int x, int y, int w, int h, float[] pixels, ProgressMonitor pm) throws IOException {
        reader.readTiePointGridRasterData(this, x, y, w, h, ProductData.createInstance(pixels), pm);
        return super.readPixels(x, y, w, h, pixels, pm);
    }

    @Override
    public double[] readPixels(int x, int y, int w, int h, double[] pixels, ProgressMonitor pm) throws IOException {
        reader.readTiePointGridRasterData(this, x, y, w, h, ProductData.createInstance(pixels), pm);
        return super.readPixels(x, y, w, h, pixels, pm);
    }
}
