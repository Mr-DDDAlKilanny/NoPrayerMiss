package kilanny.muslimalarm.fragments.onboardingconfig;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppSettings;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOnboardingOptionSelectedListener} interface
 * to handle interaction events.
 * Use the {@link OnboardingCalculationMethodFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingCalculationMethodFragment extends OnboardingBaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CALC_IDX = "param1";

    private int mCalculationIdx;

    private OnOnboardingOptionSelectedListener mListener;

    private TextView[] options = new TextView[7];

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param calcIdx Parameter 1.
     * @return A new instance of fragment OnboardingFragment.
     */
    public static OnboardingCalculationMethodFragment newInstance(int calcIdx) {
        OnboardingCalculationMethodFragment fragment = new OnboardingCalculationMethodFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CALC_IDX, calcIdx);
        fragment.setArguments(args);
        return fragment;
    }

    public OnboardingCalculationMethodFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCalculationIdx = getArguments().getInt(ARG_CALC_IDX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding_calculation_method, container, false);

        view.findViewById(R.id.prev).setOnClickListener(this);
        view.findViewById(R.id.next).setOnClickListener(this);

        if (mListener != null) {
            ((AppCompatActivity) mListener).getSupportActionBar().setTitle(R.string.title_onboarding_calc_method);
        }

        TextView title = view.findViewById(R.id.card_title);
        title.setText(R.string.calc_method);

        options[0] = (TextView) view.findViewById(R.id.karachi);
        options[1] = (TextView) view.findViewById(R.id.isna);
        options[2] = (TextView) view.findViewById(R.id.mwl);
        options[3] = (TextView) view.findViewById(R.id.makkah);
        options[4] = (TextView) view.findViewById(R.id.egypt);
        options[5] = (TextView) view.findViewById(R.id.tehran);
        options[6] = (TextView) view.findViewById(R.id.jafri);

        AppSettings settings = AppSettings.getInstance(getActivity());
        int method = settings.getCalcMethodSetFor(mCalculationIdx);

        for (TextView t : options) {
            int val = Integer.parseInt((String) t.getTag());
            if (val == method) {
                t.setSelected(true);
            }
            t.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        if (options != null)
            Arrays.fill(options, null);
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
            return;
        } else if (v.getId() == R.id.prev) {
            getActivity().onBackPressed();
            return;
        }
        if (v instanceof TextView) {
            TextView textView = (TextView) v;
            AppSettings settings = AppSettings.getInstance(getActivity());
            settings.setCalcMethodFor(mCalculationIdx, Integer.parseInt((String) textView.getTag()));
            if (options != null) {
                for (TextView t : options) {
                    if (t != null) {
                        t.setSelected(false);
                    }
                }
            }
            textView.setSelected(true);
            mListener.onOptionSelected();
        }
    }
}
