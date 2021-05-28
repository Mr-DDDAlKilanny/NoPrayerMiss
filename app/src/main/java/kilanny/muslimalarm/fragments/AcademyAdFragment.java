package kilanny.muslimalarm.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.util.AnalyticsTrackers;
import kilanny.muslimalarm.util.Utils;

public class AcademyAdFragment extends DialogFragment {

    public AcademyAdFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdsFragment.
     */
    public static AcademyAdFragment newInstance() {
        AcademyAdFragment fragment = new AcademyAdFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ad_academy, container, false);

        final String link = "https://docs.google.com/forms/d/e/1FAIpQLSfpzI2XAtw5QjL1AQ85pFLvGah9Dc4Kg025kzGe--EBOBnvmQ/viewform?usp=sf_link";

        view.findViewById(R.id.btnRegister).setOnClickListener(v -> {
            Utils.openUrlInChromeOrDefault(v.getContext().getApplicationContext(), link);
            getDialog().dismiss();
        });
        view.findViewById(R.id.btnShare).setOnClickListener(v -> {
            Utils.displayShareActivity(getContext(),
                    getContext().getString(R.string.islamic_academy) + "\n"
                            + getContext().getString(R.string.islamic_academy_descr) + "\n"
                            + getContext().getString(R.string.islamic_academy_ad_overview) + "\n\n"
                            + getContext().getString(R.string.register_for_free) + "\n"
                            + link
            );
        });
        view.findViewById(R.id.btnNotNow).setOnClickListener(v -> getDialog().dismiss());
        AnalyticsTrackers.getInstance(getContext()).logAdShow();
        return view;
    }
}
