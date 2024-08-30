/*
 * Copyright (c) 2022.  Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 *
 */

package eu.esa.opt.c2rcc.msi;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.gpf.Operator;
import org.junit.Test;

import java.io.IOException;

import static eu.esa.opt.c2rcc.msi.ExpectedSignature.EXPECTED_CONC_CHL;
import static eu.esa.opt.c2rcc.msi.ExpectedSignature.EXPECTED_CONC_TSM;
import static eu.esa.opt.c2rcc.msi.ExpectedSignature.EXPECTED_IOP_APIG;
import static eu.esa.opt.c2rcc.msi.ExpectedSignature.EXPECTED_IOP_BWIT;
import static eu.esa.opt.c2rcc.msi.ExpectedSignature.EXPECTED_RHOW_BANDS;
import static org.junit.Assert.assertEquals;


/**
 * @author Marco Peters
 */
public class MsiOperatorTest {

    @Test
    public void testWithDefaults() throws IOException {
        final Operator operator = initOperator();
        operator.setSourceProduct(MsiTestProduct.createInput());
        final Product target = operator.getTargetProduct();

        assertEquals(12.029993f, getSampleFloat(target, EXPECTED_CONC_CHL, 0, 0), 1.0e-6);
        assertEquals(5.572476f, getSampleFloat(target, EXPECTED_CONC_TSM, 0, 0), 1.0e-6);
        assertEquals(0.585264f, getSampleFloat(target, EXPECTED_IOP_APIG, 0, 0), 1.0e-6);
        assertEquals(4.568540f, getSampleFloat(target, EXPECTED_IOP_BWIT, 0, 0), 1.0e-6);

        float[] EXPECTED_RHOW_VALUES = new float[]{0.003817f, 0.005736f, 0.011709f, 0.008642f, 0.007270f, 0.002554f, 0.002591f, 0.001073f};
        for (int i = 0; i < EXPECTED_RHOW_BANDS.length; i++) {
            assertEquals(EXPECTED_RHOW_VALUES[i], getSampleFloat(target, EXPECTED_RHOW_BANDS[i], 0, 0), 1.0e-6);
        }
    }

    @Test
    public void testWithInternalECMWFAuxdata_ButAuxDataNotAvailable() throws IOException {
        final Operator operator = initOperator();
        operator.setParameter("useEcmwfAuxData", true);
        final Product input = MsiTestProduct.createInput();
        input.removeBand(input.getBand("tco3"));
        input.removeBand(input.getBand("msl"));
        input.removeBand(input.getBand("tcwv"));
        operator.setSourceProduct(input);
        final Product target = operator.getTargetProduct();

        // values should be the same as in testWithDefaults
        assertEquals(12.029993f, getSampleFloat(target, EXPECTED_CONC_CHL, 0, 0), 1.0e-6);
        assertEquals(5.572476f, getSampleFloat(target, EXPECTED_CONC_TSM, 0, 0), 1.0e-6);
        assertEquals(0.585264f, getSampleFloat(target, EXPECTED_IOP_APIG, 0, 0), 1.0e-6);
        assertEquals(4.568540f, getSampleFloat(target, EXPECTED_IOP_BWIT, 0, 0), 1.0e-6);

        float[] EXPECTED_RHOW_VALUES = new float[]{0.003817f, 0.005736f, 0.011709f, 0.008642f, 0.007270f, 0.002554f, 0.002591f, 0.001073f};
        for (int i = 0; i < EXPECTED_RHOW_BANDS.length; i++) {
            assertEquals(EXPECTED_RHOW_VALUES[i], getSampleFloat(target, EXPECTED_RHOW_BANDS[i], 0, 0), 1.0e-6);
        }

    }

    @Test
    public void testWithInternalECMWFAuxdata() throws IOException {
        final Operator operator = initOperator();
        operator.setParameter("useEcmwfAuxData", true);
        final Product input = MsiTestProduct.createInput();
        operator.setSourceProduct(input);
        final Product target = operator.getTargetProduct();

        // values should be the same as in testWithDefaults
        assertEquals(12.617269f, getSampleFloat(target, EXPECTED_CONC_CHL, 0, 0), 1.0e-6);
        assertEquals(5.383925f, getSampleFloat(target, EXPECTED_CONC_TSM, 0, 0), 1.0e-6);
        assertEquals(0.612711f, getSampleFloat(target, EXPECTED_IOP_APIG, 0, 0), 1.0e-6);
        assertEquals(4.493466f, getSampleFloat(target, EXPECTED_IOP_BWIT, 0, 0), 1.0e-6);

        float[] EXPECTED_RHOW_VALUES = new float[]{0.003418f, 0.005088f, 0.010344f, 0.007879f, 0.006699f, 0.002393f, 0.002434f, 0.001014f};

        for (int i = 0; i < EXPECTED_RHOW_BANDS.length; i++) {
            assertEquals(EXPECTED_RHOW_VALUES[i], getSampleFloat(target, EXPECTED_RHOW_BANDS[i], 0, 0), 1.0e-6);
        }
    }

    @Test
    public void testWithInternalECMWFAuxdata_WithIssue_SIITBX_497() throws IOException {
        final Operator operator = initOperator();
        operator.setParameter("useEcmwfAuxData", true);
        final Product input = MsiTestProduct.createInput();
        final Band tco3 = input.getBand("tco3");
        tco3.setName("temp");
        final Band tcwv = input.getBand("tcwv");
        tcwv.setName("tco3");
        tco3.setName("tcwv");

        operator.setSourceProduct(input);
        final Product target = operator.getTargetProduct();

        // values should be the same as in testWithInternalECMWFAuxdata, because the swap in data is considered and corrected
        assertEquals(12.617269f, getSampleFloat(target, EXPECTED_CONC_CHL, 0, 0), 1.0e-6);
        assertEquals(5.383925f, getSampleFloat(target, EXPECTED_CONC_TSM, 0, 0), 1.0e-6);
        assertEquals(0.612711f, getSampleFloat(target, EXPECTED_IOP_APIG, 0, 0), 1.0e-6);
        assertEquals(4.4934663f, getSampleFloat(target, EXPECTED_IOP_BWIT, 0, 0), 1.0e-6);

        float[] EXPECTED_RHOW_VALUES = new float[]{0.003418f, 0.005088f, 0.010344f, 0.007879f, 0.006699f, 0.002393f, 0.002434f, 0.001014f};

        for (int i = 0; i < EXPECTED_RHOW_BANDS.length; i++) {
            assertEquals(EXPECTED_RHOW_VALUES[i], getSampleFloat(target, EXPECTED_RHOW_BANDS[i], 0, 0), 1.0e-6);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static float getSampleFloat(Product target, String rasterName, int x, int y) throws IOException {
        final RasterDataNode raster = target.getRasterDataNode(rasterName);
        raster.readRasterDataFully(ProgressMonitor.NULL);
        return raster.getSampleFloat(x, y);
    }

    private Operator initOperator() {
        return new C2rccMsiOperator.Spi().createOperator();
    }
}