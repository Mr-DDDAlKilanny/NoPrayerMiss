package com.metinkale.prayer.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.core.os.LocaleListCompat;

import com.metinkale.prayer.HijriDate;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import kilanny.muslimalarm.App;
import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.util.PrayTime;

import static android.content.Context.UI_MODE_SERVICE;

public class LocaleUtils {

    private static int[] GMONTHS =
            new int[]{R.string.gmonth1, R.string.gmonth2, R.string.gmonth3, R.string.gmonth4, R.string.gmonth5, R.string.gmonth6, R.string.gmonth7,
                    R.string.gmonth8, R.string.gmonth9, R.string.gmonth10, R.string.gmonth11, R.string.gmonth12};

    private static int[] HMONTHS =
            new int[]{R.string.hmonth1, R.string.hmonth2, R.string.hmonth3, R.string.hmonth4, R.string.hmonth5, R.string.hmonth6, R.string.hmonth7,
                    R.string.hmonth8, R.string.hmonth9, R.string.hmonth10, R.string.hmonth11, R.string.hmonth12};

    private static final LocaleListCompat DEFAULT_LOCALES = LocaleListCompat.getDefault();

    public static LocaleListCompat getDefaultLocales() {
        return DEFAULT_LOCALES;
    }

    public static void init(@NonNull Context c) {
        initLocale(c);

        UiModeManager systemService = (UiModeManager) c.getSystemService(UI_MODE_SERVICE);
        if (systemService != null)
            systemService.setNightMode(UiModeManager.MODE_NIGHT_NO);


        int year = LocalDate.now().getYear();
        if (year == 2019) {
            year = 201905; // force update in 2019
        }
    }


    private static void initLocale(Context c) {
        Configuration config = new Configuration();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = getLocales(c);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Locale locale = getLocale(c);
            config.setLocale(locale);
            Locale.setDefault(locale);
        } else {
            Locale locale = getLocale(c);
            config.locale = locale;
            Locale.setDefault(locale);
        }

