package kilanny.muslimalarm.fragments.alarmedit;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.google.android.material.snackbar.Snackbar;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.Locale;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;

public class SelectQyTimeEditAlarmFragment extends EditAlarmFragment {

    private View mView;

    public SelectQyTimeEditAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WelcomeEditAlarmFragment.
     */
    public static SelectQyTimeEditAlarmFragment newInstance(Alarm alarm) {
        SelectQyTimeEditAlarmFragment fragment = new SelectQyTimeEditAlarmFragment();
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
        mView = inflater.inflate(R.layout.fragment_qy_edit_alarm, container,
                false);
        //MaterialButtonToggleGroup group = root.findViewById(R.id.alarmTypeButtons);
        //group.check(mAlarm.timeFlags == Alarm.TIME_QEYAM ?
        //        R.id.buttonNightPrayer : R.id.buttonFivePrayers);
        //root.findViewById(R.id.cardQeyam)
        //        .setVisibility(mAlarm.timeFlags == Alarm.TIME_QEYAM ? View.VISIBLE : View.GONE);
        //group.addOnButtonCheckedListener((group1, checkedId, isChecked) -> {
        //    if (checkedId == R.id.buttonNightPrayer) {
        //        root.findViewById(R.id.cardQeyam).setVisibility(isChecked ? View.VISIBLE : View.GONE);
        //    }
        //});
        AppCompatSeekBar seekBar = mView.findViewById(R.id.seekNightPercentage);
        TextView txtPercentage = mView.findViewById(R.id.txtPercentage);
        seekBar.setMax(99);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(5);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtPercentage.setText(String.format(Locale.ENGLISH, "%d%%", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        Integer progress = mAlarm.qeyamAlarmPercentageOfNightPeriod;
        if (progress == null)
            progress = 66;
        seekBar.setProgress(progress);
        return mView;
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        //MaterialButtonToggleGroup group = mView.findViewById(R.id.alarmTypeButtons);
        //if (group.getCheckedButtonId() == R.id.buttonNightPrayer) {
        mAlarm.timeFlags = Alarm.TIME_QEYAM;
        AppCompatSeekBar seekBar = mView.findViewById(R.id.seekNightPercentage);
        mAlarm.qeyamAlarmPercentageOfNightPeriod = seekBar.getProgress();
        //} else {
        //    mAlarm.timeFlags = 0;
        //    mAlarm.qeyamAlarmPercentageOfNightPeriod = null;
        //}
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
        //MaterialButtonToggleGroup group = mView.findViewById(R.id.alarmTypeButtons);
        //if (group.getCheckedButtonId() == R.id.buttonNightPrayer) {
        AppCompatSeekBar seekBar = mView.findViewById(R.id.seekNightPercentage);
        if (seekBar.getProgress() < 5)
            return new VerificationError(getString(R.string.min_5_per_night));
        //}
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
