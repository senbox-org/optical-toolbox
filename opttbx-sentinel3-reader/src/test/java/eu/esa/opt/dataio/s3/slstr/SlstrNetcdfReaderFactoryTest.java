package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.util.MetTxReader;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlstrNetcdfReaderFactoryTest {

    @Test
    @STTM("SNAP-1684")
    public void testCreateSlstrNetcdfReader() {
        final Manifest manifest = mock(Manifest.class);

        when(manifest.getProductType()).thenReturn("SL_2_FRP___");
        S3NetcdfReader reader = SlstrNetcdfReaderFactory.createSlstrNetcdfReader(new File(""), manifest);
        assertTrue(reader instanceof SlstrFRPReader);

        when(manifest.getProductType()).thenReturn("SL_2_WST");
        reader = SlstrNetcdfReaderFactory.createSlstrNetcdfReader(new File(""), manifest);
        assertTrue(reader instanceof SlstrL2WSTL2PReader);

        when(manifest.getProductType()).thenReturn("whatever");
        reader = SlstrNetcdfReaderFactory.createSlstrNetcdfReader(new File("LST_ancillary_ds.nc"), manifest);
        assertTrue(reader instanceof SlstrLSTAncillaryDsReader);

        reader = SlstrNetcdfReaderFactory.createSlstrNetcdfReader(new File("met_tx.nc"), manifest);
        assertTrue(reader instanceof MetTxReader);

        reader = SlstrNetcdfReaderFactory.createSlstrNetcdfReader(new File("default"), manifest);
        assertNotNull(reader);
    }
}
