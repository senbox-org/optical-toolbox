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
        alias = "NhfdOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Urban Spectral Indices",
        description = "Non-Homogeneous Feature Difference",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class NhfdOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "nhfd";

	@Parameter(label = "Aerosols source band",
			description = "The Aerosols band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 400, maxWavelength = 455)
	private String aerosolsSourceBand;

	@Parameter(label = "Red edge 1 source band",
			description = "The Red Edge 1 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 695, maxWavelength = 715)
	private String redEdge1SourceBand;

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing Nhfd", rectangle.height);
        try {

			Tile aerosolsTile = getSourceTile(getSourceProduct().getBand(aerosolsSourceBand), rectangle);
			Tile redEdge1Tile = getSourceTile(getSourceProduct().getBand(redEdge1SourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile nhfd = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile nhfdFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float nhfdValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float a= aerosolsTile.getSampleFloat(x, y);
					final float re1= redEdge1Tile.getSampleFloat(x, y);

                    nhfdValue = (re1-a)/(re1+a);
                    nhfd.setSample(x, y, computeFlag(x, y, nhfdValue, nhfdFlags));
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
            super(NhfdOp.class);
        }
    }
}
