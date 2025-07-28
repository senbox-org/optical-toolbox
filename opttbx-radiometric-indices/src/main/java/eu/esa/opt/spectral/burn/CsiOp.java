package eu.esa.opt.spectral.burn;

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
        alias = "CsiOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Burn Spectral Indices",
        description = "Char Soil Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class CsiOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "csi";

	@Parameter(label = "Nir source band",
			description = "The NIR band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 760, maxWavelength = 900)
	private String nirSourceBand;

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
        pm.beginTask("Computing Csi", rectangle.height);
        try {

			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);
			Tile swir2Tile = getSourceTile(getSourceProduct().getBand(swir2SourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile csi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile csiFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float csiValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float n= nirTile.getSampleFloat(x, y);
					final float s2= swir2Tile.getSampleFloat(x, y);

                    csiValue = n/s2;
                    csi.setSample(x, y, computeFlag(x, y, csiValue, csiFlags));
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
            super(CsiOp.class);
        }
    }
}
