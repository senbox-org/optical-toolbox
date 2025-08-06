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

package eu.esa.opt.spectral.urban;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Ndisimndwi
 *
 * @author Adrian Draghici
 */
public class NdisimndwiOpTest extends BaseIndexOpTest<NdisimndwiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "nir", "swir1", "thermal"}, 3, 3, new float[]{560, 760, 1550, 10400}, new float[]{0.68069965f, 0.18362594f, 0.19807053f, 0.055978656f}, new float[]{0.77931476f, 0.93378425f, 0.88686585f, 0.4400164f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.6943401f, -0.5170727f, -0.39564347f, -0.31189433f, -0.25322458f, -0.21137424f, -0.18098517f, -0.15855357f, -0.14175238f});
    }
}
