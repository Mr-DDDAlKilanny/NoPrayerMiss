package kilanny.muslimalarm.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Barcode.class, Alarm.class}, version = 5, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDb extends RoomDatabase {

    private static AppDb instance;

    private static Migration MIGRATION_1_2() {
        return new Migration(1, 2) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE alarm ADD COLUMN max_mins_ringing INTEGER");
            }
        };
    }

    private static Migration MIGRATION_2_3() {
        return new Migration(2, 3) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE alarm ADD COLUMN qeyam_night_percentage INTEGER");
            }
        };
    }

    private static Migration MIGRATION_3_4() {
        return new Migration(3, 4) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE alarm ADD COLUMN custom_time INTEGER");
            }
        };
    }

    private static Migration MIGRATION_4_5() {
        return new Migration(4, 5) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE `alarm` ADD COLUMN `stop_for_next_alarm` INTEGER NOT NULL DEFAULT 0");
            }
        };
    }

    public abstract BarcodeDao barcodeDao();

    public abstract AlarmDao alarmDao();

    public static AppDb getInstance(Context context) {
        if (instance != null)
            return instance;
        instance = Room.databaseBuilder(context.getApplicationContext(), AppDb.class,
                "app-db")
                .addMigrations(MIGRATION_1_2(), MIGRATION_2_3(), MIGRATION_3_4(), MIGRATION_4_5())
                .build();
        return instance;
    }
}
