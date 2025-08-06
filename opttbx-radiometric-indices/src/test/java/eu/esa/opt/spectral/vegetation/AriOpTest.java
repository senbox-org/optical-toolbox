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
 * Operator test class for Ari
 *
 * @author Adrian Draghici
 */
public class AriOpTest extends BaseIndexOpTest<AriOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "redEdge1", "red"}, 3, 3, new float[]{560, 695, 670}, new float[]{0.7585373f, 0.6620443f, 0.39992774f}, new float[]{0.92482734f, 0.79211694f, 0.7892021f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.1921463f, -0.19110262f, -0.18992686f, -0.1886419f, -0.18726659f, -0.18581748f, -0.18430841f, -0.18275142f, -0.18115687f});
    }
}
