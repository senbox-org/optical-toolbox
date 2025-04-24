package eu.esa.opt.olci.l1csyn;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SlstrMisrTransform implements Serializable {
    private static final int SLSTR_OFFSET = 0;
    private final Product slstrImageProduct;
    private final String misrPath;
    private final String bandType;
    private final String viewtype;
    private final int olciNumRows;
    private final int olciNumCols;
    private int minScan = 9999999;
    private int minScanOrphan = 9999999;

    SlstrMisrTransform(Product olciImageProduct, Product slstrImageProduct, File misrManifest, String bandType) {
        this.slstrImageProduct = slstrImageProduct;
        this.misrPath = misrManifest.getParent();
        this.bandType = bandType;
        final Band oa17_radiance = olciImageProduct.getBand("Oa17_radiance");
        this.olciNumRows = oa17_radiance.getRasterHeight();
        this.olciNumCols = oa17_radiance.getRasterWidth();
        if (bandType.contains("S")) {
            this.viewtype = "an";
        } else {
            this.viewtype = "ao";
        }
    }

    // package access for testing only tb 2020-07-17
    static int[] getColRow(int scan, int pixel, int detector) {
        //todo : clarify the formula
        return new int[]{pixel, scan * 4 + detector};
    }

    //step 1 for orphan pixel
    private TreeMap<int[], int[]> getSlstrOrphanImageMap() throws IOException {
        TreeMap<int[], int[]> orphanMap = new TreeMap<>(new ComparatorIntArray());

        String path = slstrImageProduct.getFileLocation().getParent();
        String indexFilePath = path + "/indices_" + viewtype + ".nc";
        NetcdfFile netcdfFile = NetcdfFiles.open(indexFilePath);
        Variable scanVariable = netcdfFile.findVariable("scan_orphan_" + viewtype);
        Variable pixelVariable = netcdfFile.findVariable("pixel_orphan_" + viewtype);
        Variable detectorVariable = netcdfFile.findVariable("detector_orphan_" + viewtype);

        ArrayShort.D2 scanArray = (ArrayShort.D2) scanVariable.read();
        ArrayShort.D2 pixelArray = (ArrayShort.D2) pixelVariable.read();
        ArrayByte.D2 detectorArray = (ArrayByte.D2) detectorVariable.read();
        int orphanPixelsLength = netcdfFile.findDimension("orphan_pixels").getLength();
        int rowLength = netcdfFile.findDimension("rows").getLength();

        for (int i = 0; i < orphanPixelsLength; i++) { // todo(mp, Jan-2021) - Use scanArray.getIndex(); avoid casting of arrays above
            for (int j = 0; j < rowLength; j++) { // todo(mp, Jan-2021) - Could stop already after each get when return is -1 --> would save some time
                short scan = scanArray.get(j, i);
                short pixel = pixelArray.get(j, i);
                byte detector = detectorArray.get(j, i);
                if (scan != -1 && pixel != -1 && detector != -1) {
                    int[] imagePosition = {i, j};
                    orphanMap.put(imagePosition, new int[]{scan, pixel, detector});
                    if (scan < minScanOrphan) {
                        this.minScanOrphan = scan;
                    }
                }
            }
        }
        return orphanMap;
    }


    /// step 1 updated juni 2020, reupdated ~1july 2020
    private TreeMap<int[], int[]> getSlstrImageMap(int x, int y) throws IOException, InvalidRangeException {
        // Provides mapping between SLSTR image grid(x,y) and SLSTR instrument grid(scan,pixel,detector)
        //x and y are dimensions of SLSTR L1B raster
        TreeMap<int[], int[]> slstrMap = new TreeMap<>(new ComparatorIntArray());

        String path = slstrImageProduct.getFileLocation().getParent();
        String indexFilePath = path + "/indices_" + viewtype + ".nc";
        NetcdfFile netcdfFile = NetcdfFiles.open(indexFilePath);
        Variable scanVariable = netcdfFile.findVariable("scan_" + viewtype);
        Variable pixelVariable = netcdfFile.findVariable("pixel_" + viewtype);
        Variable detectorVariable = netcdfFile.findVariable("detector_" + viewtype);

        ArrayShort.D2 scanArray = (ArrayShort.D2) scanVariable.read();
        ArrayShort.D2 pixelArray = (ArrayShort.D2) pixelVariable.read();
        ArrayByte.D2 detectorArray = (ArrayByte.D2) detectorVariable.read();

        for (int i = 0; i < x; i++) { // todo(mp, Jan-2021) - Could stop already after each get when return is -1 --> would save some time
            for (int j = 0; j < y; j++) {
                short scan = scanArray.get(j, i);
                short pixel = pixelArray.get(j, i);
                byte detector = detectorArray.get(j, i);
                if (scan != -1 && pixel != -1 && detector != -1) {
                    int[] imagePosition = {i - SLSTR_OFFSET, j};
                    int[] gridPosition = {scan, pixel, detector};
                    slstrMap.put(imagePosition, gridPosition);
                    if (scan < minScan) {
                        this.minScan = scan;
                    }
                }
            }
        }
        return slstrMap;
    }


    //step2
    private Map<int[], int[]> getSlstrGridMisrMap(Map<int[], int[]> mapSlstr, boolean minimize) {
        int SminOk = 0;
        //provides map between SLSTR instrument grid (scan,pixel,detector) and MISR file (row,col)
        TreeMap<int[], int[]> gridMap = new TreeMap<>(new ComparatorIntArray());
        //test block to rescale col-row
        if (minimize) {
            SminOk = 4 * this.minScan;
        }

        for (int[] scanPixelDetector : mapSlstr.values()) {
            int scan = scanPixelDetector[0];
            int pixel = scanPixelDetector[1];
            int detector = scanPixelDetector[2];
            int[] colRow = getColRow(scan, pixel, detector);
            if (minimize) {
                colRow[0] = colRow[0];
                colRow[1] = colRow[1] - SminOk;

            }
            gridMap.put(scanPixelDetector, colRow);
        }
        return gridMap;
    }

    //step2
    private Map<int[], int[]> getSlstrGridOrphanMisrMap(Map<int[], int[]> mapSlstr, boolean minimize) {
        int SminOrphan = 0;
        //provides map between SLSTR instrument grid (scan,pixel,detector) and MISR file (row,col)
        TreeMap<int[], int[]> gridMap = new TreeMap<>(new ComparatorIntArray());
        //test block to rescale col-row
        if (minimize) {
            SminOrphan = 4 * this.minScanOrphan;
        }

        for (int[] scanPixelDetector : mapSlstr.values()) {
            int scan = scanPixelDetector[0];
            int pixel = scanPixelDetector[1];
            int detector = scanPixelDetector[2];
            int[] colRow = getColRow(scan, pixel, detector);
            if (minimize) {
                colRow[0] = colRow[0];
                colRow[1] = colRow[1] - SminOrphan;

            }
            gridMap.put(scanPixelDetector, colRow);
        }
        return gridMap;
    }


    // Step 3
    private Map<int[], int[]> getMisrOlciMap() throws IOException, InvalidRangeException {
        // provides mapping between SLSTR (row/col) and OLCI instrument grid (N_LINE_OLC/N_DET_CAM/N_CAM) from MISR product
        String bandName = "/misregist_Oref_" + bandType + ".nc";

        String misrBandFile = this.misrPath + bandName;
        NetcdfFile netcdfFile = NetcdfFiles.open(misrBandFile);
        int nLineOlcLength = netcdfFile.findDimension("N_LINE_OLC").getLength();
        int nDetCamLength = netcdfFile.findDimension("N_DET_CAM").getLength();
        int nCamLength = netcdfFile.findDimension("N_CAM").getLength();
        String rowVariableName = getRowVariableName(netcdfFile, "row_corresp_\\S+");
        String colVariableName = null;
        Variable colVariable;
        int colOffset = 0;
        double colScale = 1.0;
        if (bandType.matches("S.") || bandType.matches(".o")) {
            colVariableName = getColVariableName(netcdfFile, "col_corresp_\\S+");
            colVariable = netcdfFile.findVariable(colVariableName);
            colOffset = colVariable.findAttribute("add_offset").getNumericValue().intValue();
            colScale = colVariable.findAttribute("scale_factor").getNumericValue().doubleValue();
        } else {
            // Not used as of 09.02.2021
            colVariableName = getColVariableName(netcdfFile, "L1b_orphan_\\S+");
            colVariable = netcdfFile.findVariable(colVariableName);
        }

        Variable rowVariable = netcdfFile.findVariable(rowVariableName);
        int rowOffset = rowVariable.findAttribute("add_offset").getNumericValue().intValue();
        double rowScale = rowVariable.findAttribute("scale_factor").getNumericValue().doubleValue();
        TreeMap<int[], int[]> colRowMap = new TreeMap<>(new ComparatorIntArray());
        if (nLineOlcLength < 10000) { // TODO(mp,FEB-2021) - actually no need to differentiate bot cases. We could set the step to 5000
            Array rowArray = rowVariable.read();
            Array colArray = colVariable.read();

            final Index index = rowArray.getIndex();
            for (int i = 0; i < nCamLength; i++) {
                for (int j = 0; j < nLineOlcLength; j++) {
                    for (int k = 0; k < nDetCamLength; k++) {
                        index.set(i, j, k);
                        // Type of variable of (row,col) might change with change of MISR format. Be careful here.
                        int row = (int) Math.floor(rowArray.getInt(index) * rowScale + rowOffset);
                        int col = (int) Math.floor(colArray.getInt(index) * colScale + colOffset);
                        if (col >= 0 && row >= 0) {
                            int[] colRowArray = {col, row};
                            int[] position = {i, j, k};
                            colRowMap.put(colRowArray, position);
                        }
                    }
                }
            }
        } else {
            for (int longDimSplitter = 10000; longDimSplitter < nLineOlcLength + 10000; longDimSplitter += 10000) {
                int step = 10000;
                if (longDimSplitter > nLineOlcLength) {
                    step = nLineOlcLength + step - longDimSplitter;
                    longDimSplitter = nLineOlcLength;
                }

                final int[] origin = {0, longDimSplitter - step, 0};
                final int[] shape = {nCamLength, step, nDetCamLength};
                Array rowArray = rowVariable.read(origin, shape);
                Array colArray = colVariable.read(origin, shape);

                final Index index = rowArray.getIndex();
                for (int i = 0; i < nCamLength; i++) {
                    for (int j = 0; j < step; j++) {
                        for (int k = 0; k < nDetCamLength; k++) {
                            index.set(i, j, k);
                            // Type of variable of (row,col) might change with change of MISR format. Be careful here.
                            int row = (int) (rowArray.getInt(index) * rowScale + rowOffset);
                            int col = (int) (colArray.getInt(index) * colScale + colOffset);
                            if (col >= 0 && row >= 0) {
                                int[] colRowArray = {col, row};
                                int[] position = {i, j, k};
                                colRowMap.put(colRowArray, position);
                            }
                        }
                    }
                }
            }
        }
        netcdfFile.close();
        return colRowMap;
    }

    //step 3 for orphan pixels
    private TreeMap<int[], int[]> getMisrOlciOrphanMap() throws IOException, InvalidRangeException {
        // provides mapping between MISR (row/orphan) and OLCI instrument grid (N_LINE_OLC/N_DET_CAM/N_CAM) from MISR product
        String bandName = "/misregist_Oref_" + bandType + ".nc";
        String misrBandFile = this.misrPath + bandName;
        NetcdfFile netcdfFile = NetcdfFiles.open(misrBandFile);
        int nLineOlcLength = netcdfFile.findDimension("N_LINE_OLC").getLength();
        int nDetCamLength = netcdfFile.findDimension("N_DET_CAM").getLength();
        int nCamLength = netcdfFile.findDimension("N_CAM").getLength();
        String rowVariableName = getRowVariableName(netcdfFile, "row_corresp_\\S+");
        String orphanVariableName = getOrphanVariableName(netcdfFile);
        Variable rowVariable = netcdfFile.findVariable(rowVariableName);
        Variable orphanVariable = netcdfFile.findVariable(orphanVariableName);
        ArrayInt.D3 rowArray = (ArrayInt.D3) rowVariable.read();
        ArrayShort.D3 orphanArray = (ArrayShort.D3) orphanVariable.read();

        TreeMap<int[], int[]> orphanRowMap = new TreeMap<>(new ComparatorIntArray());
        int rowOffset = rowVariable.findAttribute("add_offset").getNumericValue().intValue();
        double rowScale = rowVariable.findAttribute("scale_factor").getNumericValue().doubleValue();
        short orphan;
        int row;
        for (int i = 0; i < nCamLength; i++) {
            for (int j = 0; j < nLineOlcLength; j++) {
                for (int k = 0; k < nDetCamLength; k++) {
                    row = (int) (rowArray.get(i, j, k) * rowScale + rowOffset);
                    orphan = orphanArray.get(i, j, k);
                    if (orphan > 0 && row > 0) {
                        int[] orphanRowArray = {orphan, row};
                        int[] position = {i, j, k};
                        orphanRowMap.put(orphanRowArray, position);
                    }
                }
            }
        }
        netcdfFile.close();
        return orphanRowMap;
    }

    // Step 4.2
    private Map<int[], int[]> getOlciMisrMap() throws IOException, InvalidRangeException {
        //should provide mapping between OLCI image grid and instrument grid
        int OLCIOffset = 0;
        TreeMap<int[], int[]> olciMap = new TreeMap<>(new ComparatorIntArray());
        String bandName = "/misreg_Oref_Oa17.nc";
        String misrBandFile = this.misrPath + bandName;
        NetcdfFile netcdfFile = NetcdfFiles.open(misrBandFile);

        Variable rowVariable = netcdfFile.findVariable("L1b_row_17");
        if (rowVariable == null) {
            rowVariable = netcdfFile.findVariable("delta_row_17");

        }
        Variable colVariable = netcdfFile.findVariable("L1b_col_17");
        if (colVariable == null) {
            colVariable = netcdfFile.findVariable("delta_col_17");
        }

        int nCamLength = netcdfFile.findDimension("N_CAM").getLength();
        int nLineOlcLength = netcdfFile.findDimension("N_LINE_OLC").getLength();
        int nDetCamLength = netcdfFile.findDimension("N_DET_CAM").getLength();

        int rowOffset = 0;
        if (nLineOlcLength < 10000) {
            ArrayShort.D3 rowArray = (ArrayShort.D3) rowVariable.read();
            ArrayShort.D3 colArray = (ArrayShort.D3) colVariable.read();
            for (int i = 0; i < nCamLength; i++) {
                for (int j = 0; j < nLineOlcLength; j++) {
                    for (int k = 0; k < nDetCamLength; k++) {
                        short row = rowArray.get(i, j, k);
                        short col = colArray.get(i, j, k);
                        int rowNorm = row + rowOffset;
                        if (rowNorm >= 0 && col >= 0) {
                            if (rowNorm < olciNumRows && col < olciNumCols) {
                                int[] gridCoors = {i, j, k};
                                int[] imageCoors = {col - OLCIOffset, rowNorm};
                                olciMap.put(gridCoors, imageCoors);
                            }
                        }
                    }
                }
            }
        } else {
            for (int longDimSplitter = 10000; longDimSplitter < nLineOlcLength + 10000; longDimSplitter += 10000) {
                int step = 10000;
                if (longDimSplitter > nLineOlcLength) {
                    step = nLineOlcLength + step - longDimSplitter;
                    longDimSplitter = nLineOlcLength;
                }
                Array rowArray = rowVariable.read(new int[]{0, longDimSplitter - step, 0}, new int[]{nCamLength, step, nDetCamLength});
                Array colArray = colVariable.read(new int[]{0, longDimSplitter - step, 0}, new int[]{nCamLength, step, nDetCamLength});
                final Index index = rowArray.getIndex();
                for (int i = 0; i < nCamLength; i++) {
                    for (int j = 0; j < step; j++) {
                        for (int k = 0; k < nDetCamLength; k++) {
                            index.set(i, j, k);
                            int row = rowArray.getInt(index);
                            int col = colArray.getInt(index);
                            int rowNorm = row + rowOffset;
                            if ((rowNorm) >= 0 && col >= 0) {
                                if ((rowNorm) < olciNumRows && col < olciNumCols) {
                                    int[] gridCoors = {i, j, k};
                                    int[] imageCoors = {col - OLCIOffset, rowNorm};
                                    olciMap.put(gridCoors, imageCoors);
                                }
                            }
                        }
                    }
                }
            }
        }
        netcdfFile.close();
        return olciMap;
    }

    TreeMap<int[], int[]> getOrphanOlciMap() throws InvalidRangeException, IOException {
        //Provides mapping between orphan SLSTR pixels and OLCI image grid
        TreeMap<int[], int[]> gridMapOrphan = new TreeMap<>(new ComparatorIntArray());

        Map<int[], int[]> slstrOrphanMap = getSlstrOrphanImageMap(); // 1
        Map<int[], int[]> slstrOrphanMisrMap = getSlstrGridOrphanMisrMap(slstrOrphanMap, true); // 2
        Map<int[], int[]> misrOrphanOlciMap = getMisrOlciMap(); // 3
        Map<int[], int[]> olciImageOrphanMap = getOlciMisrMap(); // 4

        for (Map.Entry<int[], int[]> entry : slstrOrphanMap.entrySet()) {
            int[] slstrScanPixDet = entry.getValue();
            int[] rowOrphan = slstrOrphanMisrMap.get(slstrScanPixDet);
            int[] mjk = misrOrphanOlciMap.get(rowOrphan);
            if (mjk != null) {
                int[] xy = olciImageOrphanMap.get(mjk);
                if (xy != null) {
                    gridMapOrphan.put(xy, entry.getKey());
                }
            }
        }
        TreeMap<int[], int[]> gridMap = new TreeMap<>(new ComparatorIntArray());
        gridMap.putAll(gridMapOrphan);// CHECK(mp, FEB-2021) - Can't we return gridMapOrphan directly?
        return gridMap;
    }

    TreeMap<int[], int[]> getSlstrOlciMap() throws InvalidRangeException, IOException {
        //Provides mapping between SLSTR image grid and OLCI image grid
        TreeMap<int[], int[]> gridMapPixel = new TreeMap<>(new ComparatorIntArray());
        Map<int[], int[]> slstrImageMap = getSlstrImageMap(slstrImageProduct.getBand("S3_radiance_" + viewtype).getRasterWidth(), slstrImageProduct.getBand("S3_radiance_" + viewtype).getRasterHeight()); //1
        Map<int[], int[]> slstrMisrMap = getSlstrGridMisrMap(slstrImageMap, true); //2
        Map<int[], int[]> misrOlciMap = getMisrOlciMap(); //3
        Map<int[], int[]> olciImageMap = getOlciMisrMap(); // 4
        for (Map.Entry<int[], int[]> entry : slstrImageMap.entrySet()) {
            int[] slstrScanPixDet = entry.getValue();
            int[] rowCol = slstrMisrMap.get(slstrScanPixDet);
            int[] mjk = misrOlciMap.get(rowCol);
            if (mjk != null) {
                int[] xy = olciImageMap.get(mjk);
                if (xy != null) {
                    gridMapPixel.put(xy, entry.getKey());
                }
            }
        }

        TreeMap<int[], int[]> gridMap = new TreeMap<>(new ComparatorIntArray());
        gridMap.putAll(gridMapPixel);
        return gridMap;
    }

    private String getRowVariableName(NetcdfFile netcdfFile, String pattern) {
        List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            final String fullName = variable.getFullName();
            if (fullName.matches(pattern)) {
                return fullName;
            }
        }
        throw new NullPointerException("Row variable not found");
    }

    private String getColVariableName(NetcdfFile netcdfFile, String pattern) {
        List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            final String fullName = variable.getFullName();
            if (fullName.matches(pattern)) {
                return fullName;
            }
        }
        throw new NullPointerException("Col variable not found");
    }

    private String getOrphanVariableName(NetcdfFile netcdfFile) {
        List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            final String fullName = variable.getFullName();
            if (fullName.matches("L1b_orphan_.._" + "a.") || fullName.matches("orphan_corresp_s._" + "a.") || fullName.matches("L1b_orphan_" + "a.")) {
                return fullName;
            }
        }
        throw new NullPointerException("Orphan variable not found");
    }

    public static class ComparatorIntArray implements java.util.Comparator<int[]>, Serializable {
        @Override
        public int compare(int[] left, int[] right) {
            int comparedLength = Integer.compare(left.length, right.length);
            if (comparedLength == 0) {
                for (int i = 0; i < left.length; i++) {
                    int comparedValue = Integer.compare(left[i], right[i]);
                    if (comparedValue != 0) {
                        return comparedValue;
                    }
                }
                return 0;
            } else {
                return comparedLength;
            }
        }
    }

    //********************
    //  Below is the code which is not used
    // ********************

    //so far this value is not used, but may be needed in the future
    private int getSlstrS3Offset(Product slstrImageProduct) throws IOException {
        String path = slstrImageProduct.getFileLocation().getParent();
        String filePath = path + "/S3_radiance_an.nc";
        NetcdfFile netcdfFile = NetcdfFiles.open(filePath);
        Number offsetAttribute = netcdfFile.findGlobalAttribute("start_offset").getNumericValue();
        System.out.println(offsetAttribute);
        System.out.println(filePath);
        int offsetValue = (int) offsetAttribute;
        netcdfFile.close();
        return offsetValue;
    }

    Map<int[], int[]> getSlstrOlciInstrumentMap(int camIndex) throws InvalidRangeException, IOException {
        Map<int[], int[]> gridMapPixel = new TreeMap<>(new ComparatorIntArray());
        Map<int[], int[]> slstrImageMap = getSlstrImageMap(slstrImageProduct.getSceneRasterWidth(), slstrImageProduct.getSceneRasterHeight()); //1
        Map<int[], int[]> slstrMisrMap = MapToWrapedArrayFactory.createWrappedArray(getSlstrGridMisrMap(slstrImageMap, true)); //2
        Map<int[], int[]> misrOlciMap = MapToWrapedArrayFactory.createWrappedArray(getMisrOlciMap()); //3

        for (Map.Entry<int[], int[]> entry : slstrImageMap.entrySet()) {
            int[] slstrScanPixDet = entry.getValue();
            int[] rowCol = slstrMisrMap.get(slstrScanPixDet);
            int[] mjk = misrOlciMap.get(rowCol);
            if (mjk != null) {
                if (mjk[0] == camIndex) {
                    int[] camCoors = new int[]{mjk[2], mjk[1]};
                    gridMapPixel.put(camCoors, entry.getKey());
                }
            }
        }
        return gridMapPixel;
    }

    // This method is used to check intermediate results of the algorithm
    Map<int[], int[]> getSlstrOlciOrphanInstrumentMap(int camIndex) throws InvalidRangeException, IOException {
        Map<int[], int[]> gridMapPixel = new TreeMap<>(new ComparatorIntArray());
        Map<int[], int[]> slstrImageMap = getSlstrOrphanImageMap(); // 1
        Map<int[], int[]> slstrMisrMap = getSlstrGridOrphanMisrMap(slstrImageMap, true); // 2
        Map<int[], int[]> misrOlciMap = getMisrOlciOrphanMap(); // 3
        for (Map.Entry<int[], int[]> entry : slstrImageMap.entrySet()) {
            int[] slstrScanPixDet = entry.getValue();
            int[] rowCol = slstrMisrMap.get(slstrScanPixDet);
            int[] mjk = misrOlciMap.get(rowCol);
            if (mjk != null) {
                if (mjk[0] == camIndex) {
                    int[] camCoors = new int[]{mjk[2], mjk[1]};
                    gridMapPixel.put(camCoors, entry.getKey());
                }
            }
        }
        return gridMapPixel;
    }

    Map<int[], int[]> getSlstrOlciSingleCameraMap() throws InvalidRangeException, IOException {
        Map<int[], int[]> gridMapPixel = new TreeMap<>(new ComparatorIntArray());
        Map<int[], int[]> slstrImageMap = getSlstrImageMap(slstrImageProduct.getSceneRasterWidth(), slstrImageProduct.getSceneRasterHeight()); //1
        Map<int[], int[]> slstrMisrMap = getSlstrGridMisrMap(slstrImageMap, true); //2
        for (Map.Entry<int[], int[]> entry : slstrImageMap.entrySet()) {
            int[] slstrScanPixDet = entry.getValue();
            int[] rowCol = slstrMisrMap.get(slstrScanPixDet);
            gridMapPixel.put(rowCol, entry.getKey());
        }
        return gridMapPixel;
    }

    Map<int[], int[]> getSlstrOlciSingleOrphanCameraMap() throws InvalidRangeException, IOException {
        Map<int[], int[]> gridMapPixel = new TreeMap<>(new ComparatorIntArray());
        Map<int[], int[]> slstrOrphanMap = getSlstrOrphanImageMap(); // 1
        Map<int[], int[]> slstrOrphanMisrMap = getSlstrGridOrphanMisrMap(slstrOrphanMap, true); // 2
        for (Map.Entry<int[], int[]> entry : slstrOrphanMap.entrySet()) {
            int[] slstrScanPixDet = entry.getValue();
            int[] rowCol = slstrOrphanMisrMap.get(slstrScanPixDet);
            gridMapPixel.put(rowCol, entry.getKey());
        }
        return gridMapPixel;
    }

    // This method is used to check intermediate results of the algorithm
    Map<int[], int[]> getSlstrOlciOrphanSingleCameraMap() throws InvalidRangeException, IOException {
        Map<int[], int[]> gridMapPixel = new TreeMap<>(new ComparatorIntArray());
        Map<int[], int[]> slstrImageMap = getSlstrOrphanImageMap(); // 1
        Map<int[], int[]> slstrMisrMap = getSlstrGridOrphanMisrMap(slstrImageMap, true);
        for (Map.Entry<int[], int[]> entry : slstrImageMap.entrySet()) {
            int[] slstrScanPixDet = entry.getValue();
            int[] rowCol = slstrMisrMap.get(slstrScanPixDet);
            gridMapPixel.put(rowCol, entry.getKey());
        }
        return gridMapPixel;
    }
}

