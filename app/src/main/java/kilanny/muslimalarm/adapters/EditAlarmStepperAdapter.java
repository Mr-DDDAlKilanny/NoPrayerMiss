package kilanny.muslimalarm.adapters;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;

import java.lang.ref.WeakReference;

import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.fragments.AlarmStopMethodFragment;
import kilanny.muslimalarm.fragments.alarmedit.StopMethodEditAlarmFragment;

public abstract class EditAlarmStepperAdapter extends AbstractFragmentStepAdapter
        implements AlarmStopMethodFragment.OnFragmentInteractionListener {

    protected Alarm mAlarm;
    WeakReference<StopMethodEditAlarmFragment> mStopMethodEditAlarmFragment;

    EditAlarmStepperAdapter(FragmentManager fm, Context context,
                            Alarm alarm) {
        super(fm, context);
        setAlarm(alarm);
    }

    public void setAlarm(Alarm alarm) {
        mAlarm = alarm;
    }

    @Override
    public void onFragmentPreviewClick(AlarmStopMethodFragment sender) {
        if (mStopMethodEditAlarmFragment != null
                && mStopMethodEditAlarmFragment.get() != null
                && !mStopMethodEditAlarmFragment.get().isDetached()) {
            mStopMethodEditAlarmFragment.get().onFragmentPreviewClick(sender);
        }
    }

    @Override
    public void onFragmentLogoButtonClick(AlarmStopMethodFragment sender) {
        if (mStopMethodEditAlarmFragment != null
                && mStopMethodEditAlarmFragment.get() != null
                && !mStopMethodEditAlarmFragment.get().isDetached()) {
            mStopMethodEditAlarmFragment.get().onFragmentLogoButtonClick(sender);
        }
    }
}
