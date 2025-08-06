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
 * Operator test class for Ikaw
 *
 * @author Adrian Draghici
 */
public class IkawOpTest extends BaseIndexOpTest<IkawOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "red"}, 3, 3, new float[]{450, 670}, new float[]{0.038957715f, 0.006302476f}, new float[]{0.6249233f, 0.33268762f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.7215003f, -0.40867013f, -0.35687256f, -0.33557224f, -0.3239608f, -0.3166524f, -0.31162897f, -0.30796376f, -0.3051716f});
    }
}
