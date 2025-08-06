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
 * Operator test class for Mcari2
 *
 * @author Adrian Draghici
 */
public class Mcari2OpTest extends BaseIndexOpTest<Mcari2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir"}, 3, 3, new float[]{560, 670, 760}, new float[]{0.11388308f, 0.1875338f, 0.20264047f}, new float[]{0.43225235f, 0.47859645f, 0.8731247f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.0747767f, -0.015071215f, 0.039995637f, 0.09009593f, 0.13516358f, 0.17535532f, 0.2109828f, 0.24244644f, 0.27018115f});
    }
}
