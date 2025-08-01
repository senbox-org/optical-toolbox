/**
 * 
 */
package eu.esa.opt.dataio.landsat.geotiff.specs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Landsat 8 & Landsat 9 product specification
 * @author Lucian Barbulescu 
 */
@SuppressWarnings("PointlessBitwiseExpression")
public class ProductSensorSpecificationL8L9 extends ProductSensorSpecification {
	
    private static final Map<String, String> bandDescriptions = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("sr_b1", "Coastal Aerosol (Operational Land Imager (OLI))");
        put("sr_b2", "Blue (OLI)");
        put("sr_b3", "Green (OLI)");
        put("sr_b4", "Red (OLI)");
        put("sr_b5", "Near-Infrared (NIR) (OLI)");
        put("sr_b6", "Short Wavelength Infrared (SWIR) 1 (OLI)");
        put("sr_b7", "SWIR 2 (OLI)");
        put("st_b10", "Thermal Infrared Sensor (TIRS) 1");
        put("st_trad", "Thermal band converted to radiance");
        put("st_urad", "Upwelled Radiance");
        put("st_drad", "Downwelled Radiance");
        put("st_atran", "Atmospheric Transmittance");
        put("st_emis", "Emissivity of Band 10 estimated from ASTER GED");
        put("st_emsd", "Emissivity standard deviation");
        put("st_cdist", "Pixel distance to cloud");
        put("qa_pixel", "Level-1 QA Band. The output from the CFMask algorithm is used as an input for the Quality " +
                        "Assessment Application, which calculates values for all fields in the QA Band file. " +
                        "The QA Band file contains quality statistics gathered from the cloud mask and statistics " +
                        "information for the scene.");
        put("qa_radsat", "Level-1 Radiometric Saturation QA and Terrain Occlusion");
        put("sr_qa_aerosol", "The SR Aerosol QA file provides low-level details about factors that may have " +
                             "influenced the final product. The default value for bits 6 and 7, “Aerosol Level’, is " +
                             "Climatology (00). A value of Climatology means no aerosol correction was applied.");
        put("st_qa", "The ST QA file indicates uncertainty of the temperatures given in the ST band file");
    }});

    private static final Map<String, Double> scalingFactors = Collections.unmodifiableMap(new HashMap<String, Double>() {{
        put("sr_b1", 2.75e-5);
        put("sr_b2", 2.75e-5);
        put("sr_b3", 2.75e-5);
        put("sr_b4", 2.75e-5);
        put("sr_b5", 2.75e-5);
        put("sr_b6", 2.75e-5);
        put("sr_b7", 2.75e-5);
        put("st_b10", 0.00341802);
        put("st_trad", 0.001);
        put("st_urad", 0.001);
        put("st_drad", 0.001);
        put("st_atran", 0.0001);
        put("st_emis", 0.0001);
        put("st_emsd", 0.0001);
        put("st_cdist", 0.01);
        put("st_qa", 0.01);
    }});

    private static final Map<String, Double> addOffsets = Collections.unmodifiableMap(new HashMap<String, Double>() {{
        put("sr_b1", -0.2);
        put("sr_b2", -0.2);
        put("sr_b3", -0.2);
        put("sr_b4", -0.2);
        put("sr_b5", -0.2);
        put("sr_b6", -0.2);
        put("sr_b7", -0.2);
        put("st_b10", 149.0);
    }});

    private static final Map<String, Double> fillValues = Collections.unmodifiableMap(new HashMap<String, Double>() {{
        put("sr_b1", 0.0);
        put("sr_b2", 0.0);
        put("sr_b3", 0.0);
        put("sr_b4", 0.0);
        put("sr_b5", 0.0);
        put("sr_b6", 0.0);
        put("sr_b7", 0.0);
        put("st_b10", 0.0);
        put("st_trad", -9999.0);
        put("st_urad", -9999.0);
        put("st_drad", -9999.0);
        put("st_atran", -9999.0);
        put("st_emis", -9999.0);
        put("st_emsd", -9999.0);
        put("st_cdist", -9999.0);
        put("qa_pixel", 1.0);
        put("sr_qa_aerosol", 1.0);
        put("st_qa", -9999.0);
    }});

    private static final Map<String, String> units = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("st_b10", "Kelvin");
        put("st_trad", "W/m^2 sr µm)");
        put("st_urad", "W/m^2 sr µm)");
        put("st_drad", "W/m^2 sr µm)");
        put("st_cdist", "km");
        put("st_qa", "Kelvin");
    }});

    private static final Map<String, Float> wavelengths = Collections.unmodifiableMap(new HashMap<String, Float>() {{
        // see doc: https://d9-wret.s3.us-west-2.amazonaws.com/assets/palladium/production/s3fs-public/atoms/files/LSDS-1328_Landsat8-9-OLI-TIRS-C2-L2-DFCB-v6.pdf
        put("sr_b1", 443.0f);     // from doc: 435.0 - 451.0 nm  =  wl: 443.0  bw: 16.0
        put("sr_b2", 482.0f);     // from doc: 452.0 - 512.0 nm  =  wl: 482.0  bw: 60.0
        put("sr_b3", 561.5f);     // from doc: 533.0 - 590.0 nm  =  wl: 561.5  bw: 57.0
        put("sr_b4", 654.5f);     // from doc: 636.0 - 673.0 nm  =  wl: 654.5  bw: 37.0
        put("sr_b5", 865.0f);     // from doc: 851.0 - 879.0 nm  =  wl: 865.0  bw: 28.0
        put("sr_b6", 1608.5f);    // from doc: 1566.0 - 1651.0 nm  =  wl: 1608.5  bw: 85.0
        put("sr_b7", 2200.5f);    // from doc: 2107.0 - 2294.0 nm  =  wl: 2200.5  bw: 187.0
        put("st_b10", 10895.0f);  // from doc: 10600.0 - 11190.0 nm  =  wl: 10895.0  bw: 590.0
    }});

    private static final Map<String, Float> bandwidths = Collections.unmodifiableMap(new HashMap<String, Float>() {{
        // see doc: https://d9-wret.s3.us-west-2.amazonaws.com/assets/palladium/production/s3fs-public/atoms/files/LSDS-1328_Landsat8-9-OLI-TIRS-C2-L2-DFCB-v6.pdf
        put("sr_b1", 16f);      // from doc: 435.0 - 451.0 nm  =  wl: 443.0  bw: 16.0
        put("sr_b2", 60f);      // from doc: 452.0 - 512.0 nm  =  wl: 482.0  bw: 60.0
        put("sr_b3", 57f);      // from doc: 533.0 - 590.0 nm  =  wl: 561.5  bw: 57.0
        put("sr_b4", 37f);      // from doc: 636.0 - 673.0 nm  =  wl: 654.5  bw: 37.0
        put("sr_b5", 28f);      // from doc: 851.0 - 879.0 nm  =  wl: 865.0  bw: 28.0
        put("sr_b6", 85f);      // from doc: 1566.0 - 1651.0 nm  =  wl: 1608.5  bw: 85.0
        put("sr_b7", 187f);     // from doc: 2107.0 - 2294.0 nm  =  wl: 2200.5  bw: 187.0
        put("st_b10", 590f);    // from doc: 10600.0 - 11190.0 nm  =  wl: 10895.0  bw: 590.0
    }});

    private final static Map<String, Map<String, FlagCodingArgs>> flagCodings = Collections.unmodifiableMap(new HashMap<String, Map<String, FlagCodingArgs>>() {{
        put("qa_pixel", // QA Pixel
            Collections.unmodifiableMap(new LinkedHashMap<String, FlagCodingArgs>() {{
                ArrayList<FlagCodingArgs> args = new ArrayList<FlagCodingArgs>() {{
                    add(new FlagCodingArgs(0b01 << 0, "designated_fill", "Designated Fill", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 1, "dillated_cloud", "Dillated Cloud", new Color(196, 174, 78), 0.0f));
                    add(new FlagCodingArgs(0b01 << 2, "cirrus", "Cirrus", new Color(220, 204, 178), 0.0f));
                    add(new FlagCodingArgs(0b01 << 3, "cloud", "Cloud", new Color(175, 159, 122), 0.0f));
                    add(new FlagCodingArgs(0b01 << 4, "cloud_shadow", "Cloud Shadow", new Color(91, 34, 143), 0.0f));
                    add(new FlagCodingArgs(0b01 << 5, "snow", "Snow/Ice Cover", new Color(255, 255, 153), 0.0f));
                    add(new FlagCodingArgs(0b01 << 6, "clear", "Cloud and Dilated Cloud bits are not set", new Color(192, 192, 192), 0.0f));
                    add(new FlagCodingArgs(0b01 << 7, "water", "WATER (false = land or cloud)", new Color(0, 0, 0), 0.0f));

                    Color cloudColor = new Color(255, 255, 255);
                    add(new FlagCodingArgs(0b11 << 8, 0b00 << 8, "cloud_confidence_not_set", "Cloud confidence level is not set", darker(3, cloudColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 8, 0b01 << 8, "cloud_confidence_low", "Cloud confidence level is low", darker(2, cloudColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 8, 0b10 << 8, "cloud_confidence_medium", "Cloud confidence level is medium", darker(1, cloudColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 8, 0b11 << 8, "cloud_confidence_high", "Cloud confidence level is high", cloudColor, 0.5f));

                    Color cloudShadowColor = new Color(52, 18, 18);
                    add(new FlagCodingArgs(0b11 << 10, 0b00 << 10, "cloud_shadow_confidence_not_set", "Cloud shadow confidence is not set", brighter(3, cloudShadowColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 10, 0b01 << 10, "cloud_shadow_confidence_low", "Cloud shadow confidence level is low", brighter(2, cloudShadowColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 10, 0b10 << 10, "cloud_shadow_confidence_medium", "Cloud shadow confidence level is medium", brighter(1, cloudShadowColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 10, 0b11 << 10, "cloud_shadow_confidence_high", "Cloud shadow confidence level is high", cloudShadowColor, 0.5f));

                    Color snowIceColor = new Color(255, 255, 153);
                    add(new FlagCodingArgs(0b11 << 12, 0b00 << 12, "snow_ice_confidence_not_set", "Snow/ice confidence is not set", darker(3, snowIceColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 12, 0b01 << 12, "snow_ice_confidence_low", "Snow/ice confidence level is low", darker(2, snowIceColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 12, 0b10 << 12, "snow_ice_confidence_medium", "Snow/ice confidence level is medium", darker(1, snowIceColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 12, 0b11 << 12, "snow_ice_confidence_high", "Snow/ice confidence level is high", snowIceColor, 0.5f));

                    Color cirrusColor = new Color(128, 128, 128);
                    add(new FlagCodingArgs(0b11 << 14, 0b00 << 14, "cirrus_confidence_not_set", "Cirrus confidence is not set", brighter(3, cirrusColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 14, 0b01 << 14, "cirrus_confidence_low", "Cirrus confidence level is low", brighter(2, cirrusColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 14, 0b10 << 14, "cirrus_confidence_medium", "Cirrus confidence level is medium", brighter(1, cirrusColor), 0.5f));
                    add(new FlagCodingArgs(0b11 << 14, 0b11 << 14, "cirrus_confidence_high", "Cirrus confidence level is high", cirrusColor, 0.5f));
                }};
                for (FlagCodingArgs arg : args) {
                    put(arg.getName(), arg);
                }
            }})
        );
        put("qa_radsat",  // Radiometric Saturation and Terrain Occlusion QA Band
            Collections.unmodifiableMap(new LinkedHashMap<String, FlagCodingArgs>() {{
                ArrayList<FlagCodingArgs> args = new ArrayList<FlagCodingArgs>() {{
                    add(new FlagCodingArgs(0b01 << 0, "radiometric_saturation_b1", "Band 1 data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 1, "radiometric_saturation_b2", "Band 2 data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 2, "radiometric_saturation_b3", "Band 3 data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 3, "radiometric_saturation_b4", "Band 4 data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 4, "radiometric_saturation_b5", "Band 5 data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 5, "radiometric_saturation_b6", "Band 6 data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 6, "radiometric_saturation_b7", "Band 7 data saturation", new Color(255, 0, 0), 0.0f));
                    // position 7 not used
                    add(new FlagCodingArgs(0b01 << 8, "radiometric_saturation_b9", "Band 9 data saturation", new Color(255, 0, 0), 0.0f));
                    // positions 9 and 10 not used
                    add(new FlagCodingArgs(0b01 << 11, "terrain_occlusion", "Terrain occlusion", new Color(255, 0, 0), 0.0f));
                }};
                for (FlagCodingArgs arg : args) {
                    put(arg.getName(), arg);
                }
            }})
        );
        put("sr_qa_aerosol", // SR Aerosol QA File
            Collections.unmodifiableMap(new LinkedHashMap<String, FlagCodingArgs>() {{
                ArrayList<FlagCodingArgs> args = new ArrayList<FlagCodingArgs>() {{
                    add(new FlagCodingArgs(0b01 << 0, "aerosol_fill", "Pixel is fill", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 1, "aerosol_retrieval_valid", "Pixel aerosol retrieval is valid", new Color(0, 180, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 2, "aerosol_water", "Pixel is water", new Color(46, 77, 145), 0.0f));
                    // positions 3 and 4 not used
                    add(new FlagCodingArgs(0b01 << 5, "aerosol_interpolated", " Pixel is aerosol interpolated", new Color(255, 0, 255), 0.0f));

                    final Color aerosolLevelColor = new Color(89, 60, 5);
                    add(new FlagCodingArgs(0b11 << 6, 0b00 << 6, "aerosol_level_climatology", "Aerosol level climatology", brighter(3, aerosolLevelColor), 0.0f));
                    add(new FlagCodingArgs(0b11 << 6, 0b01 << 6, "aerosol_level_low", "Aerosol level low", brighter(2, aerosolLevelColor), 0.0f));
                    add(new FlagCodingArgs(0b11 << 6, 0b10 << 6, "aerosol_level_medium", "Aerosol level medium", brighter(1, aerosolLevelColor), 0.0f));
                    add(new FlagCodingArgs(0b11 << 6, 0b11 << 6, "aerosol_level_high", "Aerosol level high", aerosolLevelColor, 0.0f));
                }};
                for (FlagCodingArgs arg : args) {
                    put(arg.getName(), arg);
                }
            }})
        );
    }});
    
	/**
	 * Package access constructor.
	 *
	 */
	ProductSensorSpecificationL8L9() {
		// Nothing to do
	}

	@Override
	public String getBandDescription(String id) {
		if (bandDescriptions.containsKey(id)) {
			return bandDescriptions.get(id);
		}
		return null;
	}

	@Override
	public Double getScalingFactor(String id) {
		if (scalingFactors.containsKey(id)) {
			return scalingFactors.get(id);
		}
		return null;
	}

	@Override
	public Double getAddOffset(String id) {
		if (addOffsets.containsKey(id)) {
			return addOffsets.get(id);
		}
		return null;
	}

	@Override
	public Double getFillValue(String id) {
		if (fillValues.containsKey(id)) {
			return fillValues.get(id);
		}
		return null;
	}

	@Override
	public String getUnit(String id) {
		if (units.containsKey(id)) {
			return units.get(id);
		}
		return null;
	}

	@Override
	public Float getWavelength(String id) {
		if (wavelengths.containsKey(id)) {
			return wavelengths.get(id);
		}
		return null;
	}

	@Override
	public Float getBandwidth(String id) {
		if (bandwidths.containsKey(id)) {
			return bandwidths.get(id);
		}
		return null;
	}

	@Override
	public Map<String, FlagCodingArgs> getFlagCoding(String id) {
		if (flagCodings.containsKey(id)) {
			return flagCodings.get(id);
		}
		return null;
	}
}
