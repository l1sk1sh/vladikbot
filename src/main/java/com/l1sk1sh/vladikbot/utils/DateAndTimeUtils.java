package com.l1sk1sh.vladikbot.utils;

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
}
