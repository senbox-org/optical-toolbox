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
        alias = "TtviOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Vegetation Spectral Indices",
        description = "Transformed Triangular Vegetation Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class TtviOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "ttvi";

	@Parameter(label = "Red edge 2 source band",
			description = "The Red Edge 2 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 730, maxWavelength = 750)
	private String redEdge2SourceBand;

	@Parameter(label = "Red edge 3 source band",
			description = "The Red Edge 3 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 765, maxWavelength = 795)
	private String redEdge3SourceBand;

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
        pm.beginTask("Computing Ttvi", rectangle.height);
        try {

			Tile redEdge2Tile = getSourceTile(getSourceProduct().getBand(redEdge2SourceBand), rectangle);
			Tile redEdge3Tile = getSourceTile(getSourceProduct().getBand(redEdge3SourceBand), rectangle);
			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile ttvi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile ttviFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float ttviValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float re2= redEdge2Tile.getSampleFloat(x, y);
					final float re3= redEdge3Tile.getSampleFloat(x, y);
					final float n= nirTile.getSampleFloat(x, y);

                    ttviValue = 0.5f*((865.0f-740.0f)*(re3-re2)-(pow(n,2)-re2)*(783.0f-740));
                    ttvi.setSample(x, y, computeFlag(x, y, ttviValue, ttviFlags));
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
            super(TtviOp.class);
        }
    }
}
