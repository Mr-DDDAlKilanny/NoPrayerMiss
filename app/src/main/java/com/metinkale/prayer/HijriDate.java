package com.metinkale.prayer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.metinkale.prayer.utils.FastTokenizer;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kilanny.muslimalarm.App;
import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppSettings;

public class HijriDate {
    public static final int MUHARRAM = 1;
    public static final int SAFAR = 2;
    public static final int RABIAL_AWWAL = 3;
    public static final int RABIAL_AKHIR = 4;
    public static final int JUMADAAL_AWWAL = 5;
    public static final int JUMADAAL_AKHIR = 6;
    public static final int RAJAB = 7;
    public static final int SHABAN = 8;
    public static final int RAMADAN = 9;
    public static final int SHAWWAL = 10;
    public static final int DHUL_QADA = 11;
    public static final int DHUL_HIJJA = 12;


    public static final int MONTH = 0;
    public static final int ISLAMIC_NEW_YEAR = 1;
    public static final int ASHURA = 2;
    public static final int MAWLID_AL_NABI = 3;
    public static final int THREE_MONTHS = 4;
    public static final int RAGAIB = 5;
    public static final int MIRAJ = 6;
    public static final int BARAAH = 7;
    public static final int RAMADAN_BEGIN = 8;
    public static final int LAYLATALQADR = 9;
    public static final int LAST_RAMADAN = 10;
    public static final int EID_AL_FITR_DAY1 = 11;
    public static final int EID_AL_FITR_DAY2 = 12;
    public static final int EID_AL_FITR_DAY3 = 13;
    public static final int ARAFAT = 14;
    public static final int EID_AL_ADHA_DAY1 = 15;
    public static final int EID_AL_ADHA_DAY2 = 16;
    public static final int EID_AL_ADHA_DAY3 = 17;
    public static final int EID_AL_ADHA_DAY4 = 18;

    private int day, month, year;

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public HijriDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static HijriDate now(Context context) {
        return get(context, LocalDate.now());
    }

    public static HijriDate get(Context context, LocalDate date) {
        DateMap greg = HijriDateDb.getInstance(context).dateMapDao()
                .getByGreg(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        return new HijriDate(greg.hjiriYear, greg.hjiriMonth, greg.hjiriDay);
    }

//    @NonNull
//    public static List<Pair<HijriDate, Integer>> getHolydaysForHijriYear(Context context, int year) {
//        List<Pair<HijriDate, Integer>> dates = new ArrayList<>(12 + 18);
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, MUHARRAM, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, MUHARRAM, 1), ISLAMIC_NEW_YEAR));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, MUHARRAM, 10), ASHURA));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SAFAR, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RABIAL_AWWAL, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RABIAL_AWWAL, 11), MAWLID_AL_NABI));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RAJAB, 1), THREE_MONTHS));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RABIAL_AKHIR, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, JUMADAAL_AWWAL, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, JUMADAAL_AKHIR, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RAJAB, 1), MONTH));
//        HijriDate ragaib = HijriDate.fromGreg(context, HijriDate.fromHijri(context, year, RAJAB, 1)
//                .getLocalDate().withDayOfWeek(DateTimeConstants.FRIDAY));
//        if (ragaib.getMonth() < RAJAB)
//            ragaib = ragaib.plusDays(context, 7);
//        dates.add(new Pair<>(ragaib.plusDays(context, -1), RAGAIB));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RAJAB, 26), MIRAJ));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SHABAN, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SHABAN, 14), BARAAH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RAMADAN, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RAMADAN, 1), RAMADAN_BEGIN));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, RAMADAN, 26), LAYLATALQADR));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SHAWWAL, 1).plusDays(context, -1), LAST_RAMADAN));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SHAWWAL, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SHAWWAL, 1), EID_AL_FITR_DAY1));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SHAWWAL, 2), EID_AL_FITR_DAY2));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, SHAWWAL, 3), EID_AL_FITR_DAY3));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, DHUL_QADA, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, DHUL_HIJJA, 9), ARAFAT));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, DHUL_HIJJA, 1), MONTH));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, DHUL_HIJJA, 10), EID_AL_ADHA_DAY1));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, DHUL_HIJJA, 11), EID_AL_ADHA_DAY2));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, DHUL_HIJJA, 12), EID_AL_ADHA_DAY3));
//        dates.add(new Pair<>(HijriDate.fromHijri(context, year, DHUL_HIJJA, 13), EID_AL_ADHA_DAY4));
//        return dates;
//    }


