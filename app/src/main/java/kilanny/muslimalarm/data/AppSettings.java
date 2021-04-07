//https://github.com/alphamu/PrayTime-Android
package kilanny.muslimalarm.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.text.format.DateFormat;

import androidx.annotation.RawRes;

import java.lang.ref.WeakReference;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.util.PrayTime;

/*
 * A Singleton for managing your SharedPreferences.
 *
 * You should make sure to change the SETTINGS_NAME to what you want
 * and choose the operating made that suits your needs, the default is
 * MODE_PRIVATE.
 *
 * IMPORTANT: The class is not thread safe. It should work fine in most
 * circumstances since the write and read operations are fast. However
 * if you call edit for bulk updates and do not commit your changes
 * there is a possibility of data loss if a background thread has modified
 * preferences at the same time.
 *
 * Usage:
 *
 * int sampleInt = AppSettings.getInstance(context).getInt(Key.SAMPLE_INT);
 * AppSettings.getInstance(context).set(Key.SAMPLE_INT, sampleInt);
 *
 * If AppSettings.getInstance(Context) has been called once, you can
 * simple use AppSettings.getInstance() to save some precious line space.
 */
public class AppSettings {
    //public static final PrayTime sDefaults = new PrayTime();

    private static final String SETTINGS_NAME = "default_settings";
    private static AppSettings sSharedPrefs;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;
    private WeakReference<Context> mContextRef;

    /**
     * Class for keeping all the keys used for shared preferences in one place.
     */
    public static class Key {
        /* Recommended naming convention:
         * ints, floats, doubles, longs:
         * SAMPLE_NUM or SAMPLE_COUNT or SAMPLE_INT, SAMPLE_LONG etc.
         *
         * boolean: IS_SAMPLE, HAS_SAMPLE, CONTAINS_SAMPLE
         *
         * String: SAMPLE_KEY, SAMPLE_STR or just SAMPLE
         */
        //ALARM RELATED
        public static final String IS_ALARM_SET = "is_alarm_set_for_%d";
        public static final String IS_FAJR_ALARM_SET = "is_fajr_alarm_set_for_%d";
        public static final String IS_DHUHR_ALARM_SET = "is_dhuhr_alarm_set_for_%d";
        public static final String IS_ASR_ALARM_SET = "is_asr_alarm_set_for_%d";
        public static final String IS_MAGHRIB_ALARM_SET = "is_maghrib_alarm_set_for_%d";
        public static final String IS_ISHA_ALARM_SET = "is_isha_alarm_set_for_%d";
        public static final String IS_RAMADAN = "is_ramadan";
        public static final String SUHOOR_OFFSET = "suhoor_offset";
        public static final String IFTAR_OFFSET = "iftar_offset";
        public static final String IS_ASCENDING_ALARM = "is_ascending_alarm";
        public static final String IS_RANDOM_ALARM = "is_random_alarm";
        public static final String SELECTED_RINGTONE = "ringtone_selected";
        public static final String SELECTED_RINGTONE_NAME = "ringtone_selected_name";
        public static final String USE_ADHAN = "use_adhan";

        //CONFIG RELATED
        public static final String HAS_DEFAULT_SET = "has_default_set";
        public static final String CALC_METHOD = "calc_method_for_%d";
        public static final String ASR_METHOD = "asr_method_for_%d";
        public static final String ADJUST_METHOD = "adjust_high_latitudes_method_for_%d";
        public static final String TIME_FORMAT = "time_format_for_%d";

        //LOCATION RELATED
        public static final String LAT_FOR = "lat_for_%d";
        public static final String LNG_FOR = "lng_for_%d";
        public static final String SHOW_ORIENATATION_INSTRACTIONS = "showOrientationInstructions";

        //APP RELATED
        public static final String IS_INIT = "app_init";
        public static final String APP_VERSION_CODE = "current_version_code";
        public static final String IS_TNC_ACCEPTED = "is_tnc_accepted";

    }


    private AppSettings(Context context) {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        mContextRef = new WeakReference<Context>(context);
    }


