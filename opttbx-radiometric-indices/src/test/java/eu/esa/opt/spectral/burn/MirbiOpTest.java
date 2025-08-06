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
 * Operator test class for Mirbi
 *
 * @author Adrian Draghici
 */
public class MirbiOpTest extends BaseIndexOpTest<MirbiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"swir1", "swir2"}, 3, 3, new float[]{1550, 2130}, new float[]{0.22508115f, 0.10531676f}, new float[]{0.69929653f, 0.78326404f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.8473723f, 1.1138923f, 1.3804128f, 1.6469333f, 1.9134531f, 2.1799726f, 2.4464931f, 2.7130146f, 2.9795346f});
    }
}
