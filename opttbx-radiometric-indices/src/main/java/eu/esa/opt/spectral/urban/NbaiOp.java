package eu.esa.opt.spectral.urban;

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

@OperatorMetadata(
        alias = "NbaiOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Urban Spectral Indices",
        description = "Normalized Built-up Area Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class NbaiOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "nbai";

	@Parameter(label = "Green source band",
			description = "The Green band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 510, maxWavelength = 600)
	private String greenSourceBand;

	@Parameter(label = "Swir 1 source band",
			description = "The SWIR 1 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 1550, maxWavelength = 1750)
	private String swir1SourceBand;

	@Parameter(label = "Swir 2 source band",
			description = "The SWIR 2 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 2080, maxWavelength = 2350)
	private String swir2SourceBand;

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Nbai", rectangle.height);
        try {

			Tile greenTile = getSourceTile(getSourceProduct().getBand(greenSourceBand), rectangle);
			Tile swir1Tile = getSourceTile(getSourceProduct().getBand(swir1SourceBand), rectangle);
			Tile swir2Tile = getSourceTile(getSourceProduct().getBand(swir2SourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile nbai = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile nbaiFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float nbaiValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float g= greenTile.getSampleFloat(x, y);
					final float s1= swir1Tile.getSampleFloat(x, y);
					final float s2= swir2Tile.getSampleFloat(x, y);

                    nbaiValue = ((s2-s1)/g)/((s2+s1)/g);
                    nbai.setSample(x, y, computeFlag(x, y, nbaiValue, nbaiFlags));
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
            super(NbaiOp.class);
        }
    }
}
