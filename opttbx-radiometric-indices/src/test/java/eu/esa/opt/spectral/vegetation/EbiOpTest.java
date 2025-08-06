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
 * Operator test class for Ebi
 *
 * @author Adrian Draghici
 */
public class EbiOpTest extends BaseIndexOpTest<EbiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"blue", "green", "red"}, 3, 3, new float[]{450, 560, 670}, new float[]{0.62977386f, 0.48846704f, 0.60394156f}, new float[]{0.8224423f, 0.4894756f, 0.96773595f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.7347153f);
                                  put("epsilon", 0.7176238f);
                              }}
        );
        setTargetValues(new float[]{
                3.209618f, 3.3623338f, 3.5153806f, 3.6687226f, 3.8223293f, 3.9761724f, 4.130228f, 4.2844725f, 4.438886f});
    }
}
