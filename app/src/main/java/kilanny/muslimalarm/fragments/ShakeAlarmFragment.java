package kilanny.muslimalarm.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import kilanny.muslimalarm.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowAlarmFragment.FragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShakeAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShakeAlarmFragment extends ShowAlarmFragment implements SensorEventListener {

    private static final String ARG_SHAKE_COUNT = "shakeCount";
    private static final String ARG_SHAKE_LEVEL = "shakeLevel";


    private SensorManager mSensorManager;

    private AppCompatTextView mTxtShakedCount;
    private int mShakeCount, mShakedCount;
    private int mShakeThreshold = 800;
    private long lastUpdate;
    private float last_x, last_y, last_z;
    private boolean isUnregistered = true;

    private ShowAlarmFragment.FragmentInteractionListener mListener;

    public ShakeAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShakeAlarmFragment.
     */
    public static ShakeAlarmFragment newInstance(int shakeCount, int shakeLevel) {
        ShakeAlarmFragment fragment = new ShakeAlarmFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SHAKE_COUNT, shakeCount);
        args.putInt(ARG_SHAKE_LEVEL, shakeLevel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShakeCount = getArguments().getInt(ARG_SHAKE_COUNT);
            int shakeLevel = getArguments().getInt(ARG_SHAKE_LEVEL);
            mShakeThreshold = 800 + (shakeLevel - 1) * 100;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shake_alarm, container, false);
        AppCompatTextView txtTotalShakes = view.findViewById(R.id.txtTotalShakes);
        txtTotalShakes.setText(String.format(Locale.ENGLISH, "/ %d", mShakeCount));
        mTxtShakedCount = view.findViewById(R.id.txtShakedCount);
        mTxtShakedCount.setText("0");

        mSensorManager = (SensorManager) view.getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        isUnregistered = false;
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ShowAlarmFragment.FragmentInteractionListener) {
            mListener = (ShowAlarmFragment.FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (!isUnregistered)
            mSensorManager.unregisterListener(this);
        mListener = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensor = sensorEvent.sensor.getType();
        if (sensor == Sensor.TYPE_ACCELEROMETER) {
            float[] values = sensorEvent.values;
            long curTime = System.currentTimeMillis();
            // only allow one update every 250ms.
            if ((curTime - lastUpdate) > 150) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = values[0];
                float y = values[1];
                float z = values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > mShakeThreshold) {
                    Log.d("sensor", "shake detected w/ speed: " + speed);
                    //Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                    ++mShakedCount;
                    mTxtShakedCount.setText(String.format(Locale.ENGLISH, "%d", mShakedCount));
                    if (mShakedCount >= mShakeCount) {
                        mSensorManager.unregisterListener(this);
                        isUnregistered = true;
                        mListener.onDismissed(true);
                    } else {
                        mListener.onResetSleepTimeout();
                    }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
