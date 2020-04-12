package kilanny.muslimalarm.fragments.alarmedit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import kilanny.muslimalarm.AlarmRingBroadcastReceiver;
import kilanny.muslimalarm.R;
import kilanny.muslimalarm.activities.BarcodeStopConfigActivity;
import kilanny.muslimalarm.activities.TwoNumbersConfigActivity;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.Tune;
import kilanny.muslimalarm.fragments.AlarmStopMethodFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StopMethodEditAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StopMethodEditAlarmFragment extends EditAlarmFragment
        implements AlarmStopMethodFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CODE_CONFIG_SHAKE = 1;
    private static final int REQUEST_CODE_CONFIG_MATH = 2;
    private static final int REQUEST_CODE_CONFIG_BARCODE = 3;

    private static final String STATE_ALARM_TYPE = "beforePreviewDismissAlarmType";
    private static final String STATE_ALARM_TUNE = "beforePreviewDismissTune";
    private static final String STATE_ALARM_SOUND_LEVEL = "beforePreviewDismissSoundLevel";
    private static final String STATE_IS_PREVIEW = "mIsPreview";

    private AlarmStopMethodFragment[] fragments;
    private boolean mIsPreview = false;

    public StopMethodEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StopMethodEditAlarmFragment.
     */
    public static StopMethodEditAlarmFragment newInstance(Alarm alarm) {
        StopMethodEditAlarmFragment fragment = new StopMethodEditAlarmFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ALARM, alarm);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlarm = getArguments().getParcelable(ARG_ALARM);
        }
        if (savedInstanceState != null) {
            mIsPreview = savedInstanceState.getBoolean(STATE_IS_PREVIEW);
            mAlarm = savedInstanceState.getParcelable(ARG_ALARM);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_PREVIEW, mIsPreview);
        outState.putParcelable(ARG_ALARM, mAlarm);
        super.onSaveInstanceState(outState);
    }

    private static boolean isShakeAvailable(Context context) {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager == null ||
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            Toast.makeText(context, R.string.your_device_not_support_feature,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stop_method_edit_alarm, container, false);
        FragmentManager fragmentManager = getChildFragmentManager();
        String[] t = {
                getString(R.string.Default),
                getString(R.string.phone_shaking),
                getString(R.string.barcode),
                getString(R.string.math_problems)
        };
        fragments = new AlarmStopMethodFragment[4];
        /*<div>Icons made by <a href="https://www.flaticon.com/authors/freepik"
        title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/"
        title="Flaticon">www.flaticon.com</a> is licensed by
        <a href="http://creativecommons.org/licenses/by/3.0/"
        title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>*/
        fragments[0] = AlarmStopMethodFragment.newInstance(R.drawable.alarm, t[0],
                mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_DEFAULT);
        fragments[1] = AlarmStopMethodFragment.newInstance(R.drawable.shake, t[1],
                mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_SHAKE);
        fragments[2] = AlarmStopMethodFragment.newInstance(R.drawable.math, t[3],
                mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_MATH);
        fragments[3] = AlarmStopMethodFragment.newInstance(R.drawable.barcode, t[2],
                mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE);
        fragmentManager.beginTransaction()
                .replace(R.id.topLeftLayout, fragments[0])
                .replace(R.id.topRightLayout, fragments[1])
                .replace(R.id.bottomLeftLayout, fragments[3])
                .replace(R.id.bottomRightLayout, fragments[2])
                .commitAllowingStateLoss();
        return root;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        mListener.onNext(mAlarm);
        callback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        callback.complete();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CONFIG_SHAKE:
                if (resultCode == TwoNumbersConfigActivity.RESULT_CODE_OK && data != null) {
                    mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_SHAKE;
                    mAlarm.dismissAlarmTypeData1 = data.getIntExtra(
                            TwoNumbersConfigActivity.RESULT_NUM1, 0);
                    mAlarm.dismissAlarmTypeData2 = data.getIntExtra(
                            TwoNumbersConfigActivity.RESULT_NUM2, 0);
                    for (int i = 0; i < fragments.length; ++i)
                        fragments[i].setSelected(i == 1);
                }
                break;
            case REQUEST_CODE_CONFIG_MATH:
                if (resultCode == TwoNumbersConfigActivity.RESULT_CODE_OK && data != null) {
                    mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_MATH;
                    mAlarm.dismissAlarmTypeData1 = data.getIntExtra(
                            TwoNumbersConfigActivity.RESULT_NUM1, 0);
                    mAlarm.dismissAlarmTypeData2 = data.getIntExtra(
                            TwoNumbersConfigActivity.RESULT_NUM2, 0);
                    for (int i = 0; i < fragments.length; ++i)
                        fragments[i].setSelected(i == 2);
                }
                break;
            case REQUEST_CODE_CONFIG_BARCODE:
                if (resultCode == BarcodeStopConfigActivity.RESULT_CODE_OK && data != null) {
                    mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_BARCODE;
                    mAlarm.dismissAlarmTypeData1 = null;
                    mAlarm.dismissAlarmTypeData2 = null;
                    mAlarm.dismissAlarmBarcodeId = data.getIntExtra(
                            BarcodeStopConfigActivity.RESULT_BARCODE_ID, 0);
                    if (mIsPreview && getContext() != null) {
                        // show preview using the selected barcode
                        // then restore values back after preview
                        Intent intent = new Intent(getContext(), AlarmRingBroadcastReceiver.class);
                        intent.putExtra(AlarmRingBroadcastReceiver.ARG_IS_PREVIEW, true);
                        intent.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM, mAlarm);
                        intent.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM_TIME, 0);
                        getContext().sendBroadcast(intent);
                    } else {
                        for (int i = 0; i < fragments.length; ++i)
                            fragments[i].setSelected(i == 3);
                    }
                }
                break;
        }
        mIsPreview = false;
    }

    @Override
    public void onFragmentPreviewClick(AlarmStopMethodFragment sender) {
        final Context context = getContext();
        if (context == null) return;
        final Alarm alarm = mAlarm.copy();
        if (alarm.alarmTune == 0)
            alarm.alarmTune = Tune.getTunes(0)[0].id;
        if (alarm.soundLevel == 0)
            alarm.soundLevel = 75;
        mIsPreview = true;
        switch (sender.getImageDrawableId()) {
            case R.drawable.alarm:
                alarm.dismissAlarmType = Alarm.DISMISS_ALARM_DEFAULT;
                break;
            case R.drawable.shake:
                if (!isShakeAvailable(context))
                    return;
                alarm.dismissAlarmType = Alarm.DISMISS_ALARM_SHAKE;
                alarm.dismissAlarmTypeData1 = 15;
                alarm.dismissAlarmTypeData2 = 2;
                break;
            case R.drawable.barcode:
                if (alarm.dismissAlarmType != Alarm.DISMISS_ALARM_BARCODE) {
                    new AlertDialog.Builder(context)
                            .setTitle(getString(R.string.preview_alarm))
                            .setMessage(getString(R.string.to_preview_barcode_use))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok, null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    Intent intent = new Intent(context,
                                            BarcodeStopConfigActivity.class);
                                    if (alarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE)
                                        intent.putExtra(BarcodeStopConfigActivity.ARG_SELECTED_BARCODE_ID,
                                                alarm.dismissAlarmBarcodeId.intValue());
                                    startActivityForResult(intent, REQUEST_CODE_CONFIG_BARCODE);
                                }
                            })
                            .show();
                    return;
                }
                break;
            case R.drawable.math:
                alarm.dismissAlarmType = Alarm.DISMISS_ALARM_MATH;
                alarm.dismissAlarmTypeData1 = 3;
                alarm.dismissAlarmTypeData2 = 1;
                break;
        }
        Intent intent = new Intent(getContext(), AlarmRingBroadcastReceiver.class);
        intent.putExtra(AlarmRingBroadcastReceiver.ARG_IS_PREVIEW, true);
        intent.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM, alarm);
        intent.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM_TIME, 0);
        getContext().sendBroadcast(intent);
    }

    @Override
    public void onFragmentLogoButtonClick(AlarmStopMethodFragment sender) {
        Intent intent;
        int num1, num2;
        final Context context = getContext();
        if (context == null) return;
        mIsPreview = false;
        switch (sender.getImageDrawableId()) {
            case R.drawable.alarm:
                mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_DEFAULT;
                mAlarm.dismissAlarmTypeData1 = null;
                mAlarm.dismissAlarmTypeData2 = null;
                for (AlarmStopMethodFragment fragment : fragments)
                    fragment.setSelected(fragment == sender);
                break;
            case R.drawable.shake:
                if (!isShakeAvailable(context))
                    return;
                //mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_SHAKE;
                intent = new Intent(context, TwoNumbersConfigActivity.class);
                intent.putExtra(TwoNumbersConfigActivity.ARG_ACTIVITY_TITLE,
                        getString(R.string.config_shakes));
                intent.putExtra(TwoNumbersConfigActivity.ARG_TITLE1,
                        getString(R.string.num_shakes));
                intent.putExtra(TwoNumbersConfigActivity.ARG_TITLE2,
                        getString(R.string.difficulty_shake));
                //intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_LABELS,
                //        getResources().getStringArray(R.array.math_level_example));
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_FROM, 10);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_TO, 1000);
                if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_SHAKE) {
                    num1 = mAlarm.dismissAlarmTypeData1;
                    num2 = mAlarm.dismissAlarmTypeData2;
                } else {
                    num1 = 15;
                    num2 = 2;
                }
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_VALUE, num1);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_FROM, 1);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_TO, 6);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_VALUE, num2);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_SEEK_TICKS_COUNT, 6);
                startActivityForResult(intent, REQUEST_CODE_CONFIG_SHAKE);
                break;
            case R.drawable.barcode:
                intent = new Intent(context, BarcodeStopConfigActivity.class);
                if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE)
                    intent.putExtra(BarcodeStopConfigActivity.ARG_SELECTED_BARCODE_ID,
                            mAlarm.dismissAlarmBarcodeId.intValue());
                startActivityForResult(intent, REQUEST_CODE_CONFIG_BARCODE);
                break;
            case R.drawable.math:
                intent = new Intent(context, TwoNumbersConfigActivity.class);
                intent.putExtra(TwoNumbersConfigActivity.ARG_ACTIVITY_TITLE,
                        getString(R.string.config_math_problems));
                intent.putExtra(TwoNumbersConfigActivity.ARG_TITLE1,
                        getString(R.string.num_problems));
                intent.putExtra(TwoNumbersConfigActivity.ARG_TITLE2,
                        getString(R.string.problem_difficulty));
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_LABELS,
                        getResources().getStringArray(R.array.math_level_example));
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_FROM, 1);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_TO, 500);
                if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_MATH) {
                    num1 = mAlarm.dismissAlarmTypeData1;
                    num2 = mAlarm.dismissAlarmTypeData2;
                } else {
                    num1 = 3;
                    num2 = 2;
                }
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM1_VALUE, num1);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_FROM, 1);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_TO, 6);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_VALUE, num2);
                intent.putExtra(TwoNumbersConfigActivity.ARG_NUM2_SEEK_TICKS_COUNT, 6);
                startActivityForResult(intent, REQUEST_CODE_CONFIG_MATH);
                break;
        }
    }
}
