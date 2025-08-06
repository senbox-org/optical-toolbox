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
 * Operator test class for Wri
 *
 * @author Adrian Draghici
 */
public class WriOpTest extends BaseIndexOpTest<WriOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir", "swir1"}, 3, 3, new float[]{560, 670, 760, 1550}, new float[]{0.22363198f, 0.034976006f, 0.24843884f, 0.27596366f}, new float[]{0.91408837f, 0.6088699f, 0.8362787f, 0.63777983f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.49314788f, 0.64787066f, 0.75437534f, 0.8321633f, 0.8914691f, 0.93818027f, 0.9759237f, 1.0070558f, 1.0331736f});
    }
}
