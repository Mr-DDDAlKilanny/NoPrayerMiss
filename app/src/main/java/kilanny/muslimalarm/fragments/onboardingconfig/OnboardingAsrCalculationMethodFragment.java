package kilanny.muslimalarm.fragments.onboardingconfig;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.util.PrayTime;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOnboardingOptionSelectedListener} interface
 * to handle interaction events.
 * Use the {@link OnboardingAsrCalculationMethodFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingAsrCalculationMethodFragment  extends OnboardingBaseFragment
        implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private int mParam1 = 0;

    private OnOnboardingOptionSelectedListener mListener;

    private TextView mShafii;
    private TextView mHanfi;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OnboardingAsrCalculationMethod.
     */
    public static OnboardingAsrCalculationMethodFragment newInstance(int param1) {
        OnboardingAsrCalculationMethodFragment fragment = new OnboardingAsrCalculationMethodFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public OnboardingAsrCalculationMethodFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding_asr_calculation_method, container, false);

        view.findViewById(R.id.prev).setOnClickListener(this);
        view.findViewById(R.id.next).setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.card_title);
        title.setText(R.string.asr_method);

        mHanfi = (TextView) view.findViewById(R.id.asr_hanfi);
        mShafii = (TextView) view.findViewById(R.id.asr_shafii);
        mHanfi.setOnClickListener(this);
        mShafii.setOnClickListener(this);

        int method = AppSettings.getInstance(getActivity()).getAsrMethodSetFor(mParam1);
        if (method == PrayTime.SHAFII) {
            mShafii.setSelected(true);
        } else {
            mHanfi.setSelected(true);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        mHanfi = null;
        mShafii = null;
        super.onDestroyView();
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        try {
            mListener = (OnOnboardingOptionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnOnboardingOptionSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.next) {
            mListener.onOptionSelected();
        } else if (v.getId() == R.id.prev) {
            getActivity().onBackPressed();
        } else if (v instanceof TextView) {
            AppSettings settings = AppSettings.getInstance(getActivity());
            if (v.getTag().toString().equals("0")) {
                v.setSelected(true);
                if (mHanfi != null)
                    mHanfi.setSelected(false);
                settings.setAsrMethodFor(mParam1, PrayTime.SHAFII);
                mListener.onOptionSelected();
            } else if (v.getTag().toString().equals("1")) {
                if (mShafii != null)
                    mShafii.setSelected(false);
                v.setSelected(true);
                settings.setAsrMethodFor(mParam1, PrayTime.HANAFI);
                mListener.onOptionSelected();
            }
        }
    }
}
