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
 * Operator test class for S2wi
 *
 * @author Adrian Draghici
 */
public class S2wiOpTest extends BaseIndexOpTest<S2wiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "red", "swir2"}, 3, 3, new float[]{695, 670, 2130}, new float[]{0.21017301f, 0.33685356f, 0.30126643f}, new float[]{0.3446601f, 0.8438754f, 0.732089f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.17811184f, -0.22012483f, -0.2530418f, -0.27952817f, -0.3013007f, -0.3195149f, -0.3349773f, -0.34826759f, -0.3598135f});
    }
}
