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
 * Operator test class for Vrnirbi
 *
 * @author Adrian Draghici
 */
public class VrnirbiOpTest extends BaseIndexOpTest<VrnirbiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.38819557f, 0.009306192f}, new float[]{0.44315153f, 0.68029106f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.9531766f, 0.6183088f, 0.38840622f, 0.22080334f, 0.09319858f, -0.007199909f, -0.08825487f, -0.15506528f, -0.21108289f});
    }
}
