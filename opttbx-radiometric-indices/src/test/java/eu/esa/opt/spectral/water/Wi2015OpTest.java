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

package eu.esa.opt.spectral.water;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Wi2015
 *
 * @author Adrian Draghici
 */
public class Wi2015OpTest extends BaseIndexOpTest<Wi2015Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir", "swir1", "swir2"}, 3, 3, new float[]{560, 670, 760, 1550, 2130}, new float[]{0.5231738f, 0.22945529f, 0.42934787f, 0.26926708f, 0.12106609f}, new float[]{0.66133004f, 0.4744922f, 0.80307174f, 0.39735007f, 0.55163884f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                41.10442f, 36.337524f, 31.570608f, 26.80371f, 22.036802f, 17.269894f, 12.502996f, 7.736084f, 2.969181f});
    }
}
