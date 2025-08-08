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

/**
 * Operator class for Ndvit
 *
 * @author Adrian Draghici
 */
@OperatorMetadata(
        alias = "NdvitOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Burn Spectral Indices",
        description = "Normalized Difference Vegetation Index Thermal",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class NdvitOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "ndvit";

	@Parameter(label = "Red source band",
			description = "The Red band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 620, maxWavelength = 690)
	private String redSourceBand;

	@Parameter(label = "Nir source band",
			description = "The NIR band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 760, maxWavelength = 900)
	private String nirSourceBand;

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
        pm.beginTask("Computing Ndvit", rectangle.height);
        try {

			Tile redTile = getSourceTile(getSourceProduct().getBand(redSourceBand), rectangle);
			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);
			Tile thermalTile = getSourceTile(getSourceProduct().getBand(thermalSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile ndvit = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile ndvitFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float ndvitValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float r= redTile.getSampleFloat(x, y);
					final float n= nirTile.getSampleFloat(x, y);
					final float t= thermalTile.getSampleFloat(x, y);

                    ndvitValue = (n-(r*t/10000.0f))/(n+(r*t/10000.0f));
                    ndvit.setSample(x, y, computeFlag(x, y, ndvitValue, ndvitFlags));
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
            super(NdvitOp.class);
        }
    }
}
