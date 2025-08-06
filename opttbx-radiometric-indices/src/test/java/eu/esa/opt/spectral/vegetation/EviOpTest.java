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
 * Operator test class for Evi
 *
 * @author Adrian Draghici
 */
public class EviOpTest extends BaseIndexOpTest<EviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "red", "nir"}, 3, 3, new float[]{450, 670, 760}, new float[]{0.667272f, 0.5016232f, 0.5205822f}, new float[]{0.8157375f, 0.6045986f, 0.5662019f});
        setOperatorParameters(new HashMap<>() {{
                                  put("g", 0.72990775f);
                                  put("l", 0.759336f);
                                  put("c1", 0.30341804f);
                                  put("c2", 0.8583335f);
                              }}
        );
        setTargetValues(new float[]{
                0.016102709f, 0.010087594f, 0.003982623f, -0.002214186f, -0.008504867f, -0.014891621f, -0.021376718f, -0.027962402f, -0.03465096f});
    }
}
