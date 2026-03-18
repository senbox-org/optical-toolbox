package gov.nasa.gsfc.seadas.dataio;

import org.junit.Test;
import ucar.nc2.NetcdfFile;

import java.io.IOException;
import java.util.ArrayList;

import static gov.nasa.gsfc.seadas.dataio.SeadasProductReader.ProductType.Level2;
import static gov.nasa.gsfc.seadas.dataio.SeadasProductReader.ProductType.Level3_SeadasMapped;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SeadasProductReaderTest {

    @Test
    public void testGetReaderFromProductType() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        when(netcdfFile.getGlobalAttributes()).thenReturn(new ArrayList<>());
        final SeadasProductReader seadasProductReader = mock(SeadasProductReader.class);
        when(seadasProductReader.getNcfile()).thenReturn(netcdfFile);

        SeadasFileReader fileReader = SeadasProductReader.getReaderFromProductType(Level3_SeadasMapped, seadasProductReader);
        assertTrue(fileReader instanceof Level3_SeadasMappedFileReader);
    }

    @Test
    public void testFindProductTypeWithoutFileAccess(){
        SeadasProductReader.ProductType productType = SeadasProductReader.findProductTypeWithoutFileAccess("Aquarius Level-3 Standard Mapped Image", "dont_care", "dont_care");
        assertEquals(Level3_SeadasMapped.toString(), productType.toString());

        productType = SeadasProductReader.findProductTypeWithoutFileAccess("WHATEVER Level-2", "dont_care", "dont_care");
        assertEquals(Level2.toString(), productType.toString());

    }
}
