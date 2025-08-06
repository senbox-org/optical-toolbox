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
 * Operator test class for Nd705
 *
 * @author Adrian Draghici
 */
public class Nd705OpTest extends BaseIndexOpTest<Nd705Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "redEdge2", "red"}, 3, 3, new float[]{695, 730, 670}, new float[]{0.1252312f, 0.32753402f, 0.6443015f}, new float[]{0.43154883f, 0.97879064f, 0.85141075f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.44681615f, 0.42871004f, 0.41686615f, 0.4085147f, 0.40230945f, 0.3975173f, 0.39370483f, 0.3905995f, 0.38802135f});
    }
}
