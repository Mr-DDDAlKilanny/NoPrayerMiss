package kilanny.muslimalarm.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Barcode.class, Alarm.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDb extends RoomDatabase {

    private static AppDb instance;

    public abstract BarcodeDao barcodeDao();

    public abstract AlarmDao alarmDao();

    public static AppDb getInstance(Context context) {
        if (instance != null)
            return instance;
        instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDb.class, "app-db").build();
        return instance;
    }
}
