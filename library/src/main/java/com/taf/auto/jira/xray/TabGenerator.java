package com.taf.auto.jira.xray;

import java.util.Scanner;

/**
 * Created by AD96317 on 8/17/2016.
 */
public class TabGenerator {
    public static String generateTabs(int numberOfTabs) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<numberOfTabs; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }

    public static String addTabsToEachLineOfString(String src, int numberOfTabs) {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(src);

        while (scanner.hasNextLine()) {
            sb.append(generateTabs(numberOfTabs)).append(scanner.nextLine()).append("\n");
        }

        return sb.toString();
    }
}
