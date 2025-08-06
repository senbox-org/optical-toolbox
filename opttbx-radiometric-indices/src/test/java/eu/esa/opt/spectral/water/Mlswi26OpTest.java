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

package eu.esa.opt.spectral.water;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Mlswi26
 *
 * @author Adrian Draghici
 */
public class Mlswi26OpTest extends BaseIndexOpTest<Mlswi26Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir1"}, 3, 3, new float[]{760, 1550}, new float[]{0.30665094f, 0.14321715f}, new float[]{0.7796323f, 0.16739482f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.65760714f, 0.6252511f, 0.5878832f, 0.54424125f, 0.49260038f, 0.4305396f, 0.3545512f, 0.25935432f, 0.13661173f});
    }
}
