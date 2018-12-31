package com.taf.auto.common;

import com.taf.auto.StringUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.taf.auto.IOUtil.NL;
import static com.taf.auto.common.PrettyPrinter.*;
import static com.taf.auto.common.PrettyPrinterTest.ColumnWidth.*;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link PrettyPrinter}.
 *
 * @author AF04261 mmorton
 */
public class PrettyPrinterTest {
    @Test
    public void prettyArray() {
        Integer[] a = { 3, 1, 4 };
        assertEquals("3+1+4", PrettyPrinter.prettyArray(a, "+"));
    }

    interface ColumnWidth {
        String DELIM = "|";

        String SCENARIO1 =
                "  | this | that |";
        String SCENARIO3 = SCENARIO1 + NL +
                "  | door | sun |" + NL +
                "  | window | moon |";
        String AMENDED =
                "  | this   | that |" + NL +
                "  | door   | sun  |" + NL +
                "  | window | moon |";

        static List<String> prep(String foo) {
           return Arrays.asList(StringUtil.splitNewlines(foo));
        }
    }

    @Test
    public void recordColumnWidthsTest() {
        recordColumnWidthsTestHelper(SCENARIO1, 2, 6, 6);
        recordColumnWidthsTestHelper(SCENARIO3, 2, 8, 6);
    }

    private static void recordColumnWidthsTestHelper(String scenario, int... breakpoints) {
        List<Integer> widths = new ArrayList<>();
        ColumnWidth.prep(scenario).forEach(recordColumnWidths(widths, DELIM));
        assertEquals(breakpoints.length, widths.size());
        for(int i = 0; i < breakpoints.length; i++) {
            assertEquals(format("Column %d", i), breakpoints[i], widths.get(i).intValue());
        }
    }

    @Test
    public void standardizeColumnWidthsTest() {
        List<String> lines = prettyDelimiters(ColumnWidth.prep(SCENARIO3), DELIM);
        assertEquals(ColumnWidth.AMENDED, prettyList(lines, NL));
    }
}
