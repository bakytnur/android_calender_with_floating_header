package bakha.ms.outlook.data.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Convenience class for frequently used APIs
 */

public class Utils {
    public static final String SIMPLE_LOCALE_DATE_TIME_FORMAT = "EEEE, MMM dd, yyyy hh:mm a";
    public static final String SIMPLE_DATE_FORMAT = "EEEE, MMM dd, yyyy";
    public static final String SIMPLE_DATE_KEY_FORMAT = "yyyy-MM-dd";
    public static final String SIMPLE_TIME_FORMAT = "hh:mm a";

    /**
     * To check if there is a event on a particular day, we need to check the date only
     * @param date
     * @param anotherDate
     * @return
     */
    public static boolean compareDates(Calendar date, Calendar anotherDate) {
        boolean compare = date.get(Calendar.YEAR) == anotherDate.get(Calendar.YEAR)
                && date.get(Calendar.MONTH) == anotherDate.get(Calendar.MONTH)
                && date.get(Calendar.DAY_OF_MONTH) == anotherDate.get(Calendar.DAY_OF_MONTH);
        return compare;
    }

    public static String getFormattedTime(Calendar date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_TIME_FORMAT, Locale.ENGLISH);
        return simpleDateFormat.format(date.getTime());
    }

    public static Calendar getCalendarTimeFromText(String text) {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_TIME_FORMAT, Locale.ENGLISH);
        Date date;
        try {
            date = simpleDateFormat.parse(text);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }

    public static String getFormattedDate(Calendar date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
        return simpleDateFormat.format(date.getTime());
    }

    public static String makeKeyForDate(Calendar date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_KEY_FORMAT, Locale.ENGLISH);
        return simpleDateFormat.format(date.getTime());
    }

    public static Calendar getCalendarDateFromText(String text) {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
        Date date;
        try {
            date = simpleDateFormat.parse(text);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }

    public static String getFormattedDateTime(Calendar date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_LOCALE_DATE_TIME_FORMAT, Locale.ENGLISH);
        return simpleDateFormat.format(date.getTime());
    }

    public static Calendar getCalendarDateTimeFromText(String text) {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT + " " + SIMPLE_TIME_FORMAT, Locale.ENGLISH);
        Date date;
        try {
            date = simpleDateFormat.parse(text);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }

    public static int getDifferenceInDays(Calendar time, Calendar anotherTime) {
        long diff = anotherTime.getTime().getTime() - time.getTime().getTime();
        int diffInDays = (int) Math.ceil(diff / (24 * 60 * 60 * 1000));
        return diffInDays;
    }

    public static String getDifferenceInHour(Calendar time, Calendar anotherTime) {
        long diff = anotherTime.getTime().getTime() - time.getTime().getTime();
        int diffInHour = (int) Math.ceil(diff / (60 * 60 * 1000));
        int diffInMinute = (int) Math.ceil( (diff - diffInHour * 60 * 60 * 1000) / (60 * 1000));
        StringBuilder diffBuilder = new StringBuilder();
        if (diffInHour > 0) {
            diffBuilder.append(diffInHour + "h");
        }

        if (diffInMinute > 0) {
            diffBuilder.append(diffInMinute + "m");
        }
        return diffBuilder.toString();
    }

    public static int getDifferenceInMinutes(Calendar time, Calendar anotherTime) {
        long diff = anotherTime.getTime().getTime() - time.getTime().getTime();
        int diffInMinute = (int) Math.ceil(diff / (60 * 1000));
        return diffInMinute;
    }
}
