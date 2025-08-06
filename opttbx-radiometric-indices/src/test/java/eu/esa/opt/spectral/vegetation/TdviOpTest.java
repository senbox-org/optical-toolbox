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
 * Operator test class for Tdvi
 *
 * @author Adrian Draghici
 */
public class TdviOpTest extends BaseIndexOpTest<TdviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.14381719f, 0.73063695f}, new float[]{0.3472522f, 0.97467923f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.811126f, 0.7945616f, 0.77898806f, 0.764316f, 0.75046706f, 0.73737156f, 0.72496766f, 0.7132005f, 0.7020209f});
    }
}
