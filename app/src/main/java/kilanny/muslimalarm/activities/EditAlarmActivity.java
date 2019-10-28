package kilanny.muslimalarm.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.snackbar.Snackbar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.Tune;
import kilanny.muslimalarm.data.Weekday;
import kilanny.muslimalarm.dialogs.NumberPickerDialog;

public class EditAlarmActivity extends AppCompatActivity
        implements NumberPicker.OnValueChangeListener, View.OnClickListener, OnSeekChangeListener {

    public static final String ARG_ALARM = "alarm";
    public static final String RESULT_ALARM = "alarm";
    public static final int RESULT_CODE_OK = 1;
    public static final int RESULT_CODE_CANCEL = 0;

    private static final int REQUEST_CHOOSE_ALARM_STOP_METHOD = 1;
    private static final int REQUEST_CODE_CONFIG_SNOOZE = 2;
    private static final int REQUEST_CODE_CONFIG_RINGTUNE = 3;

    private Alarm mAlarm;
    private AppCompatImageView imgSelectedAlarmStopMethod;
    private MediaPlayer mediaPlayer;
    private boolean hasChangedTune;
    private boolean isSoundLevelNoAccess;

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null &&
                    !notificationManager.isNotificationPolicyAccessGranted()) {
                isSoundLevelNoAccess = true;
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                final String name = "__noAgainSoundPermission";
                if (pref.getBoolean(name, false))
                    return;
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_required)
                        .setMessage(R.string.need_sound_permission)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestSoundLevelAccess();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setNeutralButton(R.string.no_not_ask, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pref.edit().putBoolean(name, true).apply();
                            }
                        })
                        .show();
            }
        }
    }

    private void requestSoundLevelAccess() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);

        mAlarm = getIntent().getParcelableExtra(ARG_ALARM);
        if (mAlarm == null) {
            mAlarm = new Alarm();
            mAlarm.timeAlarmDiffMinutes = 5;
            mAlarm.snoozeMins = 5;
            mAlarm.snoozeCount = 3;
            mAlarm.enabled = true;
            setTitle(R.string.add_alarm);
        } else {
            setTitle(R.string.edit_alarm);
        }
        RadioButton radioButton = findViewById(mAlarm.timeAlarmDiffMinutes < 0 ?
                R.id.radioBefore : R.id.radioAfter);
        radioButton.setChecked(true);
        findViewById(R.id.repeatDaysLayout).setOnClickListener(this);
        findViewById(R.id.selectPrayerTimeLayout).setOnClickListener(this);
        findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: needs to be two pickers: hours & minutes
                NumberPickerDialog newFragment = new NumberPickerDialog(0, 600,
                        Math.abs(mAlarm.timeAlarmDiffMinutes), "", "");
                newFragment.setValueChangeListener(EditAlarmActivity.this);
                newFragment.show(getSupportFragmentManager(), "time picker");
            }
        });
        SwitchCompat switchCompat = findViewById(R.id.switchVibration);
        switchCompat.setChecked(mAlarm.vibrationEnabled);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mAlarm.vibrationEnabled = b;
            }
        });
        IndicatorSeekBar soundSeekBar = findViewById(R.id.seekBarSoundLevel);
        soundSeekBar.setProgress(mAlarm.soundLevel);
        soundSeekBar.setOnSeekChangeListener(this);
        findViewById(R.id.firstCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditAlarmActivity.this,
                        ChooseAlarmStopMethodActivity.class);
                intent.putExtra(ChooseAlarmStopMethodActivity.ARG_ALARM, mAlarm);
                startActivityForResult(intent, REQUEST_CHOOSE_ALARM_STOP_METHOD);
            }
        });
        findViewById(R.id.snoozeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snoozeLayoutClick();
            }
        });
        findViewById(R.id.alarmTuneLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(EditAlarmActivity.this,
                //        SelectRingtuneActivity.class);
                //startActivityForResult(intent, REQUEST_CODE_CONFIG_RINGTUNE);
                onSelectRingtune();
            }
        });
        findViewById(R.id.alarmLabelLayout).setOnClickListener(this);
        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate(view)) return;
                Intent res = new Intent();
                res.putExtra(RESULT_ALARM, mAlarm);
                setResult(RESULT_CODE_OK, res);
                finish();
            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CODE_CANCEL);
                finish();
            }
        });
        imgSelectedAlarmStopMethod = findViewById(R.id.imgSelectedAlarmStopMethod);
        updateSelectedAlarmStopMethodImage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMediaPlayer();
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void onSelectRingtune() {
        hasChangedTune = false;
        final Tune[] tunes = Tune.getTunes();
        final String[] names = new String[tunes.length];
        int selected = -1;
        for (int i = 0; i < names.length; ++i) {
            names[i] = getString(tunes[i].nameResId);
            if (tunes[i].rawResId == mAlarm.alarmTune)
                selected = i;
        }
        final int prevSelected = selected;
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_alarm_tune)
                .setSingleChoiceItems(names, selected, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopMediaPlayer();
                        mediaPlayer = MediaPlayer.create(EditAlarmActivity.this,
                                tunes[i].rawResId);
                        mediaPlayer.start();
                        mAlarm.alarmTune = tunes[i].rawResId;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mAlarm.alarmTune == 0) {
                            new AlertDialog.Builder(EditAlarmActivity.this)
                                    .setTitle(R.string.select_alarm_tune)
                                    .setMessage(R.string.err_tune_not_selected)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            onSelectRingtune();
                                        }
                                    })
                                    .show();
                        } else
                            hasChangedTune = true;
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        stopMediaPlayer();
                        if (!hasChangedTune)
                            mAlarm.alarmTune = prevSelected == -1 ? 0 : tunes[prevSelected].rawResId;
                    }
                })
                .show();
    }

    private boolean validate(View view) {
        if (mAlarm.timeFlags == 0) {
            Snackbar.make(view, R.string.err_select_prayer, Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (mAlarm.alarmTune == 0) {
            Snackbar.make(view, R.string.must_select_tune, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void snoozeLayoutClick() {
        Intent intent = new Intent(EditAlarmActivity.this,
                TwoNumbersConfigActivity.class);
        intent.putExtra(TwoNumbersConfigActivity.ARG_ACTIVITY_TITLE,
                getString(R.string.config_snooze));
        intent.putExtra(TwoNumbersConfigActivity.ARG_TITLE1,
                getString(R.string.snooze_period_minutes));
        intent.putExtra(TwoNumbersConfigActivity.ARG_TITLE2,
                getString(R.string.max_snooze_count));
        //intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_LABELS,
        //        getResources().getStringArray(R.array.math_level_example));
        intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_FROM, 0);
        intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_TO, 45);
        intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_VALUE, mAlarm.snoozeMins);
        intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_FROM, 0);
        intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_TO, 30);
        intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_VALUE, mAlarm.snoozeCount);
        intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_SEEK_TICKS_COUNT, 6);
        startActivityForResult(intent, REQUEST_CODE_CONFIG_SNOOZE);
    }

    private void updateSelectedAlarmStopMethodImage() {
        int res = R.drawable.clock;
        switch (mAlarm.dismissAlarmType) {
            case Alarm.DISMISS_ALARM_SHAKE:
                res = R.drawable.shake;
                break;
            case Alarm.DISMISS_ALARM_MATH:
                res = R.drawable.math;
                break;
            case Alarm.DISMISS_ALARM_BARCODE:
                res = R.drawable.barcode;
                break;
        }
        imgSelectedAlarmStopMethod.setImageResource(res);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHOOSE_ALARM_STOP_METHOD:
                if (resultCode == ChooseAlarmStopMethodActivity.RESULT_CODE_OK && data != null) {
                    mAlarm = data.getParcelableExtra(ChooseAlarmStopMethodActivity.RESULT_ALARM);
                    updateSelectedAlarmStopMethodImage();
                }
                break;
            case REQUEST_CODE_CONFIG_SNOOZE:
                if (resultCode == TwoNumbersConfigActivity.RESULT_CODE_OK && data != null) {
                    mAlarm.snoozeMins = data.getIntExtra(
                            TwoNumbersConfigActivity.RESULT_NUM1, 0);
                    mAlarm.snoozeCount = data.getIntExtra(
                            TwoNumbersConfigActivity.RESULT_NUM2, 0);
                }
                break;
            case REQUEST_CODE_CONFIG_RINGTUNE:
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        RadioGroup group = findViewById(R.id.radioGroupBeforeAfter);
        if (group.getCheckedRadioButtonId() == R.id.radioBefore)
            mAlarm.timeAlarmDiffMinutes = -numberPicker.getValue();
        else
            mAlarm.timeAlarmDiffMinutes = numberPicker.getValue();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.repeatDaysLayout:
                repeatDaysLayoutClicked();
                break;
            case R.id.selectPrayerTimeLayout:
                prayerTimeSelectClicked();
                break;
            case R.id.alarmLabelLayout:
                alarmLabelLayoutClick();
                break;
        }
    }

    private void alarmLabelLayoutClick() {
        final AppCompatEditText input = new AppCompatEditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(mAlarm.alarmLabel);
        input.setHint(R.string.write_title_of_alarm);
        new AlertDialog.Builder(EditAlarmActivity.this)
                .setTitle(getString(R.string.alarm_label))
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editable text = input.getText();
                        mAlarm.alarmLabel = text == null ? null : text.toString();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void prayerTimeSelectClicked() {
        final boolean[] sel = new boolean[6];
        for (int i = 0; i < 6; ++i) {
            sel[5 - i] = (mAlarm.timeFlags & (1 << i)) != 0;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.prayer_times)
                .setMultiChoiceItems(R.array.prayer_times, sel, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        sel[i] = b;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int count = 0;
                        for (int j = 0; j < 6; ++j)
                            count += (sel[j] ? 1 : 0);
                        if (count == 0) {
                            new AlertDialog.Builder(EditAlarmActivity.this)
                                    .setTitle(R.string.repeat_days)
                                    .setMessage(getString(R.string.err_select_prayer))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            prayerTimeSelectClicked();
                                        }
                                    })
                                    .show();
                        } else {
                            mAlarm.timeFlags = 0;
                            for (int j = 0; j < 6; ++j) {
                                mAlarm.timeFlags <<= 1;
                                mAlarm.timeFlags |= (sel[j] ? 1 : 0);
                            }
                            Log.v("SelectTimes", "selected: " + mAlarm.timeFlags);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void repeatDaysLayoutClicked() {
        final boolean[] sel = new boolean[8];
        //Arrays.fill(sel, false);
        if (mAlarm.weekDayFlags == Weekday.NO_REPEAT)
            sel[0] = true;
        else {
            for (int i = 0; i < 8; ++i) {
                sel[7 - i] = (mAlarm.weekDayFlags & (1 << i)) != 0;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.repeat_days)
                .setMultiChoiceItems(R.array.repeat_days, sel, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        //Log.v("SelectDay", "Item: " + i + ", state: " + b);
                        sel[i] = b;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int count = 0;
                        for (int j = 1; j < 8; ++j)
                            count += (sel[j] ? 1 : 0);
                        if (sel[0] && count > 0) {
                            new AlertDialog.Builder(EditAlarmActivity.this)
                                    .setTitle(R.string.repeat_days)
                                    .setMessage(getString(R.string.err_choose_one_time))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            repeatDaysLayoutClicked();
                                        }
                                    })
                                    .show();
                        } else if (!sel[0] && count == 0) {
                            new AlertDialog.Builder(EditAlarmActivity.this)
                                    .setTitle(R.string.repeat_days)
                                    .setMessage(getString(R.string.must_select_one_day))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            repeatDaysLayoutClicked();
                                        }
                                    })
                                    .show();
                        } else if (sel[0]) {
                            mAlarm.weekDayFlags = 0;
                        } else {
                            mAlarm.weekDayFlags = 0;
                            for (int j = 1; j < 8; ++j) {
                                mAlarm.weekDayFlags <<= 1;
                                mAlarm.weekDayFlags |= (sel[j] ? 1 : 0);
                            }
                            Log.v("SelectDays", "selected: " + mAlarm.weekDayFlags);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onSeeking(SeekParams seekParams) {
        if (seekParams.fromUser) {
            mAlarm.soundLevel = seekParams.progress;
        }
    }

    @Override
    public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
        if (isSoundLevelNoAccess) {
            Snackbar.make(imgSelectedAlarmStopMethod, R.string.changes_sound_no_affect, Snackbar.LENGTH_LONG)
                    .setAction(R.string.provide_permission, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestSoundLevelAccess();
                        }
                    }).show();
        }
    }
}
