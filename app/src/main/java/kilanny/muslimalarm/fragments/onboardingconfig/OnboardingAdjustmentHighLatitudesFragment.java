package kilanny.muslimalarm.fragments.onboardingconfig;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Arrays;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppSettings;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOnboardingOptionSelectedListener} interface
 * to handle interaction events.
 * Use the {@link OnboardingAdjustmentHighLatitudesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingAdjustmentHighLatitudesFragment extends OnboardingBaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "index_of_card";

    private int mParam1 = 0;

    private OnOnboardingOptionSelectedListener mListener;

    private TextView[] views = new TextView[4];

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment OnboardingAdjustmentHighLatitudesFragment.
     */
    public static OnboardingAdjustmentHighLatitudesFragment newInstance(int param1) {
        OnboardingAdjustmentHighLatitudesFragment fragment = new OnboardingAdjustmentHighLatitudesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public OnboardingAdjustmentHighLatitudesFragment() {
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
        View view = inflater.inflate(R.layout.fragment_onboarding_adjustment_high_latitudes, container, false);

        view.findViewById(R.id.prev).setOnClickListener(this);
        view.findViewById(R.id.next).setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.card_title);
        title.setText(R.string.high_latitude);

        views[0] = (TextView) view.findViewById(R.id.high_lat_none);
        views[1] = (TextView) view.findViewById(R.id.high_lat_midnight);
        views[2] = (TextView) view.findViewById(R.id.high_lat_one_seventh);
        views[3] = (TextView) view.findViewById(R.id.high_lat_angle_based);

        AppSettings settings = AppSettings.getInstance(getActivity());
        for (int i=0; i<views.length; i++) {
            TextView tv = views[i];
            tv.setOnClickListener(this);
            if (settings.getHighLatitudeAdjustmentFor(mParam1) == i) {
                tv.setSelected(true);
            }
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        if (views != null)
            Arrays.fill(views, null);
        super.onDestroyView();
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        try {
            mListener = (OnOnboardingOptionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
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
        } else {
            if (v instanceof TextView) {
                AppSettings settings = AppSettings.getInstance(getActivity());
                settings.setHighLatitudeAdjustmentMethodFor(mParam1, Integer.parseInt((String) v.getTag()));
                if (views != null) {
                    for (TextView t : views) {
                        if (t != null) {
                            t.setSelected(false);
                        }
                    }
                }
                v.setSelected(true);
                mListener.onOptionSelected();
            }
        }

    }
}
