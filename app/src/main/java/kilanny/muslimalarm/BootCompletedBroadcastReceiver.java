package kilanny.muslimalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.util.Utils;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pendingResult = goAsync();
        Task asyncTask = new Task(pendingResult, context, intent);
        asyncTask.execute();
    }

    private static class Task extends AsyncTask<Void, Void, Void> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final WeakReference<Context> mContext;

        private Task(PendingResult pendingResult, Context context, Intent intent) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            mContext = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Alarm[] alarms = AppDb.getInstance(mContext.get()).alarmDao().getAllEnabled();
            Utils.scheduleAndDeletePrevious(mContext.get(), alarms);
//            StringBuilder sb = new StringBuilder();
//            sb.append("Action: " + intent.getAction() + "\n");
//            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME) + "\n");
//            String log = sb.toString();
//            Log.d("BootTask", log);
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }
}
