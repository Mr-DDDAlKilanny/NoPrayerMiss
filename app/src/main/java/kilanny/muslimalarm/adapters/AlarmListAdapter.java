package kilanny.muslimalarm.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.arch.core.util.Function;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.activities.ShowAlarmActivity;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AlarmDao;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.Weekday;
import kilanny.muslimalarm.util.Utils;

public class AlarmListAdapter extends ArrayAdapter<Alarm>
        implements PopupMenu.OnMenuItemClickListener {

    private static final int[] times = {
            R.string.fajr,
            R.string.sun,
            R.string.zuhr,
            R.string.asr,
            R.string.maghrib,
            R.string.ishaa
    };

    private final SimpleDateFormat timeF = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private final Function<Alarm, Void> onAlarmEdit;
    private final Function<Void, Void> onNeedRefresh;
    private Alarm mClickedAlarm;
    private boolean mIsPendingOperation = false;

    public AlarmListAdapter(@NonNull Context context, @NonNull Function<Alarm, Void> onAlarmEdit,
                            @NonNull Function<Void, Void> onNeedRefresh) {
        super(context, R.layout.list_item_alarm);
        this.onAlarmEdit = onAlarmEdit;
        this.onNeedRefresh = onNeedRefresh;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView;
        final Alarm alarm = getItem(position);
        if (convertView == null) {
            int res = alarm.snoozedToTime != null ? R.layout.list_item_alarm_snoozed
                    : R.layout.list_item_alarm;
            rowView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(res, parent, false);
        } else
            rowView = convertView;

        final Context context = rowView.getContext();
        if (alarm.snoozedToTime != null) {
            rowView.findViewById(R.id.btnDismiss).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, ShowAlarmActivity.class));
                }
            });
            return rowView;
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsPendingOperation)
                    onAlarmEdit.apply(alarm);
            }
        });
        final AppCompatTextView alarmTime = rowView.findViewById(R.id.alarmTime);
        if (alarm.enabled) {
            Utils.NextAlarmInfo info = Utils.getNextAlarmDate(context, alarm);
            String time;
            switch (info.timeFlag) {
                case Alarm.TIME_FAJR:
                    time = context.getString(R.string.fajr);
                    break;
                case Alarm.TIME_SUNRISE:
                    time = context.getString(R.string.sun);
                    break;
                case Alarm.TIME_DHUHR:
                    time = context.getString(R.string.zuhr);
                    break;
                case Alarm.TIME_ASR:
                    time = context.getString(R.string.asr);
                    break;
                case Alarm.TIME_MAGHRIB:
                    time = context.getString(R.string.maghrib);
                    break;
                case Alarm.TIME_ISHAA:
                    time = context.getString(R.string.ishaa);
                    break;
                default:
                    time = "";
            }
            alarmTime.setText(String.format("%s (%s) - %s", time,
                    timeF.format(info.date),
                    Utils.getLeftTimeToDate(context, info.date)));
        } else {
            alarmTime.setText(null);
        }

        AppCompatCheckBox chkAlarmEnabled = rowView.findViewById(R.id.chkAlarmEnabled);
        chkAlarmEnabled.setEnabled(alarm.snoozedToTime == null);
        chkAlarmEnabled.setChecked(alarm.enabled);
        chkAlarmEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                compoundButton.setEnabled(false);
                Utils.runInBackground(new Function<Context, Pair<Context, Alarm[]>>() {
                    @Override
                    public Pair<Context, Alarm[]> apply(Context input) {
                        // if any other operations running wait them first
                        while (mIsPendingOperation) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        mIsPendingOperation = true;
                        alarm.enabled = b;
                        if (b) {
                            alarm.oneTimeLeftAlarmsTimeFlags = alarm.timeFlags;
                            alarm.snoozedCount = 0;
                            alarm.snoozedToTime = null;
                            alarm.skippedTimeFlag = 0;
                            alarm.skippedAlarmTime = null;
                        }
                        AlarmDao alarmDao = AppDb.getInstance(input).alarmDao();
                        alarmDao.update(alarm);
                        return new Pair<>(context, alarmDao.getAll());
                    }
                }, new Function<Pair<Context, Alarm[]>, Void>() {
                    @Override
                    public Void apply(Pair<Context, Alarm[]> input) {
                        mIsPendingOperation = false;
                        compoundButton.setEnabled(true);
                        clear();
                        addAll(input.second);
                        notifyDataSetChanged();
                        Utils.scheduleAndDeletePrevious(input.first, input.second);
                        return null;
                    }
                }, compoundButton.getContext());
            }
        });

        String[] prayerNames = context.getResources().getStringArray(R.array.prayer_times);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            if ((alarm.timeFlags & (1 << i)) != 0)
                b.insert(0, ',').insert(1, prayerNames[5 - i]);
        }
        if (b.length() > 0) b.deleteCharAt(0);
        AppCompatTextView prayerName = rowView.findViewById(R.id.prayerName);
        prayerName.setText(b.toString());

        String[] days = context.getResources().getStringArray(R.array.repeat_days);
        b.delete(0, b.length());
        if (alarm.weekDayFlags == Weekday.NO_REPEAT)
            b.append(days[0]);
        else if (alarm.weekDayFlags == 127)
            b.append(context.getString(R.string.everyday));
        else {
            ArrayList<Integer> not = new ArrayList<>();
            for (int i = 0; i < days.length; ++i) {
                int f = 1 << i;
                if ((alarm.weekDayFlags & f) == 0)
                    not.add(7 - i);
            }
            if (not.size() >= 3) {
                for (int i = 0; i < days.length; ++i) {
                    int f = 1 << i;
                    if ((alarm.weekDayFlags & f) != 0)
                        b.append(days[7 - i]).append(',');
                }
                b.delete(b.length() - 1, 1);
            } else if (not.size() == 2) {
                b.append(context.getString(R.string.allDaysExpect2Days,
                        days[not.get(0)],
                        days[not.get(1)]));
            } else {
                b.append(context.getString(R.string.allDaysExpect1Day,
                        days[not.get(0)]));
            }
        }
        AppCompatTextView alarmDays = rowView.findViewById(R.id.alarmDays);
        alarmDays.setText(b.toString());

        int res = R.drawable.clock;
        switch (alarm.dismissAlarmType) {
            case Alarm.DISMISS_ALARM_SHAKE:
                res = R.drawable.shake;
                break;
            case Alarm.DISMISS_ALARM_MATH:
                res = R.drawable.math;
                break;
            case Alarm.DISMISS_ALARM_BARCODE:
                res = R.drawable.barcode;
                break;
        }
        AppCompatImageView alarmTypeIcon = rowView.findViewById(R.id.alarmTypeIcon);
        alarmTypeIcon.setImageResource(res);

        final AppCompatImageButton imgDots = rowView.findViewById(R.id.imgDots);
        imgDots.setEnabled(alarm.snoozedToTime == null);
        imgDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsPendingOperation || alarm.snoozedToTime != null)
                    return;
                mClickedAlarm = alarm;
                PopupMenu popupMenu = new PopupMenu(context, imgDots);
                popupMenu.inflate(R.menu.menu_list_item_alarm);
                MenuBuilder menu = (MenuBuilder) popupMenu.getMenu();
                MenuItem itemSkip = menu.findItem(R.id.mnuSkipNextAlarm);
                if (mClickedAlarm.enabled) {
                    itemSkip.setEnabled(true);
                    if (mClickedAlarm.skippedTimeFlag == 0) {
                        itemSkip.setTitle(R.string.skip_next_alarm);
                    } else {
                        itemSkip.setTitle(R.string.unskip_next_alarm);
                    }
                } else {
                    itemSkip.setEnabled(false);
                }
                popupMenu.setOnMenuItemClickListener(AlarmListAdapter.this);
                MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), menu, imgDots);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();
            }
        });

        return rowView;
    }

    private void onDeleteAlarm() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_alarm)
                .setMessage(R.string.are_you_sure_delete_alarm)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mIsPendingOperation = true;
                        Utils.runInBackground(new Function<Pair<Context, Alarm>, Pair<Pair<Context, Alarm>, Alarm[]>>() {
                            @Override
                            public Pair<Pair<Context, Alarm>, Alarm[]> apply(Pair<Context, Alarm> input) {
                                AlarmDao alarmDao = AppDb.getInstance(input.first).alarmDao();
                                alarmDao.delete(input.second);
                                return new Pair<>(new Pair<>(input.first, input.second), alarmDao.getAll());
                            }
                        }, new Function<Pair<Pair<Context, Alarm>, Alarm[]>, Void>() {
                            @Override
                            public Void apply(Pair<Pair<Context, Alarm>, Alarm[]> input) {
                                remove(input.first.second);
                                notifyDataSetChanged();
                                Utils.scheduleAndDeletePrevious(input.first.first, input.second);
                                mIsPendingOperation = false;
                                return null;
                            }
                        }, new Pair<>(getContext(), mClickedAlarm));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onDuplicateAlarm() {
        mIsPendingOperation = true;
        Utils.runInBackground(new Function<Context, Void>() {
            @Override
            public Void apply(Context input) {
                Alarm alarm = mClickedAlarm.copy();
                alarm.id = 0;
                alarm.snoozedCount = 0;
                alarm.snoozedToTime = null;
                alarm.skippedTimeFlag = 0;
                alarm.skippedAlarmTime = null;
                AppDb.getInstance(input).alarmDao().insert(alarm);
                return null;
            }
        }, new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                mIsPendingOperation = false;
                onNeedRefresh.apply(null);
                return null;
            }
        }, getContext());
    }

    private void onSkipNextAlarm() {
        mIsPendingOperation = true;
        Utils.runInBackground(new Function<Context, Pair<Context, Alarm[]>>() {
            @Override
            public Pair<Context, Alarm[]> apply(Context input) {
                if (mClickedAlarm.weekDayFlags == Weekday.NO_REPEAT) {
                    if (mClickedAlarm.skippedTimeFlag == 0) {
                        Utils.NextAlarmInfo next = Utils.getNextAlarmDate(input, mClickedAlarm);
                        mClickedAlarm.skippedTimeFlag = next.timeFlag;
                        mClickedAlarm.oneTimeLeftAlarmsTimeFlags &= ~next.timeFlag;
                        if (mClickedAlarm.oneTimeLeftAlarmsTimeFlags == 0)
                            mClickedAlarm.enabled = false;
                    } else {
                        mClickedAlarm.oneTimeLeftAlarmsTimeFlags |= mClickedAlarm.skippedTimeFlag;
                        mClickedAlarm.skippedTimeFlag = 0;
                    }
                } else {
                    if (mClickedAlarm.skippedTimeFlag == 0) {
                        Utils.NextAlarmInfo next = Utils.getNextAlarmDate(input, mClickedAlarm);
                        mClickedAlarm.skippedTimeFlag = next.timeFlag;
                        mClickedAlarm.skippedAlarmTime = next.date.getTime();
                    } else {
                        mClickedAlarm.skippedAlarmTime = null;
                        mClickedAlarm.skippedTimeFlag = 0;
                    }
                }
                AlarmDao alarmDao = AppDb.getInstance(input).alarmDao();
                alarmDao.update(mClickedAlarm);
                return new Pair<>(input, alarmDao.getAll());
            }
        }, new Function<Pair<Context, Alarm[]>, Void>() {
            @Override
            public Void apply(Pair<Context, Alarm[]> input) {
                mIsPendingOperation = false;
                Utils.scheduleAndDeletePrevious(input.first, input.second);
                notifyDataSetChanged();
                return null;
            }
        }, getContext());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuDeleteAlarm:
                onDeleteAlarm();
                break;
            case R.id.mnuDuplicateAlarm:
                onDuplicateAlarm();
                break;
            case R.id.mnuPreviewAlarm:
                Intent intent = new Intent(getContext(), ShowAlarmActivity.class);
                intent.putExtra(ShowAlarmActivity.ARG_ALARM, mClickedAlarm);
                intent.putExtra(ShowAlarmActivity.ARG_IS_PREVIEW, true);
                getContext().startActivity(intent);
                break;
            case R.id.mnuSkipNextAlarm:
                onSkipNextAlarm();
                break;
        }
        return false;
    }
}
