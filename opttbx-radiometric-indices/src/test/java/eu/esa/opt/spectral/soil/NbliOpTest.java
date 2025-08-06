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
 * Operator test class for Nbli
 *
 * @author Adrian Draghici
 */
public class NbliOpTest extends BaseIndexOpTest<NbliOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"red", "thermal"}, 3, 3, new float[]{670, 10400}, new float[]{0.40636575f, 0.32675755f}, new float[]{0.5527121f, 0.37504232f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.10858774f, 0.121282764f, 0.13318767f, 0.14437395f, 0.15490483f, 0.16483621f, 0.17421791f, 0.18309432f, 0.19150516f});
    }
}
