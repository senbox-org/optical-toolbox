package eu.esa.opt.radiometry;

import com.bc.ceres.core.ProgressMonitor;
import eu.esa.opt.radiometry.annotations.BandParameter;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;

import java.awt.*;
import java.util.Map;

/**
 * The <code>NdviOp</code> retrieves the Normalized Difference Vegetation Index (NDVI).
 *
 * @author Maximilian Aulinger
 */
@OperatorMetadata(
        alias = "NdviOp",
        version = "1.3",
        category = "Optical/Thematic Land Processing/Vegetation Radiometric Indices",
        description = "The retrieves the Normalized Difference Vegetation Index (NDVI).",
        authors = "Maximilian Aulinger, Thomas Storm",
        copyright = "Copyright (C) 2016 by Brockmann Consult (info@brockmann-consult.de)")
public class NdviOp extends BaseIndexOp {

    // constants
    public static final String NDVI_BAND_NAME = "ndvi";

    @Parameter(label = "Red factor", defaultValue = "1.0F", description = "The value of the red source band is multiplied by this value.")
    private float redFactor;

    @Parameter(label = "NIR factor", defaultValue = "1.0F", description = "The value of the NIR source band is multiplied by this value.")
    private float nirFactor;

    @Parameter(label = "Red source band",
            description = "The red band for the NDVI computation. If not provided, the " +
                    "operator will try to find the best fitting band.",
            rasterDataNodeType = Band.class)
    @BandParameter(minWavelength = 600, maxWavelength = 665)
    private String redSourceBand;

    @Parameter(label = "NIR source band",
            description = "The near-infrared band for the NDVI computation. If not provided," +
                    " the operator will try to find the best fitting band.",
            rasterDataNodeType = Band.class)
    @BandParameter(minWavelength = 800, maxWavelength = 900)
    private String nirSourceBand;

    private static final float noDataValue = Float.NaN;

    public NdviOp() {
        super();
        this.lowValueThreshold = -1f;
        this.highValueThreshold = 1f;
    }

    @Override
    public String getBandName() {
        return NDVI_BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing NDVI", rectangle.height);
        try {
            Tile redTile = getSourceTile(getSourceProduct().getBand(redSourceBand), rectangle);
            Tile nirTile = getSourceTile(getSourceProduct().getBand(nirSourceBand), rectangle);

            Tile ndvi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + NDVI_BAND_NAME));
            Tile ndviFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            boolean nodataValueUsed = getBandWithSuffix(targetProduct, "_" + NDVI_BAND_NAME).isNoDataValueUsed();
            Float redNoDataValue = (float) getSourceProduct().getBand(redSourceBand).getGeophysicalNoDataValue();
            Float nirNoDataValue = (float) getSourceProduct().getBand(nirSourceBand).getGeophysicalNoDataValue();

            float ndviValue;

            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                    final float nirSample = nirTile.getSampleFloat(x, y);
                    final float redSample = redTile.getSampleFloat(x, y);
                    final float nir = nirFactor * nirSample;
                    final float red = redFactor * redSample;

                    if (nodataValueUsed && (redNoDataValue.equals(redSample) || nirNoDataValue.equals(nirSample))) {
                        ndvi.setSample(x, y, noDataValue);
                        ndviFlags.setSample(x, y, 0);
                        continue;
                    }

                    ndviValue = (nir - red) / (nir + red);

                    ndvi.setSample(x, y, computeFlag(x, y, ndviValue, ndviFlags));
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
            super(NdviOp.class);
        }

    }
}
