package eu.esa.opt.dataio.s2.filepatterns;

import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.S2Config;
import eu.esa.opt.dataio.s2.S2ProductNamingUtils;
import eu.esa.opt.dataio.s2.S2SpatialResolution;
import eu.esa.opt.dataio.s2.l2a.L2aUtils;
import eu.esa.opt.dataio.s2.l2hf.L2hfUtils;
import org.esa.snap.core.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by obarrile on 02/11/2016.
 */
public class SAFECOMPACTNamingConvention implements INamingConvention {
    //REGEXs and getters
    public static String PRODUCT_REGEX = "(S2A|S2B|S2C|S2_|LS8|LS9)_([A-Z|0-9|_]{6})_([0-9]{8}T[0-9]{6})_N([0-9]{4})_R([0-9]{3})_.*";
    // MTD_OLIL2F
    public static String PRODUCT_XML_REGEX = "MTD_(MSIL(1C|2A|2H|2F|03)|OLIL2(H|F))\\.xml";
    public static String GRANULE_REGEX = "(L1C|L2A|L2H|L2F|L03)_T([A-Z|0-9|_]{5})_A([0-9]{6})_([0-9]{8}T[0-9]{6})(|_(S2A|S2B|S2C|S2_|LS8|LS9)_R([0-9]{3}))";
    public static String GRANULE_XML_REGEX = "MTD_TL(|_L2H|_L2F)\\.xml";
    public static String DATASTRIP_REGEX = "DS_([A-Z|0-9|_]{4})_([0-9]{8}T[0-9]{6})_S([0-9]{8}T[0-9]{6})";
    public static String DATASTRIP_XML_REGEX = "MTD_DS\\.xml";
    @Override
    public String[] getProductREGEXs() {
        String[] REGEXs = {PRODUCT_REGEX};
        return REGEXs;
    }

    @Override
    public String[] getProductXmlREGEXs() {
        String[] REGEXs = {PRODUCT_XML_REGEX};
        return REGEXs;
    }

    @Override
    public String[] getGranuleREGEXs() {
        String[] REGEXs = {GRANULE_REGEX};
        return REGEXs;
    }

    @Override
    public String[] getGranuleXmlREGEXs() {
        String[] REGEXs = {GRANULE_XML_REGEX};
        return REGEXs;
    }

    @Override
    public String[] getDatastripREGEXs() {
        String[] REGEXs = {DATASTRIP_REGEX};
        return REGEXs;
    }

    @Override
    public String[] getDatastripXmlREGEXs() {
        String[] REGEXs = {DATASTRIP_XML_REGEX};
        return REGEXs;
    }


    //Templates level 1c
    public static String SPECTRAL_BAND_TEMPLATE_L1C = "IMG_DATA" + File.separator + "{{TILENUMBER}}_{{DATATAKE_START}}_{{BANDFILEID}}.jp2";
    public static String SPECTRAL_BAND_REGEX = "T([A-Z|0-9|_]{5})_([0-9]{8}T[0-9]{6})_B([A|0-9]{2})\\.jp2";

    //Templates L2a
    public static String SPECTRAL_BAND_TEMPLATE_L2A = "IMG_DATA"+ File.separator +"R{{RESOLUTION}}m" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_{{BANDFILEID}}_{{RESOLUTION}}m.jp2";
    public static String AOT_FILE_TEMPLATE_L2A = "IMG_DATA" + File.separator + "R{{RESOLUTION}}m" + File.separator +"L2A_{{TILENUMBER}}_{{DATATAKE_START}}_AOT_{{RESOLUTION}}m.jp2";
    public static String WVP_FILE_TEMPLATE_L2A = "IMG_DATA" + File.separator + "R{{RESOLUTION}}m" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_WVP_{{RESOLUTION}}m.jp2";
    public static String SCL_FILE_TEMPLATE_L2A = "IMG_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_SCL_{{RESOLUTION}}m.jp2";
    public static String SCL_FILE_TEMPLATE_L2A_PSD14 = "IMG_DATA" + File.separator + "R{{RESOLUTION}}m" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_SCL_{{RESOLUTION}}m.jp2";
    public static String CLD_FILE_TEMPLATE_L2A = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_CLD_{{RESOLUTION}}m.jp2";
    public static String SNW_FILE_TEMPLATE_L2A = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_SNW_{{RESOLUTION}}m.jp2";
    public static String DDV_FILE_TEMPLATE_L2A = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_DDV_{{RESOLUTION}}m.jp2";
    public static String CLASSI_FILE_TEMPLATE_L2A = "QI_DATA" + File.separator + "MSK_CLASSI_B00.jp2";

