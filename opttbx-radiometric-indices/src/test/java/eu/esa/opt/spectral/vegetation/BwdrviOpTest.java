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
 * Operator test class for Bwdrvi
 *
 * @author Adrian Draghici
 */
public class BwdrviOpTest extends BaseIndexOpTest<BwdrviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "nir"}, 3, 3, new float[]{450, 760}, new float[]{0.46973842f, 0.4499092f}, new float[]{0.7623431f, 0.58171016f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.111041486f);
                                  put("alpha", 0.7929592f);
                              }}
        );
        setTargetValues(new float[]{
                -0.13669573f, -0.15578632f, -0.17282961f, -0.18813817f, -0.20196417f, -0.21451288f, -0.22595349f, -0.23642667f, -0.24605007f});
    }
}
