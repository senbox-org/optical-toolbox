package eu.esa.opt.radiometry;

import com.bc.ceres.core.ProgressMonitor;
import eu.esa.opt.radiometry.annotations.BandParameter;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * The <code>NdviOp</code> retrieves the Normalized Difference Vegetation Index (NDVI).
 *
 * @author Maximilian Aulinger
 */
@OperatorMetadata(
        alias = "NdviOp",
        version = "1.4",
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
            final Product sourceProduct = getSourceProduct();

            final Band redSource = sourceProduct.getBand(redSourceBand);
            final Band nirSource = sourceProduct.getBand(nirSourceBand);

            boolean[] redValidMask;
            boolean[] nirValidMask;
            synchronized (this) {
                redValidMask = redSource.readValidMask(rectangle.x, rectangle.y, rectangle.width, rectangle.height, null);
                nirValidMask = nirSource.readValidMask(rectangle.x, rectangle.y, rectangle.width, rectangle.height, null);
            }

            final Tile redTile = getSourceTile(redSource, rectangle);
            final Tile nirTile = getSourceTile(nirSource, rectangle);

            final Tile ndvi = targetTiles.get(getBandWithSuffix(targetProduct, "_" + NDVI_BAND_NAME));
            final Tile ndviFlags = targetTiles.get(targetProduct.getBand(FLAGS_BAND_NAME));

            final Float redNoDataValue = (float) redSource.getGeophysicalNoDataValue();
            final Float nirNoDataValue = (float) nirSource.getGeophysicalNoDataValue();

            int maskIndex = 0;
            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                    final float nirSample = nirTile.getSampleFloat(x, y);
                    final float redSample = redTile.getSampleFloat(x, y);

                    final float nir = nirFactor * nirSample;
                    final float red = redFactor * redSample;

                    final boolean validMask = redValidMask[maskIndex] && nirValidMask[maskIndex];
                    maskIndex++;

                    if (!validMask || redNoDataValue.equals(redSample) || nirNoDataValue.equals(nirSample)) {
                        ndvi.setSample(x, y, noDataValue);
                        ndviFlags.setSample(x, y, 0);
                        continue;
                    }

                    final float ndviValue = (nir - red) / (nir + red);
                    ndvi.setSample(x, y, computeFlag(x, y, ndviValue, ndviFlags));
                }
                checkForCancellation();
                pm.worked(1);
            }
        } catch (IOException e) {
            throw new OperatorException(e);
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
