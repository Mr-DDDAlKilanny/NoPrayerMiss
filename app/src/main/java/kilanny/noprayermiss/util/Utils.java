package kilanny.noprayermiss.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import kilanny.noprayermiss.data.Alarm;
import kilanny.noprayermiss.data.AppSettings;
import kilanny.noprayermiss.data.Weekday;

public class Utils {

    public static final byte CONNECTION_STATUS_CONNECTED = 1,
            CONNECTION_STATUS_NOT_CONNECTED = 2,
            CONNECTION_STATUS_UNKNOWN_STATUS = 3;

    public static byte isConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting() ?
                    CONNECTION_STATUS_CONNECTED : CONNECTION_STATUS_NOT_CONNECTED;
        } catch (Exception ignored) {
            return CONNECTION_STATUS_UNKNOWN_STATUS;
        }
    }

    public static int getDayOfWeek(Calendar calendar) {
/*
1 -> 3
2 -> 4
3 -> 5
4 -> 6
5 -> 7
6 -> 1
7 -> 2
        */
        int dw = calendar.get(Calendar.DAY_OF_WEEK);
        return dw <= 5 ? dw + 2 : dw - 5;
    }

    private static Date getTimeAsDate(Calendar calendar, int offsetDays, String time) {
        DateTime offset = new DateTime(calendar.getTime()).plusDays(offsetDays);
        String s[] = time.split(":");
        DateTime to = new DateTime(offset.year().get(), offset.monthOfYear().get(),
                offset.dayOfMonth().get(), Integer.parseInt(s[0]), Integer.parseInt(s[1]));
        return to.toDate();
    }

    public static int timeAsMins(String time) {
        String s[] = time.split(":");
        return Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
    }

    public static Date getNextAlarmDate(Context context, Alarm alarm) {
        AppSettings settings = AppSettings.getInstance(context);
        Map<String, String> times = PrayTime.getPrayerTimes(context, 0, settings.getLatFor(0),
                settings.getLngFor(0));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int dw = getDayOfWeek(calendar);
        String[] sIsha = times.get("Isha").split(":");
        int isha = Integer.parseInt(sIsha[0]) * 60 + Integer.parseInt(sIsha[1]);
        int ct = calendar.get(Calendar.HOUR) * 60 + calendar.get(Calendar.MINUTE);
        //TODO: What if a prayer is in the next day? This code assumes Fajr and Isha in the same day
        if (ct > isha) {
            calendar.add(Calendar.DATE, 1);
            times = PrayTime.getPrayerTimes(context, 0, settings.getLatFor(0),
                    settings.getLngFor(0), -1, calendar.getTime());
            ct = 0;
            dw = getDayOfWeek(calendar);
            calendar.add(Calendar.DATE, -1);
        }
        String[] timeNames = {
                "Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"
        };
        if (alarm.weekDayFlags == Weekday.NO_REPEAT) {
            for (int i = 0; i < timeNames.length; ++i) {
                int f = 1 << i;
                if ((alarm.timeFlags & f) != 0)
                    return getTimeAsDate(calendar, 1, times.get(timeNames[i]));
            }
            // we should never get here ?
            throw new UnsupportedOperationException();
        }
        // loop until the same dayOfWeek in the next week
        // for example, alarm at Jumaah prayer only, and now is Jumaah after prayer
        for (int j = 0; j < 7 + 1; ++j) {
            int jdw = (dw + j) % 7;
            int f = 1 << jdw;
            if ((alarm.weekDayFlags & f) != 0) {
                for (int i = 0; i < timeNames.length; ++i) {
                    f = 1 << i;
                    if ((alarm.timeFlags & f) != 0 &&
                            (j > 0 || ct <= timeAsMins(times.get(timeNames[i])))) {
                        return getTimeAsDate(calendar, j, times.get(timeNames[i]));
                    }
                }
            }
        }
        // we should never get here ?
        throw new UnsupportedOperationException();
    }
}
