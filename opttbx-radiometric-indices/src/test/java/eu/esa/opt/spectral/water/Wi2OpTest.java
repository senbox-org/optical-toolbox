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
 * Operator test class for Wi2
 *
 * @author Adrian Draghici
 */
public class Wi2OpTest extends BaseIndexOpTest<Wi2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "swir2"}, 3, 3, new float[]{450, 2130}, new float[]{0.13852584f, 0.4186074f}, new float[]{0.15871334f, 0.6345136f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.50271916f, -0.51913244f, -0.53397334f, -0.54745764f, -0.55976313f, -0.571038f, -0.58140635f, -0.59097344f, -0.59982866f});
    }
}
