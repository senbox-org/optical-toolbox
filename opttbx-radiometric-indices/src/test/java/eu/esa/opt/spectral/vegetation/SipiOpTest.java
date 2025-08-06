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
 * Operator test class for Sipi
 *
 * @author Adrian Draghici
 */
public class SipiOpTest extends BaseIndexOpTest<SipiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"aerosols", "red", "nir"}, 3, 3, new float[]{470, 670, 760}, new float[]{0.70863116f, 0.5718169f, 0.101044595f}, new float[]{0.74032384f, 0.71709996f, 0.6686157f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                1.2906165f, 1.2933478f, 1.296869f, 1.3015797f, 1.3082064f, 1.3182148f, 1.3350816f, 1.3695332f, 1.4789982f});
    }
}
