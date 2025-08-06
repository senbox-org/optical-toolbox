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
 * Operator test class for Vari
 *
 * @author Adrian Draghici
 */
public class VariOpTest extends BaseIndexOpTest<VariOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "green", "red"}, 3, 3, new float[]{450, 560, 670}, new float[]{0.7609583f, 0.019332767f, 0.15395027f}, new float[]{0.9727882f, 0.9894199f, 0.58905125f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.22906785f, 0.15448894f, 0.003010353f, -0.47088698f, 14.757785f, 1.262846f, 0.8675059f, 0.7305274f, 0.6610201f});
    }
}
