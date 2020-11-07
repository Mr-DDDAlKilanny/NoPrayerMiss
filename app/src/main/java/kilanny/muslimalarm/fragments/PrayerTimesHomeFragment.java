package kilanny.muslimalarm.fragments;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metinkale.prayer.HijriDate;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.util.PrayTime;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PrayerTimesHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrayerTimesHomeFragment extends Fragment {

    private View mView;

    public PrayerTimesHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PrayerTimesHomeFragment.
     */
    public static PrayerTimesHomeFragment newInstance() {
        PrayerTimesHomeFragment fragment = new PrayerTimesHomeFragment();
        return fragment;
    }

    public boolean recalculate() {
        AppSettings settings = AppSettings.getInstance(getContext());
        double lat = settings.getLatFor(0);
        double lng = settings.getLngFor(0);
        if (lat > -1) {
            Map<String, String> prayerTimes = PrayTime.getPrayerTimes(getContext(), 0, lat, lng);
            TextView txtDate = mView.findViewById(R.id.date);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            txtDate.setText(dateFormat.format(new Date()));
            TextView txtHijri = mView.findViewById(R.id.hicri);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            txtHijri.setText(LocaleUtils.formatDate(getContext(), HijriDate.get(getContext(),
                    LocalDate.now().plusDays(pref.getInt("date_offset", 0)))));

            TextView fajr = (TextView) mView.findViewById(R.id.fajrTime);
            TextView dhuhr = (TextView) mView.findViewById(R.id.zuhrTime);
            TextView asr = (TextView) mView.findViewById(R.id.asrTime);
            TextView maghrib = (TextView) mView.findViewById(R.id.maghribTime);
            TextView isha = (TextView) mView.findViewById(R.id.ishaaTime);
            TextView sunrise = (TextView) mView.findViewById(R.id.sunTime);

            fajr.setText(prayerTimes.get("Fajr"));
            dhuhr.setText(prayerTimes.get("Dhuhr"));
            asr.setText(prayerTimes.get("Asr"));
            maghrib.setText(prayerTimes.get("Maghrib"));
            isha.setText(prayerTimes.get("Isha"));
            sunrise.setText(prayerTimes.get("Sunrise"));
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_prayer_times, container, false);
        if (!recalculate())
            return mView;

        mView.findViewById(R.id.fabEditHijriDate).setOnClickListener(v -> {
            if (AppSettings.getInstance(getContext()).getLatFor(0) <= -1) {
                return;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
            int offset = pref.getInt("date_offset", 0);
            new AlertDialog.Builder(v.getContext())
                    .setTitle(R.string.custom_time)
                    .setSingleChoiceItems(new String[]{
                            "(+2) " + LocaleUtils.formatDate(getContext(), HijriDate.get(getContext(), LocalDate.now().plusDays(2))),
                            "(+1) " + LocaleUtils.formatDate(getContext(), HijriDate.get(getContext(), LocalDate.now().plusDays(1))),
                            "(+0) " + LocaleUtils.formatDate(getContext(), HijriDate.now(getContext())),
                            "(-1) " + LocaleUtils.formatDate(getContext(), HijriDate.get(getContext(), LocalDate.now().plusDays(-1))),
                            "(-2) " + LocaleUtils.formatDate(getContext(), HijriDate.get(getContext(), LocalDate.now().plusDays(-2))),
                    }, offset == 0 ? 2 : offset == 1 ? 1 : offset == 2 ? 0 : offset == -1 ? 3 : 4, (dialog, which) -> {
                        dialog.dismiss();
                        pref.edit().putInt("date_offset",
                                which == 0 ? 2 : which == 1 ? 1 : which == 2 ? 0 : which == 3 ? -1 : -2).commit();
                        recalculate();
                    })
                    .show();
        });
        return mView;
    }

}
