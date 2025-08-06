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
 * Operator test class for Mmsr705
 *
 * @author Adrian Draghici
 */
public class Mmsr705OpTest extends BaseIndexOpTest<Mmsr705Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"aerosols", "redEdge2", "red"}, 3, 3, new float[]{470, 730, 670}, new float[]{0.59284616f, 0.53962666f, 0.6130104f}, new float[]{0.7716626f, 0.7666025f, 0.880471f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.046994064f, -0.039891545f, -0.033372965f, -0.027369201f, -0.02182155f, -0.016679838f, -0.01190129f, -0.0074485517f, -0.0032894698f});
    }
}
