package eu.esa.opt.dataio.probav;

import eu.esa.snap.hdf.HDFLoader;
import hdf.object.Attribute;
import hdf.object.h5.H5Datatype;
import hdf.object.h5.H5ScalarAttr;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author olafd
 */
public class ProbaVUtilsTest {

    @Before
    public void loadHdf5Library() {
        HDFLoader.ensureHDF5Initialised();
    }

    @Test
    public void testGetAttributeValue() throws Exception {
        final H5Datatype intDatatype = new H5Datatype(H5Datatype.CLASS_INTEGER, H5Datatype.NATIVE, H5Datatype.NATIVE, -1);
        final Attribute intAttr = new H5ScalarAttr(null,"intAttr", intDatatype, new long[]{1L});
        intAttr.setAttributeData(new int[]{2});
        final String intAttrValue = ProbaVUtils.getAttributeValue(intAttr);
        assertEquals("2", intAttrValue);

        // there might also be shorts or longs within an 'int' attribute:
        intAttr.setAttributeData(new short[]{3});
        final String shortAttrValue = ProbaVUtils.getAttributeValue(intAttr);
        assertEquals("3", shortAttrValue);
        intAttr.setAttributeData(new long[]{4L});
        final String longAttrValue = ProbaVUtils.getAttributeValue(intAttr);
        assertEquals("4", longAttrValue);

        final H5Datatype floatDatatype = new H5Datatype(H5Datatype.CLASS_FLOAT, H5Datatype.NATIVE, H5Datatype.NATIVE, -1);
        final Attribute floatAttr = new H5ScalarAttr(null, "floatAttr", floatDatatype, new long[]{1L});
        floatAttr.setAttributeData(new float[]{4.5f, 2.3f});
        final String floatAttrValue = ProbaVUtils.getAttributeValue(floatAttr);
        assertEquals("4.5 2.3", floatAttrValue);

        // there might also be doubles within a 'float' attribute:
        floatAttr.setAttributeData(new double[]{5.6, 7.8});
        final String doubleAttrValue = ProbaVUtils.getAttributeValue(floatAttr);
        assertEquals("5.6 7.8", doubleAttrValue);

        final H5Datatype stringDatatype = new H5Datatype(H5Datatype.CLASS_STRING, H5Datatype.NATIVE, H5Datatype.NATIVE, -1);
        final Attribute stringAttr = new H5ScalarAttr(null, "stringAttr", stringDatatype, new long[]{1L});
        stringAttr.setAttributeData(new String[]{"bla", "blubb", "lalala"});
        final String stringAttrValue = ProbaVUtils.getAttributeValue(stringAttr);
        assertEquals("bla blubb lalala", stringAttrValue);

    }

    @Test
    public void testGetStringAttributes() throws Exception {
        List<Attribute> attrList = new ArrayList<>();
        final H5Datatype stringDatatype = new H5Datatype(H5Datatype.CLASS_STRING, H5Datatype.NATIVE, H5Datatype.NATIVE, -1);
        final Attribute dummyAttr = new H5ScalarAttr(null, "dummy", stringDatatype, new long[]{1L});
        attrList.add(dummyAttr);
        assertNull(ProbaVUtils.getStringAttributeValue(attrList, "bla"));

        final Attribute descriptionAttr = new H5ScalarAttr(null, "DESCRIPTION", stringDatatype, new long[]{1L});
        descriptionAttr.setAttributeData(new String[]{"This is a description"});
        attrList.add(descriptionAttr);
        final String descriptionFromAttributes = ProbaVUtils.getStringAttributeValue(attrList, "DESCRIPTION");
        assertEquals("This is a description", descriptionFromAttributes);

        final Attribute unitsAttr = new H5ScalarAttr(null, "UNITS", stringDatatype, new long[]{1L});
        unitsAttr.setAttributeData(new String[]{"pounds per square inch"});
        attrList.add(unitsAttr);
        final String unitsFromAttributes = ProbaVUtils.getStringAttributeValue(attrList, "UNITS");
        assertEquals("pounds per square inch", unitsFromAttributes);

    }

    @Test
    public void testGetDoubleAttributes() throws Exception {
        List<Attribute> attrList = new ArrayList<>();
        final H5Datatype datatype = new H5Datatype(H5Datatype.CLASS_FLOAT, H5Datatype.NATIVE, H5Datatype.NATIVE, -1);
        final Attribute dummyAttr = new H5ScalarAttr(null, "dummy", datatype, new long[]{1L});
        attrList.add(dummyAttr);
        assertTrue(Double.isNaN(ProbaVUtils.getDoubleAttributeValue(attrList, "blubb")));

        final Attribute unitsAttr = new H5ScalarAttr(null, "NO_DATA", datatype, new long[]{1L});
        unitsAttr.setAttributeData(new float[]{Float.NaN});
        attrList.add(unitsAttr);
        final double noDataFromAttributes = ProbaVUtils.getDoubleAttributeValue(attrList, "NO_DATA");
        assertTrue(Double.isNaN(noDataFromAttributes));

        final Attribute scaleAttr = new H5ScalarAttr(null, "SCALE", datatype, new long[]{1L});
        scaleAttr.setAttributeData(new float[]{250.0f});
        attrList.add(scaleAttr);
        final double scaleFromAttributes = ProbaVUtils.getDoubleAttributeValue(attrList, "SCALE");
        assertEquals(250.0, scaleFromAttributes, 1e-8);

        final Attribute topLeftLatAttr = new H5ScalarAttr(null, "TOP_LEFT_LONGITUDE", datatype, new long[]{1L});
        topLeftLatAttr.setAttributeData(new float[]{87.3f});
        attrList.add(topLeftLatAttr);
        final double topLeftLatFromAttributes = ProbaVUtils.getDoubleAttributeValue(attrList, "TOP_LEFT_LONGITUDE");
        assertEquals(87.3, topLeftLatFromAttributes, 1e-8);
    }
}
