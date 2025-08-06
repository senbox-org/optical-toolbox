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

package eu.esa.opt.spectral.soil;

import eu.esa.opt.radiometry.BaseIndexOpTest;
import org.junit.Before;

import java.util.HashMap;

/**
 * Operator test class for Dbsi
 *
 * @author Adrian Draghici
 */
public class DbsiOpTest extends BaseIndexOpTest<DbsiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir", "swir1"}, 3, 3, new float[]{560, 670, 760, 1550}, new float[]{0.48791033f, 0.55604637f, 0.5153707f, 0.41267955f}, new float[]{0.60633034f, 0.7129242f, 0.74846107f, 0.521104f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                -0.045570664f, -0.05452254f, -0.06269866f, -0.07019464f, -0.0770911f, -0.08345638f, -0.08934897f, -0.09481917f, -0.09991045f});
    }
}
