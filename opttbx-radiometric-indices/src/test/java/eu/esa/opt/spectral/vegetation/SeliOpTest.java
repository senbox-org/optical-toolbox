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
 * Operator test class for Seli
 *
 * @author Adrian Draghici
 */
public class SeliOpTest extends BaseIndexOpTest<SeliOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "red", "nir"}, 3, 3, new float[]{695, 670, 760}, new float[]{0.20434779f, 0.74595875f, 0.021301866f}, new float[]{0.6756629f, 0.8463079f, 0.37854183f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.9955687f, -0.96748805f, -0.9268268f, -0.8810076f, -0.8334545f, -0.7858822f, -0.7391848f, -0.6938314f, -0.65005744f});
    }
}
