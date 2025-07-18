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
        alias = "S2repOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Vegetation Spectral Indices",
        description = "Sentinel-2 Red-Edge Position",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class S2repOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "s2rep";

	@Parameter(label = "Red edge 1 source band",
			description = "The Red Edge 1 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 695, maxWavelength = 715)
	private String redEdge1SourceBand;

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
        pm.beginTask("Computing S2rep", rectangle.height);
        try {

			Tile redEdge1Tile = getSourceTile(getSourceProduct().getBand(redEdge1SourceBand), rectangle);
			Tile redEdge2Tile = getSourceTile(getSourceProduct().getBand(redEdge2SourceBand), rectangle);
			Tile redEdge3Tile = getSourceTile(getSourceProduct().getBand(redEdge3SourceBand), rectangle);
			Tile redTile = getSourceTile(getSourceProduct().getBand(redSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile s2rep = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile s2repFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float s2repValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float re1= redEdge1Tile.getSampleFloat(x, y);
					final float re2= redEdge2Tile.getSampleFloat(x, y);
					final float re3= redEdge3Tile.getSampleFloat(x, y);
					final float r= redTile.getSampleFloat(x, y);

                    s2repValue = 705.0f+35.0f*((((re3+r)/2.0f)-re1)/(re2-re1));
                    s2rep.setSample(x, y, computeFlag(x, y, s2repValue, s2repFlags));
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
            super(S2repOp.class);
        }
    }
}
