package eu.esa.opt.spectral.vegetation;

import com.bc.ceres.core.ProgressMonitor;
import eu.esa.opt.radiometry.BaseIndexOp;
import eu.esa.opt.radiometry.annotations.BandParameter;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;

import java.awt.*;
import java.util.Map;

/**
 * Operator class for Bndvi
 *
 * @author Adrian Draghici
 */
@OperatorMetadata(
        alias = "BndviOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Vegetation Spectral Indices",
        description = "Blue Normalized Difference Vegetation Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class BndviOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "bndvi";

	@Parameter(label = "Blue source band",
			description = "The Blue band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 450, maxWavelength = 530)
	private String blueSourceBand;

	@Parameter(label = "Nir source band",
			description = "The NIR band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 760, maxWavelength = 900)
	private String nirSourceBand;

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Bndvi", rectangle.height);
        try {

			Tile blueTile = getSourceTile(getSourceProduct().getBand(blueSourceBand), rectangle);
			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile bndvi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile bndviFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float bndviValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float b= blueTile.getSampleFloat(x, y);
					final float n= nirTile.getSampleFloat(x, y);

                    bndviValue = (n-b)/(n+b);
                    bndvi.setSample(x, y, computeFlag(x, y, bndviValue, bndviFlags));
                }
                checkForCancellation();
                pm.worked(1);
            }
        } finally {
            pm.done();
        }
    }
    
    public static class Spi extends OperatorSpi {

        public Spi() {
            super(BndviOp.class);
        }
    }
}
