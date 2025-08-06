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
 * Operator test class for Lswi
 *
 * @author Adrian Draghici
 */
public class LswiOpTest extends BaseIndexOpTest<LswiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir1"}, 3, 3, new float[]{760, 1550}, new float[]{0.06405771f, 0.49706662f}, new float[]{0.74997264f, 0.6329528f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.7716809f, -0.54870164f, -0.3854833f, -0.26084f, -0.16254166f, -0.08303413f, -0.017399404f, 0.037702095f, 0.08461759f});
    }
}
