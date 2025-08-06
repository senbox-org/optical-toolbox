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

package eu.esa.opt.spectral.soil;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Ri4xs
 *
 * @author Adrian Draghici
 */
public class Ri4xsOpTest extends BaseIndexOpTest<Ri4xsOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red"}, 3, 3, new float[]{560, 670}, new float[]{0.13590622f, 0.82970345f}, new float[]{0.66541654f, 0.9888238f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                2017.8481f, 432.71313f, 145.93015f, 63.201332f, 32.08261f, 18.174608f, 11.15672f, 7.2814913f, 4.987276f});
    }
}
