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
 * Operator test class for Nsds
 *
 * @author Adrian Draghici
 */
public class NsdsOpTest extends BaseIndexOpTest<NsdsOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"swir1", "swir2"}, 3, 3, new float[]{1550, 2130}, new float[]{0.6949165f, 0.28497458f}, new float[]{0.8342061f, 0.43686193f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.41835457f, 0.4018222f, 0.38643312f, 0.37207264f, 0.35864094f, 0.3460508f, 0.33422548f, 0.32309732f, 0.3126065f});
    }
}
