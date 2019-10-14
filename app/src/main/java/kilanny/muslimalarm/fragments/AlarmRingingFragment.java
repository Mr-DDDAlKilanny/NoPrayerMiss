package kilanny.muslimalarm.fragments;


import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kilanny.muslimalarm.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmRingingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmRingingFragment extends ShowAlarmFragment {

    private static final String ARG_SHOW_SNOOZE = "showSnooze";

    private boolean mShowSnooze;
    private AlarmRingingFragment.FragmentInteractionListener mListener;
    private Timer mUpdateTimeTimer;

    public AlarmRingingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AlarmRingingFragment.
     */
    public static AlarmRingingFragment newInstance(boolean showSnooze) {
        AlarmRingingFragment fragment = new AlarmRingingFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_SHOW_SNOOZE, showSnooze);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShowSnooze = getArguments().getBoolean(ARG_SHOW_SNOOZE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AlarmRingingFragment.FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mUpdateTimeTimer != null) {
            mUpdateTimeTimer.cancel();
            mUpdateTimeTimer = null;
        }
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm_ringing, container, false);
        final TextView txtTime = view.findViewById(R.id.txtTime);
        final DateFormat dateFormat = new SimpleDateFormat("HH : mm", Locale.ENGLISH);
        txtTime.setText(dateFormat.format(new Date()));
        final Handler handler = new Handler(view.getContext().getMainLooper());
        mUpdateTimeTimer = new Timer();
        mUpdateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtTime.setText(dateFormat.format(new Date()));
                    }
                });
            }
        }, 5000, 5000);
        view.findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUpdateTimeTimer.cancel();
                mUpdateTimeTimer = null;
                mListener.onExitClick();
            }
        });
        Button btnSnooze = view.findViewById(R.id.btnSnooze);
        btnSnooze.setVisibility(mShowSnooze ? View.VISIBLE : View.GONE);
        btnSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUpdateTimeTimer != null)
                    mUpdateTimeTimer.cancel();
                mUpdateTimeTimer = null;
                mListener.onSnoozeClick();
            }
        });
        return view;
    }

    public interface FragmentInteractionListener {
        void onExitClick();

        void onSnoozeClick();
    }
}
