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
 * Operator test class for Si
 *
 * @author Adrian Draghici
 */
public class SiOpTest extends BaseIndexOpTest<SiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "green", "red"}, 3, 3, new float[]{450, 560, 670}, new float[]{0.7432763f, 0.024937749f, 0.2078371f}, new float[]{0.75197786f, 0.5028443f, 0.8641494f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.58313745f, 0.5497648f, 0.5152492f, 0.47935408f, 0.44174305f, 0.4019075f, 0.3590121f, 0.3115062f, 0.2558673f});
    }
}