//    public static int isHolyday(Context context) {
//        return fromGreg(context, LocalDate.now()).getHolyday(context);
//    }

//    public int getHolyday(Context context) {
//        HijriDate tmp;
//        if (hijri.day == 1 && hijri.month == MUHARRAM) {
//            return ISLAMIC_NEW_YEAR;
//        } else if (hijri.day == 10 && hijri.month == MUHARRAM) {
//            return ASHURA;
//        } else if (hijri.day == 11 && hijri.month == RABIAL_AWWAL) {
//            return MAWLID_AL_NABI;
//        } else if (hijri.day == 1 && hijri.month == RAJAB) {
//            return THREE_MONTHS;
//        } else if ((tmp = fromGreg(context, getLocalDate().plusDays(1))).getLocalDate().getWeekyear() == DateTimeConstants.FRIDAY
//                && tmp.hijri.day <= 7 && tmp.hijri.month == RAJAB) {//we need this, because it might be also the last night of the previous night
//
//
//            return RAGAIB;
//        } else if (hijri.day == 26 && hijri.month == RAJAB) {
//            return MIRAJ;
//        } else if (hijri.day == 14 && hijri.month == SHABAN) {
//            return BARAAH;
//        } else if (hijri.day == 1 && hijri.month == RAMADAN) {
//            return RAMADAN_BEGIN;
//        } else if (hijri.day == 26 && hijri.month == RAMADAN) {
//            return LAYLATALQADR;
//        } else if ((tmp = fromGreg(context, getLocalDate().plusDays(1))).getMonth() == SHAWWAL && tmp.getDay() == 1) {
//            return LAST_RAMADAN;
//        } else if (hijri.day == 1 && hijri.month == SHAWWAL) {
//            return EID_AL_FITR_DAY1;
//        } else if (hijri.day == 2 && hijri.month == SHAWWAL) {
//            return EID_AL_FITR_DAY2;
//        } else if (hijri.day == 3 && hijri.month == SHAWWAL) {
//            return EID_AL_FITR_DAY3;
//        } else if (hijri.day == 9 && hijri.month == DHUL_HIJJA) {
//            return ARAFAT;
//        } else if (hijri.day == 10 && hijri.month == DHUL_HIJJA) {
//            return EID_AL_ADHA_DAY1;
//        } else if (hijri.day == 11 && hijri.month == DHUL_HIJJA) {
//            return EID_AL_ADHA_DAY2;
//        } else if (hijri.day == 12 && hijri.month == DHUL_HIJJA) {
//            return EID_AL_ADHA_DAY3;
//        } else if (hijri.day == 13 && hijri.month == DHUL_HIJJA) {
//            return EID_AL_ADHA_DAY4;
//        }
//
//        return 0;
//    }

    private static class Hijri extends DateIntern<Hijri> {
        Hijri(int year, int month, int day) {
            super(year, month, day);
        }
    }


    private static class Greg extends DateIntern<Hijri> {
        Greg(int year, int month, int day) {
            super(year, month, day);
        }
    }

    private abstract static class DateIntern<K extends DateIntern> {
        final int year;
        final int month;
        final int day;

        public DateIntern(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof DateIntern))
                return false;
            if (!getClass().equals(o.getClass()))
                return false;
            final DateIntern other = (DateIntern) o;

            return other.hashCode() == hashCode();
        }

        public int hashCode() {
            return year * 10000 + month * 100 + day;
        }

    }
}

