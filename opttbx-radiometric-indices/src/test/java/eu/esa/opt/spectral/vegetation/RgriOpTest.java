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
 * Operator test class for Rgri
 *
 * @author Adrian Draghici
 */
public class RgriOpTest extends BaseIndexOpTest<RgriOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red"}, 3, 3, new float[]{560, 670}, new float[]{0.6139541f, 0.07996988f}, new float[]{0.6405929f, 0.8398923f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.13025384f, 0.28343552f, 0.43497342f, 0.5848938f, 0.7332226f, 0.8799849f, 1.0252054f, 1.1689082f, 1.3111172f});
    }
}
