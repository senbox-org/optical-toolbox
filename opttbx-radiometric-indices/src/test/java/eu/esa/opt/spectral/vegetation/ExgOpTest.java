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
 * Operator test class for Exg
 *
 * @author Adrian Draghici
 */
public class ExgOpTest extends BaseIndexOpTest<ExgOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "green", "red"}, 3, 3, new float[]{450, 560, 670}, new float[]{0.044525087f, 0.5410382f, 0.26283658f}, new float[]{0.56544256f, 0.67761594f, 0.56676286f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.77471477f, 0.7057537f, 0.6367928f, 0.56783164f, 0.49887064f, 0.42990956f, 0.36094844f, 0.29198748f, 0.22302645f});
    }
}
