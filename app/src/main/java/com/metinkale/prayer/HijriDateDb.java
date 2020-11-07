package com.metinkale.prayer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DateMap.class}, version = 1)
public abstract class HijriDateDb extends RoomDatabase {
    private static HijriDateDb instance;

    public static HijriDateDb getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), HijriDateDb.class, "date")
                    .createFromAsset("databases/date.db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract DateMapDao dateMapDao();
}
