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
 * Operator test class for Mcariosavi705
 *
 * @author Adrian Draghici
 */
public class Mcariosavi705OpTest extends BaseIndexOpTest<Mcariosavi705Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "redEdge1", "redEdge2", "red"}, 3, 3, new float[]{560, 695, 730, 670}, new float[]{0.24169892f, 0.12926573f, 0.13778698f, 0.16760576f}, new float[]{0.387873f, 0.94281846f, 0.23741382f, 0.8592351f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                1.3494792f, 0.22097984f, 0.23873708f, 0.24244331f, 0.24713372f, 0.25342867f, 0.26096964f, 0.26941687f, 0.27852663f});
    }
}
