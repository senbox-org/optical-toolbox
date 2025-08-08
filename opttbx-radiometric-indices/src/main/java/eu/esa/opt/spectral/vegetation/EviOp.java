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

/**
 * Operator class for Evi
 *
 * @author Adrian Draghici
 */
@OperatorMetadata(
        alias = "EviOp",
        version = "1.0",
        category = "Optical/Thematic Land Processing/Vegetation Spectral Indices",
        description = "Enhanced Vegetation Index",
        authors = "Adrian Draghici",
        copyright = "Copyright (C) 2025 by CS Group ROMANIA")
public class EviOp extends BaseIndexOp {

    // constants
    public static final String BAND_NAME = "evi";

	@Parameter(label = "G Parameter", defaultValue = "1.0F", description = "The g parameter.")
	private float g;

	@Parameter(label = "L Parameter", defaultValue = "1.0F", description = "The l parameter.")
	private float l;

	@Parameter(label = "C1 Parameter", defaultValue = "1.0F", description = "The c1 parameter.")
	private float c1;

	@Parameter(label = "C2 Parameter", defaultValue = "1.0F", description = "The c2 parameter.")
	private float c2;

	@Parameter(label = "Blue source band",
			description = "The Blue band for the Template computation. If not provided, the operator will try to find the best fitting band.",
			rasterDataNodeType = Band.class)
	@BandParameter(minWavelength = 450, maxWavelength = 530)
	private String blueSourceBand;

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
        pm.beginTask("Computing Evi", rectangle.height);
        try {

			Tile blueTile = getSourceTile(getSourceProduct().getBand(blueSourceBand), rectangle);
			Tile redTile = getSourceTile(getSourceProduct().getBand(redSourceBand), rectangle);
			Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);

            // SIITBX-494 - retrieve bands after suffix (which is the operator band name)
            Tile evi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + BAND_NAME));
            Tile eviFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            float eviValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {

					final float b= blueTile.getSampleFloat(x, y);
					final float r= redTile.getSampleFloat(x, y);
					final float n= nirTile.getSampleFloat(x, y);

                    eviValue = g*(n-r)/(n+c1*r-c2*b+l);
                    evi.setSample(x, y, computeFlag(x, y, eviValue, eviFlags));
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
            super(EviOp.class);
        }
    }
}
