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
 * Operator test class for Ari2
 *
 * @author Adrian Draghici
 */
public class Ari2OpTest extends BaseIndexOpTest<Ari2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "redEdge1", "red", "nir"}, 3, 3, new float[]{560, 695, 670, 760}, new float[]{0.053854167f, 0.28036785f, 0.66347766f, 0.44339526f}, new float[]{0.14513344f, 0.95754683f, 0.83539873f, 0.8846195f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                6.651781f, 6.27311f, 5.990131f, 5.772695f, 5.601073f, 5.4624386f, 5.3482413f, 5.252603f, 5.1713758f});
    }
}