        c.getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
        c.getApplicationContext().getResources().updateConfiguration(config, c.getResources().getDisplayMetrics());
    }


    public static CharSequence formatTimeForHTML(Context context, LocalTime localTime) {
        String time = formatTime(context, localTime);
        AppSettings settings = AppSettings.getInstance(context);
        if (settings.getTimeFormatFor(0) != PrayTime.TIME_12) {
            return time;
        }
        int d = time.indexOf(" ");
        if (d < 0)
            return time;
        time = time.replace(" ", "");

        int s = time.length();
        Spannable span = new SpannableString(time);
        span.setSpan(new SuperscriptSpan(), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.5f), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }


    @NonNull
    public static String formatTime(Context context, LocalTime localTime) {
        String time = localTime == null ? "00:00" : localTime.toString("HH:mm");
        AppSettings settings = AppSettings.getInstance(context);
        if (settings.getTimeFormatFor(0) == PrayTime.TIME_12 && time.contains(":")) {
            try {
                String fix = time.substring(0, time.indexOf(":"));
                String suffix = time.substring(time.indexOf(":"));


                int hour = Integer.parseInt(fix);
                if (hour == 0) {
                    time = "00" + suffix + " AM";
                } else if (hour < 12) {
                    time = az(hour) + suffix + " AM";
                } else if (hour == 12) {
                    time = "12" + suffix + " PM";
                } else {
                    time = az(hour - 12) + suffix + " PM";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return time;
            }
        }
        return time;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    public static LocaleList getLocales(Context context) {
        return LocaleList.forLanguageTags(getLocalesCompat(context).toLanguageTags());
    }


    @NonNull
    public static LocaleListCompat getLocalesCompat(Context context) {
        AppSettings settings = AppSettings.getInstance(context);
        if ("system".equals(settings.getString("language", "system")))
            return DEFAULT_LOCALES;

        Locale locale = LocaleUtils.getLocale(context);
        ArrayList<Locale> locales = new ArrayList<>(DEFAULT_LOCALES.size() + 1);
        locales.add(LocaleUtils.getLocale(context));
        for (int i = 0; i < DEFAULT_LOCALES.size(); i++) {
            Locale l = DEFAULT_LOCALES.get(i);
            if (!locales.contains(locale)) {
                locales.add(l);
            }
        }
        return LocaleListCompat.create(locales.toArray(new Locale[0]));
    }

    @NonNull
    public static Locale getLocale(Context context) {
        AppSettings settings = AppSettings.getInstance(context);
        String language = settings.getString("language", "system");
        if ("system".equals(language))
            return DEFAULT_LOCALES.get(0);
        else
            return new Locale(language);
    }


    @NonNull
    public static String getLanguage(Context context, @Size(min = 1) String... allow) {
        Locale lang = LocaleUtils.getLocale(context);
        Locale[] locales = new Locale[allow.length];
        for (int i = 0; i < allow.length; i++) {
            locales[i] = new Locale(allow[i]);
        }

        for (int i = 0; i < locales.length; i++) {
            if (lang.getLanguage().equals(locales[i].getLanguage()))
                return allow[i];
        }

        return allow[0];
    }

    @NonNull
    private static String getGregMonth(Context context, @IntRange(from = 0, to = 11) int which) {
        AppSettings settings = AppSettings.getInstance(context);
        String language = settings.getString("language", "system");
        if (language.equals("system"))
            return new DateFormatSymbols(getLocale(context)).getMonths()[which];
        else
            return context.getResources().getString(GMONTHS[which]);
    }

    @NonNull
    private static String getHijriMonth(Context context, @IntRange(from = 0, to = 11) int which) {
        return context.getResources().getString(HMONTHS[which]);
    }


    @NonNull
    public static String az(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return i + "";
    }

    @NonNull
    public static String formatDate(Context context, @NonNull HijriDate date) {
        String format = "DD MMM YYYY";
        format = format.replace("DD", az(date.getDay(), 2));

        if (format.contains("MMM")) {
            try {
                format = format.replace("MMM", getHijriMonth(context, date.getMonth() - 1));

            } catch (ArrayIndexOutOfBoundsException ex) {
                ex.printStackTrace();
                return "";
            }
        }
        format = format.replace("MM", az(date.getMonth(), 2));
        format = format.replace("YYYY", az(date.getYear(), 4));
        format = format.replace("YY", az(date.getYear(), 2));
        return format;
    }


    @NonNull
    public static String formatDate(Context context, @NonNull LocalDate date) {
        String format = "dd/MM/yyyy";
        format = format.replace("DD", az(date.getDayOfMonth(), 2));

        try {
            format = format.replace("MMM", getGregMonth(context, date.getMonthOfYear() - 1));


        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return "";
        }
        format = format.replace("MM", az(date.getMonthOfYear(), 2));
        format = format.replace("YYYY", az(date.getYear(), 4));
        format = format.replace("YY", az(date.getYear(), 2));
        return format;
    }

    @NonNull
    private static String az(int Int, int num) {
        StringBuilder ret = new StringBuilder(Int + "");
        if (ret.length() < num) {
            for (int i = ret.length(); i < num; i++) {
                ret.insert(0, "0");
            }
        } else if (ret.length() > num) {
            ret = new StringBuilder(ret.substring(ret.length() - num, ret.length()));
        }

        return ret.toString();
    }

    public static Context wrapContext(Context context) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(getLocale(context));
            LocaleList localeList = getLocales(context);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context = context.createConfigurationContext(configuration);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(getLocale(context));
            context = context.createConfigurationContext(configuration);
        } else {
            configuration.locale = getLocale(context);
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }

        return new ContextWrapper(context);
    }

    public static class Translation {
        private final String language;
        private final int progress;

        public Translation(String language, int progress) {
            this.language = language;
            this.progress = progress;
        }

        public String getLanguage() {
            return language;
        }

        public int getProgress() {
            return progress;
        }

        public String getDisplayLanguage(Context context) {
            if (language.equals("system"))
                return context.getResources().getString(R.string.systemLanguage);
            if (language.equals("ku"))
                return "Kurdî";
            Locale locale = new Locale(language);
            return locale.getDisplayLanguage(locale);
        }

        public CharSequence getDisplayText(Context context) {
            if (getProgress() < 0)
                return getDisplayLanguage(context);
            else
                return Html.fromHtml(getDisplayLanguage(context) + "&nbsp;<small>(" + getProgress() + "%)</small>");
        }
    }

    public static List<Translation> getSupportedLanguages(Context c) {
        String[] languages = c.getResources().getStringArray(R.array.languages);
        List<Translation> translations = new ArrayList<>();
        for (String lang : languages) {
            int divider = lang.indexOf("|");
            int progress = Integer.parseInt(lang.substring(divider + 1));
            lang = lang.substring(0, divider);
            if (lang.equals("kur"))
                lang = "ku";
            if (progress > 40) {
                translations.add(new Translation(lang, progress));
            }
        }

        Collections.sort(translations, new Comparator<Translation>() {
            @Override
            public int compare(Translation t1, Translation t2) {
                return -Integer.valueOf(t1.getProgress()).compareTo(Integer.valueOf(t2.getProgress()));
            }
        });

        translations.add(0, new Translation("system", -1));
        return translations;
    }

    public static String readableSize(int bytes) {
        int unit = 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
