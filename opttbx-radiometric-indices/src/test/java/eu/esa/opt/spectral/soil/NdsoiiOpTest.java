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
 * Operator test class for Ndsoii
 *
 * @author Adrian Draghici
 */
public class NdsoiiOpTest extends BaseIndexOpTest<NdsoiiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "swir2"}, 3, 3, new float[]{560, 2130}, new float[]{0.29920602f, 0.049390435f}, new float[]{0.45677525f, 0.6105784f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.7166326f, -0.45470926f, -0.28187564f, -0.15928443f, -0.06780777f, 0.003066037f, 0.05959287f, 0.10572879f, 0.14409766f});
    }
}
