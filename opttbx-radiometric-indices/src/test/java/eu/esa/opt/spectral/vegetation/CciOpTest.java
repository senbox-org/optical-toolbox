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
 * Operator test class for Cci
 *
 * @author Adrian Draghici
 */
public class CciOpTest extends BaseIndexOpTest<CciOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green1", "green", "red"}, 3, 3, new float[]{510, 560, 670}, new float[]{0.021914363f, 0.020353138f, 0.51453906f}, new float[]{0.35690445f, 0.8689508f, 0.95790213f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.9182991f, -0.7986955f, -0.7109279f, -0.64377815f, -0.5907449f, -0.54779994f, -0.51231486f, -0.4825011f, -0.4570997f});
    }
}
