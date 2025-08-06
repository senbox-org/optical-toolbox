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
 * Operator test class for Gvmi
 *
 * @author Adrian Draghici
 */
public class GvmiOpTest extends BaseIndexOpTest<GvmiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir2"}, 3, 3, new float[]{760, 2130}, new float[]{0.03358859f, 0.124391556f}, new float[]{0.4852615f, 0.42983055f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.038862377f, 0.020064145f, 0.05512052f, 0.078367986f, 0.09491325f, 0.10728954f, 0.1168962f, 0.12456944f, 0.13083953f});
    }
}
