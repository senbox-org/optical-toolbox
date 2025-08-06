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
 * Operator test class for Mcari1
 *
 * @author Adrian Draghici
 */
public class Mcari1OpTest extends BaseIndexOpTest<Mcari1Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir"}, 3, 3, new float[]{560, 670, 760}, new float[]{0.079478025f, 0.6360903f, 0.2305693f}, new float[]{0.8695398f, 0.92276824f, 0.27429986f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -1.4522653f, -1.3978361f, -1.3434067f, -1.2889776f, -1.2345482f, -1.1801188f, -1.1256896f, -1.0712602f, -1.016831f});
    }
}
