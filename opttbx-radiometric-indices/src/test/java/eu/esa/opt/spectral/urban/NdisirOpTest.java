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
 * Operator test class for Ndisir
 *
 * @author Adrian Draghici
 */
public class NdisirOpTest extends BaseIndexOpTest<NdisirOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir", "swir1", "thermal"}, 3, 3, new float[]{670, 760, 1550, 10400}, new float[]{0.25952035f, 0.7231276f, 0.31278986f, 0.57218724f}, new float[]{0.6485373f, 0.74325997f, 0.49023145f, 0.9946346f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.13981539f, 0.15605982f, 0.17013805f, 0.18245605f, 0.19332473f, 0.20298557f, 0.21162926f, 0.21940857f, 0.22644684f});
    }
}
