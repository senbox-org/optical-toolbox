package eu.esa.opt.spectral.water;

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
 * Operator class for Ndwins
 *
 * @author Adrian Draghici
 */
@OperatorMetadata(
        alias = "NdwinsOp",
        version = "1.0",
        category = "Optical/Thematic Water Processing/Water Spectral Indices",
        description = "Normalized Difference Water Index with no Snow Cover and Glaciers",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class NdwinsOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "ndwins";

	@Parameter(label = "L Parameter", defaultValue = "1.0F", description = "The l parameter.")
	private float l;

	@Parameter(label = "Alpha Parameter", defaultValue = "1.0F", description = "The alpha parameter.")
	private float alpha;

	@Parameter(label = "Green source band",
			description = "The Green band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 510, maxWavelength = 600)
	private String greenSourceBand;

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
        pm.beginTask("Computing Ndwins", rectangle.height);
        try {

			Tile greenTile = getSourceTile(getSourceProduct().getBand(greenSourceBand), rectangle);
			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile ndwins = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile ndwinsFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float ndwinsValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float g= greenTile.getSampleFloat(x, y);
					final float n= nirTile.getSampleFloat(x, y);

                    ndwinsValue = (g-alpha*n)/(g+n);
                    ndwins.setSample(x, y, computeFlag(x, y, ndwinsValue, ndwinsFlags));
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
            super(NdwinsOp.class);
        }
    }
}
