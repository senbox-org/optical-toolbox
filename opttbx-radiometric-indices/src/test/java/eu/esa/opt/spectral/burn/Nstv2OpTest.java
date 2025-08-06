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
 * Operator test class for Nstv2
 *
 * @author Adrian Draghici
 */
public class Nstv2OpTest extends BaseIndexOpTest<Nstv2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir2", "thermal"}, 3, 3, new float[]{760, 2130, 10400}, new float[]{0.25842506f, 0.010258019f, 0.27040774f}, new float[]{0.6392583f, 0.088451445f, 0.74212015f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.04125594f, -0.06617727f, -0.08358495f, -0.096431896f, -0.106302865f, -0.11412449f, -0.12047508f, -0.12573388f, -0.13016014f});
    }
}
