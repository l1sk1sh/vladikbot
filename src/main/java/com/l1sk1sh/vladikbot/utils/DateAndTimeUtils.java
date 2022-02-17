package com.l1sk1sh.vladikbot.utils;

import com.google.gson.internal.bind.util.ISO8601Utils;
import com.l1sk1sh.vladikbot.settings.Const;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateAndTimeUtils {

    public static int getDifferenceInDaysBetweenUnixTimestamps(long beforeTimestamp, long afterTimestamp) {
        Date beforeDate = new Date();
        beforeDate.setTime(beforeTimestamp);

        Date afterDate = new Date();
        afterDate.setTime(afterTimestamp);

        return (int) ((afterDate.getTime() - beforeDate.getTime()) / Const.DAY_IN_MILLISECONDS);
    }

    public static String getHumanReadableDate(long timestamp) {
        return ISO8601Utils.format(new Date(timestamp));
    }

    public static long getTimeNowMinusDays(long days) {
        return System.currentTimeMillis() - days * 24 * 60 * 60 * 1000;
    }
}
