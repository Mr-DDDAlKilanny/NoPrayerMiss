package kilanny.muslimalarm.fragments.alarmedit;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import com.google.android.material.snackbar.Snackbar;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectTimesEditAlarmFragment extends EditAlarmFragment {


    private static final int[] checkboxes = {
            R.id.chkFajr, R.id.chkSun, R.id.chkZuhr, R.id.chkAsr, R.id.chkMagrib, R.id.chkIsha};

    private View mView;

    public SelectTimesEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WelcomeEditAlarmFragment.
     */
    public static SelectTimesEditAlarmFragment newInstance(Alarm alarm) {
        SelectTimesEditAlarmFragment fragment = new SelectTimesEditAlarmFragment();
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
        View root = inflater.inflate(R.layout.fragment_select_times_edit_alarm, container,
                false);
        NumberPicker hoursNumberPicker = root.findViewById(R.id.numHours);
        NumberPicker minutesNumberPicker = root.findViewById(R.id.numMins);
        hoursNumberPicker.setMinValue(0);
        hoursNumberPicker.setMaxValue(23);
        minutesNumberPicker.setMinValue(0);
        minutesNumberPicker.setMaxValue(59);
        AppCompatRadioButton radioBefore = root.findViewById(R.id.radioBefore);
        AppCompatRadioButton radioAfter = root.findViewById(R.id.radioAfter);
        if (mAlarm.timeAlarmDiffMinutes < 0)
            radioBefore.setChecked(true);
        else
            radioAfter.setChecked(true);
        hoursNumberPicker.setValue(Math.abs(mAlarm.timeAlarmDiffMinutes) / 60);
        minutesNumberPicker.setValue(Math.abs(mAlarm.timeAlarmDiffMinutes) % 60);
        for (int i = 0; i < 6; ++i) {
            AppCompatCheckBox checkBox = root.findViewById(checkboxes[5 - i]);
            checkBox.setChecked((mAlarm.timeFlags & (1 << i)) != 0);
        }
        return mView = root;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        mAlarm.timeFlags = 0;
        for (int j = 0; j < 6; ++j) {
            AppCompatCheckBox checkBox = mView.findViewById(checkboxes[j]);
            mAlarm.timeFlags = (mAlarm.timeFlags << 1) | (checkBox.isChecked() ? 1 : 0);
        }
        NumberPicker hoursNumberPicker = mView.findViewById(R.id.numHours);
        NumberPicker minutesNumberPicker = mView.findViewById(R.id.numMins);
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroupBeforeAfter);
        int value = hoursNumberPicker.getValue() * 60 + minutesNumberPicker.getValue();
        mAlarm.timeAlarmDiffMinutes = value
                * (radioGroup.getCheckedRadioButtonId() == R.id.radioBefore ? -1 : 1);

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
        int count = 0;
        for (int i = 0; i < 6; ++i) {
            AppCompatCheckBox checkBox = mView.findViewById(checkboxes[i]);
            count += checkBox.isChecked() ? 1 : 0;
        }
        if (count == 0)
            return new VerificationError(getString(R.string.err_select_prayer));
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
