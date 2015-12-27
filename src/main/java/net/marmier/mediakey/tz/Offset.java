package net.marmier.mediakey.tz;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static net.marmier.mediakey.tz.Offset.OffsetSign.NEGATIVE;
import static net.marmier.mediakey.tz.Offset.OffsetSign.POSITIVE;

/**
 * Added by raphael on 27.12.15.
 */
public enum Offset {
    MINUS_1100("UTC-11", NEGATIVE, 11, 0),
    MINUS_1000("UTC-10", NEGATIVE, 10, 0),
    MINUS_0900("UTC-9", NEGATIVE, 9, 0),
    MINUS_0800("UTC-8", NEGATIVE, 8, 0),
    MINUS_0700("UTC-7", NEGATIVE, 7, 0),
    MINUS_0600("UTC-6", NEGATIVE, 6, 0),
    MINUS_0500("UTC-5", NEGATIVE, 5, 0),
    MINUS_0430("UTC-4:30", NEGATIVE, 4, 30),
    MINUS_0400("UTC-4", NEGATIVE, 4, 0),
    MINUS_0300("UTC-3", NEGATIVE, 3, 0),
    MINUS_0200("UTC-2", NEGATIVE, 2, 0),
    MINUS_0100("UTC-1", NEGATIVE, 1, 0),
    UTC("UTC", POSITIVE, 0, 0),
    PLUS_0100("UTC+1", POSITIVE, 1, 0),
    PLUS_0200("UTC+2", POSITIVE, 2, 0),
    PLUS_0300("UTC+3", POSITIVE, 3, 0),
    PLUS_0330("UTC+3:30", POSITIVE, 3, 30),
    PLUS_0400("UTC+4", POSITIVE, 4, 0),
    PLUS_0500("UTC+5", POSITIVE, 5, 0),
    PLUS_0530("UTC+5:30", POSITIVE, 5, 30),
    PLUS_0545("UTC+5:45", POSITIVE, 5, 45),
    PLUS_0600("UTC+6", POSITIVE, 6, 0),
    PLUS_0630("UTC+6:30", POSITIVE, 6, 30),
    PLUS_0700("UTC+7", POSITIVE, 7, 0),
    PLUS_0800("UTC+8", POSITIVE, 8, 0),
    PLUS_0900("UTC+9", POSITIVE, 9, 0),
    PLUS_0930("UTC+9:30", POSITIVE, 9, 30),
    PLUS_1000("UTC+10", POSITIVE, 10, 0),
    PLUS_1100("UTC+11", POSITIVE, 11, 0),
    PLUS_1200("UTC+12", POSITIVE, 12, 0),
    PLUS_1300("UTC+13", POSITIVE, 13, 0),
    PLUS_1400("UTC+14", POSITIVE, 14, 0);

    private static Map<String, Offset> map = new HashMap<String, Offset>();

    static {
        for (Offset offset : Offset.values()) {
            map.put(offset.code, offset);
        }
    }

    public static Offset forCode(String code) {
        Offset offset = map.get(code);

        if (offset == null) {
            throw new IllegalArgumentException();
        }
        return offset;
    }

    public enum OffsetSign {
        POSITIVE, NEGATIVE
    }

    private String code;
    private OffsetSign sign;
    private int hourOffset;
    private int minuteOffset;

    Offset(String code, OffsetSign sign, int hourOffset, int minuteOffset) {
        this.code = code;
        this.sign = sign;
        this.hourOffset = hourOffset;
        this.minuteOffset = minuteOffset;
    }

    /**
     * Apply the offset to a datetime in reverse, to get the time
     * in UTC corresponding to the given local time, according to the offset.
     * @param dateTime the local datetime
     * @return the utc datetime
     */
    public LocalDateTime reverse(LocalDateTime dateTime) {
        LocalDateTime theDateTime;
        if (sign == NEGATIVE) {
            theDateTime = dateTime.plusHours(hourOffset);
            theDateTime = theDateTime.plusMinutes(minuteOffset);
        } else {
            theDateTime = dateTime.minusHours(hourOffset);
            theDateTime = theDateTime.minusMinutes(minuteOffset);
        }
        return theDateTime;
    }

    @Override
    public String toString() {
        return String.format("%s%02d%02d",
            getSign() == POSITIVE ? "+" : "-",
            getHourOffset(),
            getMinuteOffset());
    }

    /**
     * Code representing the timezone offset
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Determine if the offset must be added or substracted to UTC
     * @return the code
     */
    public OffsetSign getSign() {
        return sign;
    }

    /**
     * Absolute value of the hour offset
     * @return the number of hours to add or substract
     */
    public int getHourOffset() {
        return hourOffset;
    }

    /**
     * Absolute valute of the minute offset
     * @return the number of minutes to add or substract
     */
    public int getMinuteOffset() {
        return minuteOffset;
    }
}
