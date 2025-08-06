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
 * Operator test class for Msr
 *
 * @author Adrian Draghici
 */
public class MsrOpTest extends BaseIndexOpTest<MsrOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.033845603f, 0.18045479f}, new float[]{0.043553114f, 0.9380887f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                1.7214665f, 2.3022823f, 2.7484624f, 3.1131625f, 3.4220722f, 3.6898785f, 3.9259048f, 4.1365075f, 4.3262486f});
    }
}