    public static AppSettings getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new AppSettings(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public void set(String key, String val) {
        doEdit();
        mEditor.putString(key, val);
        doCommit();
    }

    public void set(String key, int val) {
        doEdit();
        mEditor.putInt(key, val);
        doCommit();
    }

    public void set(String key, boolean val) {
        doEdit();
        mEditor.putBoolean(key, val);
        doCommit();
    }

    public void set(String key, float val) {
        doEdit();
        mEditor.putFloat(key, val);
        doCommit();
    }

    /**
     * Convenience method for storing doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The nameResId of the preference to store.
     * @param val The new value for the preference.
     */
    public void set(String key, double val) {
        doEdit();
        mEditor.putString(key, String.valueOf(val));
        doCommit();
    }

    public void set(String key, long val) {
        doEdit();
        mEditor.putLong(key, val);
        doCommit();
    }

    public String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    public String getString(String key) {
        return mPref.getString(key, null);
    }

    public int getInt(String key) {
        return mPref.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return mPref.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return mPref.getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return mPref.getLong(key, defaultValue);
    }

    public float getFloat(String key) {
        return mPref.getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue) {
        return mPref.getFloat(key, defaultValue);
    }

    /**
     * Convenience method for retrieving doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The nameResId of the preference to fetch.
     */
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    /**
     * Convenience method for retrieving doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The nameResId of the preference to fetch.
     */
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.valueOf(mPref.getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mPref.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return mPref.getBoolean(key, false);
    }

    /**
     * Remove keys from SharedPreferences.
     *
     * @param keys The nameResId of the key(s) to be removed.
     */
    public void remove(String... keys) {
        doEdit();
        for (String key : keys) {
            mEditor.remove(key);
        }
        doCommit();
    }

    /**
     * Remove all keys from SharedPreferences.
     */
    public void clear() {
        doEdit();
        mEditor.clear();
        doCommit();
    }

    public void edit() {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void commit() {
        mBulkUpdate = false;
        mEditor.commit();
        mEditor = null;
    }

    private void doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }

    public String getKeyFor(String key, int index) {
        return String.format(key, index);
    }

    public boolean isAlarmSetFor(int index) {
        return getBoolean(getKeyFor(Key.IS_ALARM_SET, index));
    }

    public void setAlarmFor(int index, boolean alarmOn) {
        set(getKeyFor(Key.IS_ALARM_SET, index), alarmOn);
    }

    public int getCalcMethodSetFor(int index) {
        return getInt(getKeyFor(Key.CALC_METHOD, index), PrayTime.MWL);
    }

    public void setCalcMethodFor(int index, int value) {
        set(getKeyFor(Key.CALC_METHOD, index), value);
    }

    public int getAsrMethodSetFor(int index) {
        return getInt(String.format(Key.ASR_METHOD, index), PrayTime.SHAFII);
    }

    public void setAsrMethodFor(int index, int value) {
        set(getKeyFor(Key.ASR_METHOD, index), value);
    }

    public int getHighLatitudeAdjustmentFor(int index) {
        return getInt(getKeyFor(Key.ADJUST_METHOD, index), PrayTime.ONE_SEVENTH);
    }

    public void setHighLatitudeAdjustmentMethodFor(int index, int value) {
        set(getKeyFor(Key.ADJUST_METHOD, index), value);
    }

    public int getTimeFormatFor(int index) {
        return getInt(getKeyFor(Key.TIME_FORMAT, index), DateFormat.is24HourFormat(mContextRef.get())? PrayTime.TIME_24 : PrayTime.TIME_12);
    }

    public void setTimeFormatFor(int index, int format) {
        set(getKeyFor(Key.TIME_FORMAT, index), format);
    }

    public double getLatFor(int index) {
        return getDouble(getKeyFor(Key.LAT_FOR, index));
    }

    public double getLngFor(int index) {
        return getDouble(getKeyFor(Key.LNG_FOR, index));
    }


    public void setLatFor(int index, double lat) {
        set(getKeyFor(Key.LAT_FOR, index), lat);
    }

    public void setLngFor(int index, double lng) {
        set(getKeyFor(Key.LNG_FOR, index), lng);
    }

    public boolean isDefaultSet() {
        return getBoolean(Key.HAS_DEFAULT_SET);
    }

    public int getAzanAlarmSoundLevel() {
        return getInt("azanAlarmSoundLevel", 100);
    }

    public void setAzanAlarmSoundLevel(int level) {
        set("azanAlarmSoundLevel", level);
    }

    public int getIqamahAlarmSoundLevel() {
        return getInt("iqamahAlarmSoundLevel", 100);
    }

    public void setIqamahAlarmSoundLevel(int level) {
        set("iqamahAlarmSoundLevel", level);
    }

    public int getAzanAlarmTune() {
        return getInt("azanAlarmTune", R.raw.azan_qatami);
    }

    public void setAzanAlarmTune(@RawRes int tune) {
        set("azanAlarmTune", tune);
    }

    public int getAlarmType() {
        return getInt("alarmType", AudioManager.STREAM_NOTIFICATION);
    }

    public void setAlarmType(int type) {
        set("alarmType", type);
    }

    public int getIqamahAlarmTune() {
        return getInt("iqamahAlarmTune", R.raw.azan_haya);
    }

    public void setIqamahAlarmTune(@RawRes int tune) {
        set("iqamahAlarmTune", tune);
    }

    public boolean isAzanAlarmsEnabled() {
        return getBoolean("azanAlarmsEnabled", true);
    }

    public void setAzanAlarmsEnabled(boolean enabled) {
        set("azanAlarmsEnabled", enabled);
    }

    public boolean isAzanViberationEnabled() {
        return getBoolean("azanViberationEnabled", false);
    }

    public void setAzanViberationEnabled(boolean enabled) {
        set("azanViberationEnabled", enabled);
    }

    public boolean isZuhrAzanAlarmEnabled() {
        return getBoolean("zuhrAzanAlarmEnabled", true);
    }

    public void setZuhrAzanAlarmEnabled(boolean enabled) {
        set("zuhrAzanAlarmEnabled", enabled);
    }

    public boolean isAsrAzanAlarmEnabled() {
        return getBoolean("asrAzanAlarmEnabled", true);
    }

    public void setAsrAzanAlarmEnabled(boolean enabled) {
        set("asrAzanAlarmEnabled", enabled);
    }

    public boolean isMagribAzanAlarmEnabled() {
        return getBoolean("magribAzanAlarmEnabled", true);
    }

    public void setMafribAzanAlarmEnabled(boolean enabled) {
        set("magribAzanAlarmEnabled", enabled);
    }

    public boolean isIshaAzanAlarmEnabled() {
        return getBoolean("ishaAzanAlarmEnabled", true);
    }

    public void setIshaAzanAlarmEnabled(boolean enabled) {
        set("ishaAzanAlarmEnabled", enabled);
    }

    public boolean isFajrAzanAlarmEnabled() {
        return getBoolean("fajrAzanAlarmEnabled", true);
    }

    public void setFajrAzanAlarmEnabled(boolean enabled) {
        set("fajrAzanAlarmEnabled", enabled);
    }

    public boolean isJumaahAzanAlarmEnabled() {
        return getBoolean("jumaahAzanAlarmEnabled", true);
    }

    public void setJumaahAzanAlarmEnabled(boolean enabled) {
        set("jumaahAzanAlarmEnabled", enabled);
    }

    public int getZuhrAzanAlarmOffsetMins() {
        return getInt("zuhrAzanAlarmOffsetMins", 0);
    }

    public void setZuhrAzanAlarmOffsetMins(int OffsetMins) {
        set("zuhrAzanAlarmOffsetMins", OffsetMins);
    }

    public int getAsrAzanAlarmOffsetMins() {
        return getInt("asrAzanAlarmOffsetMins", 0);
    }

    public void setAsrAzanAlarmOffsetMins(int OffsetMins) {
        set("asrAzanAlarmOffsetMins", OffsetMins);
    }

    public int getMagribAzanAlarmOffsetMins() {
        return getInt("magribAzanAlarmOffsetMins", 0);
    }

    public void setMafribAzanAlarmOffsetMins(int OffsetMins) {
        set("magribAzanAlarmOffsetMins", OffsetMins);
    }

    public int getIshaAzanAlarmOffsetMins() {
        return getInt("ishaAzanAlarmOffsetMins", 0);
    }

    public void setIshaAzanAlarmOffsetMins(int OffsetMins) {
        set("ishaAzanAlarmOffsetMins", OffsetMins);
    }

    public int getFajrAzanAlarmOffsetMins() {
        return getInt("fajrAzanAlarmOffsetMins", 0);
    }

    public void setFajrAzanAlarmOffsetMins(int OffsetMins) {
        set("fajrAzanAlarmOffsetMins", OffsetMins);
    }

    public int getJumaahAzanAlarmOffsetMins() {
        return getInt("jumaahAzanAlarmOffsetMins", 0);
    }

    public void setJumaahAzanAlarmOffsetMins(int OffsetMins) {
        set("jumaahAzanAlarmOffsetMins", OffsetMins);
    }

    public boolean isIqamahAlarmsEnabled() {
        return getBoolean("iqamahAlarmsEnabled", false);
    }

    public void setIqamahAlarmsEnabled(boolean enabled) {
        set("iqamahAlarmsEnabled", enabled);
    }

    public boolean isIqamahViberationEnabled() {
        return getBoolean("iqamahViberationEnabled", false);
    }

    public void setIqamahViberationEnabled(boolean enabled) {
        set("iqamahViberationEnabled", enabled);
    }

    public boolean isZuhrIqamahAlarmEnabled() {
        return getBoolean("zuhrIqamahAlarmEnabled", true);
    }

    public void setZuhrIqamahAlarmEnabled(boolean enabled) {
        set("zuhrIqamahAlarmEnabled", enabled);
    }

    public boolean isAsrIqamahAlarmEnabled() {
        return getBoolean("asrIqamahAlarmEnabled", true);
    }

    public void setAsrIqamahAlarmEnabled(boolean enabled) {
        set("asrIqamahAlarmEnabled", enabled);
    }

    public boolean isMagribIqamahAlarmEnabled() {
        return getBoolean("magribIqamahAlarmEnabled", true);
    }

    public void setMafribIqamahAlarmEnabled(boolean enabled) {
        set("magribIqamahAlarmEnabled", enabled);
    }

    public boolean isIshaIqamahAlarmEnabled() {
        return getBoolean("ishaIqamahAlarmEnabled", true);
    }

    public void setIshaIqamahAlarmEnabled(boolean enabled) {
        set("ishaIqamahAlarmEnabled", enabled);
    }

    public boolean isFajrIqamahAlarmEnabled() {
        return getBoolean("fajrIqamahAlarmEnabled", true);
    }

    public void setFajrIqamahAlarmEnabled(boolean enabled) {
        set("fajrIqamahAlarmEnabled", enabled);
    }

    public boolean isJumaahIqamahAlarmEnabled() {
        return getBoolean("jumaahIqamahAlarmEnabled", true);
    }

    public void setJumaahIqamahAlarmEnabled(boolean enabled) {
        set("jumaahIqamahAlarmEnabled", enabled);
    }

    public int getZuhrIqamahAlarmPeriodMins() {
        return getInt("zuhrIqamahAlarmPeriodMins", 10);
    }

    public void setZuhrIqamahAlarmPeriodMins(int periodMins) {
        set("zuhrIqamahAlarmPeriodMins", periodMins);
    }

    public int getAsrIqamahAlarmPeriodMins() {
        return getInt("asrIqamahAlarmPeriodMins", 10);
    }

    public void setAsrIqamahAlarmPeriodMins(int periodMins) {
        set("asrIqamahAlarmPeriodMins", periodMins);
    }

    public int getMagribIqamahAlarmPeriodMins() {
        return getInt("magribIqamahAlarmPeriodMins", 10);
    }

    public void setMagribIqamahAlarmPeriodMins(int periodMins) {
        set("magribIqamahAlarmPeriodMins", periodMins);
    }

    public int getIshaIqamahAlarmPeriodMins() {
        return getInt("ishaIqamahAlarmPeriodMins", 10);
    }

    public void setIshaIqamahAlarmPeriodMins(int periodMins) {
        set("ishaIqamahAlarmPeriodMins", periodMins);
    }

    public int getFajrIqamahAlarmPeriodMins() {
        return getInt("fajrIqamahAlarmPeriodMins", 10);
    }

    public void setFajrIqamahAlarmPeriodMins(int periodMins) {
        set("fajrIqamahAlarmPeriodMins", periodMins);
    }

    public int getJumaahIqamahAlarmPeriodMins() {
        return getInt("jumaahIqamahAlarmPeriodMins", 10);
    }

    public void setJumaahIqamahAlarmPeriodMins(int periodMins) {
        set("jumaahIqamahAlarmPeriodMins", periodMins);
    }


}
