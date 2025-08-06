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
 * Operator test class for Wi1
 *
 * @author Adrian Draghici
 */
public class Wi1OpTest extends BaseIndexOpTest<Wi1Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "swir2"}, 3, 3, new float[]{560, 2130}, new float[]{0.010039926f, 0.087182105f}, new float[]{0.8008013f, 0.7398146f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.793464f, -0.21565597f, -0.09302053f, -0.039693147f, -0.0098639075f, 0.009194423f, 0.022423444f, 0.032142974f, 0.039585922f});
    }
}
