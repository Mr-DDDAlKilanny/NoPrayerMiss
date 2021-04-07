package kilanny.muslimalarm.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.adapters.AlarmListAdapter;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.SerializableInFile;
import kilanny.muslimalarm.databinding.FragmentAlarmsBinding;
import kilanny.muslimalarm.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmsHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmsHomeFragment extends Fragment {

    private FragmentAlarmsBinding binding;
    private Animation fabOpenAnimation;
    private Animation fabCloseAnimation;
    private boolean isFabMenuOpen = false;
    private OnFragmentInteractionListener mListener;
    private AlarmListAdapter mAdapter;
    private boolean mCanUpdateTime = true;
    private Timer mUpdateTimesTimer;
    private View mView;

    public AlarmsHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AlarmsHomeFragment.
     */
    public static AlarmsHomeFragment newInstance() {
        AlarmsHomeFragment fragment = new AlarmsHomeFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        onDataSetChanged(null); // show recalced left times
        mCanUpdateTime = true;
        FloatingActionButton fab = binding.baseFloatingActionButton;
        fab.setRotation(0);
        ViewCompat.animate(fab)
                .rotation(360)
                .withLayer()
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (!pref.getBoolean("noShowAlarmTutorial", false) &&
                mAdapter != null && mAdapter.getCount() > 0) {
            Utils.showConfirm(getContext(),
                    getString(R.string.dealing_with_list_of_alarms),
                    getString(R.string.toturial_alarms),
                    getString(android.R.string.ok),
                    getString(R.string.no_not_ask),
                    null,
                    (dialog, which) -> pref.edit().putBoolean("noShowAlarmTutorial", true).apply());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCanUpdateTime = false;
    }

    public void onDataSetChanged(Runnable onDone) {
        mAdapter.clear();
        Utils.runInBackground(input -> AppDb.getInstance(input).alarmDao().getAll(), input -> {
            if (mAdapter.getCount() == 0) { // prevent duplicate threads adds
                Arrays.sort(input, (o1, o2) -> {
                    if (o1.enabled != o2.enabled) {
                        if (o1.enabled)
                            return -1;
                        return 1;
                    } else if (!o1.enabled) {
                        return o1.id - o2.id;
                    }
                    Utils.NextAlarmInfo n1 = Utils.getNextAlarmDate(getContext(), o1);
                    Utils.NextAlarmInfo n2 = Utils.getNextAlarmDate(getContext(), o2);
                    return (int) (n1.date.getTime() - n2.date.getTime());
                });
                mAdapter.addAll(input);
                mAdapter.notifyDataSetChanged();
                if (input.length > 0 && getContext() != null) {
                    boolean oneEnabled = false;
                    for (Alarm alarm : input)
                        if (alarm.enabled) {
                            oneEnabled = true;
                            break;
                        }
                    if (oneEnabled && !batteryOptimizationAd(getContext()) && new Random().nextInt(5) == 0)
                        shareAd(getContext());
                }
            }
            if (onDone != null)
                onDone.run();
            return null;
        }, getContext());
    }

    private static boolean batteryOptimizationAd(final Context context) {
        boolean not = false;
        final SerializableInFile<Integer> response2 = new SerializableInFile<>(
                context, "battery__st2", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(context.getPackageName())) {
                return false;
            } else {
                not = response2.getData() < 10;
            }
        }
        final SerializableInFile<Integer> response = new SerializableInFile<>(
                context, "battery__st", 0);
        if (not || response.getData() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.battery_opts)
                    .setMessage(R.string.battery_msg)
                    .setPositiveButton(R.string.open_settings, (dialogInterface, i) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                context.startActivity(new Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:" + context.getPackageName())));
                                return;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        context.startActivity(new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.getPackageName(), null)));
                    })
                    .setNeutralButton(android.R.string.cancel, null);
            if (!not) {
                builder.setNegativeButton(R.string.no_not_ask,
                        (dialogInterface, i) -> response.setData(-1, context));
            } else {
                response2.setData(response2.getData() + 1, context);
            }
            builder.show();
            return true;
        } else
            return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Intent isNeedingAutoStartupPermission(Context context) {

        try {
            Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0) {
                return intent;
            }
        } catch (Exception e) {
            Log.e("Permis", String.valueOf(e));
        }
        return null;
    }

    private void onDataSetObserved() {
        mView.findViewById(R.id.emptyLayout).setVisibility(mAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_alarms, container, false);

        View permissionCheckLayout = mView.findViewById(R.id.permissionCheckLayout);
        final Intent intent = isNeedingAutoStartupPermission(mView.getContext());
        permissionCheckLayout.setVisibility(intent == null ? View.GONE : View.VISIBLE);
        permissionCheckLayout.findViewById(R.id.btnPermissions)
                .setOnClickListener(view -> view.getContext().startActivity(intent));

        ListView listView = mView.findViewById(R.id.listView);
        mAdapter = new AlarmListAdapter(getContext(), input -> {
            mListener.onEditAlarm(input);
            return null;
        }, input -> {
            onDataSetChanged(null);
            return null;
        });
        listView.setAdapter(mAdapter);
        onDataSetChanged(null);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onDataSetObserved();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                onDataSetObserved();
            }
        });

        final Handler handler = new Handler();
        mUpdateTimesTimer = new Timer();
        mUpdateTimesTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!mCanUpdateTime)
                    return;
                handler.post(() -> mAdapter.notifyDataSetChanged());
            }
        }, 30000, 30000);

        binding = DataBindingUtil.bind(mView);
        binding.setFabHandler(new FabHandler());
        fabOpenAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fabCloseAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        listView.setOnScrollListener(new AutoHideFabScrollListener(listView, binding.baseFloatingActionButton));

        SwipeRefreshLayout listViewLayout = mView.findViewById(R.id.listViewLayout);
        listViewLayout.setOnRefreshListener(() -> {
            if (mAdapter != null && mAdapter.mIsPendingOperation)
                return;
            onDataSetChanged(() -> listViewLayout.setRefreshing(false));
        });

        return mView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private static boolean shareAd(Context context) {
        final SerializableInFile<Integer> response = new SerializableInFile<>(
                context, "share__st", 0);
        if (response.getData() == 0) {
            dispalyShareAd(context, response);
            return true;
        } else if (response.getData() == -1) {
            Date date = response.getFileLastModifiedDate(context);
            if (date == null) {
                response.setData(0, context);
                dispalyShareAd(context, response);
                return true;
            }
            long diffTime = new Date().getTime() - date.getTime();
            long diffDays = diffTime / (1000 * 60 * 60 * 24);
            if (diffDays > 30) {
                dispalyShareAd(context, response);
                return true;
            }
        }
        return false;
    }

    private static void dispalyShareAd(final Context context, final SerializableInFile<Integer> response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.share_app);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(R.string.share_msg_dlg);
        builder.setPositiveButton(R.string.menu_share, (dialog, id) -> {
            dialog.cancel();
            response.setData(1, context);
            Utils.displayShareActivity(context);
        });
        builder.setNegativeButton(R.string.not_now, (dialog, id) -> {
            dialog.cancel();
            response.setData(-1, context);
        });
        builder.create().show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mUpdateTimesTimer != null) {
            mUpdateTimesTimer.cancel();
            mUpdateTimesTimer = null;
        }
    }

    private static class AutoHideFabScrollListener implements AbsListView.OnScrollListener {

        private int mLastFirstVisibleItem = -1;
        private int mLastFirstVisibleItemTop = -1;
        private ListView listView;
        private FloatingActionButton fab;

        private AutoHideFabScrollListener(ListView listView, FloatingActionButton fab) {
            this.listView = listView;
            this.fab = fab;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (view.getId() == listView.getId()) {
                final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
                int currentFirstVisibleItemTop = listView.getChildAt(0).getTop();

                if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                    fab.hide();
                } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                    fab.show();
                } else {
                    if (currentFirstVisibleItemTop < mLastFirstVisibleItemTop) {
                        fab.hide();
                    } else if (currentFirstVisibleItemTop > mLastFirstVisibleItemTop) {
                        fab.show();
                    }
                }

                mLastFirstVisibleItemTop = currentFirstVisibleItemTop;
                mLastFirstVisibleItem = currentFirstVisibleItem;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    }

    private void expandFabMenu() {
        ViewCompat.animate(binding.baseFloatingActionButton)
                .rotation(45.0F)
                .withLayer()
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(10.0F))
                .start();
        binding.fivePrayersLayout.startAnimation(fabOpenAnimation);
        binding.qeyamLayout.startAnimation(fabOpenAnimation);
        binding.customTimeLayout.startAnimation(fabOpenAnimation);
        binding.fivePrayersFab.setClickable(true);
        binding.nightPrayerFab.setClickable(true);
        binding.customTimeFab.setClickable(true);
        isFabMenuOpen = true;
    }

    private void collapseFabMenu() {
        ViewCompat.animate(binding.baseFloatingActionButton)
                .rotation(0.0F)
                .withLayer()
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(10.0F))
                .start();
        binding.fivePrayersLayout.startAnimation(fabCloseAnimation);
        binding.qeyamLayout.startAnimation(fabCloseAnimation);
        binding.customTimeLayout.startAnimation(fabCloseAnimation);
        binding.fivePrayersFab.setClickable(false);
        binding.nightPrayerFab.setClickable(false);
        binding.customTimeFab.setClickable(false);
        isFabMenuOpen = false;
    }

    public class FabHandler {

        public void onBaseFabClick(View view) {
            if (isFabMenuOpen)
                collapseFabMenu();
            else
                expandFabMenu();
        }

        public void onNightPrayerFabClick(View view) {
            mListener.onAddNewAlarm(false);
        }

        public void onCustomTimeFabClick(View view) {
            mListener.onAddNewAlarm(null);
        }

        public void onFivePrayersFabClick(View view) {
            mListener.onAddNewAlarm(true);
        }
    }

    public interface OnFragmentInteractionListener {
        void onAddNewAlarm(Boolean isFivePrayers);

        void onEditAlarm(Alarm alarm);
    }
}
