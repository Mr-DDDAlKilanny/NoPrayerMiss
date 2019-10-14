package kilanny.muslimalarm.data;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import kilanny.muslimalarm.R;

@Entity(tableName = "alarm",
        foreignKeys = @ForeignKey(
                entity = Barcode.class,
                parentColumns = "id",
                childColumns = "dismiss_alarm_barcode_id"
        ),
        indices = {@Index("id"), @Index("dismiss_alarm_barcode_id")}
)
public class Alarm implements Parcelable {

    public static final int DISMISS_ALARM_DEFAULT = 0;
    public static final int DISMISS_ALARM_SHAKE = 1;
    public static final int DISMISS_ALARM_MATH = 2;
    public static final int DISMISS_ALARM_BARCODE = 3;

    public static final int TIME_FAJR = 1 << 5;
    public static final int TIME_SUNRISE = 1 << 4;
    public static final int TIME_DHUHR = 1 << 3;
    public static final int TIME_ASR = 1 << 2;
    public static final int TIME_MAGHRIB = 1 << 1;
    public static final int TIME_ISHAA = 1;

    public static final int[] SOUNDS = {
            R.raw.air_raid_alarm, R.raw.alarm, R.raw.alarm_1, R.raw.alarm_clock,
            R.raw.alarm_clock_new_s5, R.raw.alarm_loud, R.raw.alarm_ring,
            R.raw.alarm_rooster, R.raw.alarm_vs_turbo, R.raw.alarms, R.raw.animal_cow,
            R.raw.animal_horse, R.raw.animal_sounds_cats, R.raw.car_alaram_2009,
            R.raw.clock_alarm_samsung, R.raw.craw, R.raw.extreme_alarm, R.raw.loud_alarm,
            R.raw.loud_alarm_clock, R.raw.loud_alarm_sound, R.raw.loud_alarm_tone,
            R.raw.loud_continuous_beep, R.raw.loud_snoring, R.raw.monkey_sound,
            R.raw.msg_foundx20, R.raw.old_alarm_clock_best, R.raw.puma_roar,
            R.raw.real_alarm_tone, R.raw.ttwu7, R.raw.warning, R.raw.wolf
    };

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "time_flags")
    public int timeFlags;

    @ColumnInfo(name = "one_time_left_flags")
    public int oneTimeLeftAlarmsTimeFlags;

    @ColumnInfo(name = "skipped_time_flag")
    public int skippedTimeFlag;

    @ColumnInfo(name = "skipped_alarm_time")
    public Long skippedAlarmTime;

    @ColumnInfo(name = "weekday_flags")
    public int weekDayFlags;

    @ColumnInfo(name = "dismiss_alarm_type")
    public int dismissAlarmType;

    @ColumnInfo(name = "dismiss_alarm_data1")
    public Integer dismissAlarmTypeData1;

    @ColumnInfo(name = "dismiss_alarm_data2")
    public Integer dismissAlarmTypeData2;

    @ColumnInfo(name = "dismiss_alarm_barcode_id")
    public Integer dismissAlarmBarcodeId;

    @ColumnInfo(name = "alarm_diff_mins")
    public int timeAlarmDiffMinutes;

    @ColumnInfo(name = "sound_level")
    public int soundLevel;

    @ColumnInfo(name = "snooze_mins")
    public int snoozeMins;

    @ColumnInfo(name = "snooze_count")
    public int snoozeCount;

    @ColumnInfo(name = "enabled")
    public boolean enabled;

    @ColumnInfo(name = "vibration_enabled")
    public boolean vibrationEnabled;

    @ColumnInfo(name = "alarm_label")
    public String alarmLabel;

    @ColumnInfo(name = "alarm_tune")
    public String alarmTune;

    @ColumnInfo(name = "snoozed_to_time")
    public Long snoozedToTime;

    @ColumnInfo(name = "snoozed_count")
    public int snoozedCount;

    public Alarm() {
    }

    protected Alarm(Parcel in) {
        id = in.readInt();
        timeFlags = in.readInt();
        oneTimeLeftAlarmsTimeFlags = in.readInt();
        skippedTimeFlag = in.readInt();
        if (in.readByte() == 0) {
            skippedAlarmTime = null;
        } else {
            skippedAlarmTime = in.readLong();
        }
        weekDayFlags = in.readInt();
        dismissAlarmType = in.readInt();
        if (in.readByte() == 0) {
            dismissAlarmTypeData1 = null;
        } else {
            dismissAlarmTypeData1 = in.readInt();
        }
        if (in.readByte() == 0) {
            dismissAlarmTypeData2 = null;
        } else {
            dismissAlarmTypeData2 = in.readInt();
        }
        if (in.readByte() == 0) {
            dismissAlarmBarcodeId = null;
        } else {
            dismissAlarmBarcodeId = in.readInt();
        }
        snoozedCount = in.readInt();
        if (in.readByte() == 0) {
            snoozedToTime = null;
        } else {
            snoozedToTime = in.readLong();
        }
        timeAlarmDiffMinutes = in.readInt();
        soundLevel = in.readInt();
        snoozeMins = in.readInt();
        snoozeCount = in.readInt();
        enabled = in.readByte() != 0;
        vibrationEnabled = in.readByte() != 0;
        alarmLabel = in.readString();
        alarmTune = in.readString();
    }

    public Alarm copy() {
        Intent intent = new Intent();
        intent.putExtra("o", this);
        return intent.getParcelableExtra("o");
    }

    public String toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("timeFlags", timeFlags);
        jsonObject.put("oneTimeLeftAlarmsTimeFlags", oneTimeLeftAlarmsTimeFlags);
        jsonObject.put("skippedTimeFlag", skippedTimeFlag);
        jsonObject.put("skippedAlarmTime", skippedAlarmTime);
        jsonObject.put("weekDayFlags", weekDayFlags);
        jsonObject.put("dismissAlarmType", dismissAlarmType);
        jsonObject.put("dismissAlarmTypeData1", dismissAlarmTypeData1);
        jsonObject.put("dismissAlarmTypeData2", dismissAlarmTypeData2);
        jsonObject.put("dismissAlarmBarcodeId", dismissAlarmBarcodeId);
        jsonObject.put("timeAlarmDiffMinutes", timeAlarmDiffMinutes);
        jsonObject.put("soundLevel", soundLevel);
        jsonObject.put("snoozeMins", snoozeMins);
        jsonObject.put("snoozeCount", snoozeCount);
        jsonObject.put("enabled", enabled);
        jsonObject.put("vibrationEnabled", vibrationEnabled);
        jsonObject.put("alarmTune", alarmTune);
        jsonObject.put("snoozedToTime", snoozedToTime);
        jsonObject.put("snoozedCount", snoozedCount);
        return jsonObject.toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(timeFlags);
        dest.writeInt(oneTimeLeftAlarmsTimeFlags);
        dest.writeInt(skippedTimeFlag);
        if (skippedAlarmTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(skippedAlarmTime);
        }
        dest.writeInt(weekDayFlags);
        dest.writeInt(dismissAlarmType);
        if (dismissAlarmTypeData1 == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(dismissAlarmTypeData1);
        }
        if (dismissAlarmTypeData2 == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(dismissAlarmTypeData2);
        }
        if (dismissAlarmBarcodeId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(dismissAlarmBarcodeId);
        }
        dest.writeInt(snoozedCount);
        if (snoozedToTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(snoozedToTime);
        }
        dest.writeInt(timeAlarmDiffMinutes);
        dest.writeInt(soundLevel);
        dest.writeInt(snoozeMins);
        dest.writeInt(snoozeCount);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeByte((byte) (vibrationEnabled ? 1 : 0));
        dest.writeString(alarmLabel);
        dest.writeString(alarmTune);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Alarm fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Alarm alarm = new Alarm();
        alarm.id = jsonObject.getInt("id");
        alarm.timeFlags = jsonObject.getInt("timeFlags");
        alarm.oneTimeLeftAlarmsTimeFlags = jsonObject.getInt("oneTimeLeftAlarmsTimeFlags");
        alarm.skippedTimeFlag = jsonObject.getInt("skippedTimeFlag");
        if (jsonObject.has("skippedAlarmTime"))
            alarm.skippedAlarmTime = jsonObject.getLong("skippedAlarmTime");
        alarm.weekDayFlags = jsonObject.getInt("weekDayFlags");
        alarm.dismissAlarmType = jsonObject.getInt("dismissAlarmType");
        if (jsonObject.has("dismissAlarmTypeData1"))
            alarm.dismissAlarmTypeData1 = jsonObject.getInt("dismissAlarmTypeData1");
        if (jsonObject.has("dismissAlarmTypeData2"))
            alarm.dismissAlarmTypeData2 = jsonObject.getInt("dismissAlarmTypeData2");
        if (jsonObject.has("dismissAlarmBarcodeId"))
            alarm.dismissAlarmBarcodeId = jsonObject.getInt("dismissAlarmBarcodeId");
        alarm.snoozedCount = jsonObject.getInt("snoozedCount");
        if (jsonObject.has("snoozedToTime"))
            alarm.snoozedToTime = jsonObject.getLong("snoozedToTime");
        alarm.timeAlarmDiffMinutes = jsonObject.getInt("timeAlarmDiffMinutes");
        alarm.soundLevel = jsonObject.getInt("soundLevel");
        alarm.snoozeMins = jsonObject.getInt("snoozeMins");
        alarm.snoozeCount = jsonObject.getInt("snoozeCount");
        alarm.enabled = jsonObject.getBoolean("enabled");
        alarm.vibrationEnabled = jsonObject.getBoolean("vibrationEnabled");
        if (jsonObject.has("alarmTune"))
            alarm.alarmTune = jsonObject.getString("alarmTune");
        return alarm;
    }

    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };
}