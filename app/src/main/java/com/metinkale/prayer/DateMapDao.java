package com.metinkale.prayer;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface DateMapDao {

    @Query("SELECT * FROM date_map WHERE gy = :year AND gm = :month AND gd = :day")
    DateMap getByGreg(int year, int month, int day);
}
