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
 * Landsat 7 product specification
 * @author Lucian Barbulescu 
 */

@SuppressWarnings("PointlessBitwiseExpression")
public class ProductSensorSpecificationL7 extends ProductSensorSpecification {
	
    private static final Map<String, String> bandDescriptions = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("sr_b1", "Blue");
        put("sr_b2", "Green");
        put("sr_b3", "Red");
        put("sr_b4", "Near-Infrared (NIR)");
        put("sr_b5", "Short Wavelength Infrared (SWIR) 1");
        put("st_b6", "Thermal Infrared");
        put("sr_b7", "SWIR 2");
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
        put("sr_atmos_opacity", "SR atmospheric opacity less than 0.1 can be interpreted as clear. SR atmospheric " + 
        				"opacity between 0.1 and 0.3 can be considered average. SR atmospheric opacity " + 
        				"values greater than 0.3 can be considered hazy");
        put("sr_cloud_qa", "The SR Cloud QA file shares some of the data artifacts and land surface classification " + 
        				"indications as the QA Band file");
        put("st_qa", "The ST QA file indicates uncertainty of the temperatures given in the ST band file");
    }});

    private static final Map<String, Double> scalingFactors = Collections.unmodifiableMap(new HashMap<String, Double>() {{
        put("sr_b1", 2.75e-5);
        put("sr_b2", 2.75e-5);
        put("sr_b3", 2.75e-5);
        put("sr_b4", 2.75e-5);
        put("sr_b5", 2.75e-5);
        put("st_b6", 0.00341802);
        put("sr_b7", 2.75e-5);
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
        put("st_b6", 149.0);
        put("sr_b7", -0.2);
    }});

    private static final Map<String, Double> fillValues = Collections.unmodifiableMap(new HashMap<String, Double>() {{
        put("sr_b1", 0.0);
        put("sr_b2", 0.0);
        put("sr_b3", 0.0);
        put("sr_b4", 0.0);
        put("sr_b5", 0.0);
        put("st_b6", 0.0);
        put("sr_b7", 0.0);
        put("st_trad", -9999.0);
        put("st_urad", -9999.0);
        put("st_drad", -9999.0);
        put("st_atran", -9999.0);
        put("st_emis", -9999.0);
        put("st_emsd", -9999.0);
        put("st_cdist", -9999.0);
        put("qa_pixel", 1.0);
        put("sr_atmos_opacity", -9999.0);
        put("st_qa", -9999.0);
    }});

    private static final Map<String, String> units = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("st_b6", "Kelvin");
        put("st_trad", "W/m^2 sr µm)");
        put("st_urad", "W/m^2 sr µm)");
        put("st_drad", "W/m^2 sr µm)");
        put("st_cdist", "km");
        put("st_qa", "Kelvin");
    }});

    private static final Map<String, Float> wavelengths = Collections.unmodifiableMap(new HashMap<String, Float>() {{
        // see doc: https://d9-wret.s3.us-west-2.amazonaws.com/assets/palladium/production/s3fs-public/media/files/LSDS-1337_Landsat7ETM-C2-L2-DFCB-v6.pdf
        put("sr_b1", 477.5f);     // from doc: 441.0 - 514.0 nm  =  wl: 477.5  bw: 73.0
        put("sr_b2", 560.0f);     // from doc: 519.0 - 601.0 nm  =  wl: 560.0  bw: 82.0
        put("sr_b3", 661.5f);     //  from doc: 631.0 - 692.0 nm  =  wl: 661.5  bw: 61.0
        put("sr_b4", 835.0f);     // from doc: 772.0 - 898.0 nm  =  wl: 835.0  bw: 126.0
        put("sr_b5", 1648.0f);    // from doc: 1547.0 - 1749.0 nm  =  wl: 1648.0  bw: 202.0
        put("st_b6", 11335.0f);    // from doc: 10310.0 - 12360.0 nm  =  wl: 11335.0  bw: 2050.0
        put("sr_b7", 2204.5f);   // from doc: 2064.0 - 2345.0 nm  =  wl: 2204.5  bw: 281.0
    }});

    private static final Map<String, Float> bandwidths = Collections.unmodifiableMap(new HashMap<String, Float>() {{
        // see doc: https://d9-wret.s3.us-west-2.amazonaws.com/assets/palladium/production/s3fs-public/media/files/LSDS-1337_Landsat7ETM-C2-L2-DFCB-v6.pdf
        put("sr_b1", 73.0f);     // from doc: 441.0 - 514.0 nm  =  wl: 477.5  bw: 73.0
        put("sr_b2", 82.0f);     // from doc: 519.0 - 601.0 nm  =  wl: 560.0  bw: 82.0
        put("sr_b3", 61.0f);     //  from doc: 631.0 - 692.0 nm  =  wl: 661.5  bw: 61.0
        put("sr_b4", 126.0f);     // from doc: 772.0 - 898.0 nm  =  wl: 835.0  bw: 126.0
        put("sr_b5", 202.0f);    // from doc: 1547.0 - 1749.0 nm  =  wl: 1648.0  bw: 202.0
        put("st_b6", 2050.0f);    // from doc: 10310.0 - 12360.0 nm  =  wl: 11335.0  bw: 2050.0
        put("sr_b7", 281.0f);   // from doc: 2064.0 - 2345.0 nm  =  wl: 2204.5  bw: 281.0
    }});

    private final static Map<String, Map<String, FlagCodingArgs>> flagCodings = Collections.unmodifiableMap(new HashMap<String, Map<String, FlagCodingArgs>>() {{
        put("qa_pixel", // QA Pixel
            Collections.unmodifiableMap(new LinkedHashMap<String, FlagCodingArgs>() {{
                ArrayList<FlagCodingArgs> args = new ArrayList<FlagCodingArgs>() {{
                    add(new FlagCodingArgs(0b01 << 0, "designated_fill", "Designated Fill", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 1, "dillated_cloud", "Dillated Cloud", new Color(196, 174, 78), 0.0f));
                    // position 2 not used
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
                    
                    // position 14 not used
                    // position 15 not used
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
                    add(new FlagCodingArgs(0b01 << 5, "radiometric_saturation_b6L", "Band 6L data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 6, "radiometric_saturation_b7", "Band 7 data saturation", new Color(255, 0, 0), 0.0f));
                    // position 7 not used
                    add(new FlagCodingArgs(0b01 << 8, "radiometric_saturation_b6H", "Band 6H data saturation", new Color(255, 0, 0), 0.0f));
                    add(new FlagCodingArgs(0b01 << 9, "dropped_pixel", "Dropped Pixel", new Color(255, 0, 0), 0.0f));
                }};
                for (FlagCodingArgs arg : args) {
                    put(arg.getName(), arg);
                }
            }})
        );
        put("sr_cloud_qa", // SR Cloud QA File
            Collections.unmodifiableMap(new LinkedHashMap<String, FlagCodingArgs>() {{
                ArrayList<FlagCodingArgs> args = new ArrayList<FlagCodingArgs>() {{
                    add(new FlagCodingArgs(0b01 << 0, "pixel_dark_dense_vegetation", "Dark Dense Vegetation (DDV)", new Color(0, 128, 64), 0.0f));
                    add(new FlagCodingArgs(0b01 << 1, "pixel_cloud", "Cloud", new Color(175, 159, 122), 0.0f));
                    add(new FlagCodingArgs(0b01 << 2, "pixel_cloud_shadow", "Cloud Shadow", new Color(91, 34, 143), 0.0f));
                    add(new FlagCodingArgs(0b01 << 3, "pixel_adjacent_to_cloud", "Adjacent to Cloud", new Color(172, 125, 135), 0.0f));
                    add(new FlagCodingArgs(0b01 << 4, "pixel_snow", "Snow", new Color(255, 255, 153), 0.0f));
                    add(new FlagCodingArgs(0b01 << 5, "pixel_water", "Water", new Color(0, 0, 0), 0.0f));
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
	ProductSensorSpecificationL7() {
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
