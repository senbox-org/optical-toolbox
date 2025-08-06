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
 * Operator test class for Blfei
 *
 * @author Adrian Draghici
 */
public class BlfeiOpTest extends BaseIndexOpTest<BlfeiOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "swir1", "swir2"}, 3, 3, new float[]{560, 670, 1550, 2130}, new float[]{0.3494346f, 0.8558275f, 0.17994434f, 0.82843983f}, new float[]{0.91934705f, 0.94708157f, 0.68264914f, 0.97492146f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.58047354f, 0.4912008f, 0.41832277f, 0.35770297f, 0.3064884f, 0.2626479f, 0.22469586f, 0.19152044f, 0.16227335f});
    }
}