    //Templates L2h
    public static String SPECTRAL_BAND_TEMPLATE_L2H_PSD14 = "IMG_DATA"+ File.separator + "L2H_{{TILENUMBER}}_{{DATATAKE_START}}_{{MISSION_ID}}_{{ABSOLUTEORBIT}}_{{BANDFILEID}}_{{RESOLUTION}}m.TIF";
    public static String SPECTRAL_NATIVE_BAND_TEMPLATE_L2H_PSD14 = "IMG_DATA"+ File.separator + "NATIVE" + File.separator +"{{MISSION_ID}}_USER_MSI_L2H_TL_{{SITECENTRE}}_{{CREATIONDATE}}_{{ABSOLUTEORBIT}}_{{BANDFILEID}}_{{RESOLUTION}}m.TIF";
    public static String CLD_FILE_TEMPLATE_L2H = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_CLD_{{RESOLUTION}}m.TIF";
    public static String SNW_FILE_TEMPLATE_L2H = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_SNW_{{RESOLUTION}}m.TIF";
    public static String DDV_FILE_TEMPLATE_L2H = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_DDV_{{RESOLUTION}}m.TIF";

    //Templates L2f
    public static String SPECTRAL_BAND_TEMPLATE_L2F_PSD14 = "IMG_DATA"+ File.separator + "L2F_{{TILENUMBER}}_{{DATATAKE_START}}_{{MISSION_ID}}_{{ABSOLUTEORBIT}}_{{BANDFILEID}}_{{RESOLUTION}}m.TIF";
    public static String SPECTRAL_NATIVE_BAND_TEMPLATE_L2F_PSD14 = "IMG_DATA"+ File.separator + "NATIVE" + File.separator +"{{MISSION_ID}}_USER_MSI_L2F_TL_{{SITECENTRE}}_{{CREATIONDATE}}_{{ABSOLUTEORBIT}}_{{BANDFILEID}}_{{RESOLUTION}}m.TIF";
    public static String CLD_FILE_TEMPLATE_L2F = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_CLD_{{RESOLUTION}}m.TIF";
    public static String SNW_FILE_TEMPLATE_L2F = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_SNW_{{RESOLUTION}}m.TIF";
    public static String DDV_FILE_TEMPLATE_L2F = "QI_DATA" + File.separator + "L2A_{{TILENUMBER}}_{{DATATAKE_START}}_DDV_{{RESOLUTION}}m.TIF";

    //Templates L3
    public static String SPECTRAL_BAND_TEMPLATE_L3 = "IMG_DATA"+ File.separator +"R{{RESOLUTION}}m" + File.separator +"L03_{{TILENUMBER}}_{{DATATAKE_START}}_{{BANDFILEID}}_{{RESOLUTION}}m.jp2";
    public static String SCL_FILE_TEMPLATE_L3 = "IMG_DATA"+ File.separator +"R{{RESOLUTION}}m"  + File.separator + "L03_{{TILENUMBER}}_{{DATATAKE_START}}_SCL_{{RESOLUTION}}m.jp2";
    public static String MSC_FILE_TEMPLATE_L3 = "QI_DATA" + File.separator + "L03_{{TILENUMBER}}_{{DATATAKE_START}}_MSC_{{RESOLUTION}}m.jp2";

    private S2Config.Sentinel2InputType inputType = null;
    private S2Config.Sentinel2ProductLevel level = S2Config.Sentinel2ProductLevel.UNKNOWN;
    Set<String> epsgCodeList = null;
    VirtualPath inputDirPath = null;
    VirtualPath inputXmlPath = null;
    private VirtualPath inputProductXml = null;
    private S2SpatialResolution resolution = S2SpatialResolution.R10M;

