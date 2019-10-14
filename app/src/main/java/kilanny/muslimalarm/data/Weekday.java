package kilanny.muslimalarm.data;

public interface Weekday {
    public static final int NO_REPEAT = 0;
    public static final int FRIDAY = 1 << 6;
    public static final int SATURDAY = 1 << 5;
    public static final int SUNDAY = 1 << 4;
    public static final int MONDAY = 1 << 3;
    public static final int TUESDAY = 1 << 2;
    public static final int WEDNESDAY = 1 << 1;
    public static final int THURSDAY = 1;
}
