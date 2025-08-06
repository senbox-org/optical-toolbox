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

package eu.esa.opt.spectral.soil;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Bain
 *
 * @author Adrian Draghici
 */
public class BainOpTest extends BaseIndexOpTest<BainOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir", "swir1"}, 3, 3, new float[]{670, 760, 1550}, new float[]{0.032091558f, 0.49693f, 0.29478365f}, new float[]{0.8250085f, 0.5092566f, 0.8635793f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.1700548f, -0.0013815463f, 0.1672917f, 0.33596498f, 0.50463814f, 0.6733115f, 0.8419846f, 1.010658f, 1.1793312f});
    }
}
