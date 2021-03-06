package kilanny.muslimalarm.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AlarmDao;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.data.SerializableInFile;
import kilanny.muslimalarm.fragments.AcademyAdFragment;
import kilanny.muslimalarm.fragments.AlarmsHomeFragment;
import kilanny.muslimalarm.fragments.PrayerTimesHomeFragment;
import kilanny.muslimalarm.services.AlarmRingingService;
import kilanny.muslimalarm.util.AnalyticsTrackers;
import kilanny.muslimalarm.util.Utils;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        AlarmsHomeFragment.OnFragmentInteractionListener {

    private static final int REQUEST_ONBOARDING = 102;
    private static final int REQUEST_ADD_ALARM = 0;
    private static final int REQUEST_EDIT_ALARM = 1;

    private PrayerTimesHomeFragment prayerTimesHomeFragment;
    private AlarmsHomeFragment alarmsHomeFragment;
    private int mSelectedFragmentItemId;
    SerializableInFile<Integer> appResponse;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_EDIT_ALARM:
            case REQUEST_ADD_ALARM:
                if (resultCode == EditAlarmOnboardingActivity.RESULT_CODE_OK && data != null) {
                    Alarm alarm = data.getParcelableExtra(EditAlarmOnboardingActivity.RESULT_ALARM);
                    if (alarm != null) {
                        boolean isNew = requestCode == REQUEST_ADD_ALARM;
                        onAlarmEdited(isNew, alarm);
                        AnalyticsTrackers.getInstance(this).logModifyAlarm(alarm, isNew);
                    }
                }
                break;
            case REQUEST_ONBOARDING:
                if (prayerTimesHomeFragment != null && prayerTimesHomeFragment.isAdded())
                    prayerTimesHomeFragment.recalculate();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onAlarmEdited(final boolean isNew, final Alarm alarm) {
        Utils.runInBackground((Function<Context, Pair<Context, Alarm[]>>) input -> {
            AlarmDao alarmDao = AppDb.getInstance(input).alarmDao();
            alarm.snoozedCount = 0;
            alarm.snoozedToTime = null;
            alarm.skippedTimeFlag = 0;
            alarm.skippedAlarmTime = null;
            alarm.oneTimeLeftAlarmsTimeFlags = alarm.timeFlags;
            alarm.enabled = true;
            if (isNew) {
                alarmDao.insert(alarm);
            } else {
                alarmDao.update(alarm);
            }
            return new Pair<>(input, alarmDao.getAll());
        }, input -> {
            Utils.scheduleAndDeletePrevious(input.first, input.second);
            if (alarmsHomeFragment != null) {
                alarmsHomeFragment.onDataSetChanged(null);
            }
            return null;
        }, this);
    }

    private void startOnbroading() {
        Intent intent = new Intent(getApplicationContext(), ConfigOnboardingActivity.class);
        intent.putExtra(ConfigOnboardingActivity.EXTRA_CARD_INDEX, 0);
        startActivityForResult(intent, REQUEST_ONBOARDING);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Utils.isServiceRunning(this, AlarmRingingService.class)) {
            startActivity(new Intent(this, ShowAlarmActivity.class));
            return;
        }
        //TODO: show message first
        AppSettings appSettings = AppSettings.getInstance(getApplicationContext());
        if (!appSettings.isDefaultSet()) {
            AnalyticsTrackers.getInstance(this).logNewUserStartConfig();
            startOnbroading();
            return;
        }
        if (!Utils.isValidTimes(this)) {
            Utils.showAlert(this, getString(R.string.prayer_times),
                    getString(R.string.times_invalid), dialog -> startOnbroading());
            return;
        } /*else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //// https://developer.android.com/about/versions/12/behavior-changes-12#exact-alarm-check-for-permission
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.canScheduleExactAlarms();
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse(getPackageName()));
            startActivityForResult(intent, 1);
        }*/

        if (Utils.isConnected(this) == Utils.CONNECTION_STATUS_CONNECTED) {
            Date date = appResponse.getFileLastModifiedDate(getApplicationContext());
            boolean display;
            if (date != null) {
                long diffTime = new Date().getTime() - date.getTime();
                long diffDays = diffTime / (1000 * 60 * 60 * 24);
                display = diffDays >= 7;
            } else
                display = true;
            if (display) {
                displayAcademyAd();
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appResponse = new SerializableInFile<>(
                getApplicationContext(), "ad_ac__st", 0);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (savedInstanceState != null) {
            mSelectedFragmentItemId = savedInstanceState.getInt("mSelectedFragmentItemId",
                    R.id.nav_home);
        } else {
            mSelectedFragmentItemId = R.id.nav_home;
        }
        bottomNavigationView.setSelectedItemId(mSelectedFragmentItemId);

        AppSettings settings = AppSettings.getInstance(this);

        if (!settings.getBoolean(AppSettings.Key.IS_INIT)) {
            settings.set(settings.getKeyFor(AppSettings.Key.IS_ALARM_SET,         0), true);
            settings.set(settings.getKeyFor(AppSettings.Key.IS_FAJR_ALARM_SET,    0), true);
            settings.set(settings.getKeyFor(AppSettings.Key.IS_DHUHR_ALARM_SET,   0), true);
            settings.set(settings.getKeyFor(AppSettings.Key.IS_ASR_ALARM_SET,     0), true);
            settings.set(settings.getKeyFor(AppSettings.Key.IS_MAGHRIB_ALARM_SET, 0), true);
            settings.set(settings.getKeyFor(AppSettings.Key.IS_ISHA_ALARM_SET,    0), true);
            settings.set(AppSettings.Key.USE_ADHAN, true);
            settings.setLatFor(0, -1);
            settings.setLngFor(0, -1);
            settings.set(AppSettings.Key.IS_INIT, true);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("mSelectedFragmentItemId", mSelectedFragmentItemId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.nav_tools) {
            startOnbroading();
            return true;
        } else if (id == R.id.nav_share) {
            Utils.displayShareActivity(this);
            return true;
        } else if (id == R.id.nav_academy) {
            displayAcademyAd();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;

        mSelectedFragmentItemId = id;
        if (id == R.id.nav_home) {
            fragment = prayerTimesHomeFragment = PrayerTimesHomeFragment.newInstance();
        } else if (id == R.id.nav_alarms) {
            fragment = alarmsHomeFragment = AlarmsHomeFragment.newInstance();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commitAllowingStateLoss();
        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle() + " - " + getString(R.string.app_name));
        return true;
    }

    @Override
    public void onAddNewAlarm(Boolean isFivePrayers) {
        Intent intent = new Intent(this, EditAlarmOnboardingActivity.class);
        intent.putExtra(EditAlarmOnboardingActivity.ARG_IS_FIVE_PRAYERS,
                isFivePrayers == null ? "" : isFivePrayers ? "true" : "false");
        startActivityForResult(intent, REQUEST_ADD_ALARM);
    }

    @Override
    public void onEditAlarm(Alarm alarm) {
        Intent intent = new Intent(this, EditAlarmOnboardingActivity.class);
        intent.putExtra(EditAlarmOnboardingActivity.ARG_ALARM, alarm);
        startActivityForResult(intent, REQUEST_EDIT_ALARM);
    }

    private void displayAcademyAd() {
        FragmentManager fm = getSupportFragmentManager();
        AcademyAdFragment fragment = AcademyAdFragment.newInstance();
        fragment.show(fm, "fragment_ads");
        appResponse.setData(appResponse.getData() + 1, getApplicationContext());
    }
}
