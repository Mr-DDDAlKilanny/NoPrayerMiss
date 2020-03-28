package kilanny.muslimalarm.util;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import kilanny.muslimalarm.AlarmRingBroadcastReceiver;
import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.data.Weekday;

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
1 -> 1 << 4
2 -> 1 << 3
3 -> 1 << 2
4 -> 1 << 1
5 -> 1 << 0
6 -> 1 << 6
7 -> 1 << 5
        */
        int dw = calendar.get(Calendar.DAY_OF_WEEK);
        return 1 << (dw <= 5 ? 5 - dw : dw == 6 ? 6 : 5);
    }

    private static Date getTimeAsDate(Calendar calendar, int offsetDays, String time, int diffMins) {
        DateTime offset = new DateTime(calendar.getTime()).plusDays(offsetDays);
        String s[] = time.split(":");
        DateTime to = new DateTime(offset.year().get(), offset.monthOfYear().get(),
                offset.dayOfMonth().get(), Integer.parseInt(s[0]), Integer.parseInt(s[1]));
        if (diffMins > 0)
            to = to.plusMinutes(diffMins);
        else
            to = to.minusMinutes(-diffMins);
        return to.toDate();
    }

    public static String getLeftTimeToDate(Context context, Date date) {
        long minutes = (date.getTime() - System.currentTimeMillis()) / 60000;
        if (minutes < 1) {
            return context.getString(R.string.less_than_minute);
        } else if (minutes < 60) {
            return context.getString(R.string.afterNMinutes,
                    String.format(Locale.ENGLISH, "%d", (int) minutes));
        } else if (minutes < 24 * 60) {
            int hours = (int) minutes / 60;
            minutes %= 60;
            return context.getString(R.string.afterNHoursAndNMinutes,
                    String.format(Locale.ENGLISH, "%d", hours),
                    String.format(Locale.ENGLISH, "%d", (int) minutes));
        } else {
            int days = (int) (minutes / (24 * 60));
            int hours = (int) ((minutes - days * 24 * 60) / 60);
            return context.getString(R.string.afterNDaysAndNHours,
                    String.format(Locale.ENGLISH, "%d", days),
                    String.format(Locale.ENGLISH, "%d", hours));
        }
    }

    public static void displayShareActivity(Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                context.getString(R.string.share_msg)
                        + " https://play.google.com/store/apps/details?id=kilanny.muslimalarm");
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static String getTimeOffsetDescription(@NonNull Context context, int mins) {
        if (mins == 0)
            return context.getString(R.string.just_on_time);
        String s = context.getString(mins > 0 ? R.string.after_time_by : R.string.before_time_by);
        mins = Math.abs(mins);
        if (mins < 60) {
            return s + " " + context.getString(R.string.nMinutes,
                    String.format(Locale.ENGLISH, "%d", mins));
        } else {
            int hours = mins / 60;
            mins %= 60;
            if (mins == 0) {
                return s + " " + context.getString(R.string.nHours,
                        String.format(Locale.ENGLISH, "%d", hours));
            } else {
                return s + " " + context.getString(R.string.nHoursAndNMinutes,
                        String.format(Locale.ENGLISH, "%d", hours),
                        String.format(Locale.ENGLISH, "%d", mins));
            }
        }
    }

    public static int timeAsMins(String time) {
        try {
            String s[] = time.split(":");
            return Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
        } catch (NumberFormatException ex) { //TODO: calculated pray time can be NaN
            return -1;
        }
    }

    public static NextAlarmInfo getNextAlarmDate(Context context, Alarm alarm) {
        if (!alarm.enabled) {
            throw new IllegalArgumentException("alarm is not enabled");
        }
        if (alarm.snoozedToTime != null && alarm.snoozedToTime >= System.currentTimeMillis())
            return new NextAlarmInfo(alarm, new Date(alarm.snoozedToTime), 0);
        Date from = alarm.skippedAlarmTime == null ?
                new Date() : new Date(alarm.skippedAlarmTime);
        AppSettings settings = AppSettings.getInstance(context);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        int dw = getDayOfWeek(calendar);
        String[] timeNames = {
                "Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"
        };
        //String[] sFajr = times.get(timeNames[0]).split(":");
        //int ifajr = Integer.parseInt(sFajr[0]) * 60 + Integer.parseInt(sFajr[1]);
        //String[] sIsha = times.get(timeNames[5]).split(":");
        //int isha = Integer.parseInt(sIsha[0]) * 60 + Integer.parseInt(sIsha[1]);
        //if (isha < ifajr) {
        //TODO: What if a prayer (e.g. Isha) is in the next day?
        //}
        int ct = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
//        if (ct > isha) {
//            calendar.add(Calendar.DATE, 1);
//            times = PrayTime.getPrayerTimes(context, 0, settings.getLatFor(0),
//                    settings.getLngFor(0), PrayTime.TIME_24, calendar.getTime());
//            ct = 0;
//            dw = getDayOfWeek(calendar);
//            calendar.add(Calendar.DATE, -1);
//        }
        if (alarm.weekDayFlags == Weekday.NO_REPEAT) {
            for (int o = 0; o < 2; ++o) {
                Map<String, String> times = PrayTime.getPrayerTimes(context, 0,
                        settings.getLatFor(0), settings.getLngFor(0),
                        PrayTime.TIME_24, new DateTime(from).plusDays(o).toDate());
                for (int i = 0; i < timeNames.length; ++i) {
                    int t = timeAsMins(times.get(timeNames[i])) + alarm.timeAlarmDiffMinutes;
                    int f = 1 << (5 - i);
                    if ((alarm.oneTimeLeftAlarmsTimeFlags & f) != 0 && (o > 0 || t > ct))
                        return new NextAlarmInfo(alarm,
                                getTimeAsDate(calendar, o, times.get(timeNames[i]), alarm.timeAlarmDiffMinutes),
                                f);
                }
            }
            // we should never get here ?
            throw new UnsupportedOperationException();
        }
        // loop until the same dayOfWeek in the next week
        // for example, alarm at Jumaah prayer only, and now is Jumaah after prayer
        int idw = (int) (Math.log(dw) / Math.log(2));
        for (int j = 0; j < 7 + 1; ++j) {
            int jdw = idw - j;
            if (jdw < 0) jdw += 7;
            if ((alarm.weekDayFlags & (1 << jdw)) != 0) {
                Map<String, String> times = PrayTime.getPrayerTimes(context, 0,
                        settings.getLatFor(0), settings.getLngFor(0),
                        PrayTime.TIME_24, new DateTime(from).plusDays(j).toDate());
                for (int i = 0; i < timeNames.length; ++i) {
                    int f = 1 << (5 - i);
                    if ((alarm.timeFlags & f) != 0 &&
                            (j > 0 || ct < timeAsMins(times.get(timeNames[i])) + alarm.timeAlarmDiffMinutes)) {
                        return new NextAlarmInfo(alarm,
                                getTimeAsDate(calendar, j, times.get(timeNames[i]), alarm.timeAlarmDiffMinutes),
                                f);
                    }
                }
            }
        }
        // we should never get here ?
        throw new UnsupportedOperationException();
    }

    public static String getPrayerNames(Context context, Alarm alarm) {
        String[] prayerNames = context.getResources().getStringArray(R.array.prayer_times);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            if ((alarm.timeFlags & (1 << i)) != 0)
                b.insert(0, ',').insert(1, prayerNames[5 - i]);
        }
        if (b.length() > 0) b.deleteCharAt(0);
        return b.toString();
    }

    public static String getAlarmDays(Context context, Alarm alarm) {
        String[] days = context.getResources().getStringArray(R.array.repeat_days);
        StringBuilder b = new StringBuilder();
        if (alarm.weekDayFlags == Weekday.NO_REPEAT)
            b.append(days[0]);
        else if (alarm.weekDayFlags == 127)
            b.append(context.getString(R.string.everyday));
        else {
            ArrayList<Integer> not = new ArrayList<>();
            for (int i = 0; i < days.length - 1 /*Exclude NO_REPEAT*/; ++i) {
                int f = 1 << i;
                if ((alarm.weekDayFlags & f) == 0)
                    not.add(7 - i);
            }
            if (not.size() >= 3) {
                for (int i = 0; i < days.length; ++i) {
                    int f = 1 << i;
                    if ((alarm.weekDayFlags & f) != 0)
                        b.append(days[7 - i]).append(',');
                }
                b.delete(b.length() - 1, b.length());
            } else if (not.size() == 2) {
                b.append(context.getString(R.string.allDaysExpect2Days,
                        days[not.get(0)],
                        days[not.get(1)]));
            } else {
                b.append(context.getString(R.string.allDaysExpect1Day,
                        days[not.get(0)]));
            }
        }
        return b.toString();
    }

    public static String getTimeName(Context context, int timeFlag) {
        String time;
        switch (timeFlag) {
            case Alarm.TIME_FAJR:
                time = context.getString(R.string.fajr);
                break;
            case Alarm.TIME_SUNRISE:
                time = context.getString(R.string.sun);
                break;
            case Alarm.TIME_DHUHR:
                time = context.getString(R.string.zuhr);
                break;
            case Alarm.TIME_ASR:
                time = context.getString(R.string.asr);
                break;
            case Alarm.TIME_MAGHRIB:
                time = context.getString(R.string.maghrib);
                break;
            case Alarm.TIME_ISHAA:
                time = context.getString(R.string.ishaa);
                break;
            default:
                time = "";
        }
        return time;
    }

    private static class BackgroundTask<TInput, TResult> extends AsyncTask<TInput, Void, TResult> {

        private final Function<TInput, TResult> work;
        private final Function<TResult, Void> onDone;

        public BackgroundTask(Function<TInput, TResult> work, Function<TResult, Void> onDone) {
            this.work = work;
            this.onDone = onDone;
        }

        @SafeVarargs
        @Override
        protected final TResult doInBackground(TInput... input) {
            return this.work.apply(input[0]);
        }

        @Override
        protected void onPostExecute(TResult tResult) {
            super.onPostExecute(tResult);
            if (this.onDone != null)
                this.onDone.apply(tResult);
        }
    }

    public static <TInput, TResult> void runInBackground(
            Function<TInput, TResult> work, @Nullable Function<TResult, Void> onDone, TInput input) {
        new BackgroundTask<>(work, onDone).execute(input);
    }

    public static class NextAlarmInfo {
        public Alarm alarm;
        public Date date;
        public int timeFlag;

        public boolean isSnoozed() {
            return timeFlag == 0;
        }

        public NextAlarmInfo() {
        }

        public NextAlarmInfo(Alarm alarm, Date date, int timeFlag) {
            this.alarm = alarm;
            this.date = date;
            this.timeFlag = timeFlag;
        }
    }

    public static void vibrateFor(@NonNull Context context,
                                  @IntRange(from = 10, to = 10000) int milliseconds) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect
                        .createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(milliseconds);
            }
        }
    }

    public static void scheduleAndDeletePreviousBackground(Context context) {
        Utils.runInBackground(new Function<Context, Pair<Context, Alarm[]>>() {
            @Override
            public Pair<Context, Alarm[]> apply(Context input) {
                return new Pair<>(input, AppDb.getInstance(input).alarmDao().getAll());
            }
        }, new Function<Pair<Context, Alarm[]>, Void>() {
            @Override
            public Void apply(Pair<Context, Alarm[]> input) {
                scheduleAndDeletePrevious(input.first, input.second);
                return null;
            }
        }, context);
    }

    private static PendingIntent getAlarmPendingIntent(Context context, Alarm alarm, int timeFlag) {
        Intent intentToFire = new Intent();
        intentToFire.setClass(context, AlarmRingBroadcastReceiver.class);
        //TODO: putExtra here not delivered to receiver !?
        intentToFire.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM, alarm);
        intentToFire.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM_TIME, timeFlag);
        return PendingIntent.getBroadcast(context, 1,
                intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleAndDeletePrevious(Context context, Alarm... alarms) {
        NextAlarmInfo nearestAlarm = null;
        for (Alarm alarm : alarms) {
            if (alarm.enabled) {
                NextAlarmInfo nextAlarm = getNextAlarmDate(context, alarm);
                if (nearestAlarm == null || nextAlarm.date.getTime() < nearestAlarm.date.getTime())
                    nearestAlarm = nextAlarm;
            }
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int timeFlag;
        try {
            String json;
            if ((json = pref.getString("nextAlarmJson", null)) != null) {
                Alarm oldAlarm = Alarm.fromJson(json);
                alarmManager.cancel(getAlarmPendingIntent(context, oldAlarm, pref.getInt("nextAlarmTime", 0)));
            }
            if (nearestAlarm == null) {
                pref.edit()
                        .putString("nextAlarmJson", null)
                        .putInt("nextAlarmTime", 0)
                        .apply();
                Log.v("schNext", "No alarm enabled; skipping.");
                return;
            }
            timeFlag = !nearestAlarm.isSnoozed() ?
                    nearestAlarm.timeFlag : pref.getInt("nextAlarmTime", 0);
            pref.edit()
                    .putString("nextAlarmJson", nearestAlarm.alarm.toJson())
                    .putInt("nextAlarmTime", timeFlag)
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
            if (nearestAlarm == null) {
                Log.v("schNext", "No alarm enabled; skipping.");
            }
            return;
        }
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager,
                AlarmManager.RTC_WAKEUP, nearestAlarm.date.getTime(),
                getAlarmPendingIntent(context, nearestAlarm.alarm, timeFlag));
        Log.v("schNext", "Scheduled next alarm at " + nearestAlarm.date);
    }

    /**
     * http://stackoverflow.com/a/5921190/7429464
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
