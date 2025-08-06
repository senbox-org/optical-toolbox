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
 * Operator test class for Nbrplus
 *
 * @author Adrian Draghici
 */
public class NbrplusOpTest extends BaseIndexOpTest<NbrplusOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "green", "nir2", "nir", "swir2"}, 3, 3, new float[]{450, 560, 900, 760, 2130}, new float[]{0.59228945f, 0.67162424f, 0.046604335f, 0.7287326f, 0.043143034f}, new float[]{0.72855586f, 0.8167925f, 0.45774215f, 0.7958461f, 0.22089463f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.93625724f, -0.9106131f, -0.8885201f, -0.8692883f, -0.85239583f, -0.8374403f, -0.8241064f, -0.8121443f, -0.8013524f});
    }
}
