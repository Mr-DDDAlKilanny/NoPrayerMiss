package kilanny.muslimalarm.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.stepstone.stepper.StepperLayout;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.adapters.Edit5AlarmStepperAdapter;
import kilanny.muslimalarm.adapters.EditAlarmStepperAdapter;
import kilanny.muslimalarm.adapters.EditQiyamAlarmStepperAdapter;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.fragments.AlarmStopMethodFragment;
import kilanny.muslimalarm.fragments.alarmedit.OnFragmentInteractionListener;

public class EditAlarmOnboardingActivity extends AppCompatActivity implements
        OnFragmentInteractionListener,
        AlarmStopMethodFragment.OnFragmentInteractionListener {

    public static final String ARG_ALARM = "alarm";
    public static final String ARG_IS_FIVE_PRAYERS = "isFivePrayers";
    public static final String RESULT_ALARM = "alarm";
    public static final int RESULT_CODE_OK = 1;
    public static final int RESULT_CODE_CANCEL = 0;

    private EditAlarmStepperAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm_onboarding);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Alarm alarm = getIntent().getParcelableExtra(ARG_ALARM);
        if (alarm == null) {
            alarm = new Alarm();
            alarm.snoozeMins = 5;
            alarm.snoozeCount = 3;
            alarm.enabled = true;
            if (!getIntent().getBooleanExtra(ARG_IS_FIVE_PRAYERS, false)) {
                alarm.timeFlags = Alarm.TIME_QEYAM;
                alarm.timeAlarmDiffMinutes = 0;
            } else {
                alarm.timeAlarmDiffMinutes = 5;
            }
            setTitle(R.string.add_alarm);
        } else {
            setTitle(R.string.edit_alarm);
        }

        StepperLayout stepperLayout = findViewById(R.id.stepperLayout);
        if (alarm.timeFlags == Alarm.TIME_QEYAM) {
            mAdapter = new EditQiyamAlarmStepperAdapter(getSupportFragmentManager(),
                    this, alarm);
        } else {
            mAdapter = new Edit5AlarmStepperAdapter(getSupportFragmentManager(),
                    this, alarm);
        }
        stepperLayout.setAdapter(mAdapter);
    }

    @Override
    public void onNext(Alarm alarm) {
        mAdapter.setAlarm(alarm);
    }

    @Override
    public void onComplete(Alarm alarm) {
        Intent res = new Intent();
        res.putExtra(RESULT_ALARM, alarm);
        setResult(RESULT_CODE_OK, res);
        finish();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        setResult(RESULT_CODE_CANCEL);
        finish();
    }

    @Override
    public void onFragmentPreviewClick(AlarmStopMethodFragment sender) {
        mAdapter.onFragmentPreviewClick(sender);
    }

    @Override
    public void onFragmentLogoButtonClick(AlarmStopMethodFragment sender) {
        mAdapter.onFragmentLogoButtonClick(sender);
    }
}
