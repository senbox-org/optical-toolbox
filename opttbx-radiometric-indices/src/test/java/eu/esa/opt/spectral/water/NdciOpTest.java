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
 * Operator test class for Ndci
 *
 * @author Adrian Draghici
 */
public class NdciOpTest extends BaseIndexOpTest<NdciOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "red"}, 3, 3, new float[]{695, 670}, new float[]{0.23232889f, 0.175075f}, new float[]{0.8623346f, 0.3373562f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.1405335f, 0.228496f, 0.28768298f, 0.33022967f, 0.3622887f, 0.38731244f, 0.40738764f, 0.4238502f, 0.43759474f});
    }
}
