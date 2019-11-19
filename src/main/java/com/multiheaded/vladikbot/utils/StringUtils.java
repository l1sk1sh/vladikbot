package com.multiheaded.vladikbot.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Oliver Johnson
 */
public class StringUtils {
    public static String replaceLast(String string, String find, String replace) {
        int lastIndex = string.lastIndexOf(find);

        if (lastIndex == -1) {
            return string;
        }

        String beginString = string.substring(0, lastIndex);
        String endString = string.substring(lastIndex + find.length());

        return beginString + replace + endString;
    }

    public static boolean notInArray(String input, String[] check) {
        for (String value : check) {
            if (input.contains(value)) return false;
        }
        return true;
    }

    public static String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("ddmmyyyy");
        return dateFormat.format(date);
    }
}
