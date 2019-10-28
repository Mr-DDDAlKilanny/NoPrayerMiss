package kilanny.muslimalarm.fragments.alarmedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WelcomeEditAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeEditAlarmFragment extends EditAlarmFragment {

    public WelcomeEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WelcomeEditAlarmFragment.
     */
    public static WelcomeEditAlarmFragment newInstance(Alarm alarm) {
        WelcomeEditAlarmFragment fragment = new WelcomeEditAlarmFragment();
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
        View root = inflater.inflate(R.layout.fragment_welcome_edit_alarm, container, false);
        if (mAlarm.id == 0) {
            root.findViewById(R.id.txtAlarmIsNew).setVisibility(View.VISIBLE);
            root.findViewById(R.id.txtAlarmIsEdit).setVisibility(View.GONE);
            root.findViewById(R.id.cardViewAlarmEditInfo).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.txtAlarmIsNew).setVisibility(View.GONE);
            root.findViewById(R.id.txtAlarmIsEdit).setVisibility(View.VISIBLE);
            root.findViewById(R.id.cardViewAlarmEditInfo).setVisibility(View.VISIBLE);

            AppCompatTextView prayerName = root.findViewById(R.id.prayerName);
            prayerName.setText(Utils.getPrayerNames(root.getContext(), mAlarm));
            AppCompatTextView alarmDays = root.findViewById(R.id.alarmDays);
            alarmDays.setText(Utils.getAlarmDays(root.getContext(), mAlarm));
        }
        return root;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        mListener.onNext(mAlarm);
        callback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        mListener.onNext(mAlarm);
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
}
