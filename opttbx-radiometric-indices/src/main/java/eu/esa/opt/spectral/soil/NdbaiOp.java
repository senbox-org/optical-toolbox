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
 * Operator class for Ndbai
 *
 * @author Adrian Draghici
 */
@OperatorMetadata(
        alias = "NdbaiOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Soil Spectral Indices",
        description = "Normalized Difference Bareness Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class NdbaiOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "ndbai";

	@Parameter(label = "Swir 1 source band",
			description = "The SWIR 1 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 1550, maxWavelength = 1750)
	private String swir1SourceBand;

	@Parameter(label = "Thermal source band",
			description = "The Thermal band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 10400, maxWavelength = 12500)
	private String thermalSourceBand;

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Ndbai", rectangle.height);
        try {

			Tile swir1Tile = getSourceTile(getSourceProduct().getBand(swir1SourceBand), rectangle);
			Tile thermalTile = getSourceTile(getSourceProduct().getBand(thermalSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile ndbai = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile ndbaiFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float ndbaiValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float s1= swir1Tile.getSampleFloat(x, y);
					final float t= thermalTile.getSampleFloat(x, y);

                    ndbaiValue = (s1-t)/(s1+t);
                    ndbai.setSample(x, y, computeFlag(x, y, ndbaiValue, ndbaiFlags));
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
            super(NdbaiOp.class);
        }
    }
}
