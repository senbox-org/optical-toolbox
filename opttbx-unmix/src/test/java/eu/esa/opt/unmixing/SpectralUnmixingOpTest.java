/*
 * Copyright (C) 2002-2007 by ?
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.esa.opt.unmixing;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpectralUnmixingOpTest {

    @Test
    public void testDefaults() {
        SpectralUnmixingOp op = new SpectralUnmixingOp();
        op.setParameterDefaultValues();
        assertEquals("_error", op.getErrorBandNameSuffix());
        assertEquals("_abundance", op.getAbundanceBandNameSuffix());
        assertNull(op.getEndmemberFile());
        assertEquals(10.0, op.getMinBandwidth(), 1e-8);
        assertFalse(op.getComputeErrorBands());
        assertNull(op.getSourceBandNames());
        assertEquals("Constrained LSU", op.getUnmixingModelName());
    }
}
