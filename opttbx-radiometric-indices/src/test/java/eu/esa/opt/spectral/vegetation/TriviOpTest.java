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
 * Operator test class for Trivi
 *
 * @author Adrian Draghici
 */
public class TriviOpTest extends BaseIndexOpTest<TriviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir"}, 3, 3, new float[]{560, 670, 760}, new float[]{0.6749219f, 0.17060626f, 0.06865668f}, new float[]{0.8066529f, 0.6349389f, 0.43309486f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                14.055649f, 11.643433f, 9.231216f, 6.8190002f, 4.406782f, 1.994566f, -0.41765022f, -2.8298645f, -5.2420826f});
    }
}
