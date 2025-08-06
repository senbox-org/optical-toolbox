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

package eu.esa.opt.spectral.burn;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Bai
 *
 * @author Adrian Draghici
 */
public class BaiOpTest extends BaseIndexOpTest<BaiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.1827566f, 0.021149218f}, new float[]{0.45680672f, 0.9911638f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                119.6453f, 48.823494f, 15.538363f, 7.1437383f, 4.0426974f, 2.587406f, 1.7941523f, 1.3157961f, 1.0056547f});
    }
}
