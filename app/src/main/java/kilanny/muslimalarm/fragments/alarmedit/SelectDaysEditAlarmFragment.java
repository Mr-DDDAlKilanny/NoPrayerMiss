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
import android.widget.RadioGroup;

import com.google.android.material.snackbar.Snackbar;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.Weekday;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectDaysEditAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectDaysEditAlarmFragment extends EditAlarmFragment {

    private static final int[] checkboxes = {
            R.id.chkFri, R.id.chkSat, R.id.chkSun, R.id.chkMon, R.id.chkTues,
            R.id.chkWedns, R.id.chkThurs};

    private View mView;

    public SelectDaysEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectDaysEditAlarmFragment.
     */
    public static SelectDaysEditAlarmFragment newInstance(Alarm alarm) {
        SelectDaysEditAlarmFragment fragment = new SelectDaysEditAlarmFragment();
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
        final View root = inflater.inflate(R.layout.fragment_select_days_edit_alarm, container, false);
        RadioGroup radioGroup = root.findViewById(R.id.radioGroupOneTimeRepeat);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioOneTime)
                    root.findViewById(R.id.cardRepeatDays).setVisibility(View.GONE);
                else
                    root.findViewById(R.id.cardRepeatDays).setVisibility(View.VISIBLE);
            }
        });
        if (mAlarm.weekDayFlags == Weekday.NO_REPEAT) {
            AppCompatRadioButton radioButton = root.findViewById(R.id.radioOneTime);
            radioButton.setChecked(true);
        } else {
            AppCompatRadioButton radioButton = root.findViewById(R.id.radioRepeated);
            radioButton.setChecked(true);
            for (int i = 0; i < 7; ++i) {
                AppCompatCheckBox checkBox = root.findViewById(checkboxes[6 - i]);
                checkBox.setChecked((mAlarm.weekDayFlags & (1 << i)) != 0);
            }
        }
        return mView = root;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroupOneTimeRepeat);
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioOneTime)
            mAlarm.weekDayFlags = Weekday.NO_REPEAT;
        else {
            mAlarm.weekDayFlags = 0;
            for (int j = 0; j < 7; ++j) {
                AppCompatCheckBox checkBox = mView.findViewById(checkboxes[j]);
                mAlarm.weekDayFlags = (mAlarm.weekDayFlags << 1) | (checkBox.isChecked() ? 1 : 0);
            }
        }
        mListener.onNext(mAlarm);
        callback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        throw new RuntimeException();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroupOneTimeRepeat);
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioRepeated) {
            int count = 0;
            for (int i = 0; i < 7; ++i) {
                AppCompatCheckBox checkBox = mView.findViewById(checkboxes[i]);
                count += checkBox.isChecked() ? 1 : 0;
            }
            if (count == 0)
                return new VerificationError(getString(R.string.must_select_one_day));
        }
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
