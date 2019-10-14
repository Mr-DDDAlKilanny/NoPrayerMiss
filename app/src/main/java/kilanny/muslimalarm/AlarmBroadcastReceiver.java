package kilanny.muslimalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import kilanny.muslimalarm.activities.ShowAlarmActivity;
import kilanny.muslimalarm.data.Alarm;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_ALARM = "kilanny.muslimalarm.ACTION_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_ALARM.equals(intent.getAction())) {
            Alarm alarm = intent.getParcelableExtra(ShowAlarmActivity.ARG_ALARM);
            //Toast.makeText(context, ACTION_ALARM, Toast.LENGTH_SHORT).show();
            Intent showActivityIntent = new Intent(context, ShowAlarmActivity.class);
            showActivityIntent.putExtra(ShowAlarmActivity.ARG_ALARM, alarm);
            showActivityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showActivityIntent);
        }
    }
}
