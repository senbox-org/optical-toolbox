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

@OperatorMetadata(
        alias = "NdmiOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Vegetation Spectral Indices",
        description = "Normalized Difference Moisture Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class NdmiOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "ndmi";

	@Parameter(label = "Nir source band",
			description = "The NIR band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 760, maxWavelength = 900)
	private String nirSourceBand;

	@Parameter(label = "Swir 1 source band",
			description = "The SWIR 1 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 1550, maxWavelength = 1750)
	private String swir1SourceBand;

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Ndmi", rectangle.height);
        try {

			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);
			Tile swir1Tile = getSourceTile(getSourceProduct().getBand(swir1SourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile ndmi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile ndmiFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float ndmiValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float n= nirTile.getSampleFloat(x, y);
					final float s1= swir1Tile.getSampleFloat(x, y);

                    ndmiValue = (n-s1)/(n+s1);
                    ndmi.setSample(x, y, computeFlag(x, y, ndmiValue, ndmiFlags));
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
            super(NdmiOp.class);
        }
    }
}
