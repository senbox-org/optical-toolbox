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
 * Operator test class for Vig
 *
 * @author Adrian Draghici
 */
public class VigOpTest extends BaseIndexOpTest<VigOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red"}, 3, 3, new float[]{560, 670}, new float[]{0.7062759f, 2.9563904E-4f}, new float[]{0.89397043f, 0.06976658f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.99916315f, 0.97568893f, 0.9541725f, 0.93437874f, 0.9161086f, 0.89919287f, 0.88348633f, 0.8688637f, 0.85521656f});
    }
}
