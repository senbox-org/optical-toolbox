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
 * Operator test class for Savi2
 *
 * @author Adrian Draghici
 */
public class Savi2OpTest extends BaseIndexOpTest<Savi2Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "nir"}, 3, 3, new float[]{670, 760}, new float[]{0.53812724f, 0.7709568f}, new float[]{0.88449967f, 0.95101917f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.17132556f);
                                  put("sla", 0.688935f);
                                  put("slb", 0.80121607f);
                              }}
        );
        setTargetValues(new float[]{
                0.4532094f, 0.45486346f, 0.45643744f, 0.45793697f, 0.45936725f, 0.4607329f, 0.46203828f, 0.4632872f, 0.46448332f});
    }
}
