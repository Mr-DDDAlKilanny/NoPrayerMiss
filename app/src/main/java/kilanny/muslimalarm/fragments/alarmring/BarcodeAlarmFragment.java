package kilanny.muslimalarm.fragments.alarmring;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.Result;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.util.Utils;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowAlarmFragment.FragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BarcodeAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeAlarmFragment extends ShowAlarmFragment
        implements ZXingScannerView.ResultHandler {

    private static final String ARG_BARCODE = "barcode";
    private static final String ARG_IS_SILENT = "isSilent";

    private String mBarcode;
    private boolean mIsSilent;
    private ZXingScannerView mScannerView;
    private boolean started = false;
    private boolean mFlash = false;
    private int mCameraId = -1;
    private int mNumberOfCameras;

    private ShowAlarmFragment.FragmentInteractionListener mListener;

    public BarcodeAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BarcodeAlarmFragment.
     */
    public static BarcodeAlarmFragment newInstance(String barcode, boolean isSilent) {
        BarcodeAlarmFragment fragment = new BarcodeAlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BARCODE, barcode);
        args.putBoolean(ARG_IS_SILENT, isSilent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNumberOfCameras = Camera.getNumberOfCameras();
        if (getArguments() != null) {
            mBarcode = getArguments().getString(ARG_BARCODE);
            mIsSilent = getArguments().getBoolean(ARG_IS_SILENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_barcode_alarm, container, false);
        ImageView imageView = root.findViewById(R.id.imgIsSilent);
        imageView.setImageResource(mIsSilent ? android.R.drawable.ic_lock_silent_mode
                : android.R.drawable.ic_lock_silent_mode_off);
        imageView.setBackgroundResource(mIsSilent ? android.R.color.holo_green_light :
                android.R.color.holo_red_dark);
        mScannerView = root.findViewById(R.id.scannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        root.findViewById(R.id.toggle_camera_fab).setOnClickListener(view -> {
            if (mCameraId == -1)
                ++mCameraId;
            else
                mCameraId = (mCameraId + 1) % mNumberOfCameras;
            mScannerView.stopCamera();
            mScannerView.startCamera(mCameraId);
        });
        root.findViewById(R.id.toggle_flash_fab).setOnClickListener(view -> {
            mFlash = !mFlash;
            mScannerView.setFlash(mFlash);
        });

        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(true);
        started = true;
        return root;
    }

    @Override
    public void handleResult(Result rawResult) {
        Utils.vibrateFor(getContext(), 500);
        String code = rawResult.getText();
        if (code != null && code.equals(mBarcode)) {
            started = false;
            mScannerView.stopCamera();
            mListener.onDismissed(true);
        } else {
            mListener.onResetSleepTimeout();
            Toast.makeText(getContext(), R.string.incorrect_barcode, Toast.LENGTH_LONG).show();
            mScannerView.resumeCameraPreview(this);
        }
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
        if (started)
            mScannerView.stopCamera();
        mListener = null;
        super.onDetach();
    }
}
