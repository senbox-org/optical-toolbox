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
 * Operator test class for Iavi
 *
 * @author Adrian Draghici
 */
public class IaviOpTest extends BaseIndexOpTest<IaviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "red", "nir"}, 3, 3, new float[]{450, 670, 760}, new float[]{0.5495546f, 0.4601844f, 0.4927299f}, new float[]{0.70938253f, 0.5874168f, 0.838721f});
        setOperatorParameters(new HashMap<>() {{
                                  put("g", 0.1208449f);
                                  put("gamma", 0.5776713f);
                              }}
        );
        setTargetValues(new float[]{
                0.093390934f, 0.11885207f, 0.14146328f, 0.16167775f, 0.1798573f, 0.19629447f, 0.2112283f, 0.22485617f, 0.23734203f});
    }
}
