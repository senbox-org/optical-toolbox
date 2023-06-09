package eu.esa.opt.grm.segmentation.tiles;

import org.esa.snap.utils.AbstractImageTilesParallelComputing;

import java.io.IOException;

/**
 * @author Jean Coravu
 */
public class SecondTileStabilityMarginParallelComputing extends AbstractImageTilesParallelComputing {
    private final AbstractTileSegmenter tileSegmenter;
    private final int iteration;
    private final int numberOfNeighborLayers;

    public SecondTileStabilityMarginParallelComputing(int iteration, int numberOfNeighborLayers, AbstractTileSegmenter tileSegmenter) {
        super(tileSegmenter.getImageWidth(), tileSegmenter.getImageHeight(), tileSegmenter.getTileWidth(), tileSegmenter.getTileHeight());

        this.tileSegmenter = tileSegmenter;
        this.iteration = iteration;
        this.numberOfNeighborLayers = numberOfNeighborLayers;
    }

    @Override
    protected void runTile(int tileLeftX, int tileTopY, int tileWidth, int tileHeight, int localRowIndex, int localColumnIndex) throws IOException, IllegalAccessException, InterruptedException {
        this.tileSegmenter.runTileStabilityMarginSecondSegmentation(this.iteration, localRowIndex, localColumnIndex, this.numberOfNeighborLayers);
    }
}