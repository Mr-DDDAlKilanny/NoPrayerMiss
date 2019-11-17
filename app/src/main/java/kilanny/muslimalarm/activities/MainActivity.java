package kilanny.muslimalarm.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.navigation.NavigationView;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AlarmDao;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.fragments.AlarmsHomeFragment;
import kilanny.muslimalarm.fragments.PrayerTimesHomeFragment;
import kilanny.muslimalarm.util.AnalyticsTrackers;
import kilanny.muslimalarm.util.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AlarmsHomeFragment.OnFragmentInteractionListener {

    private static final int REQUEST_ONBOARDING = 102;
    private static final int REQUEST_ADD_ALARM = 0;
    private static final int REQUEST_EDIT_ALARM = 1;

    private PrayerTimesHomeFragment prayerTimesHomeFragment;
    private AlarmsHomeFragment alarmsHomeFragment;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private ShowcaseView mShowcaseView = null;

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
        Utils.runInBackground(new Function<Context, Pair<Context, Alarm[]>>() {
            @Override
            public Pair<Context, Alarm[]> apply(Context input) {
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
            }
        }, new Function<Pair<Context, Alarm[]>, Void>() {
            @Override
            public Void apply(Pair<Context, Alarm[]> input) {
                Utils.scheduleAndDeletePrevious(input.first, input.second);
                if (alarmsHomeFragment != null) {
                    alarmsHomeFragment.onDataSetChanged();
                }
                return null;
            }
        }, this);
    }

    private void startOnboardingFor(int index) {
        Intent intent = new Intent(getApplicationContext(), ConfigOnboardingActivity.class);
        intent.putExtra(ConfigOnboardingActivity.EXTRA_CARD_INDEX, index);
        startActivityForResult(intent, REQUEST_ONBOARDING);
    }

    private ImageButton getNavButtonView(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++)
            if (toolbar.getChildAt(i) instanceof ImageButton)
                return (ImageButton) toolbar.getChildAt(i);
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO: show message first
        AppSettings appSettings = AppSettings.getInstance(getApplicationContext());
        if (!appSettings.isDefaultSet()) {
            startOnboardingFor(0);
            return;
        }

        int count = appSettings.getInt("menuOpenShowCaseViewCount", 0);
        if (count < 3) {
            TextPaint paintTitle = new TextPaint();
            paintTitle.setColor(Color.rgb(255, 0, 0));
            paintTitle.setTextSize(85);
            TextPaint paintText = new TextPaint();
            paintText.setColor(Color.rgb(98, 8, 8));
            paintText.setTextSize(45);

            mShowcaseView = new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(getNavButtonView((Toolbar) findViewById(R.id.toolbar))))
                    .setContentTitle(R.string.open_app_alarm_menu)
                    .setContentTitlePaint(paintTitle)
                    .setContentText(R.string.click_here_to_open_menu)
                    .setContentTextPaint(paintText)
                    .hideOnTouchOutside()
                    .build();
            appSettings.set("menuOpenShowCaseViewCount", count + 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (mShowcaseView != null) {
                    mShowcaseView.hide();
                    mShowcaseView = null;
                }
                mActionBarDrawerToggle.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                mActionBarDrawerToggle.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                mActionBarDrawerToggle.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                mActionBarDrawerToggle.onDrawerStateChanged(newState);
            }
        });
        mActionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_home) {
            fragment = prayerTimesHomeFragment = PrayerTimesHomeFragment.newInstance();
        } else if (id == R.id.nav_alarms) {
            fragment = alarmsHomeFragment = AlarmsHomeFragment.newInstance();
        }/* else if (id == R.id.nav_slideshow) {

        }*/ else if (id == R.id.nav_tools) {
            startOnboardingFor(0);
            return true;
        } else if (id == R.id.nav_share) {
            Utils.displayShareActivity(this);
            return true;
        } /*else if (id == R.id.nav_send) {

        }*/
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commitAllowingStateLoss();
        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle() + " - " + getString(R.string.app_name));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onAddNewAlarm() {
        startActivityForResult(new Intent(this, EditAlarmOnboardingActivity.class),
                REQUEST_ADD_ALARM);
    }

    @Override
    public void onEditAlarm(Alarm alarm) {
        Intent intent = new Intent(this, EditAlarmOnboardingActivity.class);
        intent.putExtra(EditAlarmOnboardingActivity.ARG_ALARM, alarm);
        startActivityForResult(intent, REQUEST_EDIT_ALARM);
    }
}
