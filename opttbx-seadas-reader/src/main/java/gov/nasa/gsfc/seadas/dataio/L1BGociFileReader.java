package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.dataio.netcdf.GenericNetCdfReader;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.iosp.hdf5.H5iosp;

import java.io.IOException;
import java.util.logging.Level;

public class L1BGociFileReader extends GenericNetCdfReader {

    private static final double DEFAULT_PROJECTION_CENTRAL_LONGITUDE = 130.0;
    private static final double DEFAULT_PROJECTION_CENTRAL_LATITUDE = 36.0;

    public L1BGociFileReader(L1BGociProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        H5iosp.useHdfEos(false);
        try (NetcdfFile ncfile = NetcdfFileOpener.open(getProductFile())) {
            Product product = super.readProductNodesImpl();
            addGeocoding(product, ncfile);
            return product;
        } finally {
            H5iosp.useHdfEos(true);
        }
    }

    public void addGeocoding(Product product, NetcdfFile ncfile) {

        //This gets VERY close, based on one scene's coastline matching.  Not quite as good as the l2gen output,
        // but can't figure out where the discrepancy lies...so going with this as good enough.

        double pixelX = 0.0;
        double pixelY = 0.0;

        final Group sceneHeader = findGroup(ncfile.getRootGroup(), "Scene Header");
        final Group mapProjection = findGroup(ncfile.getRootGroup(), "Map Projection");
        final Double sceneCenterLon = getAttributeValue(sceneHeader, "Scene center longitude");
        final Double sceneCenterLat = getAttributeValue(sceneHeader, "Scene center latitude");
        final Double upperLeftLon = getAttributeValue(sceneHeader, "Scene upper-left longitude");
        final Double upperLeftLat = getAttributeValue(sceneHeader, "Scene upper-left latitude");
        if (sceneCenterLon == null || sceneCenterLat == null || upperLeftLon == null || upperLeftLat == null) {
            SystemUtils.LOG.warning("Cannot add GOCI geocoding: required scene location attributes are missing.");
            return;
        }

        final double projCentralLon = getAttributeValue(mapProjection, "Central Longitude (meridian)",
                                                         DEFAULT_PROJECTION_CENTRAL_LONGITUDE);
        final double projCentralLat = getAttributeValue(mapProjection, "Central Latitude (parallel)",
                                                         DEFAULT_PROJECTION_CENTRAL_LATITUDE);

        // The projection information contained in the GOCI data file suggests slightly odd
        // Earth sphericity values, so not quite WGS84.  Using these, the transform needs the "lenient" keyword
        // set to true
        // the WKT used was derived from gdal/proj4 using the projection info (and ESRI format):
        // gdalsrsinfo -o wkt_esri -p "+proj=ortho +lon_0=130 +lat_0=36 +a=6378169.0 +b=6356584.0"
        //
        //        PROJCS["Orthographic",
        //                GEOGCS["GCS_unnamed ellipse",
        //                DATUM["D_unknown",
        //                SPHEROID["Unknown",6378169,295.4908037989359]],
        //        PRIMEM["Greenwich",0],
        //        UNIT["Degree",0.017453292519943295]],
        //        PROJECTION["Orthographic"],
        //                PARAMETER["Latitude_Of_Center",36],
        //        PARAMETER["Longitude_Of_Center",130],
        //        PARAMETER["false_easting",0],
        //        PARAMETER["false_northing",0],
        //        UNIT["Meter",1]]

        // OK, using WGS84...seems to not make a hill 'o beans difference - but stickin' with the ugly.
        String wkt =
            "PROJCS[\"Orthographic\","
                + "  GEOGCS[\"GCS_unnamed ellipse\","
                + "    DATUM["+"\"D_unknown\","
                + "      SPHEROID[\"Unknown\",6378169,295.4908037989359]],"
                + "    PRIMEM[\"Greenwich\", 0.0],"
                + "    UNIT[\"Degree\", 0.017453292519943295]],"
                + "  PROJECTION[\"Orthographic\"],"
                + "  PARAMETER[\"Latitude_Of_Center\","+projCentralLat +"],"
                + "  PARAMETER[\"Longitude_Of_Center\","+ projCentralLon +"],"
                + "  PARAMETER[\"false_easting\", 0.0],"
                + "  PARAMETER[\"false_northing\", 0.0],"
                + "  UNIT[\"Meter\", 1]]";

        try {
            CoordinateReferenceSystem orthoCRS = CRS.parseWKT(wkt);
            CoordinateReferenceSystem wgs84crs = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
            MathTransform transform = CRS.findMathTransform(wgs84crs, orthoCRS,true);
            double[] sceneCenter = new double[2];
            double [] sceneUpperLeft = new double[2];
//            transform.transform(new double[] {projCentralLon,projCentralLat}, 0, sceneCenter, 0, 1);
            transform.transform(new double[] {sceneCenterLon,sceneCenterLat}, 0, sceneCenter, 0, 1);
            transform.transform(new double[] {upperLeftLon,upperLeftLat}, 0, sceneUpperLeft, 0, 1);
            double northing = Math.round(sceneUpperLeft[1]);
            double easting = Math.round(sceneUpperLeft[0]);

            double pixelSize = Math.round((sceneCenter[0]-sceneUpperLeft[0])/(product.getSceneRasterWidth()/2.0));

            product.setSceneGeoCoding(new CrsGeoCoding(orthoCRS,
                                                       product.getSceneRasterWidth(),
                                                       product.getSceneRasterHeight(),
                                                       easting, northing, pixelSize, pixelSize,
                                                       pixelX, pixelY));
        } catch (FactoryException | TransformException e) {
            SystemUtils.LOG.log(Level.WARNING, "Cannot add GOCI geocoding.", e);
        }
    }

    private static double getAttributeValue(Group group, String attributeName,
                                            double defaultValue) {
        final Double value = getAttributeValue(group, attributeName);
        return value != null ? value : defaultValue;
    }

    private static Double getAttributeValue(Group group, String attributeName) {
        final Attribute attribute = findAttribute(group, attributeName);
        final Number value = attribute != null ? attribute.getNumericValue() : null;
        return value != null ? value.doubleValue() : null;
    }

    static Group findGroup(Group parent, String groupName) {
        if (parent == null) {
            return null;
        }
        if (normalizeName(parent.getShortName()).equals(normalizeName(groupName))) {
            return parent;
        }
        for (Group child : parent.getGroups()) {
            final Group match = findGroup(child, groupName);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    static Attribute findAttribute(Group group, String attributeName) {
        if (group == null) {
            return null;
        }
        final String normalizedName = normalizeName(attributeName);
        for (Attribute attribute : group.attributes()) {
            if (normalizeName(attribute.getShortName()).equals(normalizedName)) {
                return attribute;
            }
        }
        return null;
    }

    private static String normalizeName(String name) {
        final StringBuilder normalizedName = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char character = name.charAt(i);
            if (Character.isLetterOrDigit(character)) {
                normalizedName.append(Character.toLowerCase(character));
            }
        }
        return normalizedName.toString();
    }
}
