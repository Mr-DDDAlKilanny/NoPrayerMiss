package kilanny.muslimalarm.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.fragments.AlarmStopMethodFragment;

public class ChooseAlarmStopMethodActivity extends AppCompatActivity
        implements AlarmStopMethodFragment.OnFragmentInteractionListener {

    public static final String ARG_ALARM = "alarm";
    public static final int RESULT_CODE_OK = 1;
    public static final int RESULT_CODE_CANCEL = 0;
    public static final String RESULT_ALARM = "alarm";

    private static final String STATE_ALARM_TYPE = "beforePreviewDismissAlarmType";
    private static final String STATE_IS_PREVIEW = "mIsPreview";

    private static final int REQUEST_CODE_PREVIEW_ALARM = 0;
    private static final int REQUEST_CODE_CONFIG_SHAKE = 1;
    private static final int REQUEST_CODE_CONFIG_MATH = 2;
    private static final int REQUEST_CODE_CONFIG_BARCODE = 3;

    private AlarmStopMethodFragment[] fragments;
    private Alarm mAlarm;
    private boolean mIsPreview = false;
    private int beforePreviewDismissAlarmType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_alarm_stop_method);

        mAlarm = getIntent().getParcelableExtra(ARG_ALARM);
        if (mAlarm == null && savedInstanceState != null)
            mAlarm = savedInstanceState.getParcelable(ARG_ALARM);
        if (mAlarm == null)
            throw new RuntimeException("AlarmId must be passed to this activity");

        if (savedInstanceState != null) {
            mIsPreview = savedInstanceState.getBoolean(STATE_IS_PREVIEW);
            beforePreviewDismissAlarmType = savedInstanceState.getInt(STATE_ALARM_TYPE);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
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
        fragments[1] = AlarmStopMethodFragment.newInstance(R.drawable.cell_phone_vibration, t[1],
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
                .commit();
        setTitle(R.string.alarm_stop_method);

        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // a fragment must be selected here.
                // TODO: maybe some fragment not selected !?
                Intent intent = new Intent();
                intent.putExtra(RESULT_ALARM, mAlarm);
                setResult(RESULT_CODE_OK, intent);
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_ALARM_TYPE, beforePreviewDismissAlarmType);
        outState.putBoolean(STATE_IS_PREVIEW, mIsPreview);
        outState.putParcelable(ARG_ALARM, mAlarm);
        super.onSaveInstanceState(outState);
    }

    private boolean isShakeAvailable() {
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager == null ||
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            Toast.makeText(this, R.string.your_device_not_support_feature,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onFragmentPreviewClick(AlarmStopMethodFragment sender) {
        beforePreviewDismissAlarmType = mAlarm.dismissAlarmType;
        mIsPreview = true;
        switch (sender.getImageDrawableId()) {
            case R.drawable.alarm:
                mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_DEFAULT;
                break;
            case R.drawable.cell_phone_vibration:
                if (!isShakeAvailable())
                    return;
                mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_SHAKE;
                mAlarm.dismissAlarmTypeData1 = 15;
                mAlarm.dismissAlarmTypeData2 = 2;
                break;
            case R.drawable.barcode:
                if (mAlarm.dismissAlarmType != Alarm.DISMISS_ALARM_BARCODE) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.preview_alarm))
                            .setMessage(getString(R.string.to_preview_barcode_use))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok, null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    Intent intent = new Intent(ChooseAlarmStopMethodActivity.this,
                                            BarcodeStopConfigActivity.class);
                                    if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE)
                                        intent.putExtra(BarcodeStopConfigActivity.ARG_SELECTED_BARCODE_ID,
                                                mAlarm.dismissAlarmBarcodeId.intValue());
                                    startActivityForResult(intent, REQUEST_CODE_CONFIG_BARCODE);
                                }
                            })
                            .show();
                    return;
                }
                break;
            case R.drawable.math:
                mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_MATH;
                mAlarm.dismissAlarmTypeData1 = 3;
                mAlarm.dismissAlarmTypeData2 = 1;
                break;
        }
        Intent intent = new Intent(this, ShowAlarmActivity.class);
        intent.putExtra(ShowAlarmActivity.ARG_IS_PREVIEW, true);
        intent.putExtra(ShowAlarmActivity.ARG_ALARM, mAlarm);
        startActivityForResult(intent, REQUEST_CODE_PREVIEW_ALARM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW_ALARM:
                mAlarm.dismissAlarmType = beforePreviewDismissAlarmType;
                if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE
                        || mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_DEFAULT) {
                    mAlarm.dismissAlarmTypeData1 = null;
                    mAlarm.dismissAlarmTypeData2 = null;
                }
                if (mAlarm.dismissAlarmType != Alarm.DISMISS_ALARM_BARCODE)
                    mAlarm.dismissAlarmBarcodeId = null;
                break;
            case REQUEST_CODE_CONFIG_SHAKE:
                if (resultCode == TwoNumbersConfigActivity.RESULT_CODE_OK) {
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
                if (resultCode == TwoNumbersConfigActivity.RESULT_CODE_OK) {
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
                if (resultCode == BarcodeStopConfigActivity.RESULT_CODE_OK) {
                    mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_BARCODE;
                    mAlarm.dismissAlarmTypeData1 = null;
                    mAlarm.dismissAlarmTypeData2 = null;
                    mAlarm.dismissAlarmBarcodeId = data.getIntExtra(
                            BarcodeStopConfigActivity.RESULT_BARCODE_ID, 0);
                    if (mIsPreview) {
                        // show preview using the selected barcode
                        // then restore values back after preview
                        Intent intent = new Intent(this, ShowAlarmActivity.class);
                        intent.putExtra(ShowAlarmActivity.ARG_IS_PREVIEW, true);
                        intent.putExtra(ShowAlarmActivity.ARG_ALARM, mAlarm);
                        startActivityForResult(intent, REQUEST_CODE_PREVIEW_ALARM);
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
    public void onFragmentLogoButtonClick(AlarmStopMethodFragment sender) {
        Intent intent;
        int num1, num2;
        mIsPreview = false;
        switch (sender.getImageDrawableId()) {
            case R.drawable.alarm:
                mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_DEFAULT;
                mAlarm.dismissAlarmTypeData1 = null;
                mAlarm.dismissAlarmTypeData2 = null;
                for (AlarmStopMethodFragment fragment : fragments)
                    fragment.setSelected(fragment == sender);
                break;
            case R.drawable.cell_phone_vibration:
                if (!isShakeAvailable())
                    return;
                //mAlarm.dismissAlarmType = Alarm.DISMISS_ALARM_SHAKE;
                intent = new Intent(this, TwoNumbersConfigActivity.class);
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
                intent = new Intent(this, BarcodeStopConfigActivity.class);
                if (mAlarm.dismissAlarmType == Alarm.DISMISS_ALARM_BARCODE)
                    intent.putExtra(BarcodeStopConfigActivity.ARG_SELECTED_BARCODE_ID,
                            mAlarm.dismissAlarmBarcodeId.intValue());
                startActivityForResult(intent, REQUEST_CODE_CONFIG_BARCODE);
                break;
            case R.drawable.math:
                intent = new Intent(this, TwoNumbersConfigActivity.class);
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
