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
 * Operator test class for Tci
 *
 * @author Adrian Draghici
 */
public class TciOpTest extends BaseIndexOpTest<TciOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "redEdge1", "red"}, 3, 3, new float[]{560, 695, 670}, new float[]{0.4376561f, 0.13775915f, 0.5032555f}, new float[]{0.5074065f, 0.7382583f, 0.6536125f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.41135854f, -0.35274047f, -0.294495f, -0.23679593f, -0.17966041f, -0.1230602f, -0.06695417f, -0.011299491f, 0.043944612f});
    }
}
