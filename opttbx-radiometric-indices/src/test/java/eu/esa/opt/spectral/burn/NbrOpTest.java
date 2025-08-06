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
 * Operator test class for Nbr
 *
 * @author Adrian Draghici
 */
public class NbrOpTest extends BaseIndexOpTest<NbrOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir2"}, 3, 3, new float[]{760, 2130}, new float[]{0.18517792f, 0.01830089f}, new float[]{0.6619118f, 0.79642135f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.8201199f, 0.3585649f, 0.17697552f, 0.07990079f, 0.019477215f, -0.021755792f, -0.051687848f, -0.074404895f, -0.09223514f});
    }
}
