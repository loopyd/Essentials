package com.earth2me.essentials.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.earth2me.essentials.I18n.tl;


/**
 * <p>DateUtil class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class DateUtil {
    private static Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
    private static final int maxYears = 100000;

    /**
     * <p>removeTimePattern.</p>
     *
     * @param input a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String removeTimePattern(String input) {
        return timePattern.matcher(input).replaceFirst("").trim();
    }

    /**
     * <p>parseDateDiff.</p>
     *
     * @param time a {@link java.lang.String} object.
     * @param future a boolean.
     * @return a long.
     * @throws java.lang.Exception if any.
     */
    public static long parseDateDiff(String time, boolean future) throws Exception {
        Matcher m = timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() == null || m.group().isEmpty()) {
                continue;
            }
            for (int i = 0; i < m.groupCount(); i++) {
                if (m.group(i) != null && !m.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (m.group(1) != null && !m.group(1).isEmpty()) {
                    years = Integer.parseInt(m.group(1));
                }
                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    months = Integer.parseInt(m.group(2));
                }
                if (m.group(3) != null && !m.group(3).isEmpty()) {
                    weeks = Integer.parseInt(m.group(3));
                }
                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    days = Integer.parseInt(m.group(4));
                }
                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    hours = Integer.parseInt(m.group(5));
                }
                if (m.group(6) != null && !m.group(6).isEmpty()) {
                    minutes = Integer.parseInt(m.group(6));
                }
                if (m.group(7) != null && !m.group(7).isEmpty()) {
                    seconds = Integer.parseInt(m.group(7));
                }
                break;
            }
        }
        if (!found) {
            throw new Exception(tl("illegalDate"));
        }
        Calendar c = new GregorianCalendar();
        if (years > 0) {
            if (years > maxYears) {
                years = maxYears;
            }
            c.add(Calendar.YEAR, years * (future ? 1 : -1));
        }
        if (months > 0) {
            c.add(Calendar.MONTH, months * (future ? 1 : -1));
        }
        if (weeks > 0) {
            c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
        }
        if (days > 0) {
            c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
        }
        if (hours > 0) {
            c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
        }
        if (minutes > 0) {
            c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
        }
        if (seconds > 0) {
            c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
        }
        Calendar max = new GregorianCalendar();
        max.add(Calendar.YEAR, 10);
        if (c.after(max)) {
            return max.getTimeInMillis();
        }
        return c.getTimeInMillis();
    }

    static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int year = Calendar.YEAR;

        int fromYear = fromDate.get(year);
        int toYear = toDate.get(year);
        if (Math.abs(fromYear - toYear) > maxYears) {
            toDate.set(year, fromYear +
                    (future ? maxYears : -maxYears));
        }

        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            diff++;
        }
        diff--;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

    /**
     * <p>formatDateDiff.</p>
     *
     * @param date a long.
     * @return a {@link java.lang.String} object.
     */
    public static String formatDateDiff(long date) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        Calendar now = new GregorianCalendar();
        return DateUtil.formatDateDiff(now, c);
    }

    /**
     * <p>formatDateDiff.</p>
     *
     * @param fromDate a {@link java.util.Calendar} object.
     * @param toDate a {@link java.util.Calendar} object.
     * @return a {@link java.lang.String} object.
     */
    public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return tl("now");
        }
        if (toDate.after(fromDate)) {
            future = true;
        }
        StringBuilder sb = new StringBuilder();
        int[] types = new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND};
        String[] names = new String[]{tl("year"), tl("years"), tl("month"), tl("months"), tl("day"), tl("days"), tl("hour"), tl("hours"), tl("minute"), tl("minutes"), tl("second"), tl("seconds")};
        int accuracy = 0;
        for (int i = 0; i < types.length; i++) {
            if (accuracy > 2) {
                break;
            }
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                accuracy++;
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
            }
        }
        if (sb.length() == 0) {
            return tl("now");
        }
        return sb.toString().trim();
    }
}
