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
 * Operator test class for Nddi
 *
 * @author Adrian Draghici
 */
public class NddiOpTest extends BaseIndexOpTest<NddiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir"}, 3, 3, new float[]{560, 670, 760}, new float[]{0.50916326f, 0.7723518f, 0.55962485f}, new float[]{0.5406649f, 0.9525595f, 0.7707931f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.5436547f, 0.3901969f, 0.2586313f, 0.14479168f, 0.045481283f, -0.041791603f, -0.11899364f, -0.18769534f, -0.2491657f});
    }
}
