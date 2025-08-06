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
 * Operator test class for Ndsiwv
 *
 * @author Adrian Draghici
 */
public class NdsiwvOpTest extends BaseIndexOpTest<NdsiwvOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "yellow"}, 3, 3, new float[]{560, 585}, new float[]{0.17573607f, 0.34503847f}, new float[]{0.93589103f, 0.57862914f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.32509732f, -0.1604388f, -0.04896097f, 0.031518113f, 0.09235001f, 0.13994665f, 0.1782037f, 0.20962453f, 0.23589115f});
    }
}
