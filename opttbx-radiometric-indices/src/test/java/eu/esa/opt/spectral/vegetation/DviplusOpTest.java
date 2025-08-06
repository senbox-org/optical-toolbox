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
 * Operator test class for Dviplus
 *
 * @author Adrian Draghici
 */
public class DviplusOpTest extends BaseIndexOpTest<DviplusOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir"}, 3, 3, new float[]{560, 670, 760}, new float[]{0.44753677f, 0.19604456f, 0.58234066f}, new float[]{0.82967377f, 0.9497809f, 0.9745691f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.7383413f);
                                  put("lambdaN", 0.5302097f);
                                  put("lambdaR", 0.66655785f);
                                  put("lambdaG", 0.6213263f);
                              }}
        );
        setTargetValues(new float[]{
                0.18457371f, 0.1374976f, 0.09042153f, 0.04334539f, -0.003730774f, -0.05080676f, -0.097882986f, -0.14495909f, -0.19203514f});
    }
}
