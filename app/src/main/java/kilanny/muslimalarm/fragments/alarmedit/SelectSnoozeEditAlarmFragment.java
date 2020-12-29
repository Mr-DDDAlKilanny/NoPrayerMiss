package kilanny.muslimalarm.fragments.alarmedit;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.Locale;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectSnoozeEditAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectSnoozeEditAlarmFragment extends EditAlarmFragment {

    private View mView;
    private String[] mMaxMinsValues, mSnoozeCountValues;
    private int mDuration;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_select_snooze_edit_alarm, container, false);

        final TextInputEditText numberPickerPeriod = mView.findViewById(R.id.numMins);
        numberPickerPeriod.setText(String.format(Locale.ENGLISH, "%d",
                Math.max(1, mAlarm.snoozeMins)));

        final AutoCompleteTextView numberPickerCount = mView.findViewById(R.id.numPickerSnoozeCount);
        mSnoozeCountValues = new String[99];
        for (int i = 0; i < mSnoozeCountValues.length; ++i) {
            mSnoozeCountValues[i] = String.format(Locale.getDefault(), "%d", i + 1);
        }
        numberPickerCount.setAdapter(new ArrayAdapter<>(getContext(),
                R.layout.cat_exposed_dropdown_popup_item, mSnoozeCountValues));
        numberPickerCount.setOnTouchListener((v, event) -> {
            numberPickerCount.showDropDown();
            return true;
        });
        numberPickerCount.setText(mSnoozeCountValues[Math.max(mAlarm.snoozeCount - 1, 0)]);
        ((ArrayAdapter) numberPickerCount.getAdapter()).getFilter().filter(null);

        AutoCompleteTextView numMaxMins = mView.findViewById(R.id.numMaxMins);
        mMaxMinsValues = new String[91];
        mMaxMinsValues[0] = getString(R.string.none);
        for (int i = 1; i < mMaxMinsValues.length; ++i) {
            mMaxMinsValues[i] = getString(R.string.afterNMinutes,
                    String.format(Locale.getDefault(), "%d", i));
        }
        numMaxMins.setAdapter(new ArrayAdapter<>(getContext(),
                R.layout.cat_exposed_dropdown_popup_item, mMaxMinsValues));
        numMaxMins.setOnTouchListener((v, event) -> {
            numMaxMins.showDropDown();
            return true;
        });
        numMaxMins.setText(mMaxMinsValues[mAlarm.maxMinsRinging == null ? 0 : mAlarm.maxMinsRinging]);
        ((ArrayAdapter) numMaxMins.getAdapter()).getFilter().filter(null);

        RadioGroup radioGroupMaxSnoozes = mView.findViewById(R.id.radioGroupMaxSnoozes);
        radioGroupMaxSnoozes.setOnCheckedChangeListener((radioGroup, i) -> {
            LinearLayout layout = mView.findViewById(R.id.layoutSnoozePeriod);
            View snoozeCountBox = mView.findViewById(R.id.snoozeCountBox);
            snoozeCountBox.setVisibility(
                    radioGroup.getCheckedRadioButtonId() == R.id.radioSpecifiedSnoozes ?
                            View.VISIBLE : View.INVISIBLE);
            layout.setVisibility(radioGroup.getCheckedRadioButtonId() == R.id.radioNoSnoozes ?
                    View.INVISIBLE : View.VISIBLE);
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
        //throw new RuntimeException(); //TODO: invoked sometimes on fast next !!
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelError();
    }

    private void cancelError() {
        TextInputEditText numberPickerPeriod = mView.findViewById(R.id.numMins);
        numberPickerPeriod.setError(null);
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroupMaxSnoozes);
        AutoCompleteTextView numberPickerCount = mView.findViewById(R.id.numPickerSnoozeCount);
        AutoCompleteTextView numMaxMins = mView.findViewById(R.id.numMaxMins);
        String max = numMaxMins.getText().toString();
        for (int i = 0; i < mMaxMinsValues.length; ++i) {
            if (mMaxMinsValues[i].equals(max)) {
                mAlarm.maxMinsRinging = i == 0 ? null : i;
                break;
            }
        }
        max = numberPickerCount.getText().toString();
        for (int i = 0; i < mSnoozeCountValues.length; ++i) {
            if (mSnoozeCountValues[i].equals(max)) {
                mAlarm.snoozeCount = i + 1;
                break;
            }
        }
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioNoSnoozes:
                mAlarm.snoozeMins = 0;
                mAlarm.snoozeCount = 0;
                break;
            case R.id.radioUnlimitedSnoozes:
                mAlarm.snoozeMins = mDuration;
                mAlarm.snoozeCount = 0;
                break;
            case R.id.radioSpecifiedSnoozes:
                mAlarm.snoozeMins = mDuration;
                //mAlarm.snoozeCount = numberPickerCount.getValue();
                break;
        }
        mListener.onComplete(mAlarm);
        callback.complete();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        cancelError();
        callback.goToPrevStep();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        cancelError();
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroupMaxSnoozes);
        TextInputEditText numberPickerPeriod = mView.findViewById(R.id.numMins);
        mDuration = 0;
        if (radioGroup.getCheckedRadioButtonId() != R.id.radioNoSnoozes) {
            try {
                mDuration = Integer.parseInt(numberPickerPeriod.getText().toString());
                if (mDuration < 1 || mDuration > 59)
                    throw new Exception("");
            } catch (Throwable ex) {
                ex.printStackTrace();
                numberPickerPeriod.setError("1 .. 59");
                return new VerificationError("");
            }
        }
        return null;
    }

    @Override
    public void onSelected() {
        cancelError();
    }

    @Override
    public void onError(@NonNull VerificationError error) {
    }
}
