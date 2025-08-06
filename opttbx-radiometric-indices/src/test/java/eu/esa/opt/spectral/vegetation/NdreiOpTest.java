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
 * Operator test class for Ndrei
 *
 * @author Adrian Draghici
 */
public class NdreiOpTest extends BaseIndexOpTest<NdreiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "red", "nir"}, 3, 3, new float[]{695, 670, 760}, new float[]{0.0068739057f, 0.11617541f, 0.39396417f}, new float[]{0.6248091f, 0.490223f, 0.8542376f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.96570235f, 0.68590903f, 0.5186156f, 0.4073284f, 0.32795566f, 0.2684905f, 0.22227792f, 0.18533193f, 0.1551192f});
    }
}
