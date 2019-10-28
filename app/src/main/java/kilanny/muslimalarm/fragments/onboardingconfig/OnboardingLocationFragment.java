package kilanny.muslimalarm.fragments.onboardingconfig;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.activities.ConfigOnboardingActivity;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.util.Utils;

public class OnboardingLocationFragment extends OnboardingBaseFragment
        implements LocationListener, ConfigOnboardingActivity.PermissionCallback {

    public static OnboardingLocationFragment newInstance() {
        OnboardingLocationFragment fragment = new OnboardingLocationFragment();
        return fragment;
    }

    private OnOnboardingOptionSelectedListener mListener;
    private View mView;
    private int requestLocation = -1;

    public OnboardingLocationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_location, container, false);
        final View layoutManualLocation = root.findViewById(R.id.layoutManualLocation);

        root.findViewById(R.id.btnManualLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutManualLocation.getVisibility() == View.INVISIBLE)
                    layoutManualLocation.setVisibility(View.VISIBLE);
                else
                    layoutManualLocation.setVisibility(View.INVISIBLE);
            }
        });
        root.findViewById(R.id.prev).setOnClickListener(this);
        root.findViewById(R.id.next).setOnClickListener(this);

        root.findViewById(R.id.btnManualOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okManual();
            }
        });
        root.findViewById(R.id.btnDetectLocationWithGps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnGps();
            }
        });
        root.findViewById(R.id.btnDetectLocationWithNetwork).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNetwork();
            }
        });
        AppSettings settings = AppSettings.getInstance(getContext());
        if (settings.getDouble(settings.getKeyFor(AppSettings.Key.LAT_FOR, 0)) > -1) {
            root.findViewById(R.id.txtLocationAlreadySet).setVisibility(View.VISIBLE);
            layoutManualLocation.setVisibility(View.VISIBLE);
            EditText txtLat = root.findViewById(R.id.manualLat);
            EditText txtLng = root.findViewById(R.id.manualLng);
            txtLat.setText(String.format(Locale.ENGLISH, "%f",
                    settings.getDouble(settings.getKeyFor(AppSettings.Key.LAT_FOR, 0))));
            txtLng.setText(String.format(Locale.ENGLISH, "%f",
                    settings.getDouble(settings.getKeyFor(AppSettings.Key.LNG_FOR, 0))));
        }
        return mView = root;
    }

    @Override
    public void onAttach(Context activity) {
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
            AppSettings settings = AppSettings.getInstance(getContext());
            if (settings.getDouble(settings.getKeyFor(AppSettings.Key.LAT_FOR,     0)) > -1)
                mListener.onOptionSelected();
            else
                Toast.makeText(getContext(), getContext().getString(R.string.please_specify_location),
                        Toast.LENGTH_LONG).show();
        } else if (v.getId() == R.id.prev) {
            getActivity().onBackPressed();
        }
    }

    private void okManual() {
        double lng, lat;
        try {
            EditText txtLat = mView.findViewById(R.id.manualLat);
            EditText txtLng = mView.findViewById(R.id.manualLng);
            lng = Double.parseDouble(txtLng.getText().toString());
            lat = Double.parseDouble(txtLat.getText().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getContext(), R.string.please_enter_lng_lat, Toast.LENGTH_LONG).show();
            return;
        }
        setLocation(lng, lat);
    }

    private void setLocation(double lng, double lat) {
        AppSettings settings = AppSettings.getInstance(getContext());
        settings.setLatFor(0, lat);
        settings.setLngFor(0, lng);
        mListener.onOptionSelected();
        mView.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
    }

    private void btnGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestLocation = 0;
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ConfigOnboardingActivity.PERMISSIONS_REQUEST);
            return;
        }
        if (mView.findViewById(R.id.progress).getVisibility() == View.VISIBLE)
            return;
        final LocationManager locationManager = (LocationManager) getContext()
                .getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getContext(), getContext().getString(R.string.gps_not_enable), Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.gps_location))
                .setMessage(getContext().getString(R.string.gps_long_time))
                .setPositiveButton(getContext().getString(R.string.use_my_last_location), new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Location location = locationManager.getLastKnownLocation(
                                LocationManager.GPS_PROVIDER);
                        if (location == null) {
                            Toast.makeText(getContext(), getContext().getString(R.string.gps_no_last_location), Toast.LENGTH_LONG).show();
                            findUsingGps(locationManager);
                        } else
                            setLocation(location.getLongitude(), location.getLatitude());
                    }
                })
                .setNegativeButton(getContext().getString(R.string.use_gps_anyway), new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        findUsingGps(locationManager);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void findUsingGps(LocationManager locationManager) {
        ProgressBar progressBar = mView.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                OnboardingLocationFragment.this, getContext().getMainLooper());
    }

    private void btnNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestLocation = 1;
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ConfigOnboardingActivity.PERMISSIONS_REQUEST);
            return;
        }
        if (mView.findViewById(R.id.progress).getVisibility() == View.VISIBLE)
            return;
        if (Utils.isConnected(getContext()) != Utils.CONNECTION_STATUS_CONNECTED) {
            Toast.makeText(getContext(), getContext().getString(R.string.not_connected),
                    Toast.LENGTH_LONG).show();
            return;
        }
        final LocationManager locationManager = (LocationManager) getContext()
                .getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(getContext(), getContext().getString(R.string.network_location_not_enable),
                    Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                OnboardingLocationFragment.this, getContext().getMainLooper());
        mView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocation(location.getLongitude(), location.getLatitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onPermissionGranted() {
        if (requestLocation == 0)
            btnGps();
        else if (requestLocation == 1)
            btnNetwork();
    }
}
