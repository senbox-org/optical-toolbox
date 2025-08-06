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
 * Operator test class for Vibi
 *
 * @author Adrian Draghici
 */
public class VibiOpTest extends BaseIndexOpTest<VibiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir", "swir1"}, 3, 3, new float[]{670, 760, 1550}, new float[]{0.1391024f, 0.060922086f, 0.026313245f}, new float[]{0.9570154f, 0.32198972f, 0.81937426f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.49626964f, 1.4924973f, 2.5389602f, 3.570402f, 4.5688796f, 5.529021f, 6.4498196f, 7.3319964f, 8.177018f});
    }
}
