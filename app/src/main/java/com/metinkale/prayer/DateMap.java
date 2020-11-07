package com.metinkale.prayer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "date_map", primaryKeys = {"gd", "gm", "gy"})
public class DateMap {
    @ColumnInfo(name = "gd")
    public int gregDay;

    @ColumnInfo(name = "gm")
    public int gregMonth;

    @ColumnInfo(name = "gy")
    public int gregYear;

    @ColumnInfo(name = "dw")
    public int dayOfWeek;

    @ColumnInfo(name = "hd")
    public int hjiriDay;

    @ColumnInfo(name = "hm")
    public int hjiriMonth;

    @ColumnInfo(name = "hy")
    public int hjiriYear;
}
