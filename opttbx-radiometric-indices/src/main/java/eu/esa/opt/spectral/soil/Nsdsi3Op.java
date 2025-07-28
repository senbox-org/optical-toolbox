package eu.esa.opt.spectral.soil;

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
        alias = "Nsdsi3Op",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Soil Spectral Indices",
        description = "Normalized Shortwave-Infrared Difference Bare Soil Moisture Index 3",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class Nsdsi3Op extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "nsdsi3";

	@Parameter(label = "Swir 1 source band",
			description = "The SWIR 1 band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 1550, maxWavelength = 1750)
	private String swir1SourceBand;

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
        pm.beginTask("Computing Nsdsi3", rectangle.height);
        try {

			Tile swir1Tile = getSourceTile(getSourceProduct().getBand(swir1SourceBand), rectangle);
			Tile swir2Tile = getSourceTile(getSourceProduct().getBand(swir2SourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile nsdsi3 = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile nsdsi3Flags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float nsdsi3Value;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float s1= swir1Tile.getSampleFloat(x, y);
					final float s2= swir2Tile.getSampleFloat(x, y);

                    nsdsi3Value = (s1-s2)/(s1+s2);
                    nsdsi3.setSample(x, y, computeFlag(x, y, nsdsi3Value, nsdsi3Flags));
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
            super(Nsdsi3Op.class);
        }
    }
}
