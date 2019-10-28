package kilanny.muslimalarm.fragments.alarmedit;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.adapters.SelectTuneAdapter;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.Tune;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectTuneEditAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectTuneEditAlarmFragment extends EditAlarmFragment {

    private SelectTuneAdapter mAdapter;
    private View mView;

    public SelectTuneEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectTuneEditAlarmFragment.
     */
    public static SelectTuneEditAlarmFragment newInstance(Alarm alarm) {
        SelectTuneEditAlarmFragment fragment = new SelectTuneEditAlarmFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_select_tune_edit_alarm, container,
                false);
        ListView listView = root.findViewById(R.id.listViewTunes);
        Tune[] tunes = Tune.getTunes();
        mAdapter = new SelectTuneAdapter(root.getContext(), tunes);
        listView.setAdapter(mAdapter);
        for (Tune tune : tunes)
            if (tune.rawResId == mAlarm.alarmTune) {
                mAdapter.setSelectedTune(tune);
                break;
            }
        return mView = root;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        SelectTuneAdapter.stopPlayback();
        mAlarm.alarmTune = mAdapter.getSelectedTune().rawResId;
        mListener.onNext(mAlarm);
        callback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        throw new RuntimeException();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        SelectTuneAdapter.stopPlayback();
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        SelectTuneAdapter.stopPlayback();
        if (mAdapter.getSelectedTune() == null)
            return new VerificationError(getString(R.string.must_select_tune));
        return null;
    }

    @Override
    public void onSelected() {
        SelectTuneAdapter.stopPlayback();
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        SelectTuneAdapter.stopPlayback();
        Snackbar.make(mView, error.getErrorMessage(), Snackbar.LENGTH_LONG).show();
    }
}