    @Override
    public boolean matches(String filename) {
        return S2NamingConventionUtils.matches(filename,this);
    }

    @Override
    public VirtualPath getXmlFromDir(VirtualPath path) {
        return S2NamingConventionUtils.getXmlFromDir(path, PRODUCT_XML_REGEX, GRANULE_XML_REGEX);
    }

    @Override
    public S2Config.Sentinel2InputType getInputType() {
        return inputType;
    }

    @Override
    public S2Config.Sentinel2ProductLevel getProductLevel() {
        return level;
    }

    @Override
    public Set<String> getEPSGList() {
        return epsgCodeList;
    }

    @Override
    public VirtualPath getInputXml() {
        return inputXmlPath;
    }

    @Override
    public VirtualPath getInputProductXml() {
        return inputProductXml;
    }

    @Override
    public S2SpatialResolution getResolution() {
        return resolution;
    }

    @Override
    public String getProductName() {
        if(getInputXml().getParent() != null) {
            return FileUtils.getFilenameWithoutExtension(getInputXml().getParent().getFileName().toString());
        }
        return FileUtils.getFilenameWithoutExtension(getInputXml().getFileName().toString());
    }

    @Override
    public VirtualPath findGranuleFolderFromTileId(String tileId) {
        VirtualPath granuleFolder = null;
        if (getInputType() == S2Config.Sentinel2InputType.INPUT_TYPE_PRODUCT_METADATA) {
            granuleFolder = inputXmlPath.resolveSibling("GRANULE");
        } else {
            if (inputXmlPath.getParent() == null) {
                return null;
            }
            granuleFolder = inputXmlPath.getParent().getParent();
        }
        if (granuleFolder == null) {
            return null;
        }

        String tileIdentifier = S2ProductNamingUtils.getTileIdFromString(tileId);
        if (tileIdentifier == null) {
            return null;
        }
        List<VirtualPath> granulePaths = S2NamingConventionUtils.getAllFilesFromDir(granuleFolder, getGranuleREGEXs());
        VirtualPath path = null;
        for (VirtualPath granulePath : granulePaths) {
            String tileIdAux = S2ProductNamingUtils.getTileIdFromString(granulePath.getFileName().toString());
            if (tileIdentifier.equals(tileIdAux)) {
                path = granulePath;
                break;
            }
        }
        if (path.existsAndHasChildren() && S2NamingConventionUtils.matches(path.getFileName().toString(), getGranuleREGEXs())) {
            return path;
        }
        return null;
    }

    @Override
    public VirtualPath findXmlFromTileId(String tileID) {
        VirtualPath granuleFolderPath = findGranuleFolderFromTileId(tileID);
        if(granuleFolderPath == null) {
            return null;
        }
        VirtualPath path = S2NamingConventionUtils.getFileFromDir(granuleFolderPath,getGranuleXmlREGEXs());
        if(path != null && path.exists()) {
            return path;
        }
        return null;
    }

    @Override
    public String findGranuleId(Collection<String> availableGranuleIds, String granuleFolder) {
        return S2ProductNamingUtils.searchGranuleId(availableGranuleIds,granuleFolder);
    }

    @Override
    public boolean hasValidStructure() throws IOException {
        return S2ProductNamingUtils.hasValidStructure(inputType, getInputXml());
    }

    @Override
    public boolean matchesProductMetadata(String filename) {
        return S2NamingConventionUtils.matches(filename, PRODUCT_XML_REGEX);
    }

    @Override
    public ArrayList<VirtualPath> getDatastripXmlPaths() {
        return S2NamingConventionUtils.getDatastripXmlPaths(inputType, getInputXml(), getDatastripREGEXs(), getDatastripXmlREGEXs());
    }

    @Override
    public ArrayList<VirtualPath> getGranulesXmlPaths() {
        return S2NamingConventionUtils.getGranulesXmlPaths(inputType, getInputXml(), getGranuleREGEXs(), getGranuleXmlREGEXs());
    }

