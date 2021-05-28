package kilanny.muslimalarm.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.arch.core.util.Function;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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

    private static Date getTimeAsDate(Calendar calendar, int offsetDays, int h, int m, int diffMins) {
        DateTime offset = new DateTime(calendar.getTime()).plusDays(offsetDays);
        DateTime to = new DateTime(offset.year().get(), offset.monthOfYear().get(),
                offset.dayOfMonth().get(), h, m);
        if (diffMins > 0)
            to = to.plusMinutes(diffMins);
        else
            to = to.minusMinutes(-diffMins);
        return to.toDate();
    }

    private static Date getTimeAsDate(Calendar calendar, int offsetDays, String time, int diffMins) {
        String s[] = time.split(":");
        return getTimeAsDate(calendar, offsetDays, Integer.parseInt(s[0]), Integer.parseInt(s[1]), diffMins);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
        displayShareActivity(context,
                context.getString(R.string.share_msg)
                        + " https://play.google.com/store/apps/details?id=kilanny.muslimalarm");
    }

    public static void displayShareActivity(@NonNull Context context, @NonNull String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static String getTimeOffsetDescription(@NonNull Context context, Alarm alarm) {
        if (alarm.timeFlags == Alarm.TIME_QEYAM)
            return context.getString(R.string.of_night,
                    alarm.qeyamAlarmPercentageOfNightPeriod);
        int mins = alarm.timeAlarmDiffMinutes;
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

    private static int calcNightPeriod(String maghribTime, String fajrTime) {
        int m = timeAsMins(maghribTime),
                f = timeAsMins(fajrTime);
        if (m <= f)
            return f - m;
        else
            return (24 * 60 - m) + f;
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
                if (alarm.timeFlags == Alarm.TIME_CUSTOM) {
                    if (o > 0 || alarm.customTime > ct) {
                        return new NextAlarmInfo(alarm,
                                getTimeAsDate(calendar, o,
                                        alarm.customTime / 60, alarm.customTime % 60, 0),
                                alarm.timeFlags);
                    }
                } else {
                    Map<String, String> times = PrayTime.getPrayerTimes(context, 0,
                            settings.getLatFor(0), settings.getLngFor(0),
                            PrayTime.TIME_24, new DateTime(from).plusDays(o).toDate());
                    if (alarm.timeFlags != Alarm.TIME_QEYAM) {
                        for (int i = 0; i < timeNames.length; ++i) {
                            int t = timeAsMins(times.get(timeNames[i])) + alarm.timeAlarmDiffMinutes;
                            int f = 1 << (5 - i);
                            if ((alarm.oneTimeLeftAlarmsTimeFlags & f) != 0 &&
                                    (o > 0 || t + alarm.timeAlarmDiffMinutes > ct)) {
                                return new NextAlarmInfo(alarm,
                                        getTimeAsDate(calendar, o, times.get(timeNames[i]), alarm.timeAlarmDiffMinutes),
                                        f);
                            }
                        }
                    } else {
                        String maghrib = times.get(timeNames[4]);
                        int t = (Math.round(calcNightPeriod(maghrib, times.get(timeNames[0]))
                                * alarm.qeyamAlarmPercentageOfNightPeriod / 100.0f)
                                + timeAsMins(maghrib)) % (24 * 60);
                        if (o > 0 || t + alarm.timeAlarmDiffMinutes > ct) {
                            return new NextAlarmInfo(alarm,
                                    getTimeAsDate(calendar, o, t / 60, t % 60, alarm.timeAlarmDiffMinutes),
                                    alarm.timeFlags);
                        }
                    }
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
                if (alarm.timeFlags == Alarm.TIME_CUSTOM) {
                    if (j > 0 || alarm.customTime > ct) {
                        return new NextAlarmInfo(alarm,
                                getTimeAsDate(calendar, j,
                                        alarm.customTime / 60, alarm.customTime % 60, 0),
                                alarm.timeFlags);
                    }
                } else {
                    Map<String, String> times = PrayTime.getPrayerTimes(context, 0,
                            settings.getLatFor(0), settings.getLngFor(0),
                            PrayTime.TIME_24, new DateTime(from).plusDays(j).toDate());
                    if (alarm.timeFlags != Alarm.TIME_QEYAM) {
                        for (int i = 0; i < timeNames.length; ++i) {
                            int f = 1 << (5 - i);
                            if ((alarm.timeFlags & f) != 0 &&
                                    (j > 0 || ct < timeAsMins(times.get(timeNames[i])) + alarm.timeAlarmDiffMinutes)) {
                                return new NextAlarmInfo(alarm,
                                        getTimeAsDate(calendar, j, times.get(timeNames[i]), alarm.timeAlarmDiffMinutes),
                                        f);
                            }
                        }
                    } else {
                        String maghrib = times.get(timeNames[4]);
                        int t = (Math.round(calcNightPeriod(maghrib, times.get(timeNames[0]))
                                * alarm.qeyamAlarmPercentageOfNightPeriod / 100.0f)
                                + timeAsMins(maghrib)) % (24 * 60);
                        if (j > 0 || t + alarm.timeAlarmDiffMinutes > ct) {
                            return new NextAlarmInfo(alarm,
                                    getTimeAsDate(calendar, j, t / 60, t % 60, alarm.timeAlarmDiffMinutes),
                                    alarm.timeFlags);
                        }
                    }
                }
            }
        }
        // we should never get here ?
        throw new UnsupportedOperationException();
    }

    public static void showConfirm(Context context, String title, String msg,
                                   String okText, String cancelText,
                                   DialogInterface.OnClickListener ok,
                                   DialogInterface.OnClickListener cancel) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(okText, ok)
                .setNegativeButton(cancelText, cancel)
                .show();
    }

    public static boolean isValidTimes(Map<String, String> calcResult) {
        for (String value : calcResult.values()) {
            if (value.equals(PrayTime.InvalidTime))
                return false;
        }
        return true;
    }

    public static boolean isValidTimes(Context context) {
        AppSettings appSettings = AppSettings.getInstance(context);
        if (!appSettings.isDefaultSet()) return false;
        return Utils.isValidTimes(PrayTime.getPrayerTimes(context, 0,
                appSettings.getLatFor(0), appSettings.getLngFor(0)));
    }

    public static String getPrayerNames(Context context, Alarm alarm) {
        if (alarm.timeFlags == Alarm.TIME_QEYAM)
            return context.getString(R.string.qeyam);
        if (alarm.timeFlags == Alarm.TIME_CUSTOM) {
            return context.getString(R.string.custom_time);
        }
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
            case Alarm.TIME_QEYAM:
                time = context.getString(R.string.qeyam);
                break;
            case Alarm.TIME_CUSTOM:
                time = context.getString(R.string.custom_time);
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
        runInBackground(input -> new Pair<>(input, AppDb.getInstance(input).alarmDao().getAll()),
                input -> {
                    scheduleAndDeletePrevious(input.first, input.second);
                    return null;
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
                        .putLong("nextAlarmAt", 0)
                        .apply();
                Log.v("schNext", "No alarm enabled; skipping.");
                return;
            }
            timeFlag = !nearestAlarm.isSnoozed() ?
                    nearestAlarm.timeFlag : pref.getInt("nextAlarmTime", 0);
            pref.edit()
                    .putString("nextAlarmJson", nearestAlarm.alarm.toJson())
                    .putInt("nextAlarmTime", timeFlag)
                    .putLong("nextAlarmAt", nearestAlarm.date.getTime())
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

    public static void openUrlInChromeOrDefault(Context context, String urlString) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ex2) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(
                            context.getString(R.string.app_name), urlString));
                    Toast.makeText(context,
                            R.string.browser_not_found_paste_link,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
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

    public static void showAlert(Context context, String title, String message,
                                 @Nullable DialogInterface.OnDismissListener onDismissListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setOnDismissListener(onDismissListener)
                .show();
    }

    public static androidx.appcompat.app.AlertDialog showIndeterminateProgressDialog(Context context,
                                                                                     String title,
                                                                                     boolean cancelable) {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(context);
        tvText.setText(title);
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setCancelable(cancelable);
        builder.setView(ll);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
        return dialog;
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
                switch (errorCode) {
                    case ConnectionResult.SUCCESS:
                        Log.d("isGmsAvailable", "SUCCESS");
                        // Google Play Services installed and up to date
                        return true;
                    case ConnectionResult.SERVICE_MISSING:
                        Log.d("isGmsAvailable", "MISSING");
                        // Google Play services is missing on this device.
                        break;
                    case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                        Log.d("isGmsAvailable", "VERSION_UPDATE_REQUIRED");
                        // The installed version of Google Play services is out of date.
                        break;
                    case ConnectionResult.SERVICE_DISABLED:
                        Log.d("isGmsAvailable", "DISABLED");
                        // The installed version of Google Play services has been disabled on this device.
                        break;
                    case ConnectionResult.SERVICE_INVALID:
                        Log.d("isGmsAvailable", "INVALID");
                        // The version of the Google Play services installed on this device is not authentic.
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
