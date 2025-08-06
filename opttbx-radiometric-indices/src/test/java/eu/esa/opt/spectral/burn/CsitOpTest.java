/*
 *
 *  * Copyright (C) 2025 CS GROUP ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package eu.esa.opt.spectral.burn;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Csit
 *
 * @author Adrian Draghici
 */
public class CsitOpTest extends BaseIndexOpTest<CsitOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir2", "thermal"}, 3, 3, new float[]{760, 2130, 10400}, new float[]{0.61231774f, 0.109375894f, 0.04253179f}, new float[]{0.9949414f, 0.83575654f, 0.56676483f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                1316259.6f, 305186.03f, 140165.95f, 82792.914f, 55820.598f, 40828.562f, 31551.395f, 25364.578f, 21004.62f});
    }
}
