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
 * Operator test class for Ndisindwi
 *
 * @author Adrian Draghici
 */
public class NdisindwiOpTest extends BaseIndexOpTest<NdisindwiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "nir", "swir1", "thermal"}, 3, 3, new float[]{560, 760, 1550, 10400}, new float[]{0.29407728f, 0.7407537f, 0.3970526f, 0.6054517f}, new float[]{0.6476412f, 0.94827026f, 0.41223007f, 0.7287269f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.4401119f, 0.41077888f, 0.3865904f, 0.36638194f, 0.3493112f, 0.33475378f, 0.32223752f, 0.31139907f, 0.30195427f});
    }
}
