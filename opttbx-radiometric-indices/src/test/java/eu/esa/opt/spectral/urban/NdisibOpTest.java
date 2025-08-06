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
 * Operator test class for Ndisib
 *
 * @author Adrian Draghici
 */
public class NdisibOpTest extends BaseIndexOpTest<NdisibOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "nir", "swir1", "thermal"}, 3, 3, new float[]{450, 760, 1550, 10400}, new float[]{0.51728475f, 0.035008192f, 0.12713146f, 0.11904502f}, new float[]{0.5739913f, 0.28736168f, 0.30906802f, 0.18449461f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.3109222f, -0.31993535f, -0.32766688f, -0.33437195f, -0.3402422f, -0.34542447f, -0.35003304f, -0.35415807f, -0.35787195f});
    }
}
