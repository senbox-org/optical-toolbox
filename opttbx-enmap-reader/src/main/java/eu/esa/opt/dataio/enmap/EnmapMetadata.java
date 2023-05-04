package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.IntStream;

public abstract class EnmapMetadata {
    static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX";
    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    private final XPath xpath;
    private final Document doc;
    String NOT_AVAILABLE = "NA";

    protected EnmapMetadata(Document doc, XPath xpath) {
        this.xpath = xpath;
        this.doc = doc;
    }

    static EnmapMetadata create(InputStream inputStream) throws IOException {
        Document xmlDocument = EnmapMetadata.createXmlDocument(inputStream);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String processingLevel = getProcessingLevel(xPath, xmlDocument);
        switch (PROCESSING_LEVEL.valueOf(processingLevel)) {
            case L1B:
                return new EnmapL1BMetadata(xmlDocument, xPath);
            case L1C:
                return new EnmapL1CMetadata(xmlDocument, xPath);
            case L2A:
                return new EnmapL2AMetadata(xmlDocument, xPath);
            default:
                throw new IOException(String.format("Unknown product level '%s'", processingLevel));
        }
    }

    private static Document createXmlDocument(InputStream inputStream) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(inputStream);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Cannot create document from manifest XML file.", e);
        }
    }

    // todo - this method is borrowed from ISO8601Converter class in SNAP 10 (snap-core)
    // todo - when using SNAP 10 this method should be replaced
    private static ProductData.UTC parseTimeString(String iso8601String) {
        final TemporalAccessor accessor = EnmapMetadata.FORMATTER.parse(iso8601String);
        final ZonedDateTime time = ZonedDateTime.from(accessor);
        final Date date = Date.from(time.toInstant());
        return ProductData.UTC.create(date, time.get(ChronoField.MICRO_OF_SECOND));
    }

    private static String getProcessingLevel(XPath xPath, Document xmlDocument) throws IOException {
        return getNodeContent("/level_X/base/level", xPath, xmlDocument);
    }

    private static String getNodeContent(String path, XPath xpath, Document doc) throws IOException {
        Node node = getNode(path, xpath, doc);
        if (node != null) {
            return node.getTextContent();
        } else {
            throw new IOException(String.format("Not able to read metadata from xml path '%s'", path));
        }
    }

    private static Node getNode(String path, XPath xpath, Document doc) throws IOException {
        try {
            XPathExpression expr = xpath.compile(path);
            return (Node) expr.evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new IOException(String.format("Not able to read metadata from xml path '%s'", path), e);
        }
    }

    private static void addTo(Node node, MetadataElement elem) {
        MetadataElement subElement = new MetadataElement(node.getNodeName());
        elem.addElement(subElement);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node subNode = childNodes.item(i);
            if (Node.ELEMENT_NODE == subNode.getNodeType()) {
                boolean hasChildren = hasChildElements(subNode);
                if (hasChildren) {
                    addTo(subNode, subElement);
                } else {
                    subElement.addAttribute(new MetadataAttribute(subNode.getNodeName(), ProductData.createInstance(subNode.getTextContent()), true));
                }
            }
        }

    }

    private static boolean hasChildElements(Node node) {
        NodeList childNodes = node.getChildNodes();
        return IntStream.range(0, childNodes.getLength()).anyMatch(i -> Node.ELEMENT_NODE == childNodes.item(i).getNodeType());
    }

    private static int[] joinArrays(int[] vnirIndices, int[] swirIndices) {
        return IntStream.concat(Arrays.stream(vnirIndices), Arrays.stream(swirIndices)).toArray();
    }

    /**
     * Converts and inserts the XML metadata into the product metadata element
     *
     * @param elem the element to contain the XML metadata
     */
    public void insertInto(MetadataElement elem) throws IOException {
        // start with the child Nodes and skip the start element 'level_X'
        NodeList childNodes = getNodeSet("/level_X/*");
        for (int i = 0; i < childNodes.getLength(); i++) {
            addTo(childNodes.item(i), elem);
        }
    }

    /**
     * The version of the XML schema.
     *
     * @return the version
     * @throws IOException in case the metadata XML file could not be read
     */
    public String getSchemaVersion() throws IOException {
        return getNodeContent("/level_X/metadata/schema/versionSchema");
    }

    /**
     * The version of the processing chain.
     *
     * @return the version
     * @throws IOException in case the metadata XML file could not be read
     */
    public String getProcessingVersion() throws IOException {
        return getNodeContent("/level_X/base/revision");
    }

    /**
     * The version of the processor used to generate the archived L0 product.
     *
     * @return the version
     * @throws IOException in case the metadata XML file could not be read
     */
    public String getL0ProcessingVersion() throws IOException {
        return getNodeContent("/level_X/base/archivedVersion");
    }

    /**
     * The processing level of the product, one of {@code [L1B, L1C, L2A]}
     *
     * @return the processing level
     * @throws IOException in case the metadata XML file could not be read
     */
    public PROCESSING_LEVEL getProcessingLevel() throws IOException {
        return PROCESSING_LEVEL.valueOf(getProcessingLevel(xpath, doc));
    }

    /**
     * The name of the product
     *
     * @return the name
     * @throws IOException in case the metadata XML file could not be read
     */
    public String getProductName() throws IOException {
        String fileName = getNodeContent("/level_X/metadata/name");
        return fileName.substring(0, 74);
    }

    /**
     * The type string of the product
     *
     * @return the product type
     * @throws IOException in case the metadata XML file could not be read
     */
    public String getProductType() throws IOException {
        return getNodeContent("/level_X/base/format");
    }

    /**
     * The data format of the product contents.
     * Is one of the following values {@code [BSQ+Metadata, BIL+Metadata, BIP+Metadata, JPEG2000+Metadata, GeoTIFF+Metadata]}
     *
     * @return the data format
     * @throws IOException in case the metadata XML file could not be read
     */
    public String getProductFormat() throws IOException {
        return getNodeContent("/level_X/processing/productFormat");
    }

    /**
     * The sensing start time of the scene at UTC time zone
     *
     * @return the sensing start time
     * @throws IOException in case the metadata XML file could not be read
     */
    public ProductData.UTC getStartTime() throws IOException {
        return EnmapMetadata.parseTimeString(getNodeContent("/level_X/base/temporalCoverage/startTime"));
    }

    /**
     * The sensing stop time of the scene at UTC time zone
     *
     * @return the sensing stop time
     * @throws IOException in case the metadata XML file could not be read
     */
    public ProductData.UTC getStopTime() throws IOException {
        return EnmapMetadata.parseTimeString(getNodeContent("/level_X/base/temporalCoverage/stopTime"));
    }

    /**
     * The width and height of the scene as dimension object
     *
     * @return the dimension of the scene
     * @throws IOException in case the metadata XML file could not be read
     */
    public abstract Dimension getSceneDimension() throws IOException;

    /**
     * returns the size of a pixel
     *
     * @return the pixel size
     * @throws IOException in case the metadata XML file could not be read
     */
    public abstract double getPixelSize() throws IOException;

    /**
     * Returns the spatial coverage of the scene raster in WGS84 coordinates for the satellite raster (Level L1B)
     *
     * @return the spatial coverage
     * @throws IOException in case the metadata XML file could not be read
     */
    public Geometry getSpatialCoverage() throws IOException {
        double[] lats = getDoubleValues("/level_X/base/spatialCoverage/boundingPolygon/*/latitude", 5);
        double[] lons = getDoubleValues("/level_X/base/spatialCoverage/boundingPolygon/*/longitude", 5);
        return createPolygon(lats, lons);
    }

    /**
     * returns the latitude values of the 4 corners in the order upper-left, upper-right, lower-left and lower-right.
     *
     * @return the 4 latitude values
     * @throws IOException in case the metadata XML file could not be read
     */
    public double[] getCornerLatitudes() throws IOException {
        double[] lats = new double[4];
        lats[0] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='upper_left']/../latitude"));
        lats[1] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='upper_right']/../latitude"));
        lats[2] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='lower_left']/../latitude"));
        lats[3] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='lower_right']/../latitude"));
        return lats;
    }

    /**
     * returns the longitude values of the 4 corners in the order upper-left, upper-right, lower-left and lower-right.
     *
     * @return the 4 longitude values
     * @throws IOException in case the metadata XML file could not be read
     */
    public double[] getCornerLongitudes() throws IOException {
        double[] lons = new double[4];
        lons[0] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='upper_left']/../longitude"));
        lons[1] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='upper_right']/../longitude"));
        lons[2] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='lower_left']/../longitude"));
        lons[3] = Double.parseDouble(getNodeContent("/level_X/base/spatialCoverage/boundingPolygon/point/frame[text()='lower_right']/../longitude"));
        return lons;
    }

    /**
     * Returns the spatial coverage of the reprojected and orthorectified scene (Level L1C and L2A) in WGS84 coordinates,
     * regardless of the coordinate reference system.
     *
     * @return the spatial coverage
     * @throws IOException in case the metadata XML file could not be read
     */
    public Geometry getSpatialOrthoCoverage() throws IOException {
        double[] lats = getDoubleValues("/level_X/specific/spatialCoverageOfOrthoScene/boundingPolygon/*/latitude", 5);
        double[] lons = getDoubleValues("/level_X/specific/spatialCoverageOfOrthoScene/boundingPolygon/*/longitude", 5);
        return createPolygon(lats, lons);
    }

    /**
     * Returns the geo-referencing information of the reprojected and orthorectified scene (Level L1C and L2A)
     *
     * @throws IOException in case the metadata XML file could not be read
     */
    public GeoReferencing getGeoReferencing() throws IOException {
        String projection = getNodeContent("/level_X/product/ortho/projection");
        String resString = getNodeContent("/level_X/product/ortho/resolution");
        double resolution = Double.NaN;
        if (!NOT_AVAILABLE.equalsIgnoreCase(resString)) {
            resolution = Double.parseDouble(resString);
        }
        return new GeoReferencing(projection, resolution);
    }

    /**
     * returns the sun elevation angles at the four corner points of the scene in the order
     * upper-left, upper-right, lower-left and lower-right
     *
     * @return an array containing the sun elevation angles at the four corner points
     * @throws IOException in case the metadata XML file could not be read
     */
    public double[] getSunElevationAngles() throws IOException {
        double[] angles = new double[4];
        angles[0] = Double.parseDouble(getNodeContent("/level_X/specific/sunElevationAngle/upper_left"));
        angles[1] = Double.parseDouble(getNodeContent("/level_X/specific/sunElevationAngle/upper_right"));
        angles[2] = Double.parseDouble(getNodeContent("/level_X/specific/sunElevationAngle/lower_left"));
        angles[3] = Double.parseDouble(getNodeContent("/level_X/specific/sunElevationAngle/lower_right"));
        return angles;
    }

    /**
     * The sun elevation angle at the center of the scene
     *
     * @return the sun elevation angle
     * @throws IOException in case the metadata XML file could not be read
     */
    public double getSunElevationAngleCenter() throws IOException {
        return getAngleCenter("sunElevationAngle");
    }

    /**
     * the sun azimuth angles at the four corner points of the scene in the order upper-left,
     * upper-right, lower-left and lower-right
     *
     * @return an array containing the sun azimuth angles at the four corner points
     * @throws IOException in case the metadata XML file could not be read
     */
    public double[] getSunAzimuthAngles() throws IOException {
        double[] angles = new double[4];
        angles[0] = Double.parseDouble(getNodeContent("/level_X/specific/sunAzimuthAngle/upper_left"));
        angles[1] = Double.parseDouble(getNodeContent("/level_X/specific/sunAzimuthAngle/upper_right"));
        angles[2] = Double.parseDouble(getNodeContent("/level_X/specific/sunAzimuthAngle/lower_left"));
        angles[3] = Double.parseDouble(getNodeContent("/level_X/specific/sunAzimuthAngle/lower_right"));
        return angles;
    }

    /**
     * The sun azimuth angle at the center of the scene
     *
     * @return the sun azimuth angle
     * @throws IOException in case the metadata XML file could not be read
     */
    public double getSunAzimuthAngleCenter() throws IOException {
        return getAngleCenter("sunAzimuthAngle");
    }

    /**
     * The across off-nadir angles at the four corner points of the scene in the order upper-left,
     * upper-right, lower-left and lower-right
     *
     * @return an array containing the across off-nadir angles at the four corner points
     * @throws IOException in case the metadata XML file could not be read
     */
    public double[] getAcrossOffNadirAngles() throws IOException {
        double[] angles = new double[4];
        angles[0] = Double.parseDouble(getNodeContent("/level_X/specific/acrossOffNadirAngle/upper_left"));
        angles[1] = Double.parseDouble(getNodeContent("/level_X/specific/acrossOffNadirAngle/upper_right"));
        angles[2] = Double.parseDouble(getNodeContent("/level_X/specific/acrossOffNadirAngle/lower_left"));
        angles[3] = Double.parseDouble(getNodeContent("/level_X/specific/acrossOffNadirAngle/lower_right"));
        return angles;
    }

    /**
     * The across off-nadir angle at the center of the scene
     *
     * @return the across off-nadir angle
     * @throws IOException in case the metadata XML file could not be read
     */
    public double getAcrossOffNadirAngleCenter() throws IOException {
        return getAngleCenter("acrossOffNadirAngle");
    }

    /**
     * The along off-nadir angles at the four corner points of the scene in the order upper-left,
     * upper-right, lower-left and lower-right
     *
     * @return an array containing the along off-nadir angles at the four corner points
     * @throws IOException in case the metadata XML file could not be read
     */
    public double[] getAlongOffNadirAngles() throws IOException {
        double[] angles = new double[4];
        angles[0] = Double.parseDouble(getNodeContent("/level_X/specific/alongOffNadirAngle/upper_left"));
        angles[1] = Double.parseDouble(getNodeContent("/level_X/specific/alongOffNadirAngle/upper_right"));
        angles[2] = Double.parseDouble(getNodeContent("/level_X/specific/alongOffNadirAngle/lower_left"));
        angles[3] = Double.parseDouble(getNodeContent("/level_X/specific/alongOffNadirAngle/lower_right"));
        return angles;
    }

    /**
     * The along off-nadir angle at the center of the scene
     *
     * @return the along off-nadir angle
     * @throws IOException in case the metadata XML file could not be read
     */
    public double getAlongOffNadirAngleCenter() throws IOException {
        return getAngleCenter("alongOffNadirAngle");
    }

    /**
     * The scene azimuth angles at the four corner points of the scene in the order
     * upper-left, upper-right, lower-left and lower-right
     *
     * @return an array containing the scene azimuth angles at the four corner points
     * @throws IOException in case the metadata XML file could not be read
     */
    public double[] getSceneAzimuthAngles() throws IOException {
        double[] angles = new double[4];
        angles[0] = Double.parseDouble(getNodeContent("/level_X/specific/sceneAzimuthAngle/upper_left"));
        angles[1] = Double.parseDouble(getNodeContent("/level_X/specific/sceneAzimuthAngle/upper_right"));
        angles[2] = Double.parseDouble(getNodeContent("/level_X/specific/sceneAzimuthAngle/lower_left"));
        angles[3] = Double.parseDouble(getNodeContent("/level_X/specific/sceneAzimuthAngle/lower_right"));
        return angles;
    }

    /**
     * The scene azimuth angle at the center of the scene
     *
     * @return the scene azimuth angle
     * @throws IOException in case the metadata XML file could not be read
     */
    public double getSceneAzimuthAngleCenter() throws IOException {
        return getAngleCenter("sceneAzimuthAngle");
    }

    /**
     * Returns a map of files of the product.
     *
     * @throws IOException in case the metadata XML file could not be read
     */
    public abstract Map<String, String> getFileNameMap() throws IOException;

    /**
     * returns the number of spectral bands
     *
     * @return the number of spectral bands
     * @throws IOException in case the metadata XML file could not be read
     */
    public abstract int getNumSpectralBands() throws IOException;

    /**
     * Gets the name of the spectral measurements. This 'radiance' in case of
     * L1B and L1C data and 'surface reflectance' for L2 data
     *
     * @return the name for the spectral measurements
     */
    public abstract String getSpectralMeasurementName();

    /**
     * returns the central wavelength of the channel at the specified spectral index
     *
     * @param index the spectral index
     * @return the central wavelength
     * @throws IOException in case the metadata XML file could not be read
     */
    public float getCentralWavelength(int index) throws IOException {
        return getNodeContentAsFloat(String.format("/level_X/specific/bandCharacterisation/bandID[@number='%d']/wavelengthCenterOfBand", index + 1));
    }

    /**
     * returns the bandwidth of the channel at the specified spectral index
     *
     * @param index the spectral index
     * @return the bandwidth
     * @throws IOException in case the metadata XML file could not be read
     */
    public float getBandwidth(int index) throws IOException {
        return getNodeContentAsFloat(String.format("/level_X/specific/bandCharacterisation/bandID[@number='%d']/FWHMOfBand", index + 1));
    }

    /**
     * returns the scaling factor to convert from raw numbers to geophysical values of the channel at the specified spectral index
     *
     * @param index the spectral index
     * @return the scaling factor
     * @throws IOException in case the metadata XML file could not be read
     */
    public float getBandScaling(int index) throws IOException {
        return getNodeContentAsFloat(String.format("/level_X/specific/bandCharacterisation/bandID[@number='%d']/GainOfBand", index + 1));
    }

    /**
     * returns the scaling offset to convert from raw numbers to geophysical values of the channel at the specified spectral index
     *
     * @param index the spectral index
     * @return the scaling offset
     * @throws IOException in case the metadata XML file could not be read
     */
    public float getBandOffset(int index) throws IOException {
        return getNodeContentAsFloat(String.format("/level_X/specific/bandCharacterisation/bandID[@number='%d']/OffsetOfBand", index + 1));
    }

    /**
     * returns the background (no-data) value for the spectral bands indicating that the pixel contains no measurement
     *
     * @return the background value
     * @throws IOException in case the metadata XML file could not be read
     */
    public float getSpectralBackgroundValue() throws IOException {
        return getNodeContentAsFloat("/level_X/specific/backgroundValue");
    }

    /**
     * returns the background (no-data) value for the pixel mask bands indicating that the pixel contains no measurement
     *
     * @return the background value
     */
    public double getPixelmaskBackgroundValue() {
        return 255;
    }

    /**
     * returns the number of SWIR bands
     *
     * @return the number of SWIR bands
     * @throws IOException in case the metadata XML file could not be read
     */
    public int getNumVnirBands() throws IOException {
        return Integer.parseInt(getNodeContent("/level_X/specific/vnirProductQuality/numChannelsExpected"));
    }

    /**
     * returns the indices (one based) of VNIR bands
     *
     * @return the indices of the VNIR bands
     * @throws IOException in case the metadata XML file could not be read
     */
    public int[] getVnirIndices() throws IOException {
        String csv = getNodeContent("/level_X/specific/vnirProductQuality/expectedChannelsList");
        String[] strIndices = StringUtils.csvToArray(csv);
        return Arrays.stream(strIndices).mapToInt(Integer::parseInt).toArray();
    }

    /**
     * returns the number of SWIR bands
     *
     * @return the number of SWIR bands
     * @throws IOException in case the metadata XML file could not be read
     */
    public int getNumSwirBands() throws IOException {
        return getNodeContentAsInt("/level_X/specific/swirProductQuality/numChannelsExpected");
    }

    /**
     * returns the indices (one based) of SWIR bands
     *
     * @return the indices of the SWIR bands
     * @throws IOException in case the metadata XML file could not be read
     */
    public int[] getSwirIndices() throws IOException {
        String csv = getNodeContent("/level_X/specific/swirProductQuality/expectedChannelsList");
        String[] strIndices = StringUtils.csvToArray(csv);
        return Arrays.stream(strIndices).mapToInt(Integer::parseInt).toArray();
    }

    /**
     * returns the indices for all spectral bands
     *
     * @return the indices for all spectral bands
     * @throws IOException in case the metadata XML file could not be read
     */
    public int[] getSpectralIndices() throws IOException {
        return joinArrays(getVnirIndices(), getSwirIndices());
    }

    /**
     * returns a description for the spectral channel at the specified index
     *
     * @param index the index (zero-based) of the spectral channel to retrieve the description for
     * @return description for the specified spectral channel
     */
    public String getSpectralBandDescription(int index) throws IOException {
        String spectralArea = index < getNumVnirBands() ? "VNIR" : "SWIR";
        String measurementName = getSpectralMeasurementName();
        float wavelength = getCentralWavelength(index);
        return String.format("%s %s @%s", spectralArea, measurementName, wavelength);
    }

    /**
     * Returns the physical unit of the spectral channels
     *
     * @return the physical Unit.
     */
    public abstract String getSpectralUnit();

    /**
     * Returns the raw data type of the spectral channels as a
     * type of {@link ProductData}
     *
     * @return The data type of the raw spectral data.
     */
    public abstract int getSpectralDataType();

    protected String getNodeContent(String path) throws IOException {
        return getNodeContent(path, xpath, doc);

    }

    protected NodeList getNodeSet(String path) throws IOException {
        try {
            XPathExpression expr = xpath.compile(path);
            return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IOException(String.format("Not able to read metadata from xml path '%s'", path), e);
        }
    }

    protected Geometry createPolygon(double[] lats, double[] lons) {
        Coordinate[] coords = new Coordinate[lats.length];
        for (int i = 0; i < lats.length; i++) {
            double lat = lats[i];
            double lon = lons[i];
            coords[i] = new Coordinate(lon, lat);
        }
        return new GeometryFactory().createPolygon(coords);
    }

    double[] getDoubleValues(String path, int count) throws IOException {
        NodeList nodeSet = getNodeSet(path);
        double[] angles = new double[count];
        for (int i = 0; i < angles.length; i++) {
            angles[i] = Double.parseDouble(nodeSet.item(i).getTextContent());
        }
        return angles;
    }

    protected String getFileName(String key, NodeList fileNodeSet) {
        for (int i = 0; i < fileNodeSet.getLength(); i++) {
            String fileName = fileNodeSet.item(i).getTextContent();
            if (FileUtils.getFilenameWithoutExtension(fileName).endsWith(key)) {
                return fileName;
            }
        }
        return null;
    }

    private int getNodeContentAsInt(String path) throws IOException {
        return Integer.parseInt(getNodeContent(path));
    }

    private float getNodeContentAsFloat(String path) throws IOException {
        return Float.parseFloat(getNodeContent(path));
    }

    private double getAngleCenter(String alongOffNadirAngle) throws IOException {
        return Double.parseDouble(getNodeContent("/level_X/specific/" + alongOffNadirAngle + "/center"));
    }

    public enum PROCESSING_LEVEL {L1B, L1C, L2A}
}
