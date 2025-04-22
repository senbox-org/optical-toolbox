package eu.esa.opt.olci.l1csyn;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.OperatorException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Class providing utility methods.
 */
public class L1cSynUtils {

    public static String getSynName(Product slstrSource, Product olciSource) throws OperatorException {
        // pattern is MMM_SS_L_TTTTTT_yyyymmddThhmmss_YYYYMMDDTHHMMSS_yyyyMMDDTHHMMSS_<instance ID>_GGG_<class ID>.<extension>
        if (slstrSource == null || olciSource == null) {
            return "L1C";
        }

        String slstrName = slstrSource.getName();
        String olciName = olciSource.getName();

        if (slstrName.length() < 81 || olciName.length() < 81) {
            return "L1C";
        }

        StringBuilder synName = new StringBuilder();
        if (olciName.contains("S3A") && slstrName.contains("S3A")) {
            synName.append("S3A_SY_1_SYN____");
        } else if (olciName.contains("S3B") && slstrName.contains("S3B")) {
            synName.append("S3B_SY_1_SYN____");
        } else {
            synName.append("________________");
        }
        Map<String, ProductData.UTC> startEndDateMap = getStartEndDate(slstrSource, olciSource);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date startDate = startEndDateMap.get("startDate").getAsDate();
        String dateStringStart = dateFormat.format(startDate);
        Date endDate = startEndDateMap.get("endDate").getAsDate();
        String dateStringEnd = dateFormat.format(endDate);
        synName.append(dateStringStart);
        synName.append("_");
        synName.append(dateStringEnd);
        synName.append("_");

        Date date = new Date();
        String currentDate = dateFormat.format(date);
        synName.append(currentDate);
        synName.append("_");
        String instanceString = slstrSource.getName().substring(64, 81);
        synName.append(instanceString);
        synName.append("_");
        synName.append("LN2_");
        synName.append("O_NT_"); /// Operational, non-time-critical
        String slstrBaseline = "___"; //slstrSource.getName().substring(91,94); //todo: clarify if there should be any baseline
        synName.append(slstrBaseline);
        synName.append(".SEN3");
        return synName.toString();
    }

    static Map<String, ProductData.UTC> getStartEndDate(Product slstrSource, Product olciSource) {
        HashMap<String, ProductData.UTC> dateMap = new HashMap<>();
        ProductData.UTC startDateUTC;
        ProductData.UTC endDateUTC;

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        final long slstrStartTime = slstrSource.getStartTime().getAsDate().getTime();
        final long olciStartTime = olciSource.getStartTime().getAsDate().getTime();
        if (slstrStartTime < olciStartTime) {
            startDateUTC = slstrSource.getStartTime();
        } else {
            startDateUTC = olciSource.getStartTime();
        }
        final long slstrEndTime = slstrSource.getStartTime().getAsDate().getTime();
        final long olciEndTime = olciSource.getStartTime().getAsDate().getTime();
        if (slstrEndTime > olciEndTime) {
            endDateUTC = slstrSource.getEndTime();
        } else {
            endDateUTC = olciSource.getEndTime();
        }
        dateMap.put("startDate", startDateUTC);
        dateMap.put("endDate", endDateUTC);
        return dateMap;
    }
}

