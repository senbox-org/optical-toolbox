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
 * Operator test class for Ebbi
 *
 * @author Adrian Draghici
 */
public class EbbiOpTest extends BaseIndexOpTest<EbbiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir1", "thermal"}, 3, 3, new float[]{760, 1550, 10400}, new float[]{0.22425038f, 0.009367049f, 0.07292038f}, new float[]{0.23691589f, 0.45751768f, 0.9392529f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.074909344f, -0.03231014f, -0.016537992f, -0.0068004024f, 3.3248513E-4f, 0.0060265693f, 0.010810748f, 0.014967736f, 0.018665794f});
    }
}
