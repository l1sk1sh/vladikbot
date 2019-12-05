package com.l1sk1sh.vladikbot.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Oliver Johnson
 */
public final class StringUtils {
    private StringUtils() {}

    public static String replaceLast(String string, String find, String replace) {
        int lastIndex = string.lastIndexOf(find);

        if (lastIndex == -1) {
            return string;
        }

        String beginString = string.substring(0, lastIndex);
        String endString = string.substring(lastIndex + find.length());

        return beginString + replace + endString;
    }

    public static boolean inArray(String input, String[] check) {
        for (String value : check) {
            if (input.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public static String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
        return dateFormat.format(date);
    }
}
