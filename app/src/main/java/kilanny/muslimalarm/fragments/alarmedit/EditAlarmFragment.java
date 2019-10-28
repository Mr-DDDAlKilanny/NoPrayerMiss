package kilanny.muslimalarm.fragments.alarmedit;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.stepstone.stepper.BlockingStep;

import kilanny.muslimalarm.data.Alarm;

public abstract class EditAlarmFragment extends Fragment implements BlockingStep {

    protected static final String ARG_ALARM = "alarm";

    protected OnFragmentInteractionListener mListener;
    protected Alarm mAlarm;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
