/*
 * Copyright (C) 2002-2007 by ?
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.esa.opt.unmixing;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SpectralUnmixingOpTest {

    @Test
    public void testDefaults() {
        SpectralUnmixingOp op = new SpectralUnmixingOp();
        op.setParameterDefaultValues();
        assertEquals("_error", op.getErrorBandNameSuffix());
        assertEquals("_abundance", op.getAbundanceBandNameSuffix());
        assertNull(op.getEndmemberFile());
        assertEquals(10.0, op.getMinBandwidth(), 1e-8);
        assertFalse(op.getComputeErrorBands());
        assertNull(op.getSourceBandNames());
        assertEquals("Constrained LSU", op.getUnmixingModelName());
    }

    @Test
    @STTM("SNAP-4383")
    public void testNoDataIsPropagatedForAllUnmixingModels() throws IOException {
        for (String modelName : new String[]{"Unconstrained LSU", "Constrained LSU", "Fully Constrained LSU"}) {
            Product targetProduct = createUnmixedProduct(createSourceProduct(true), modelName, false);

            for (String endmemberName : new String[]{"water", "soil", "vegetation"}) {
                Band abundanceBand = targetProduct.getBand(endmemberName + "_abundance");
                assertTrue(abundanceBand.isNoDataValueUsed());
                assertTrue(Double.isNaN(abundanceBand.getNoDataValue()));

                float[] samples = new float[9];
                abundanceBand.readPixels(0, 0, 3, 3, samples);
                assertTrue("invalid pixel must be NoData for " + modelName, Float.isNaN(samples[3]));
                assertFalse(abundanceBand.isPixelValid(0, 1));
                assertTrue(abundanceBand.isPixelValid(1, 0));
            }
            assertNotNull(targetProduct.getMaskGroup().get("unmixing_input_valid"));
        }
    }

    @Test
    @STTM("SNAP-4383")
    public void testValidPixelExpressionsAndErrorBandsArePropagated() throws IOException {
        Product sourceProduct = createSourceProduct(false);
        sourceProduct.getBand("band_2").setValidPixelExpression("X >= Y");

        Product targetProduct = createUnmixedProduct(sourceProduct, "Fully Constrained LSU", true);
        for (String bandName : new String[]{"water_abundance", "soil_abundance", "vegetation_abundance",
                                            "band_1_error", "band_2_error", "band_3_error", "summary_error"}) {
            Band targetBand = targetProduct.getBand(bandName);
            float[] samples = new float[9];
            targetBand.readPixels(0, 0, 3, 3, samples);

            assertTrue(bandName + " must be NoData outside the source footprint", Float.isNaN(samples[3]));
            assertFalse(targetBand.isPixelValid(0, 1));
        }

        Band vegetationAbundance = targetProduct.getBand("vegetation_abundance");
        float[] vegetationSamples = new float[9];
        vegetationAbundance.readPixels(0, 0, 3, 3, vegetationSamples);

        assertEquals(1.0f, vegetationSamples[1], 1.0e-6f);
        assertTrue(vegetationAbundance.isPixelValid(1, 0));
    }


    private static Product createSourceProduct(boolean withNoDataFootprint) {
        Product product = new Product("source", "source", 3, 3);

        for (int i = 0; i < 3; i++) {
            Band band = product.addBand("band_" + (i + 1), ProductData.TYPE_FLOAT32);
            band.setSpectralWavelength(400.0f + i * 100.0f);

            if (withNoDataFootprint) {
                band.setNoDataValue(0.0);
                band.setNoDataValueUsed(true);
            }

            float[] samples = new float[9];
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    samples[y * 3 + x] = withNoDataFootprint && y > x ? 0.0f : i + 1;
                }
            }
            band.setRasterData(ProductData.createInstance(samples));
        }
        return product;
    }

    private static Product createUnmixedProduct(Product sourceProduct, String modelName, boolean computeErrorBands) {
        SpectralUnmixingOp operator = new SpectralUnmixingOp();
        operator.setParameterDefaultValues();
        operator.setSourceProduct(sourceProduct);
        operator.setSourceBandNames(new String[]{"band_1", "band_2", "band_3"});
        operator.setEndmembers(new Endmember[]{
                new Endmember("water", new double[]{400.0, 500.0, 600.0}, new double[]{1.0, 0.0, 0.0}),
                new Endmember("soil", new double[]{400.0, 500.0, 600.0}, new double[]{0.0, 1.0, 0.0}),
                new Endmember("vegetation", new double[]{400.0, 500.0, 600.0}, new double[]{1.0, 2.0, 3.0})
        });
        operator.setUnmixingModelName(modelName);
        operator.setComputeErrorBands(computeErrorBands);
        return operator.getTargetProduct();
    }
}
