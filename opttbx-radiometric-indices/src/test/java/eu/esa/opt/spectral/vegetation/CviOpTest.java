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
 * Operator test class for Cvi
 *
 * @author Adrian Draghici
 */
public class CviOpTest extends BaseIndexOpTest<CviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir"}, 3, 3, new float[]{560, 670, 760}, new float[]{0.3931257f, 0.41925204f, 0.12974471f}, new float[]{0.55711794f, 0.6440523f, 0.5866837f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.351967f, 0.4886047f, 0.61550695f, 0.7335812f, 0.84364647f, 0.9464369f, 1.0426099f, 1.1327534f, 1.2173933f});
    }
}
