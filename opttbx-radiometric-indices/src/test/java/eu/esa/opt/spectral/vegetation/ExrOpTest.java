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
 * Operator test class for Exr
 *
 * @author Adrian Draghici
 */
public class ExrOpTest extends BaseIndexOpTest<ExrOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red"}, 3, 3, new float[]{560, 670}, new float[]{0.5063788f, 0.16584802f}, new float[]{0.88913643f, 0.6425831f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.29077643f, -0.26115167f, -0.23152691f, -0.20190221f, -0.17227739f, -0.14265275f, -0.11302799f, -0.08340323f, -0.05377847f});
    }
}
