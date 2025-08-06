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

package eu.esa.opt.spectral.urban;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Nhfd
 *
 * @author Adrian Draghici
 */
public class NhfdOpTest extends BaseIndexOpTest<NhfdOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"aerosols", "redEdge1", "red"}, 3, 3, new float[]{470, 695, 670}, new float[]{0.14311033f, 0.5038443f, 0.7614866f}, new float[]{0.18228757f, 0.8865246f, 0.93859476f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.55758774f, 0.5769322f, 0.5935652f, 0.6080195f, 0.6206968f, 0.6319059f, 0.64188784f, 0.65083367f, 0.6588969f});
    }
}
