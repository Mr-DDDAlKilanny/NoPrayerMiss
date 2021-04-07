package kilanny.muslimalarm.fragments.onboardingconfig;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.arch.core.util.Function;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.activities.ConfigOnboardingActivity;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.util.AppExecutors;
import kilanny.muslimalarm.util.Utils;

public class OnboardingLocationFragment extends OnboardingBaseFragment
        implements LocationListener, ConfigOnboardingActivity.PermissionCallback {

    //private static final int AUTOCOMPLETE_REQUEST_CODE = 12345;

    public static OnboardingLocationFragment newInstance() {
        OnboardingLocationFragment fragment = new OnboardingLocationFragment();
        return fragment;
    }

    private OnOnboardingOptionSelectedListener mListener;
    private View mView;
    private AlertDialog mInputStringDlg, mSearchCityDlg;
    private WebView mWebView;
    private int requestLocation = -1;

    public OnboardingLocationFragment() {
    }

    private void tryGetWebViewCoords(int attempt, final Function<Double[], Void> output) {
        AppExecutors.getInstance().executeOnCachedExecutor(() -> {
            if (attempt == 10) {
                Log.d("webView/Url", "Failed to get coords from url !");
                output.apply(null);
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FragmentActivity activity = getActivity();
            if (activity == null) return; // user closed app
            activity.runOnUiThread(() -> {
                String url11 = mWebView.getUrl();
                int idx = url11.indexOf("/@");
                if (idx < 0)
                    tryGetWebViewCoords(attempt + 1, output);
                else {
                    Double[] res;
                    try {
                        url11 = url11.substring(idx + 2);
                        url11 = url11.substring(0,
                                url11.indexOf('/') == -1 ? url11.length() : url11.indexOf('/'));
                        String[] strs = url11.split(",");
                        res = new Double[]{Double.parseDouble(strs[0]), Double.parseDouble(strs[1])};
                    } catch (Throwable e) {
                        Log.d("webView/Url", "Failed to get coords from url: " + mWebView.getUrl());
                        e.printStackTrace();
                        output.apply(null);
                        return;
                    }
                    Log.d("webView/Url", String.format(Locale.ENGLISH, "%f - %f",
                            res[0], res[1]));
                    output.apply(res);
                }
            });
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_location, container, false);
        mWebView = new WebView(getContext().getApplicationContext()); // API 21 bug
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mSearchCityDlg != null && mSearchCityDlg.isShowing()) {
                    tryGetWebViewCoords(0, input -> {
                        FragmentActivity activity = getActivity();
                        if (activity == null) return null;
                        activity.runOnUiThread(() -> {
                            if (mSearchCityDlg != null && mSearchCityDlg.isShowing()) {
                                mSearchCityDlg.dismiss();
                                mSearchCityDlg = null;
                            }
                            if (input == null) {
                                Utils.showAlert(getContext(), getString(R.string.prayer_time_config),
                                        getString(R.string.city_search_failed), null);
                            } else {
                                setLocation(input[1], input[0]);
                            }
                        });
                        return null;
                    });
                }
            }
        });
        final View layoutManualLocation = root.findViewById(R.id.layoutManualLocation);

        //should be the first fragment, dont need it.
        root.findViewById(R.id.prev).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.btnManualLocation).setOnClickListener(view -> {
            if (layoutManualLocation.getVisibility() == View.INVISIBLE)
                layoutManualLocation.setVisibility(View.VISIBLE);
            else
                layoutManualLocation.setVisibility(View.INVISIBLE);
        });
        root.findViewById(R.id.next).setOnClickListener(this);

        root.findViewById(R.id.btnSearchCity).setOnClickListener(view -> btnSearchCity());
        root.findViewById(R.id.btnManualOk).setOnClickListener(view -> okManual());
        root.findViewById(R.id.btnDetectLocationWithGps).setOnClickListener(view -> btnGps());
        root.findViewById(R.id.btnDetectLocationWithNetwork).setOnClickListener(view -> btnNetwork());

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
            //txtLat.clearFocus();
            //txtLng.clearFocus();
        }
        return mView = root;
    }

    private void btnSearchCity() {
        if (Utils.isConnected(getContext()) != Utils.CONNECTION_STATUS_CONNECTED) {
            Toast.makeText(getContext(), R.string.not_connected,
                    Toast.LENGTH_LONG).show();
            return;
        }
        inputString(getString(R.string.search_your_city), "", "", input -> {
            mWebView.loadUrl(
                    String.format("https://www.google.com/maps/search/%s?hl=en&source=opensearch",
                            input));
            mSearchCityDlg = Utils.showIndeterminateProgressDialog(getContext(),
                    getString(R.string.search_your_city), false);
            return null;
        });
//        if (!Utils.isGooglePlayServicesAvailable(getContext())) {
//            Toast.makeText(getContext(), R.string.device_feature_not_supported,
//                    Toast.LENGTH_LONG).show();
//            return;
//        }
//        try {
//            // Initialize the SDK
//            Places.initialize(getContext(), getString(R.string.google_places_api_key));
//            // Set the fields to specify which types of place data return after the user has made a selection.
//            // Start the autocomplete intent.
//            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
//                    Collections.singletonList(Place.Field.LAT_LNG))
//                    .setTypeFilter(TypeFilter.CITIES)
//                    .setHint(getString(R.string.search_your_city))
//                    .build(getContext());
//            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Utils.showAlert(getContext(), "", getString(R.string.places_search_error));
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
//            boolean success = false;
//            String err = null;
//            if (resultCode == Activity.RESULT_OK && data != null) {
//                Place place = Autocomplete.getPlaceFromIntent(data);
//                LatLng latLng = place.getLatLng();
//                Log.d("PlacesAPI", "Selected: " + latLng);
//                if (latLng != null) {
//                    setLocation(latLng.longitude, latLng.latitude);
//                    success = true;
//                }
//            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
//                Status status = Autocomplete.getStatusFromIntent(data);
//                err = status.getStatusMessage();
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                Toast.makeText(getContext(), R.string.auto_location,
//                        Toast.LENGTH_LONG).show();
//                return;
//            }
//
//            if (!success) {
//                if (err == null)
//                    err  = getString(R.string.places_search_error);
//                Log.d("PlacesAPI", err);
//                Utils.showAlert(getContext(), "", err);
//            }
//        }
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
    public void onDestroyView() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        mView = null;
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.next) {
            AppSettings settings = AppSettings.getInstance(getContext());
            if (settings.getDouble(settings.getKeyFor(AppSettings.Key.LAT_FOR,     0)) > -1)
                mListener.onOptionSelected();
            else
                Toast.makeText(getContext(), R.string.please_specify_location,
                        Toast.LENGTH_LONG).show();
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
        if (mListener != null) {
            mListener.onOptionSelected();
            if (mView != null) {
                mView.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            }
        }
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
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.gps_not_enable)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.gps_location))
                .setMessage(getString(R.string.gps_long_time))
                .setPositiveButton(getString(R.string.use_my_last_location), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    Location location = locationManager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER);
                    if (location == null)
                        location = locationManager.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER);
                    if (location == null)
                        Toast.makeText(getContext(), R.string.gps_no_last_location, Toast.LENGTH_LONG).show();
                    else
                        setLocation(location.getLongitude(), location.getLatitude());
                })
                .setNegativeButton(getString(R.string.use_gps_anyway), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (getContext() == null)
                        return;
                    ProgressBar progressBar = mView.findViewById(R.id.progress);
                    progressBar.setVisibility(View.VISIBLE);
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                            OnboardingLocationFragment.this, getContext().getMainLooper());
                })
                .setNeutralButton(R.string.detect_with_network, (dialog, which) -> btnNetwork())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
            Toast.makeText(getContext(), R.string.not_connected,
                    Toast.LENGTH_LONG).show();
            return;
        }
        final LocationManager locationManager = (LocationManager) getContext()
                .getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.network_location_not_enable)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return;
        }
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                OnboardingLocationFragment.this, getContext().getMainLooper());
        mView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isVisible())
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
        if (isVisible()) {
            if (requestLocation == 0)
                btnGps();
            else if (requestLocation == 1)
                btnNetwork();
        }
    }

    private void inputString(String title, String hint, String initValue,
                             final Function<String, Void> resultCallback) {
        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setView(R.layout.edit_text)
                .setCancelable(true);
        View view = View.inflate(getContext(), R.layout.edit_text, null);
        TextInputEditText textView = view.findViewById(android.R.id.text1);
        textView.setText(initValue);
        TextInputLayout text = view.findViewById(R.id.text_input_layout);
        text.setHint(hint);
        textView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        textView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (v.getText() != null) {
                    resultCallback.apply(v.getText().toString());
                    mInputStringDlg.dismiss();
                }
                return true;
            }
            return false;
        });
        b.setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    TextInputEditText input = ((AlertDialog) dialog).findViewById(android.R.id.text1);
                    if (input.getText() == null) return;
                    resultCallback.apply(input.getText().toString());
                    dialog.dismiss();
                });
        mInputStringDlg = b.create();
        mInputStringDlg.show();
    }
}