    public SAFECOMPACTNamingConvention(VirtualPath input) {
        String inputName = input.getFileName().toString();
        if (input.existsAndHasChildren()) {
            inputDirPath = input;
            Pattern pattern = Pattern.compile(PRODUCT_REGEX);
            if (pattern.matcher(inputName).matches()) {
                inputXmlPath = getXmlProductFromDir(input);
                inputProductXml = inputXmlPath;
                inputType = S2Config.Sentinel2InputType.INPUT_TYPE_PRODUCT_METADATA;
            }
            if (inputXmlPath == null) {
                pattern = Pattern.compile(GRANULE_REGEX);
                if (pattern.matcher(inputName).matches()) {
                    inputXmlPath = getXmlGranuleFromDir(input);
                    inputProductXml = S2NamingConventionUtils.getProductXmlFromGranuleXml(inputXmlPath, getProductXmlREGEXs());
                    inputType = S2Config.Sentinel2InputType.INPUT_TYPE_GRANULE_METADATA;
                }
            }
            if (inputXmlPath == null) {
                inputType = null;
                return;
            }
        } else {
            Pattern pattern = Pattern.compile(PRODUCT_XML_REGEX);
            if (pattern.matcher(inputName).matches()) {
                inputXmlPath = input;
                inputProductXml = inputXmlPath;
                inputType = S2Config.Sentinel2InputType.INPUT_TYPE_PRODUCT_METADATA;
            }
            if (inputXmlPath == null) {
                pattern = Pattern.compile(GRANULE_XML_REGEX);
                if (pattern.matcher(inputName).matches()) {
                    inputXmlPath = input;
                    inputProductXml = S2NamingConventionUtils.getProductXmlFromGranuleXml(inputXmlPath, getProductXmlREGEXs());
                    inputType = S2Config.Sentinel2InputType.INPUT_TYPE_GRANULE_METADATA;
                }
            }
            if (inputXmlPath == null) {
                inputType = null;
                return;
            }
        }

        //TODO implement an specific methd for each namingConvention
        level = S2ProductNamingUtils.getLevel(inputXmlPath, inputType);

        if (level == S2Config.Sentinel2ProductLevel.L1C || level == S2Config.Sentinel2ProductLevel.L2A
             || level == S2Config.Sentinel2ProductLevel.L2H || level == S2Config.Sentinel2ProductLevel.L2F || level == S2Config.Sentinel2ProductLevel.L3) {
            epsgCodeList = S2ProductNamingUtils.getEpsgCodeList(inputXmlPath, inputType);
        }

        if (level == S2Config.Sentinel2ProductLevel.L2A || level == S2Config.Sentinel2ProductLevel.L3) {

            if (inputType.equals(S2Config.Sentinel2InputType.INPUT_TYPE_GRANULE_METADATA)) {
                if (L2aUtils.checkGranuleSpecificFolder(getInputXml()/*.toFile()*/, "10m")) {
                    resolution = S2SpatialResolution.R10M;
                } else if (L2aUtils.checkGranuleSpecificFolder(getInputXml()/*.toFile()*/, "20m")) {
                    resolution = S2SpatialResolution.R20M;
                } else {
                    resolution = S2SpatialResolution.R60M;
                }
            }
            if (inputType.equals(S2Config.Sentinel2InputType.INPUT_TYPE_PRODUCT_METADATA)) {
                if (L2aUtils.checkMetadataSpecificFolder(getInputXml()/*.toFile()*/, "10m")) {
                    resolution = S2SpatialResolution.R10M;
                } else if (L2aUtils.checkMetadataSpecificFolder(getInputXml()/*.toFile()*/, "20m")) {
                    resolution = S2SpatialResolution.R20M;
                } else {
                    resolution = S2SpatialResolution.R60M;
                }
            }
        }
    }

    private VirtualPath getXmlProductFromDir(VirtualPath path) {
        return S2NamingConventionUtils.getFileFromDir(path, getProductXmlREGEXs());
    }

    private VirtualPath getXmlGranuleFromDir(VirtualPath path) {
        return S2NamingConventionUtils.getFileFromDir(path, getGranuleXmlREGEXs());
    }
}
