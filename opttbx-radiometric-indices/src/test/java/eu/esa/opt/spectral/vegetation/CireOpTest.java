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
 * Operator test class for Cire
 *
 * @author Adrian Draghici
 */
public class CireOpTest extends BaseIndexOpTest<CireOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "red", "nir"}, 3, 3, new float[]{695, 670, 760}, new float[]{0.45874894f, 0.07569641f, 0.3785445f}, new float[]{0.9793337f, 0.76300037f, 0.5918575f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.17483294f, -0.22643828f, -0.26663893f, -0.2988392f, -0.3252113f, -0.34720606f, -0.36582994f, -0.3818028f, -0.3956529f});
    }
}
