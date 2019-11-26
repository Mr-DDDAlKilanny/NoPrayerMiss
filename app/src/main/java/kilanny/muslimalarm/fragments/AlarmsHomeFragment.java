package kilanny.muslimalarm.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.arch.core.util.Function;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import kilanny.muslimalarm.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmsHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmsHomeFragment extends Fragment {

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
        onDataSetChanged(); // show recalced left times
        mCanUpdateTime = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCanUpdateTime = false;
    }

    public void onDataSetChanged() {
        mAdapter.clear();
        Utils.runInBackground(new Function<Context, Alarm[]>() {
            @Override
            public Alarm[] apply(Context input) {
                return AppDb.getInstance(input).alarmDao().getAll();
            }
        }, new Function<Alarm[], Void>() {
            @Override
            public Void apply(Alarm[] input) {
                if (mAdapter.getCount() == 0) { // prevent duplicate threads adds
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
                return null;
            }
        }, getContext());
    }

    private boolean batteryOptimizationAd(final Context context) {
        final SerializableInFile<Integer> response = new SerializableInFile<>(
                context, "battery__st", 0);
        if (response.getData() == 0) {
            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.battery_opts)
                    .setMessage(R.string.battery_msg)
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                            intent.setData(uri);
                            context.startActivity(intent);
                        }
                    })
                    .setNeutralButton(android.R.string.cancel, null)
                    .setNegativeButton(R.string.no_not_ask, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            response.setData(-1, context);
                        }
                    })
                    .show();
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
        mView.findViewById(R.id.tutorialLayout).setVisibility(mAdapter.getCount() == 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_alarms, container, false);

        View permissionCheckLayout = mView.findViewById(R.id.permissionCheckLayout);
        final Intent intent = isNeedingAutoStartupPermission(mView.getContext());
        permissionCheckLayout.setVisibility(intent == null ? View.GONE : View.VISIBLE);
        permissionCheckLayout.findViewById(R.id.btnPermissions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(intent);
            }
        });

        ListView listView = mView.findViewById(R.id.listView);
        mAdapter = new AlarmListAdapter(getContext(), new Function<Alarm, Void>() {
            @Override
            public Void apply(Alarm input) {
                mListener.onEditAlarm(input);
                return null;
            }
        }, new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                onDataSetChanged();
                return null;
            }
        });
        listView.setAdapter(mAdapter);
        onDataSetChanged();
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, 30000, 30000);

        FloatingActionButton fab = mView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddNewAlarm();
            }
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
        builder.setPositiveButton(R.string.menu_share, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                response.setData(1, context);
                Utils.displayShareActivity(context);
            }
        });
        builder.setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                response.setData(-1, context);
            }
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

    public interface OnFragmentInteractionListener {
        void onAddNewAlarm();

        void onEditAlarm(Alarm alarm);
    }
}
