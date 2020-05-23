package kilanny.muslimalarm.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.viewmodel.StepViewModel;

import java.lang.ref.WeakReference;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.fragments.alarmedit.SelectDaysEditAlarmFragment;
import kilanny.muslimalarm.fragments.alarmedit.SelectQyTimeEditAlarmFragment;
import kilanny.muslimalarm.fragments.alarmedit.SelectSnoozeEditAlarmFragment;
import kilanny.muslimalarm.fragments.alarmedit.SelectSoundVibrationLabelEditAlarmFragment;
import kilanny.muslimalarm.fragments.alarmedit.SelectTuneEditAlarmFragment;
import kilanny.muslimalarm.fragments.alarmedit.StopMethodEditAlarmFragment;
import kilanny.muslimalarm.fragments.alarmedit.WelcomeEditAlarmFragment;

public class EditQiyamAlarmStepperAdapter extends EditAlarmStepperAdapter {
    private final int[] TITLES = {
            R.string.welcome,
            R.string.alarm_times,
            R.string.alarm_days,
            R.string.alarm_tune,
            R.string.details_of_alarm,
            R.string.alarm_stop_method,
            R.string.snooze_settings
    };

    public EditQiyamAlarmStepperAdapter(FragmentManager fm, Context context,
                                        Alarm alarm) {
        super(fm, context, alarm);
    }

    @Override
    public Step createStep(int position) {
        switch (position) {
            case 0:
                return WelcomeEditAlarmFragment.newInstance(mAlarm);
            case 1:
                return SelectQyTimeEditAlarmFragment.newInstance(mAlarm);
            case 2:
                return SelectDaysEditAlarmFragment.newInstance(mAlarm);
            case 3:
                return SelectTuneEditAlarmFragment.newInstance(mAlarm);
            case 4:
                return SelectSoundVibrationLabelEditAlarmFragment.newInstance(mAlarm);
            case 5:
                return (mStopMethodEditAlarmFragment = new WeakReference<>(
                        StopMethodEditAlarmFragment.newInstance(mAlarm))).get();
            case 6:
                return SelectSnoozeEditAlarmFragment.newInstance(mAlarm);
        }
        return null;
    }

    @Override
    public int getCount() {
        /*
         * welcome to wizard of alarm !
         * select prayer time(s), time offset hours, minutes - before or after
         * do you want to repeat alarm? if yes, show days of week
         * config alarm tune
         * config alarm sound level, vibration, and optional label
         * select alarm stop method & details
         * select snooze: 1) none, 2) unlimited 3) specified? if yes, how many? what period?
         */
        return 7;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(int position) {
        return new StepViewModel.Builder(context)
                .setTitle(TITLES[position])
                .setBackButtonLabel(R.string.back)
                .setEndButtonLabel(position == getCount() - 1 ?
                        R.string.complete : R.string.next)
                .create();
    }
}
