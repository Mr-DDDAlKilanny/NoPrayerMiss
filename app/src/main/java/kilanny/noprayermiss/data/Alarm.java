package kilanny.noprayermiss.data;

import java.io.Serializable;

public class Alarm implements Serializable {

    public static final int DISMISS_ALARM_DEFAULT = 0;
    public static final int DISMISS_ALARM_SHAKE = 1;
    public static final int DISMISS_ALARM_MATH = 2;
    public static final int DISMISS_ALARM_BARCODE = 3;

    public static final int TIME_FAJR = 1;
    public static final int TIME_SUNRISE = 1 << 2;
    public static final int TIME_DHUHR = 1 << 3;
    public static final int TIME_ASR = 1 << 4;
    public static final int TIME_MAGHRIB = 1 << 5;
    public static final int TIME_ISHAA = 1 << 6;

    static final long serialVersionUID = 1L;

    public int timeFlags;
    public int weekDayFlags;
    public int dismissAlarmType;
    public Object dismissAlarmInfo;
    public int timeAlarmDiffMinutes;
    public int soundLevel;
    public int snoozeMins;
    public boolean skipNext, enabled, vibrationEnabled;
}
