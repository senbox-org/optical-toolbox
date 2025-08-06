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

package eu.esa.opt.spectral.vegetation;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Ndii
 *
 * @author Adrian Draghici
 */
public class NdiiOpTest extends BaseIndexOpTest<NdiiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir1"}, 3, 3, new float[]{760, 1550}, new float[]{0.033677578f, 0.5629425f}, new float[]{0.10279906f, 0.8367791f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.88710546f, -0.8676515f, -0.850642f, -0.8356433f, -0.822319f, -0.81040335f, -0.7996841f, -0.78998995f, -0.7811804f});
    }
}
