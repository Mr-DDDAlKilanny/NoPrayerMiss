package kilanny.muslimalarm.fragments.alarmedit;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.material.snackbar.Snackbar;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectSoundVibrationLabelEditAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectSoundVibrationLabelEditAlarmFragment extends EditAlarmFragment
        implements OnSeekChangeListener {

    private View mView;
    private boolean isSoundLevelNoAccess;

    public SelectSoundVibrationLabelEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectSoundVibrationLabelEditAlarmFragment.
     */
    public static SelectSoundVibrationLabelEditAlarmFragment newInstance(Alarm alarm) {
        SelectSoundVibrationLabelEditAlarmFragment fragment = new SelectSoundVibrationLabelEditAlarmFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ALARM, alarm);
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestSoundLevelAccess() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlarm = getArguments().getParcelable(ARG_ALARM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_select_sound_vibration_label_edit_alarm, container,
                false);
        IndicatorSeekBar soundSeekBar = mView.findViewById(R.id.seekBarSoundLevel);
        if (mAlarm.soundLevel < 1)
            mAlarm.soundLevel = 80;
        soundSeekBar.setProgress(mAlarm.soundLevel);
        soundSeekBar.setOnSeekChangeListener(this);

        SwitchCompat switchCompat = mView.findViewById(R.id.switchVibration);
        switchCompat.setChecked(mAlarm.vibrationEnabled);
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> mAlarm.vibrationEnabled = b);
        mView.findViewById(R.id.btnSetAlarmLabel).setOnClickListener(view -> {
            final AppCompatEditText input = new AppCompatEditText(view.getContext());
            // Specify the type of input expected
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(mAlarm.alarmLabel);
            input.setHint(R.string.write_title_of_alarm);
            new AlertDialog.Builder(view.getContext())
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
        });
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null &&
                    !notificationManager.isNotificationPolicyAccessGranted()) {
                isSoundLevelNoAccess = true;
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                final String name = "__noAgainSoundPermission";
                if (pref.getBoolean(name, false))
                    return;
                new AlertDialog.Builder(context)
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

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        mListener.onNext(mAlarm);
        callback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        throw new RuntimeException();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {
    }

    @Override
    public void onError(@NonNull VerificationError error) {
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
        if (isSoundLevelNoAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Snackbar.make(mView, R.string.changes_sound_no_affect, Snackbar.LENGTH_LONG)
                    .setAction(R.string.provide_permission, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestSoundLevelAccess();
                        }
                    }).show();
        }
    }
}
