package kilanny.muslimalarm.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.Result;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.fragments.CameraSelectorDialogFragment;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CaptureBarcodeActivity extends AppCompatActivity
        implements ZXingScannerView.ResultHandler,
        CameraSelectorDialogFragment.CameraSelectorDialogListener {

    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String CAMERA_ID = "CAMERA_ID";
    private static final String TAG = "captureBarcode";

    public static final int RESULT_OK = 0;
    public static final String EXTRA_BARCODE = "EXTRA_BARCODE";
    public static final String EXTRA_BARCODE_FORMAT = "EXTRA_BARCODE_FORMAT";

    private ZXingScannerView mScannerView;
    private boolean mFlash;
    private boolean mAutoFocus;
    private int mCameraId = -1;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            mCameraId = state.getInt(CAMERA_ID, -1);
        } else {
            mFlash = false;
            mAutoFocus = true;
            mCameraId = -1;
        }

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        setTitle(R.string.capture_barcode);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
        outState.putInt(CAMERA_ID, mCameraId);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(final Result rawResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.barcode_captured))
                .setMessage(rawResult.getText() + "\n" + getString(R.string.do_you_want_to_save_it))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent res = new Intent();
                        res.putExtra(EXTRA_BARCODE, rawResult.getText());
                        res.putExtra(EXTRA_BARCODE_FORMAT, rawResult.getBarcodeFormat().toString());
                        setResult(RESULT_OK, res);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // If you would like to resume scanning, call this method below:
                        mScannerView.resumeCameraPreview(CaptureBarcodeActivity.this);
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_capture_barcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menuItemFlash:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.flash)
                        .setSingleChoiceItems(R.array.onOff, mFlash ? 0 : 1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mFlash = i == 0;
                                mScannerView.setFlash(mFlash);
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.menuItemAutoFocus:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.flash)
                        .setSingleChoiceItems(R.array.onOff, mAutoFocus ? 0 : 1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mAutoFocus = i == 0;
                                mScannerView.setAutoFocus(mAutoFocus);
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.menuItemSelectCam:
                mScannerView.stopCamera();
                DialogFragment cFragment = CameraSelectorDialogFragment.newInstance(this,
                        mCameraId);
                cFragment.show(getSupportFragmentManager(), "camera_selector");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCameraSelected(int cameraId) {
        mCameraId = cameraId;
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }
}
