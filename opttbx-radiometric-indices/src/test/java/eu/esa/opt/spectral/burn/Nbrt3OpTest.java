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
 * Operator test class for Nbrt3
 *
 * @author Adrian Draghici
 */
public class Nbrt3OpTest extends BaseIndexOpTest<Nbrt3Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir2", "thermal"}, 3, 3, new float[]{760, 2130, 10400}, new float[]{0.10143912f, 0.3213631f, 0.18913949f}, new float[]{0.7059592f, 0.54932517f, 0.20474547f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.5202261f, -0.3281304f, -0.19940257f, -0.1071267f, -0.037739903f, 0.016334618f, 0.059661753f, 0.09515607f, 0.12476543f});
    }
}
