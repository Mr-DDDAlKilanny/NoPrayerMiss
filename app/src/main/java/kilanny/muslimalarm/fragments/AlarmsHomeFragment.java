package kilanny.muslimalarm.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import androidx.arch.core.util.Function;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.activities.EditAlarmActivity;
import kilanny.muslimalarm.adapters.AlarmListAdapter;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AppDb;
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
                }
                return null;
            }
        }, getContext());
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        View permissionCheckLayout = view.findViewById(R.id.permissionCheckLayout);
        final Intent intent = isNeedingAutoStartupPermission(view.getContext());
        permissionCheckLayout.setVisibility(intent == null ? View.GONE : View.VISIBLE);
        permissionCheckLayout.findViewById(R.id.btnPermissions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(intent);
            }
        });

        ListView listView = view.findViewById(R.id.listView);
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

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddNewAlarm();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
