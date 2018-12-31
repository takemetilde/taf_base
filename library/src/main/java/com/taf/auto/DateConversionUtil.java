package com.taf.auto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by AF04261 on 6/1/2016.
 */
public final class DateConversionUtil {
    public static final SimpleDateFormat STANDARD_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public static final SimpleDateFormat MONTH_SPELLED_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    private DateConversionUtil() { /** static only */ }

    public static String convertStandardToMonthSpelled(String MMddyyyy) {
        try {
            Date date = STANDARD_FORMAT.parse(MMddyyyy);
            return MONTH_SPELLED_FORMAT.format(date);
        } catch(ParseException pe) {
            throw new RuntimeException("Failed to parse MMddyyyy: " + MMddyyyy, pe);
        }
    }
}
