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
 * Operator test class for Ndponi
 *
 * @author Adrian Draghici
 */
public class NdponiOpTest extends BaseIndexOpTest<NdponiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "swir1"}, 3, 3, new float[]{560, 1550}, new float[]{0.5007321f, 0.12395722f}, new float[]{0.89127547f, 0.13627428f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.60313964f, -0.6281833f, -0.64974993f, -0.66851664f, -0.68499535f, -0.69958055f, -0.7125808f, -0.72424126f, -0.7347588f});
    }
}
