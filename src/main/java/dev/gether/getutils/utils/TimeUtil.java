package dev.gether.getutils.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtil {


    /**
     * Formats time in seconds to a short form string (e.g., "5d 2h 30m 15s").
     *
     * @param seconds Time in seconds
     * @return Formatted time string
     */
    public static String formatTimeShort(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append("d ");
        if (hours > 0) result.append(hours).append("h ");
        if (minutes > 0) result.append(minutes).append("m ");
        result.append(seconds).append("s");

        return result.toString().trim();
    }

    /**
     * Converts seconds to a Polish time format, choosing the most significant unit.
     *
     * @param totalSeconds Time in seconds
     * @return Formatted time string in Polish
     */
    public static String convertSecondsToPolishTime(long totalSeconds) {
        long days = totalSeconds / (60 * 60 * 24);
        long hours = (totalSeconds / (60 * 60)) % 24;
        long minutes = (totalSeconds / 60) % 60;
        long seconds = totalSeconds % 60;

        if (seconds > 0) minutes++;
        if (minutes >= 60) {
            hours++;
            minutes = 0;
        }
        if (hours >= 24) {
            days++;
            hours = 0;
        }

        if (days > 0) {
            return days == 1 ? "1 dzień" : days + " dni";
        } else if (hours > 0) {
            if (hours == 1) return "1 godzina";
            if ((hours >= 2 && hours <= 4) || hours == 22) return hours + " godziny";
            return hours + " godzin";
        } else {
            if (minutes == 1) return "1 minuta";
            if ((minutes >= 2 && minutes <= 4) || (minutes % 10 >= 2 && minutes % 10 <= 4 && (minutes < 10 || minutes > 20)))
                return minutes + " minuty";
            return minutes + " minut";
        }
    }

    /**
     * Formats time in seconds to a colon-separated string (e.g., "01:30:45").
     *
     * @param seconds Time in seconds
     * @return Formatted time string
     */
    public static String formatTimeColon(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;

        minutes %= 60;
        seconds %= 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
