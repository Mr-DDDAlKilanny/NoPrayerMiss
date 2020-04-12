package kilanny.muslimalarm;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.metinkale.prayer.InternalBroadcastReceiver;
import com.metinkale.prayer.utils.AndroidTimeZoneProvider;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.TimeZoneChangedReceiver;

import org.joda.time.DateTimeZone;

import kilanny.muslimalarm.util.Utils;

public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static App sApp;

    @NonNull
    private Handler mHandler = new Handler();

    private Thread.UncaughtExceptionHandler mDefaultUEH;

    @NonNull
    private Thread.UncaughtExceptionHandler mCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, @NonNull Throwable ex) {
            // This will make Crashlytics do its job
            mDefaultUEH.uncaughtException(thread, ex);
        }
    };


    @NonNull
    public static App get() {
        return sApp;
    }

    public static void setApp(App app) {
        sApp = app;
    }

    @NonNull
    public Handler getHandler() {
        return mHandler;
    }

    public App() {
        super();
        sApp = this;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        }

        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler);


        DateTimeZone.setProvider(new AndroidTimeZoneProvider());
        LocaleUtils.init(getBaseContext());


        registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));


        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);


        /*if (AppRatingDialog.getInstallationTime() == 0) {
            AppRatingDialog.setInstalltionTime(System.currentTimeMillis());
        }*/

        InternalBroadcastReceiver.loadAll();
        InternalBroadcastReceiver.sender(this).sendOnStart();

        Utils.scheduleAndDeletePreviousBackground(getApplicationContext());
    }



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null)
            return;

        InternalBroadcastReceiver.sender(this).sendOnPrefsChanged(key);
        switch (key) {
            case "language":
                LocaleUtils.init(getBaseContext());
                break;
        }
    }
}
