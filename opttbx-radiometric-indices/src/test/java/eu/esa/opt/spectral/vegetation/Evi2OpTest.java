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
 * Operator test class for Evi2
 *
 * @author Adrian Draghici
 */
public class Evi2OpTest extends BaseIndexOpTest<Evi2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.08405948f, 0.19304043f}, new float[]{0.98053f, 0.38830066f});
        setOperatorParameters(new HashMap<>() {{
                                  put("g", 0.6759559f);
                                  put("l", 0.57741797f);
                              }}
        );
        setTargetValues(new float[]{
                0.07577271f, 0.01139261f, -0.0287578f, -0.05619061f, -0.07612211f, -0.09125902f, -0.103145644f, -0.11272734f, -0.12061526f});
    }
}
