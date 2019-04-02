package com.multiheaded.vladikbot.utils;

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

    public static boolean containsStringFromArray(String input, String[] check) {
        for (String value : check) {
            if (input.contains(value)) return true;
        }
        return false;
    }
}
