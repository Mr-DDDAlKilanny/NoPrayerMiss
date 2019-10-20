package kilanny.muslimalarm.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AlarmDao;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.Weekday;
import kilanny.muslimalarm.fragments.alarmring.AlarmRingingFragment;
import kilanny.muslimalarm.fragments.alarmring.BarcodeAlarmFragment;
import kilanny.muslimalarm.fragments.alarmring.MathAlarmFragment;
import kilanny.muslimalarm.fragments.alarmring.ShakeAlarmFragment;
import kilanny.muslimalarm.fragments.alarmring.ShowAlarmFragment;
import kilanny.muslimalarm.util.Utils;

public class ShowAlarmActivity extends AppCompatActivity implements
        ShowAlarmFragment.FragmentInteractionListener,
        AlarmRingingFragment.FragmentInteractionListener {

    public static final String ARG_IS_PREVIEW = "isPreview";
    public static final String ARG_ALARM = "alarm";
    private static boolean isVisible = false;

    private boolean mIsPreview, mIsVibrating;
    private Alarm mAlarm;
    private ProgressBar mProgressBar;
    private Timer mDismissTimer;
    private MediaPlayer mediaPlayer;
    private Vibrator mVibrator;
    private int mOldUserSoundVolume;
    private AudioManager mAudioManager;
    private String mBarcode;
    private Date fetchBarcodeFromDbDate;
    private int currentAlarmFTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isVisible) {
            finish();
            return;
        }
        // Hiding the title bar has to happen before the view is created
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_alarm);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mIsPreview = getIntent().getBooleanExtra(ARG_IS_PREVIEW, false);

        mAlarm = getIntent().getParcelableExtra(ARG_ALARM);
        if (mAlarm == null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String json = pref.getString("nextAlarmJson", null);
            currentAlarmFTime = pref.getInt("nextAlarmTime", 0);
            if (json != null) {
                try {
                    mAlarm = Alarm.fromJson(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (mAlarm == null)
                throw new RuntimeException("An alarm must be passed to this activity");
        }
        mProgressBar = findViewById(R.id.progrssBar);
        int selected = Integer.parseInt(mAlarm.alarmTune);
        mediaPlayer = MediaPlayer.create(this, Alarm.SOUNDS[selected]);
        mediaPlayer.setLooping(true);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOldUserSoundVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int level = (int) Math.round((mAlarm.soundLevel / 100.0) * maxVolume);
        try {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        Button previewBtn = findViewById(R.id.btnExitPreview);
        previewBtn.setVisibility(mIsPreview ? View.VISIBLE : View.GONE);
        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDismissed(false);
            }
        });

        if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE) {
            fetchBarcodeFromDbDate = new Date();
            Utils.runInBackground(new Function<Context, Void>() {
                @Override
                public Void apply(Context input) {
                    mBarcode = AppDb.getInstance(input).barcodeDao()
                            .getById(mAlarm.dismissAlarmBarcodeId).getCode();
                    return null;
                }
            }, null, this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
        if (!mediaPlayer.isPlaying())
            startRinging();
    }

    private void startRinging() {
        mProgressBar.setVisibility(View.GONE);
        boolean hasSnooze = mAlarm.snoozeMins > 0 &&
                (mAlarm.snoozeCount == 0 || mAlarm.snoozedCount < mAlarm.snoozeCount);
        AlarmRingingFragment fragment = AlarmRingingFragment.newInstance(hasSnooze);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();

        mediaPlayer.start();

        if (mAlarm.vibrationEnabled && mVibrator != null) {
            Log.v("showAlarm", "Vibrating...");
            //TODO: permission for vibration; not working on redmi 4a
            mVibrator.vibrate(
                    new long[]{5000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}, 0);
            mIsVibrating = true;
        }
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
        mProgressBar.setProgress(mProgressBar.getMax());
    }

    private void stopRinging() {
        if (mIsVibrating) {
            mVibrator.cancel();
            mIsVibrating = false;
        }
        if (mediaPlayer.isPlaying()) { // stop preview while solving math problems
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }

    @Override
    public void onDismissed(final boolean isDone) {
        stopRinging();
        isVisible = false;
        try {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOldUserSoundVolume, 0);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        if (mDismissTimer != null) {
            mDismissTimer.cancel();
            mDismissTimer = null;
        }
        mediaPlayer.release();

        Utils.runInBackground(new Function<Pair<Context, Alarm>, Pair<Context, Alarm[]>>() {
            @Override
            public Pair<Context, Alarm[]> apply(Pair<Context, Alarm> input) {
                AlarmDao alarmDao = AppDb.getInstance(input.first).alarmDao();
                input.second.skippedTimeFlag = 0;
                input.second.skippedAlarmTime = null;
                if (isDone) {
                    input.second.snoozedCount = 0;
                    input.second.snoozedToTime = null;

                    if (input.second.weekDayFlags == Weekday.NO_REPEAT) {
                        input.second.oneTimeLeftAlarmsTimeFlags &= ~currentAlarmFTime;
                        if (input.second.oneTimeLeftAlarmsTimeFlags == 0) { // done!
                            input.second.enabled = false;
                        }
                    }
                }
                alarmDao.update(input.second);
                return new Pair<>(input.first, alarmDao.getAll());
            }
        }, new Function<Pair<Context, Alarm[]>, Void>() {
            @Override
            public Void apply(Pair<Context, Alarm[]> input) {
                Utils.scheduleAndDeletePrevious(input.first, input.second);
                return null;
            }
        }, new Pair<>(getApplicationContext(), mAlarm));

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

        stopRinging();
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
                .commit();

        mProgressBar.setProgress(mProgressBar.getMax());
        mProgressBar.setVisibility(View.VISIBLE);
        mDismissTimer = new Timer();
        mDismissTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mProgressBar.getProgress() > 0)
                    mProgressBar.setProgress(mProgressBar.getProgress() - 1);
                else {
                    mDismissTimer.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startRinging();
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    @Override
    public void onSnoozeClick() {
        mAlarm.snoozedToTime = System.currentTimeMillis() + mAlarm.snoozeMins * 60000;
        ++mAlarm.snoozedCount;

        Utils.runInBackground(new Function<Pair<Context, Alarm>, Pair<Context, Alarm[]>>() {
            @Override
            public Pair<Context, Alarm[]> apply(Pair<Context, Alarm> input) {
                AlarmDao alarmDao = AppDb.getInstance(input.first).alarmDao();
                alarmDao.update(input.second);
                return new Pair<>(input.first, alarmDao.getAll());
            }
        }, new Function<Pair<Context, Alarm[]>, Void>() {
            @Override
            public Void apply(Pair<Context, Alarm[]> input) {
                Utils.scheduleAndDeletePrevious(input.first, input.second);
                return null;
            }
        }, new Pair<>(getApplicationContext(), mAlarm));

        onDismissed(false);
    }
}
