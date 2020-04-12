package kilanny.muslimalarm.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import kilanny.muslimalarm.fragments.onboardingconfig.OnOnboardingOptionSelectedListener;
import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.fragments.onboardingconfig.OnboardingAdjustmentHighLatitudesFragment;
import kilanny.muslimalarm.fragments.onboardingconfig.OnboardingAsrCalculationMethodFragment;
import kilanny.muslimalarm.fragments.onboardingconfig.OnboardingCalculationMethodFragment;
import kilanny.muslimalarm.fragments.onboardingconfig.OnboardingLocationFragment;
import kilanny.muslimalarm.fragments.onboardingconfig.OnboardingTimeFormatFragment;
import kilanny.muslimalarm.util.AnalyticsTrackers;
import kilanny.muslimalarm.util.Utils;

public class ConfigOnboardingActivity extends AppCompatActivity
        implements OnOnboardingOptionSelectedListener {

    public static final String EXTRA_CARD_INDEX = "card_index";
    public static final int PERMISSIONS_REQUEST = 2;

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_config_onboarding);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Instantiate a ViewPager and a PagerAdapter.
        Intent intent = getIntent();
        int mCardIndex = intent.getIntExtra(EXTRA_CARD_INDEX, 0);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), mCardIndex);
        mPager.beginFakeDrag(); //To disable swipe
        mPager.setAdapter(mPagerAdapter);
    }

    private static boolean isAllPermissionsAccepted(int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length == 0) return false;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (isAllPermissionsAccepted(grantResults)) {
                if (mPagerAdapter != null && mPagerAdapter.locationFragment != null)
                    mPagerAdapter.locationFragment.onPermissionGranted();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.rejected_permission)
                        .show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onOptionSelected() {
        AnalyticsTrackers.getInstance(this).logSetup(mPager.getCurrentItem());
        if (mPager.getCurrentItem() + 1 == mPagerAdapter.getCount()) {
            AppSettings appSettings = AppSettings.getInstance(this);
            if (!appSettings.isDefaultSet())
                AnalyticsTrackers.getInstance(this).logNewUserEndConfig();
            appSettings.set(AppSettings.Key.HAS_DEFAULT_SET, true);
            Intent data = new Intent();
            if (getParent() == null) {
                setResult(RESULT_OK, data);
            } else {
                getParent().setResult(RESULT_OK, data);
            }
            Utils.scheduleAndDeletePreviousBackground(getApplicationContext());
            finish();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
    }

    private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        int mCardIndex = 0;
        OnboardingLocationFragment locationFragment;

        ScreenSlidePagerAdapter(FragmentManager fm, int cardIndex) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            mCardIndex = cardIndex;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return locationFragment = OnboardingLocationFragment.newInstance();
                case 1:
                    return OnboardingCalculationMethodFragment.newInstance(mCardIndex);
                case 2:
                    return OnboardingAsrCalculationMethodFragment.newInstance(mCardIndex);
                case 3:
                    return OnboardingAdjustmentHighLatitudesFragment.newInstance(mCardIndex);
                case 4:
                    return OnboardingTimeFormatFragment.newInstance(mCardIndex);
            }
            throw new IllegalArgumentException("position of Fragment out of range");
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    public interface PermissionCallback {
        void onPermissionGranted();
    }
}
