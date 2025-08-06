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

package eu.esa.opt.spectral.burn;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Nbr2
 *
 * @author Adrian Draghici
 */
public class Nbr2OpTest extends BaseIndexOpTest<Nbr2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"swir1", "swir2"}, 3, 3, new float[]{1550, 2130}, new float[]{0.095122695f, 0.1450333f}, new float[]{0.6426218f, 0.20668375f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.20782577f, 0.034209576f, 0.18232326f, 0.2823011f, 0.35432866f, 0.40868935f, 0.4511732f, 0.48528904f, 0.51328766f});
    }
}
