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
 * Operator test class for Ibi
 *
 * @author Adrian Draghici
 */
public class IbiOpTest extends BaseIndexOpTest<IbiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir", "swir1"}, 3, 3, new float[]{560, 670, 760, 1550}, new float[]{0.92687786f, 0.3449949f, 0.075303435f, 0.5544311f}, new float[]{0.9555308f, 0.6515643f, 0.5698979f, 0.6495246f});
        setOperatorParameters(new HashMap<>() {{
                                  put("l", 0.23741484f);
                              }}
        );
        setTargetValues(new float[]{
                1.4044566f, 1.2989683f, 1.1928973f, 1.0799434f, 0.953423f, 0.80463296f, 0.62044144f, 0.37857738f, 0.03657101f});
    }
}
