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

package eu.esa.opt.spectral.soil;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Nsdsi3
 *
 * @author Adrian Draghici
 */
public class Nsdsi3OpTest extends BaseIndexOpTest<Nsdsi3Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"swir1", "swir2"}, 3, 3, new float[]{1550, 2130}, new float[]{0.014938831f, 0.5393385f}, new float[]{0.74465615f, 0.5535872f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.9460962f, -0.67199785f, -0.4667663f, -0.3073441f, -0.17993365f, -0.07577271f, 0.010970344f, 0.0843274f, 0.147175f});
    }
}
