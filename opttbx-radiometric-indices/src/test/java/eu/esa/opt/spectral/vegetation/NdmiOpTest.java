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
 * Operator test class for Ndmi
 *
 * @author Adrian Draghici
 */
public class NdmiOpTest extends BaseIndexOpTest<NdmiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir1"}, 3, 3, new float[]{760, 1550}, new float[]{0.37033588f, 0.3631459f}, new float[]{0.5822008f, 0.74186575f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.009802546f, -0.016929004f, -0.039181285f, -0.05799311f, -0.07410518f, -0.08805963f, -0.100262806f, -0.111024834f, -0.120586775f});
    }
}
