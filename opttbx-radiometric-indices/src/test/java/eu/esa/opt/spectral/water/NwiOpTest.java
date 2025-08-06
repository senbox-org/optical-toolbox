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

package eu.esa.opt.spectral.water;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Nwi
 *
 * @author Adrian Draghici
 */
public class NwiOpTest extends BaseIndexOpTest<NwiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "nir", "swir1", "swir2"}, 3, 3, new float[]{450, 760, 1550, 2130}, new float[]{0.11110586f, 0.1989556f, 0.022672415f, 0.12889397f}, new float[]{0.80550706f, 0.8611909f, 0.19455826f, 0.71648526f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.5186345f, -0.45491102f, -0.42521703f, -0.40803853f, -0.3968401f, -0.38896206f, -0.38311833f, -0.3786111f, -0.37502882f});
    }
}
