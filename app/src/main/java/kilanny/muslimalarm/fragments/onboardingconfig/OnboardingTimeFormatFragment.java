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
 * Use the {@link OnboardingTimeFormatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingTimeFormatFragment extends OnboardingBaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private int mParam1 = 0;

    protected OnOnboardingOptionSelectedListener mListener;

    private TextView m12h;
    private TextView m24h;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OnboardingAsrCalculationMethod.
     */
    public static OnboardingTimeFormatFragment newInstance(int param1) {
        OnboardingTimeFormatFragment fragment = new OnboardingTimeFormatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public OnboardingTimeFormatFragment() {
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
    public void onDestroyView() {
        m12h = null;
        m24h = null;
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding_time_format, container, false);

        view.findViewById(R.id.prev).setOnClickListener(this);
        TextView next = (TextView) view.findViewById(R.id.next);
        next.setOnClickListener(this);
        next.setText(R.string.button_done);

        TextView title = (TextView) view.findViewById(R.id.card_title);
        title.setText(R.string.time_title);

        m12h = (TextView) view.findViewById(R.id.twelve);
        m24h = (TextView) view.findViewById(R.id.twenty_four);
        m12h.setOnClickListener(this);
        m24h.setOnClickListener(this);

        int method = AppSettings.getInstance(getActivity()).getTimeFormatFor(mParam1);
        if (method == PrayTime.TIME_12) {
            m12h.setSelected(true);
        } else {
            m24h.setSelected(true);
        }

        return view;
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
            int sel = Integer.parseInt(v.getTag().toString());
            settings.setTimeFormatFor(mParam1, sel);
            v.setSelected(true);
            if (sel == 1 && m24h != null) {
                m24h.setSelected(false);
            } else if (sel == 0 && m12h != null) {
                m12h.setSelected(false);
            }
            mListener.onOptionSelected();
        }
    }
}
