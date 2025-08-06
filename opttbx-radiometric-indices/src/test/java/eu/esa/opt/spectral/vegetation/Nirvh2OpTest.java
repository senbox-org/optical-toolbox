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
 * Operator test class for Nirvh2
 *
 * @author Adrian Draghici
 */
public class Nirvh2OpTest extends BaseIndexOpTest<Nirvh2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.32282144f, 0.52692723f}, new float[]{0.71981907f, 0.8580843f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.55422324f);
                                  put("k", 0.876758f);
                                  put("lambdaN", 0.04485345f);
                                  put("lambdaR", 0.6804487f);
                              }}
        );
        setTargetValues(new float[]{
                0.761369f, 0.7531389f, 0.74490887f, 0.7366788f, 0.72844875f, 0.72021866f, 0.71198857f, 0.7037585f, 0.69552845f});
    }
}
