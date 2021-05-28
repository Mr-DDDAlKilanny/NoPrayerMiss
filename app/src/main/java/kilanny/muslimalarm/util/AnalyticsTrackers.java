package kilanny.muslimalarm.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Locale;

import kilanny.muslimalarm.data.Alarm;

public final class AnalyticsTrackers {

    private static AnalyticsTrackers instance;

    public static AnalyticsTrackers getInstance(Context context) {
        if (instance == null)
            instance = new AnalyticsTrackers(context);
        return instance;
    }

    private FirebaseAnalytics mFirebaseAnalytics;

    public boolean canMakeAnalytics() {
        return mFirebaseAnalytics != null;
    }

    public void logSetup(int step) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("number", step);
            mFirebaseAnalytics.logEvent("Step", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logNewUserStartConfig() {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent("NewUserStartConfig", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logNewUserEndConfig() {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent("NewUserEndConfig", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logModifyAlarm(Alarm alarm, boolean isNew) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            bundle.putBoolean("isNew", isNew);
            mFirebaseAnalytics.logEvent("AlarmModified", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmSnoozed(Alarm alarm, int afterMins) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            bundle.putInt("afterMins", afterMins);
            mFirebaseAnalytics.logEvent("AlarmSnoozed", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmRing(Alarm alarm, boolean isPreview) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            bundle.putBoolean("alarm_isPreview", isPreview);
            mFirebaseAnalytics.logEvent("AlarmRing", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmDismissed(Alarm alarm, int afterMins) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            bundle.putInt("afterMins", afterMins);
            mFirebaseAnalytics.logEvent("AlarmDismissed", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmMaxRing(Alarm alarm) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            mFirebaseAnalytics.logEvent("AlarmMaxRing", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmSkipped(Alarm alarm) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            mFirebaseAnalytics.logEvent("AlarmSkipped", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmDeleted(Alarm alarm) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            mFirebaseAnalytics.logEvent("AlarmDeleted", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmPreviewed(Alarm alarm) {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            putAlarm(alarm, bundle);
            mFirebaseAnalytics.logEvent("AlarmPreviewed", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void logAdShow() {
        if (!canMakeAnalytics()) return;
        try {
            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent("AlarmShown", bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void putAlarm(Alarm alarm, Bundle bundle) {
        bundle.putInt("alarm_dismissAlarmType", alarm.dismissAlarmType);
        bundle.putInt("alarm_alarmTone", alarm.alarmTune);
        bundle.putBoolean("alarm_isBarcode", alarm.dismissAlarmBarcodeId != null);
        bundle.putInt("alarm_data1", alarm.dismissAlarmTypeData1 != null ? alarm.dismissAlarmTypeData1 : -1);
        bundle.putInt("alarm_data2", alarm.dismissAlarmTypeData2 != null ? alarm.dismissAlarmTypeData2 : -1);
        bundle.putBoolean("alarm_enabled", alarm.enabled);
        bundle.putInt("alarm_id", alarm.id);
        bundle.putInt("alarm_weekDayFlags", alarm.weekDayFlags);
        bundle.putInt("alarm_timeFlags", alarm.timeFlags);
        bundle.putInt("alarm_snoozeMins", alarm.snoozeMins);
        bundle.putInt("alarm_snoozeCount", alarm.snoozeCount);
        bundle.putInt("alarm_snoozedCount", alarm.snoozedCount);
        bundle.putInt("alarm_soundLevel", alarm.soundLevel);
        bundle.putInt("alarm_timeAlarmDiffMinutes", alarm.timeAlarmDiffMinutes);
        bundle.putBoolean("alarm_vibrationEnabled", alarm.vibrationEnabled);
    }

    public void logException(Throwable throwable) {
        if (!canMakeAnalytics()) return;
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private AnalyticsTrackers(Context context) {
        if (Utils.isGooglePlayServicesAvailable(context)) {
            try {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
                mFirebaseAnalytics.setUserProperty("locale", Locale.getDefault().getDisplayName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
