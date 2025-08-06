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

package eu.esa.opt.spectral.urban;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Nbui
 *
 * @author Adrian Draghici
 */
public class NbuiOpTest extends BaseIndexOpTest<NbuiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir", "swir1", "thermal"}, 3, 3, new float[]{560, 670, 760, 1550, 10400}, new float[]{0.6127934f, 0.037534595f, 0.33320266f, 0.03834462f, 0.12649173f}, new float[]{0.64875305f, 0.34933442f, 0.65076286f, 0.1760987f, 0.706718f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.7434238f);
                              }}
        );
        setTargetValues(new float[]{
                -1.4509299f, -1.3946793f, -1.3461387f, -1.3023527f, -1.2621157f, -1.2247765f, -1.1899159f, -1.1572329f, -1.1264946f});
    }
}
