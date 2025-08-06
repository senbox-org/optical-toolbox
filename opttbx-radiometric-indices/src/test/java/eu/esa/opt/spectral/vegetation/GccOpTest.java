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
 * Operator test class for Gcc
 *
 * @author Adrian Draghici
 */
public class GccOpTest extends BaseIndexOpTest<GccOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "green", "red"}, 3, 3, new float[]{450, 560, 670}, new float[]{0.01273936f, 0.1203478f, 0.11173284f}, new float[]{0.55846596f, 0.3259669f, 0.78759265f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.49157667f, 0.3450923f, 0.28548282f, 0.25314024f, 0.23283811f, 0.218908f, 0.2087569f, 0.20103075f, 0.19495331f});
    }
}
