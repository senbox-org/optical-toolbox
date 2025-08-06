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
 * Operator test class for Afri2100
 *
 * @author Adrian Draghici
 */
public class Afri2100OpTest extends BaseIndexOpTest<Afri2100Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir2"}, 3, 3, new float[]{760, 2130}, new float[]{0.07749629f, 0.79011047f}, new float[]{0.5411375f, 0.84233195f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.67200917f, -0.49247313f, -0.34988233f, -0.23389669f, -0.1377055f, -0.056639828f, 0.012607904f, 0.07244629f, 0.124671206f});
    }
}
