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
        alias = "MgrviOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Vegetation Spectral Indices",
        description = "Modified Green Red Vegetation Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class MgrviOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "mgrvi";

	@Parameter(label = "Green source band",
			description = "The Green band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 510, maxWavelength = 600)
	private String greenSourceBand;

	@Parameter(label = "Red source band",
			description = "The Red band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 620, maxWavelength = 690)
	private String redSourceBand;

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Mgrvi", rectangle.height);
        try {

			Tile greenTile = getSourceTile(getSourceProduct().getBand(greenSourceBand), rectangle);
			Tile redTile = getSourceTile(getSourceProduct().getBand(redSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile mgrvi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile mgrviFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float mgrviValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float g= greenTile.getSampleFloat(x, y);
					final float r= redTile.getSampleFloat(x, y);

                    mgrviValue = (pow(g,2.0f)-pow(r,2.0f))/(pow(g,2.0f)+pow(r,2.0f));
                    mgrvi.setSample(x, y, computeFlag(x, y, mgrviValue, mgrviFlags));
                }
                checkForCancellation();
                pm.worked(1);
            }
        } finally {
            pm.done();
        }
    }
    
	private static float pow(float n, float p) {
			return (float) Math.pow(n, p);
	}

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MgrviOp.class);
        }
    }
}
