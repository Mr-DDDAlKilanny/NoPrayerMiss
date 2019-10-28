package kilanny.muslimalarm.fragments.alarmedit;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectSnoozeEditAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectSnoozeEditAlarmFragment extends EditAlarmFragment {

    private View mView;

    public SelectSnoozeEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectSnoozeEditAlarmFragment.
     */
    public static SelectSnoozeEditAlarmFragment newInstance(Alarm alarm) {
        SelectSnoozeEditAlarmFragment fragment = new SelectSnoozeEditAlarmFragment();
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
        mView = inflater.inflate(R.layout.fragment_select_snooze_edit_alarm, container, false);

        final NumberPicker numberPickerPeriod = mView.findViewById(R.id.numMins);
        numberPickerPeriod.setMinValue(1);
        numberPickerPeriod.setMaxValue(59);
        numberPickerPeriod.setValue(Math.max(1, mAlarm.snoozeMins));

        final NumberPicker numberPickerCount = mView.findViewById(R.id.numPickerSnoozeCount);
        numberPickerCount.setMinValue(1);
        numberPickerCount.setMaxValue(99);
        numberPickerCount.setValue(Math.max(1, mAlarm.snoozeCount));

        RadioGroup radioGroupMaxSnoozes = mView.findViewById(R.id.radioGroupMaxSnoozes);
        radioGroupMaxSnoozes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                LinearLayout layout = mView.findViewById(R.id.layoutSnoozePeriod);
                numberPickerCount.setVisibility(
                        radioGroup.getCheckedRadioButtonId() == R.id.radioSpecifiedSnoozes ?
                                View.VISIBLE : View.INVISIBLE);
                layout.setVisibility(radioGroup.getCheckedRadioButtonId() == R.id.radioNoSnoozes ?
                        View.INVISIBLE : View.VISIBLE);
            }
        });
        if (mAlarm.snoozeMins == 0) {
            AppCompatRadioButton radioButton = mView.findViewById(R.id.radioNoSnoozes);
            radioButton.setChecked(true);
        } else if (mAlarm.snoozeCount == 0) {
            AppCompatRadioButton radioButton = mView.findViewById(R.id.radioUnlimitedSnoozes);
            radioButton.setChecked(true);
        } else {
            AppCompatRadioButton radioButton = mView.findViewById(R.id.radioSpecifiedSnoozes);
            radioButton.setChecked(true);
        }
        return mView;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        throw new RuntimeException();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroupMaxSnoozes);
        NumberPicker numberPickerPeriod = mView.findViewById(R.id.numMins);
        NumberPicker numberPickerCount = mView.findViewById(R.id.numPickerSnoozeCount);
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioNoSnoozes:
                mAlarm.snoozeMins = 0;
                mAlarm.snoozeCount = 0;
                break;
            case R.id.radioUnlimitedSnoozes:
                mAlarm.snoozeMins = numberPickerPeriod.getValue();
                mAlarm.snoozeCount = 0;
                break;
            case R.id.radioSpecifiedSnoozes:
                mAlarm.snoozeMins = numberPickerPeriod.getValue();
                mAlarm.snoozeCount = numberPickerCount.getValue();
                break;
        }
        mListener.onComplete(mAlarm);
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
