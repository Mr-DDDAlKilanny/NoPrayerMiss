package kilanny.muslimalarm.util;

import android.content.Context;

import com.amplitude.api.Amplitude;

import org.json.JSONException;
import org.json.JSONObject;

import kilanny.muslimalarm.App;
import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;

public final class AnalyticsTrackers {

    private static AnalyticsTrackers instance;

    public static AnalyticsTrackers getInstance(Context context) {
        if (instance == null)
            instance = new AnalyticsTrackers(context);
        return instance;
    }

    public void logSetup(int step) {
        try {
            Amplitude.getInstance().logEvent("SetupStep", new JSONObject().put("no", step));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void logModifyAlarm(Alarm alarm, boolean isNew) {
        try {
            JSONObject jsonObject = new JSONObject(alarm.toJson());
            jsonObject.put("isNew", isNew);
            Amplitude.getInstance().logEvent("AddAlarm", jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmSnoozed(Alarm alarm, int afterMins) {
        try {
            Amplitude.getInstance().logEvent("AlarmSnoozed",
                    new JSONObject(alarm.toJson()).put("afterMins", afterMins));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmRing(Alarm alarm) {
        try {
            JSONObject jsonObject = new JSONObject(alarm.toJson());
            Amplitude.getInstance().logEvent("AlarmRingBroadcastReceiver", jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmDismissed(Alarm alarm, int afterMins) {
        try {
            JSONObject jsonObject = new JSONObject(alarm.toJson())
                    .put("afterMins", afterMins);
            Amplitude.getInstance().logEvent("AlarmDismissed", jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void logAlarmPreviewed(Alarm alarm) {
        try {
            JSONObject jsonObject = new JSONObject(alarm.toJson());
            Amplitude.getInstance().logEvent("AlarmPreviewed", jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private AnalyticsTrackers(Context context) {
        Amplitude.getInstance().initialize(context, context.getString(R.string.amplitude_key))
                .enableForegroundTracking(App.get());
    }
}
