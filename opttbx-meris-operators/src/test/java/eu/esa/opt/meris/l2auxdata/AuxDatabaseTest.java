package eu.esa.opt.meris.l2auxdata;

import com.bc.ceres.annotation.STTM;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuxDatabaseTest {

    private AuxDatabase auxDatabase;

    @Before
    public void setUp() {
        auxDatabase = AuxDatabase.getInstance();
    }

    @Test
    @STTM("SNAP-3680")
    public void testGetInstance() {
        final AuxDatabase instance_2 = AuxDatabase.getInstance();

        assertSame(auxDatabase, instance_2);
    }

    @Test
    @STTM("SNAP-3680")
    public void testGetFileInfoCount()  {
        assertEquals(30, auxDatabase.getFileInfoCount());
    }

    @Test
    @STTM("SNAP-3680")
    public void testGetFileInfo()  {
        final AuxFileInfo fileInfo = auxDatabase.getFileInfo('H');
        assertNotNull(fileInfo);

        assertEquals('H', fileInfo.getTypeId());
        assertEquals("clo", fileInfo.getDirName());
        assertEquals(8, fileInfo.getDatasetCount());
        assertFalse(fileInfo.isEditable());
        assertTrue(fileInfo.isImport());

        final AuxDatasetInfo datasetInfo = fileInfo.getDatasetInfo(3);
        assertEquals(3, datasetInfo.getIndex());
        assertEquals('3', datasetInfo.getId());
        assertEquals(4, datasetInfo.getType());
        assertEquals(400, datasetInfo.getRecordSize());
        assertEquals("ADS Surface Flag .1 deg", datasetInfo.getName());

        final AuxVariableInfo variableInfo = fileInfo.getVariableInfo(5);
        assertEquals("H112", variableInfo.getId());
        assertEquals(1425, variableInfo.getOffset());
        assertEquals(1.0, variableInfo.getScale(), 1e-8);
        assertEquals(41, variableInfo.getDataType());
        assertEquals(11, variableInfo.getDim1());
        assertEquals(0, variableInfo.getDim2());
        assertEquals(0, variableInfo.getDim3());
        assertEquals(0, variableInfo.getEditFlag());
        assertEquals(0, variableInfo.getEditType());
        assertEquals(1, variableInfo.getDisplayType());
        assertEquals("Number of Data Set Records", variableInfo.getComment());
    }

    @Test
    @STTM("SNAP-3680")
    public void testGetVariableInfo()  {
        AuxVariableInfo variableInfo = auxDatabase.getVariableInfo("Q10E");
        assertNotNull(variableInfo);

        assertEquals("Q10E", variableInfo.getId());
        assertEquals(585, variableInfo.getOffset());
        assertEquals(1.0, variableInfo.getScale(), 1e-8);
        assertEquals(41, variableInfo.getDataType());
        assertEquals(11, variableInfo.getDim1());
        assertEquals(0, variableInfo.getDim2());
        assertEquals(0, variableInfo.getDim3());
        assertEquals(0, variableInfo.getEditFlag());
        assertEquals(1, variableInfo.getEditType());
        assertEquals(1, variableInfo.getDisplayType());
        assertEquals("Number of Data Set Records", variableInfo.getComment());
    }
}
