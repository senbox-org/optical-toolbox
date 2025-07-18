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
        alias = "SipiOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Vegetation Spectral Indices",
        description = "Structure Insensitive Pigment Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class SipiOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "sipi";

	@Parameter(label = "Aerosols source band",
			description = "The Aerosols band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 400, maxWavelength = 455)
	private String aerosolsSourceBand;

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

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Sipi", rectangle.height);
        try {

			Tile aerosolsTile = getSourceTile(getSourceProduct().getBand(aerosolsSourceBand), rectangle);
			Tile redTile = getSourceTile(getSourceProduct().getBand(redSourceBand), rectangle);
			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile sipi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile sipiFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float sipiValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float a= aerosolsTile.getSampleFloat(x, y);
					final float r= redTile.getSampleFloat(x, y);
					final float n= nirTile.getSampleFloat(x, y);

                    sipiValue = (n-a)/(n-r);
                    sipi.setSample(x, y, computeFlag(x, y, sipiValue, sipiFlags));
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
            super(SipiOp.class);
        }
    }
}
