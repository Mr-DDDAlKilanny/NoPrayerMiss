package kilanny.muslimalarm.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import java.lang.reflect.Method;
import java.util.Date;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.fragments.alarmring.AlarmRingingFragment;
import kilanny.muslimalarm.fragments.alarmring.BarcodeAlarmFragment;
import kilanny.muslimalarm.fragments.alarmring.MathAlarmFragment;
import kilanny.muslimalarm.fragments.alarmring.ShakeAlarmFragment;
import kilanny.muslimalarm.fragments.alarmring.ShowAlarmFragment;
import kilanny.muslimalarm.services.AlarmRingingService;
import kilanny.muslimalarm.util.Utils;

public class ShowAlarmActivity extends AppCompatActivity implements
        ShowAlarmFragment.FragmentInteractionListener,
        AlarmRingingFragment.FragmentInteractionListener, ServiceConnection {

    public static final String ARG_IS_PREVIEW = "isPreview";
    public static final String ARG_ALARM = "alarm";
    public static final String ARG_ALARM_TIME = "alarmTime";
    private static final String ARG_BARCODE = "barcode";

    private boolean mIsPreview;
    private Alarm mAlarm;
    private ProgressBar mProgressBar;
    private String mBarcode;
    private Date fetchBarcodeFromDbDate;
    private int currentAlarmFTime;
    private AlarmRingingService.LocalBinder mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        // Hiding the title bar has to happen before the view is created
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_alarm);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState == null)
            mIsPreview = getIntent().getBooleanExtra(ARG_IS_PREVIEW, false);
        else
            mIsPreview = savedInstanceState.getBoolean(ARG_IS_PREVIEW);

        if (savedInstanceState == null) {
            mAlarm = getIntent().getParcelableExtra(ARG_ALARM);
            if (mAlarm == null)
                throw new RuntimeException("An alarm must be passed to this activity");
            currentAlarmFTime = getIntent().getIntExtra(ARG_ALARM_TIME, 0);
        } else {
            mAlarm = savedInstanceState.getParcelable(ARG_ALARM);
            currentAlarmFTime = savedInstanceState.getInt(ARG_ALARM_TIME);
        }
        mProgressBar = findViewById(R.id.progrssBar);
        mProgressBar.setMax(AlarmRingingService.MAX_SECONDS_ATTEMPT);

        Button previewBtn = findViewById(R.id.btnExitPreview);
        previewBtn.setVisibility(mIsPreview ? View.VISIBLE : View.GONE);
        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDismissed(false);
            }
        });

        if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE) {
            if (savedInstanceState == null) {
                fetchBarcodeFromDbDate = new Date();
                Utils.runInBackground(new Function<Context, Void>() {
                    @Override
                    public Void apply(Context input) {
                        mBarcode = AppDb.getInstance(input).barcodeDao()
                                .getById(mAlarm.dismissAlarmBarcodeId).getCode();
                        return null;
                    }
                }, null, this);
            } else {
                mBarcode = savedInstanceState.getString(ARG_BARCODE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(ARG_ALARM, mAlarm);
        outState.putBoolean(ARG_IS_PREVIEW, mIsPreview);
        outState.putInt(ARG_ALARM_TIME, currentAlarmFTime);
        outState.putString(ARG_BARCODE, mBarcode);
        //TODO: can this occur?
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, AlarmRingingService.class), this, BIND_ABOVE_CLIENT);
        cancelAttempt();
    }

    private void cancelAttempt() {
        mProgressBar.setVisibility(View.GONE);
        boolean hasSnooze = mAlarm.snoozeMins > 0 &&
                (mAlarm.snoozeCount == 0 || mAlarm.snoozedCount < mAlarm.snoozeCount);
        AlarmRingingFragment fragment = AlarmRingingFragment.newInstance(hasSnooze, mAlarm.alarmLabel);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBinder != null) {
            mBinder.getService().cancelAttemptingDismissAlarm();
        }
        unbindService(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
        } else {
            flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        }
        getWindow().addFlags(flags);
    }

    @Override
    public void onBackPressed() {
        if (mIsPreview) {
            onDismissed(false);
            //super.onBackPressed();
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //super.onWindowFocusChanged(hasFocus);
        try {
            if (!hasFocus) {
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method collapse;
                try {
                    collapse = statusbarManager.getMethod("collapse");
                } catch (NoSuchMethodException ex) {
                    collapse = statusbarManager.getMethod("collapsePanels");
                }
                collapse.setAccessible(true);
                collapse.invoke(getSystemService("statusbar"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //TODO: power button press not prevented
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_POWER == event.getKeyCode()) {
            event.startTracking(); // Needed to track long presses
            Log.d("onKeyDown", "Event hacked");
            return true;//If event is handled, falseif
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // Do something here...
            Log.d("onKeyLongPress", "Event hacked");
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            Log.d("dispatchKeyEvent", "Event hacked");
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onResetSleepTimeout() {
        mBinder.getService().resetCountDown();
        mProgressBar.setProgress(mProgressBar.getMax());
    }

    @Override
    public void onDismissed(boolean isDone) {
        mBinder.getService().onDismissed(isDone);
        finish();
    }

    @Override
    public void onExitClick() {
        if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_DEFAULT) {
            onDismissed(true);
            return;
        } else if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE && mBarcode == null) {
            // barcode is still loading in db in onCreate, wait
            long seconds = (System.currentTimeMillis() - fetchBarcodeFromDbDate.getTime()) / 1000;
            if (seconds > 10) {
                // timed out; some exception in db, dismiss alarm
                onDismissed(false);
            }
            return;
        }

        ShowAlarmFragment fragment = null;
        switch (mAlarm.dismissAlarmType) {
            case Alarm.DISMISS_ALARM_BARCODE:
                fragment = BarcodeAlarmFragment.newInstance(mBarcode);
                break;
            case Alarm.DISMISS_ALARM_SHAKE:
                fragment = ShakeAlarmFragment.newInstance(mAlarm.dismissAlarmTypeData1,
                        mAlarm.dismissAlarmTypeData2);
                break;
            case Alarm.DISMISS_ALARM_MATH:
                fragment = MathAlarmFragment.newInstance(mAlarm.dismissAlarmTypeData1,
                        mAlarm.dismissAlarmTypeData2);
                break;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();

        mProgressBar.setProgress(mProgressBar.getMax());
        mProgressBar.setVisibility(View.VISIBLE);
        mBinder.getService().onCountDownChanged = new Function<Integer, Void>() {
            @Override
            public Void apply(Integer input) {
                mProgressBar.setProgress(input);
                if (input == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cancelAttempt();
                        }
                    });
                }
                return null;
            }
        };
        mBinder.getService().onAttemptingDismissAlarm();
    }

    @Override
    public void onSnoozeClick() {
        mBinder.getService().onSnoozeAlarm();
        onDismissed(false);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d("onServiceConnected", componentName.getClassName());
        mBinder = (AlarmRingingService.LocalBinder) iBinder;
        mBinder.getService().cancelAttemptingDismissAlarm();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d("onServiceDisconnected", componentName.getClassName());
        mBinder = null;
    }
}
