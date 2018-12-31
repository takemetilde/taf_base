package com.taf.auto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Library of color related utilities.
 *
 * @author AF04261 mmorton
 */
public final class ColorUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ColorUtil.class);

    public static String hexToRGBA(String colorAsHex) {
        try {
            StringBuilder rgba = new StringBuilder("rgba(");
            int startAt = colorAsHex.startsWith("#") ? 1 : 0;
            // handle RRGGBB
            for (int i = startAt, len = startAt + 6; i < len; i += 2) {
                int val = Integer.valueOf(colorAsHex.substring(i, i + 2), 16);
                rgba.append(val);
                rgba.append(", ");
            }
            // handle alpha
            if(colorAsHex.length() - startAt == 8) {
                int hexStartAt = startAt + 6;
                int val = Integer.valueOf(colorAsHex.substring(hexStartAt, hexStartAt + 2), 16);
                // the alpha value is 0 to 1
                double convertedVal = val / 255.;
                if(convertedVal == (long)convertedVal)
                    rgba.append((long)convertedVal);
                else
                    rgba.append(convertedVal);
            } else {
                rgba.append('1');
            }

            rgba.append(')');
            return rgba.toString();
        } catch(Exception e) {
            throw new NumberFormatException("Invalid hex expression: " + colorAsHex);
        }
    }

    /**
     * Looks for the pattern "rbg (r, g, b)" inside the input string and extracts it.
     *
     * @param input the input to extract from
     * @return the extracted rbg expression or <code>null</code> if no match
     */
    public static String extractRGB(String input) {
        LOG.trace("extractRGB: " + input);
        Pattern c = Pattern.compile(".*rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\).*");
        Matcher m = c.matcher(input);

        if (m.matches())
        {
            StringBuilder rbg = new StringBuilder("rgb(");
            int r = Integer.valueOf(m.group(1));
            int g = Integer.valueOf(m.group(2));
            int b = Integer.valueOf(m.group(3));
            rbg.append(r).append(", ").append(g).append(", ").append(b).append(')');
            return rbg.toString();
        }

        return null;
    }
}
