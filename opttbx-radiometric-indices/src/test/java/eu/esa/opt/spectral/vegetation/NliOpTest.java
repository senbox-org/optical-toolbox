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
 * Operator test class for Nli
 *
 * @author Adrian Draghici
 */
public class NliOpTest extends BaseIndexOpTest<NliOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.108872f, 0.87083375f}, new float[]{0.97264737f, 0.9080306f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.74891824f, 0.55895334f, 0.40913028f, 0.28796017f, 0.18795663f, 0.10403238f, 0.03260924f, -0.028902339f, -0.082422905f});
    }
}
