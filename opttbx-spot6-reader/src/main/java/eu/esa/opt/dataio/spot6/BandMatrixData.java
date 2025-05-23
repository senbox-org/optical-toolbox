package eu.esa.opt.dataio.spot6;

import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.image.MosaicMatrix;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by jcoravu on 11/8/2020.
 */
public interface BandMatrixData {

    public int getLevelCount();

    public MosaicMatrix getMosaicMatrix();

    public DefaultMultiLevelImage buildBandSourceImage(int bandLevelCount, double noDataValue, Dimension defaultJAIReadTileSize, int bandIndex,
                                                       Rectangle bandBounds, GeoCoding bandGeoCoding, AffineTransform imageToModelTransform);
}
