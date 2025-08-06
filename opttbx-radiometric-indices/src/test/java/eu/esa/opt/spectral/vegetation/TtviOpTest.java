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
 * Operator test class for Ttvi
 *
 * @author Adrian Draghici
 */
public class TtviOpTest extends BaseIndexOpTest<TtviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"redEdge2", "redEdge3", "red", "nir"}, 3, 3, new float[]{730, 815, 670, 760}, new float[]{0.14329779f, 0.026580632f, 0.75786555f, 0.6341201f}, new float[]{0.3185528f, 0.61947674f, 0.9244592f, 0.9486372f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                33.742107f, 35.098293f, 36.218292f, 37.102116f, 37.749752f, 38.161213f, 38.336494f, 38.27559f, 37.978504f});
    }
}
