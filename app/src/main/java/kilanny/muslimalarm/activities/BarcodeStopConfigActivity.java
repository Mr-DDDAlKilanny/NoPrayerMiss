package kilanny.muslimalarm.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.View;

import java.util.concurrent.ExecutionException;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.Barcode;
import kilanny.muslimalarm.fragments.BarcodeStopConfigFragment;
import kilanny.muslimalarm.fragments.BarcodeStopEmptyListFragment;
import kilanny.muslimalarm.fragments.ShowAlarmFragment;
import kilanny.muslimalarm.util.Utils;

public class BarcodeStopConfigActivity extends AppCompatActivity
        implements BarcodeStopEmptyListFragment.OnFragmentInteractionListener,
        BarcodeStopConfigFragment.FragmentInteractionListener {

    public static final String ARG_SELECTED_BARCODE_ID = "selectedBarcodeId";
    public static final int RESULT_CODE_OK = 0;
    public static final int RESULT_CODE_CANCEL = 1;
    public static final String RESULT_BARCODE_ID = "barcodeId";
    private static final int REQUEST_CAPTURE_BARCODE = 0;
    private static final int REQUEST_PERMISSION = 1;

    private Integer mSelectedBarcodeId;
    private BarcodeStopConfigFragment barcodeStopConfigFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_stop_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.select_barcode);

        Intent intent = getIntent();
        mSelectedBarcodeId = intent.getIntExtra(ARG_SELECTED_BARCODE_ID, 0);
        if (mSelectedBarcodeId == 0)
            mSelectedBarcodeId = null;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAdd();
            }
        });

        Utils.runInBackground(new Function<Context, Integer>() {
            @Override
            public Integer apply(Context input) {
                return AppDb.getInstance(input).barcodeDao().count();
            }
        }, new Function<Integer, Void>() {
            @Override
            public Void apply(Integer count) {
                Fragment fragment;
                if (count == 0) {
                    fragment = BarcodeStopEmptyListFragment.newInstance();
                } else {
                    fragment = barcodeStopConfigFragment =
                            BarcodeStopConfigFragment.newInstance(mSelectedBarcodeId);
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, fragment)
                        .commit();
                return null;
            }
        }, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            onAdd();
    }

    private void onAdd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSION);
            return;
        }
        startActivityForResult(new Intent(this, CaptureBarcodeActivity.class),
                REQUEST_CAPTURE_BARCODE);
    }

    private void onBarcodeAdded() {
        if (barcodeStopConfigFragment == null) {
            barcodeStopConfigFragment = BarcodeStopConfigFragment.newInstance(mSelectedBarcodeId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, barcodeStopConfigFragment)
                    .commit();
        } else {
            barcodeStopConfigFragment.onBarcodeAdded();
        }
        Snackbar.make(findViewById(R.id.fab), getString(R.string.barcode_added), Snackbar.LENGTH_LONG)
                .setAction("Undo", null).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_BARCODE && resultCode == CaptureBarcodeActivity.RESULT_OK
                && data != null) {
            final String barcode = data.getStringExtra(CaptureBarcodeActivity.EXTRA_BARCODE);
            final String barcodeFormat = data.getStringExtra(CaptureBarcodeActivity.EXTRA_BARCODE_FORMAT);
            Utils.runInBackground(new Function<Context, Void>() {
                @Override
                public Void apply(Context input) {
                    AppDb.getInstance(input).barcodeDao()
                            .insert(new Barcode(barcode, barcodeFormat));
                    return null;
                }
            }, new Function<Void, Void>() {
                @Override
                public Void apply(Void input) {
                    onBarcodeAdded();
                    return null;
                }
            }, this);
        }
    }

    @Override
    public void onClicked() {
        onAdd();
    }

    @Override
    public void onDismiss(Integer selectedBarcodeId) {
        if (selectedBarcodeId == null) {
            setResult(RESULT_CODE_CANCEL);
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra(RESULT_BARCODE_ID, selectedBarcodeId.intValue());
            setResult(RESULT_CODE_OK, intent);
            finish();
        }
    }
}
