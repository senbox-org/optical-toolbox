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
 * Operator test class for Mtvi1
 *
 * @author Adrian Draghici
 */
public class Mtvi1OpTest extends BaseIndexOpTest<Mtvi1Op> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"green", "red", "nir"}, 3, 3, new float[]{560, 670, 760}, new float[]{0.5168569f, 0.53716457f, 0.68602234f}, new float[]{0.87383336f, 0.65373546f, 0.8198844f});
        setOperatorParameters(new HashMap<>());
        setTargetValues(new float[]{
                0.18267527f, 0.23266673f, 0.28265822f, 0.33264968f, 0.38264123f, 0.4326329f, 0.4826243f, 0.5326158f, 0.58260727f});
    }
}
