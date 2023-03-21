package eu.esa.opt.grm.segmentation.tiles;

import org.esa.snap.utils.AbstractImageTilesParallelComputing;

import java.io.IOException;

/**
 * @author Jean Coravu
 */
public class CheckTemporaryTileFilesParallelComputing extends AbstractImageTilesParallelComputing {
    private final AbstractTileSegmenter tileSegmenter;
    private final int iteration;

    public CheckTemporaryTileFilesParallelComputing(int iteration, AbstractTileSegmenter tileSegmenter) {
        super(tileSegmenter.getImageWidth(), tileSegmenter.getImageHeight(), tileSegmenter.getTileWidth(), tileSegmenter.getTileHeight());

        this.iteration = iteration;
        this.tileSegmenter = tileSegmenter;
    }

    @Override
    protected void runTile(int tileLeftX, int tileTopY, int tileWidth, int tileHeight, int localRowIndex, int localColumnIndex)
                           throws IllegalAccessException, IOException, InterruptedException {

        this.tileSegmenter.checkTemporaryTileFiles(this.iteration, tileLeftX, tileTopY, tileWidth, tileHeight, localRowIndex, localColumnIndex);
    }
}