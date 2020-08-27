package kilanny.muslimalarm.fragments.alarmedit;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;

public class SelectCustomTimeEditAlarmFragment extends EditAlarmFragment {
    private View mView;

    public SelectCustomTimeEditAlarmFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectCustomTimeEditAlarmFragment.
     */
    public static SelectCustomTimeEditAlarmFragment newInstance(Alarm alarm) {
        SelectCustomTimeEditAlarmFragment fragment = new SelectCustomTimeEditAlarmFragment();
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
        mView = inflater.inflate(R.layout.fragment_custom_edit_alarm, container,
                false);
        TimePicker timePicker = mView.findViewById(R.id.timePicker);

        Integer progress = mAlarm.customTime;
        if (progress == null)
            progress = 9 * 60;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(progress / 60);
            timePicker.setMinute(progress % 60);
        } else {
            timePicker.setCurrentHour(progress / 60);
            timePicker.setCurrentMinute(progress % 60);
        }

        return mView;
    }

    private int selectedTime() {
        TimePicker timePicker = mView.findViewById(R.id.timePicker);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return timePicker.getHour() * 60 + timePicker.getMinute();
        } else {
            return timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
        }
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        mAlarm.timeFlags = Alarm.TIME_CUSTOM;
        mAlarm.customTime = selectedTime();

        mListener.onNext(mAlarm);
        callback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        //callback.complete();
        throw new RuntimeException();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
//        int sel = selectedTime();
//        if (sel < 0 || sel >= 24 * 60) {
//            return new VerificationError("00:00 <--> 23:59");
//        }
        return null;
    }

    @Override
    public void onSelected() {
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        Snackbar.make(mView, error.getErrorMessage(), Snackbar.LENGTH_LONG).show();
    }
}
