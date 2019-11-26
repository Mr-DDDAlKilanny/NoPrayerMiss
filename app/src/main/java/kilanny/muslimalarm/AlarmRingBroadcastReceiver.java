package kilanny.muslimalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.services.AlarmRingingService;
import kilanny.muslimalarm.util.Utils;

public class AlarmRingBroadcastReceiver extends BroadcastReceiver {

    public static final String ARG_IS_PREVIEW = "isPreview";
    public static final String ARG_ALARM = "alarm";
    public static final String ARG_ALARM_TIME = "alarmTime";

    @Override
    public void onReceive(Context context, Intent intent) {
        Alarm alarm = intent.getParcelableExtra(ARG_ALARM);
        int alarmTime = intent.getIntExtra(ARG_ALARM_TIME, 0);
        boolean isPreview = intent.getBooleanExtra(ARG_IS_PREVIEW, false);
        if (!Utils.isServiceRunning(context, AlarmRingingService.class)) {
            intent = new Intent(context, AlarmRingingService.class);
            intent.putExtra(AlarmRingingService.ARG_ALARM, alarm);
            intent.putExtra(AlarmRingingService.ARG_IS_PREVIEW, isPreview);
            intent.putExtra(AlarmRingingService.ARG_ALARM_TIME, alarmTime);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent);
            else
                context.startService(intent);
        } else {
            Log.d("onReceive", "Ignored some request to start service, since it is running");
            Toast.makeText(context, R.string.cannot_start_new_alarm_old_running, Toast.LENGTH_LONG).show();
        }
    }
}
