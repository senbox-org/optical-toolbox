package eu.esa.opt.spectral.soil;

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
 * Operator class for Ndsiwv
 *
 * @author Adrian Draghici
 */
@OperatorMetadata(
        alias = "NdsiwvOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Soil Spectral Indices",
        description = "WorldView Normalized Difference Soil Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class NdsiwvOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "ndsiwv";

	@Parameter(label = "Green source band",
			description = "The Green band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 510, maxWavelength = 600)
	private String greenSourceBand;

	@Parameter(label = "Yellow source band",
			description = "The Yellow band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 585, maxWavelength = 625)
	private String yellowSourceBand;

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Ndsiwv", rectangle.height);
        try {

			Tile greenTile = getSourceTile(getSourceProduct().getBand(greenSourceBand), rectangle);
			Tile yellowTile = getSourceTile(getSourceProduct().getBand(yellowSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile ndsiwv = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile ndsiwvFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float ndsiwvValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float g= greenTile.getSampleFloat(x, y);
					final float yw= yellowTile.getSampleFloat(x, y);

                    ndsiwvValue = (g-yw)/(g+yw);
                    ndsiwv.setSample(x, y, computeFlag(x, y, ndsiwvValue, ndsiwvFlags));
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
            super(NdsiwvOp.class);
        }
    }
}
