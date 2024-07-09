package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.olci.OlciLevel1ProductFactory;
import eu.esa.opt.dataio.s3.olci.OlciLevel2LProductFactory;
import eu.esa.opt.dataio.s3.olci.OlciLevel2WProductFactory;
import eu.esa.opt.dataio.s3.slstr.*;
import eu.esa.opt.dataio.s3.synergy.AODProductFactory;
import eu.esa.opt.dataio.s3.synergy.SynL1CProductFactory;
import eu.esa.opt.dataio.s3.synergy.SynLevel2ProductFactory;
import eu.esa.opt.dataio.s3.synergy.VgtProductFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class Sentinel3ProductReaderTest {

    @Test
    @STTM("SNAP-3666")
    public void testGetProductFactory() {
        Sentinel3ProductReader sentinel3ProductReader = new Sentinel3ProductReader(new Sentinel3ProductReaderPlugIn());

        // with Sentinel3ProductReaderPlugIn -----------------------
        ProductFactory productFactory = sentinel3ProductReader.getProductFactory("S3B_OL_1_EFR____20231214T092214_20231214T092514_20231214T204604_0179_087_207_2340_PS2_O_NT_003.SEN3");
        assertTrue(productFactory instanceof OlciLevel1ProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3");
        assertTrue(productFactory instanceof OlciLevel2LProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_OL_2_WFR____20070425T152940_20070425T153025_20140610T112151_0045_000_000______MAR_D_NR____.SEN3");
        assertTrue(productFactory instanceof OlciLevel2WProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_SL_2_LST____20160329T084522_20160329T084822_20160330T145846_0180_046_207______SVL_O_NR_001.SEN3");
        assertTrue(productFactory instanceof SlstrLstProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_SL_2_WST____20130621T101013_20130621T101053_20140613T135758_0039_009_022______MAR_O_NR____.SEN3");
        assertTrue(productFactory instanceof SlstrWstProductFactory);

        // @todo 3 is that an old type? Where is the spec and do we have testdata? tb 2024-05-29
        productFactory = sentinel3ProductReader.getProductFactory("S3A_SL_2_WCT____20130621T101013_20130621T101053_20140613T135758_0039_009_022______MAR_O_NR____.SEN3");
        assertTrue(productFactory instanceof SlstrSstProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3B_SL_2_FRP____20240325T213812_20240325T214112_20240327T003545_0179_091_129_0720_PS2_O_NT_004.SEN3");
        assertTrue(productFactory instanceof SlstrFrpProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3B_SY_1_SYN____20190601T135821_20190601T140121_20190603T030203_0179_026_067_3240_LN2_O_NT_002");
        assertTrue(productFactory instanceof SynL1CProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_SY_2_SYN____20160415T110058_20160415T110845_20160502T125457_0466_003_094______LN1_D_NC____.SEN3");
        assertTrue(productFactory instanceof SynLevel2ProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_SY_2_AOD____20170619T101637_20170619T101937_20190605T093814_0180_019_065______LR1_D_NT_001.SEN3");
        assertTrue(productFactory instanceof AODProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_SY_2_VG1____20130621T100922_20130621T104922_20140527T011902_GLOBAL____________LN2_D_NR____.SEN3");
        assertTrue(productFactory instanceof VgtProductFactory);


        // with zip extension
        productFactory = sentinel3ProductReader.getProductFactory("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3.zip");
        assertTrue(productFactory instanceof OlciLevel2LProductFactory);


        // with SlstrLevel1B1kmProductReaderPlugIn
        sentinel3ProductReader = new Sentinel3ProductReader(new SlstrLevel1B1kmProductReaderPlugIn());
        productFactory = sentinel3ProductReader.getProductFactory("S3A_SL_1_RBT____20180809T035343_20180809T035643_20180810T124116_0179_034_218_2520_MAR_O_NT_002.SEN3");
        assertTrue(productFactory instanceof SlstrLevel1B1kmProductFactory);


        // with SlstrLevel1B500mProductReaderPlugIn
        sentinel3ProductReader = new Sentinel3ProductReader(new SlstrLevel1B500mProductReaderPlugIn());
        productFactory = sentinel3ProductReader.getProductFactory("S3A_SL_1_RBT____20160329T084522_20160329T084822_20160329T110904_0179_002_235_3420_MAR_O_NR_001.SEN3");
        assertTrue(productFactory instanceof SlstrLevel1B500mProductFactory);

        productFactory = sentinel3ProductReader.getProductFactory("S3A_SL_1_RBT____20130707T154252_20130707T154752_20150217T183537_0299_158_182______SVL_O_NR_001.SEN3.zip");
        assertTrue(productFactory instanceof SlstrLevel1B500mProductFactory);


        // not matching
        sentinel3ProductReader = new Sentinel3ProductReader(new Sentinel3ProductReaderPlugIn());
        productFactory = sentinel3ProductReader.getProductFactory("S5P_NRTI_L2__CLOUD__20240219T082248_20240219T082748_32914_03_020601_20240219T090524");
        assertNull(productFactory);
    }
}
