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
 * Operator test class for Tcariosavi
 *
 * @author Adrian Draghici
 */
public class TcariosaviOpTest extends BaseIndexOpTest<TcariosaviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "redEdge1", "red", "nir"}, 3, 3, new float[]{560, 695, 670, 760}, new float[]{0.26334405f, 0.52066875f, 0.59572214f, 0.17627794f}, new float[]{0.8913971f, 0.5363617f, 0.7722736f, 0.31935024f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.68978035f, 0.7461427f, 0.80966175f, 0.88020784f, 0.95764756f, 1.0418433f, 1.1326544f, 1.2299422f, 1.3335654f});
    }
}
