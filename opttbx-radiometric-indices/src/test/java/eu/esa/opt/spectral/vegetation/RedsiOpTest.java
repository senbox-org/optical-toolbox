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
 * Operator test class for Redsi
 *
 * @author Adrian Draghici
 */
public class RedsiOpTest extends BaseIndexOpTest<RedsiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge1", "redEdge3", "red"}, 3, 3, new float[]{695, 815, 670}, new float[]{0.4987982f, 0.2752962f, 0.061904967f}, new float[]{0.7278206f, 0.85609174f, 0.30841064f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -347.44995f, -221.57697f, -158.49855f, -120.606f, -95.32528f, -77.25831f, -63.70299f, -53.15693f, -44.718185f});
    }
}
