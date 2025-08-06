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
 * Operator test class for Sarvi
 *
 * @author Adrian Draghici
 */
public class SarviOpTest extends BaseIndexOpTest<SarviOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "red", "nir"}, 3, 3, new float[]{450, 670, 760}, new float[]{0.567296f, 0.078392506f, 0.26819026f}, new float[]{0.81188303f, 0.9871748f, 0.75381f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.66427493f);
                              }}
        );
        setTargetValues(new float[]{
                -0.33191565f, -0.28135806f, -0.2362866f, -0.19585428f, -0.15937982f, -0.12630929f, -0.09618745f, -0.068636656f, -0.04334121f});
    }
}
