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
 * Operator test class for Grvi
 *
 * @author Adrian Draghici
 */
public class GrviOpTest extends BaseIndexOpTest<GrviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "nir"}, 3, 3, new float[]{560, 760}, new float[]{0.21326768f, 0.11990398f}, new float[]{0.7940141f, 0.7754407f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.5622229f, 0.7060987f, 0.7916997f, 0.8484684f, 0.8888721f, 0.9190958f, 0.94255644f, 0.9612956f, 0.9766082f});
    }
}
