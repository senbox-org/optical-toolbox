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

package eu.esa.opt.spectral.burn;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Csi
 *
 * @author Adrian Draghici
 */
public class CsiOpTest extends BaseIndexOpTest<CsiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"nir", "swir2"}, 3, 3, new float[]{760, 2130}, new float[]{0.14494735f, 0.5316432f}, new float[]{0.18991363f, 0.7119107f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.27264026f, 0.271697f, 0.27082744f, 0.2700233f, 0.26927742f, 0.2685837f, 0.2679369f, 0.26733235f, 0.26676607f});
    }
}
