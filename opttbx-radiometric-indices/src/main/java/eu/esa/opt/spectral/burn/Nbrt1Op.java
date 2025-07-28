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
        alias = "Nbrt1Op",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Burn Spectral Indices",
        description = "Normalized Burn Ratio Thermal 1",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class Nbrt1Op extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "nbrt1";

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
        pm.beginTask("Computing Nbrt1", rectangle.height);
        try {

			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);
			Tile swir2Tile = getSourceTile(getSourceProduct().getBand(swir2SourceBand), rectangle);
			Tile thermalTile = getSourceTile(getSourceProduct().getBand(thermalSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile nbrt1 = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile nbrt1Flags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float nbrt1Value;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float n= nirTile.getSampleFloat(x, y);
					final float s2= swir2Tile.getSampleFloat(x, y);
					final float t= thermalTile.getSampleFloat(x, y);

                    nbrt1Value = (n-(s2*t/10000.0f))/(n+(s2*t/10000.0f));
                    nbrt1.setSample(x, y, computeFlag(x, y, nbrt1Value, nbrt1Flags));
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
            super(Nbrt1Op.class);
        }
    }
}
