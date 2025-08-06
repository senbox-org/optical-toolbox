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
 * Operator test class for Nbai
 *
 * @author Adrian Draghici
 */
public class NbaiOpTest extends BaseIndexOpTest<NbaiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "swir1", "swir2"}, 3, 3, new float[]{560, 1550, 2130}, new float[]{0.2459197f, 0.26208818f, 0.6669823f}, new float[]{0.2686082f, 0.79843223f, 0.9362863f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.43580562f, 0.36077157f, 0.299106f, 0.24752836f, 0.20375045f, 0.16612731f, 0.13344628f, 0.104793616f, 0.07946767f});
    }
}
