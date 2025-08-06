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
 * Operator test class for Ndvimndwi
 *
 * @author Adrian Draghici
 */
public class NdvimndwiOpTest extends BaseIndexOpTest<NdvimndwiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir", "swir1"}, 3, 3, new float[]{560, 670, 760, 1550}, new float[]{0.598291f, 0.04065019f, 0.115912914f, 0.51403093f}, new float[]{0.6068682f, 0.24072576f, 0.7805086f, 0.94653654f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.40496665f, 0.4770107f, 0.53125495f, 0.5772964f, 0.61801165f, 0.65471315f, 0.688163f, 0.7188729f, 0.7472198f});
    }
}
