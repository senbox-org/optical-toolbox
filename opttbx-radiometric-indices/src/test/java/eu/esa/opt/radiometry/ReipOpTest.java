/*
 *
 *  * Copyright (C) 2016 CS ROMANIA
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

package eu.esa.opt.radiometry;

import org.junit.Before;

import java.util.HashMap;

/**
 * Created by dmihailescu on 2/10/2016.
 */

public class ReipOpTest extends BaseIndexOpTest<ReipOp> {

    @Before
    public void setUp() throws Exception {
        setupBands(new String[]{"RED (B4)", "RED (B5)", "RED (B6)", "NIR (B7)"}, 3, 3, new float[]{665, 705, 740, 783}, new float[]{1, 2, 3, 4}, new float[]{9, 10, 11, 12});
        setOperatorParameters(new HashMap<String, Float>() {{
            put("redB4Factor", 1.0f);
            put("redB5Factor", 1.0f);
            put("redB6Factor", 1.0f);
            put("nirFactor", 1.0f);
        }});
        setTargetValues(new float[]{
                722.5f, 722.5f, 722.5f,
                722.5f, 722.5f, 722.5f,
                722.5f, 722.5f, 722.5f});
    }
}