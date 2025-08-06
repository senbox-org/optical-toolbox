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
 * Operator test class for Vi700
 *
 * @author Adrian Draghici
 */
public class Vi700OpTest extends BaseIndexOpTest<Vi700Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "red"}, 3, 3, new float[]{695, 670}, new float[]{0.06254804f, 0.24581361f}, new float[]{0.79037195f, 0.5768027f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.59432024f, -0.3032838f, -0.14667949f, -0.048839938f, 0.01808598f, 0.06675074f, 0.1037311f, 0.1327842f, 0.15621215f});
    }
}
