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
 * Operator test class for Ndisig
 *
 * @author Adrian Draghici
 */
public class NdisigOpTest extends BaseIndexOpTest<NdisigOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "nir", "swir1", "thermal"}, 3, 3, new float[]{560, 760, 1550, 10400}, new float[]{0.22780019f, 0.24165434f, 0.85565865f, 0.46993023f}, new float[]{0.90209377f, 0.3564192f, 0.9588774f, 0.9850739f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.03096179f, 0.054717977f, 0.07414515f, 0.09032782f, 0.1040161f, 0.11574543f, 0.12590836f, 0.13479885f, 0.14264196f});
    }
}
