package kilanny.muslimalarm.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.arch.core.util.Function;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.internal.ViewUtils;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kilanny.muslimalarm.AlarmRingBroadcastReceiver;
import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Alarm;
import kilanny.muslimalarm.data.AlarmDao;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.AppSettings;
import kilanny.muslimalarm.data.Weekday;
import kilanny.muslimalarm.util.AnalyticsTrackers;
import kilanny.muslimalarm.util.PrayTime;
import kilanny.muslimalarm.util.Utils;

public class AlarmListAdapter extends ArrayAdapter<Alarm>
        implements PopupMenu.OnMenuItemClickListener {

    private static final int ICON_MARGIN = 8;

    private final SimpleDateFormat timeF;
    private final Function<Alarm, Void> onAlarmEdit;
    private final Function<Void, Void> onNeedRefresh;
    private Alarm mClickedAlarm;
    public boolean mIsPendingOperation = false;

    public AlarmListAdapter(@NonNull Context context, @NonNull Function<Alarm, Void> onAlarmEdit,
                            @NonNull Function<Void, Void> onNeedRefresh) {
        super(context, R.layout.list_item_alarm);
        this.onAlarmEdit = onAlarmEdit;
        this.onNeedRefresh = onNeedRefresh;
        int format = AppSettings.getInstance(context).getTimeFormatFor(0);
        if (format != PrayTime.TIME_24)
            timeF = new SimpleDateFormat("hh:mm " + (format == PrayTime.TIME_12_NS ? "" : "aa"),
                    Locale.getDefault());
        else
            timeF = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    private static void setTextThru(AppCompatTextView textView, boolean isThru) {
        if (isThru) {
            textView.setPaintFlags(
                    textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(
                    textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private boolean checkConvertView(View convertView, boolean isSnoozed) {
        if (convertView == null)
            return false;
        if (!isSnoozed)
            return convertView.findViewById(R.id.chkAlarmEnabled) != null;
        else
            return convertView.findViewById(R.id.btnDismiss) != null;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView;
        final Alarm alarm = getItem(position);
        boolean isSnoozed = alarm.snoozedToTime != null;
        if (!checkConvertView(convertView, isSnoozed)) {
            int res = isSnoozed ? R.layout.list_item_alarm_snoozed
                    : R.layout.list_item_alarm;
            rowView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(res, parent, false);
        } else
            rowView = convertView;

        final Context context = rowView.getContext();
        View dismiss = rowView.findViewById(R.id.btnDismiss);
        if (alarm.snoozedToTime != null && dismiss != null) {
            dismiss.setOnClickListener(view -> {
                Intent intent = new Intent(context, AlarmRingBroadcastReceiver.class);
                intent.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM, alarm);
                intent.putExtra(AlarmRingBroadcastReceiver.ARG_IS_PREVIEW, false);
                intent.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM_TIME,
                        alarm.weekDayFlags == Weekday.NO_REPEAT ? alarm.timeFlags : 0);
                context.sendBroadcast(intent);
            });
            return rowView;
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = pref.getString("nextAlarmJson", null);
        Alarm nextAlarm = null;
        if (json != null) {
            try {
                nextAlarm = Alarm.fromJson(json);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RuntimeException("Invalid JSON", e);
            }
        }
        ((MaterialCardView) rowView).setStrokeColor(nextAlarm != null && nextAlarm.id == alarm.id ?
                context.getResources().getColor(R.color.ms_material_blue_500)
                : context.getResources().getColor(R.color.ms_material_grey_400));
        ValueAnimator oldAnimator = (ValueAnimator) rowView.getTag();
        if (oldAnimator != null) {
            oldAnimator.cancel();
            rowView.setTag(null);
            ((MaterialCardView) rowView).setStrokeWidth(4);
        }
        if (nextAlarm != null && nextAlarm.id == alarm.id) {
            ValueAnimator animation = ValueAnimator.ofInt(1, 2, 3, 4);
            animation.setRepeatCount(ValueAnimator.INFINITE);
            animation.setRepeatMode(ValueAnimator.REVERSE);
            animation.addUpdateListener(animation1 -> {
                ((MaterialCardView) rowView).setStrokeWidth((Integer) animation1.getAnimatedValue());
                ((MaterialCardView) rowView).setCardElevation(8 + (Integer) animation1.getAnimatedValue());
            });
            animation.setDuration(1300);
            animation.start();
            rowView.setTag(animation);
        }

        rowView.setOnClickListener(view -> {
            if (!mIsPendingOperation)
                onAlarmEdit.apply(alarm);
        });
        final AppCompatTextView alarmTime = rowView.findViewById(R.id.alarmTime);
        AppCompatTextView alarmTimeAfterSkip = rowView.findViewById(R.id.alarmTimeAfterSkip);
        if (alarm.enabled) {
            int timeFlagSkip, timeFlagAct = 0;
            Date dateSkip, dateAct = null;
            boolean isSkipped = alarm.skippedAlarmTime != null &&
                    alarm.skippedAlarmTime >= System.currentTimeMillis();
            Utils.NextAlarmInfo info = Utils.getNextAlarmDate(context, alarm);
            if (isSkipped) {
                timeFlagSkip = alarm.skippedTimeFlag;
                dateSkip = new Date(alarm.skippedAlarmTime);
                timeFlagAct = info.timeFlag;
                dateAct = info.date;
            } else {
                timeFlagSkip = info.timeFlag;
                dateSkip = info.date;
            }
            alarmTime.setText(String.format("%s (%s) - %s",
                    Utils.getTimeName(context, timeFlagSkip),
                    timeF.format(dateSkip),
                    Utils.getLeftTimeToDate(context, dateSkip)));
            setTextThru(alarmTime, isSkipped);
            if (isSkipped) {
                alarmTimeAfterSkip.setVisibility(View.VISIBLE);
                alarmTimeAfterSkip.setText(String.format("%s (%s) - %s",
                        Utils.getTimeName(context, timeFlagAct),
                        timeF.format(dateAct),
                        Utils.getLeftTimeToDate(context, dateAct)));
            } else {
                alarmTimeAfterSkip.setVisibility(View.GONE);
            }
        } else {
            alarmTime.setText(null);
            alarmTimeAfterSkip.setVisibility(View.GONE);
        }

        AppCompatCheckBox chkAlarmEnabled = rowView.findViewById(R.id.chkAlarmEnabled);
        chkAlarmEnabled.setEnabled(alarm.snoozedToTime == null);
        chkAlarmEnabled.setChecked(alarm.enabled);
        chkAlarmEnabled.setOnClickListener(v -> {
            boolean b = ((AppCompatCheckBox) v).isChecked();
            v.setEnabled(false);
            Utils.runInBackground(input -> {
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
            }, input -> {
                mIsPendingOperation = false;
                v.setEnabled(true);
                clear();
                addAll(input.second);
                notifyDataSetChanged();
                Utils.scheduleAndDeletePrevious(input.first, input.second);
                return null;
            }, v.getContext());
        });

        AppCompatTextView prayerName = rowView.findViewById(R.id.prayerName);
        prayerName.setText(Utils.getPrayerNames(context, alarm));

        AppCompatTextView timeOffset = rowView.findViewById(R.id.timeOffset);
        if (alarm.timeFlags != Alarm.TIME_CUSTOM) {
            timeOffset.setVisibility(View.VISIBLE);
            timeOffset.setText(Utils.getTimeOffsetDescription(context, alarm));
        } else {
            timeOffset.setVisibility(View.GONE);
        }

        AppCompatTextView alarmDays = rowView.findViewById(R.id.alarmDays);
        alarmDays.setText(Utils.getAlarmDays(context, alarm));

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

        final MaterialButton imgDots = rowView.findViewById(R.id.btnActions);
        imgDots.setEnabled(alarm.snoozedToTime == null);
        imgDots.setOnClickListener(view -> {
            if (mIsPendingOperation || alarm.snoozedToTime != null)
                return;
            mClickedAlarm = alarm;
            PopupMenu popupMenu = new PopupMenu(context, imgDots);
            popupMenu.inflate(R.menu.menu_list_item_alarm);
            MenuBuilder menu = (MenuBuilder) popupMenu.getMenu();
            menu.setOptionalIconsVisible(true);
            for (MenuItem item : menu.getVisibleItems()) {
                int iconMarginPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, ICON_MARGIN, getContext().getResources().getDisplayMetrics());

                if (item.getIcon() != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0));
                    } else {
                        item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0) {
                            @Override
                            public int getIntrinsicWidth() {
                                return getIntrinsicHeight() + iconMarginPx + iconMarginPx;
                            }
                        });
                    }
                }
            }
            MenuItem itemSkip = menu.findItem(R.id.mnuSkipNextAlarm);
            if (mClickedAlarm.enabled) {
                itemSkip.setEnabled(true);
                if (mClickedAlarm.skippedAlarmTime == null
                        || mClickedAlarm.skippedAlarmTime < System.currentTimeMillis()) {
                    itemSkip.setTitle(R.string.skip_next_alarm);
                } else {
                    itemSkip.setTitle(R.string.unskip_next_alarm);
                }
            } else {
                itemSkip.setEnabled(false);
            }
            popupMenu.setOnMenuItemClickListener(this);
            MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), menu, imgDots);
            menuHelper.setForceShowIcon(true);
            menuHelper.show();
        });

        float elevation = ViewUtils.dpToPx(getContext(), 16);
        ViewCompat.setElevation(rowView, elevation);
        return rowView;
    }

    private void onDeleteAlarm() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_alarm)
                .setMessage(R.string.are_you_sure_delete_alarm)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    mIsPendingOperation = true;
                    Utils.runInBackground(input -> {
                        AlarmDao alarmDao = AppDb.getInstance(input.first).alarmDao();
                        alarmDao.delete(input.second);
                        AnalyticsTrackers.getInstance(input.first).logAlarmDeleted(input.second);
                        return new Pair<>(new Pair<>(input.first, input.second), alarmDao.getAll());
                    }, input -> {
                        remove(input.first.second);
                        notifyDataSetChanged();
                        Utils.scheduleAndDeletePrevious(input.first.first, input.second);
                        mIsPendingOperation = false;
                        return null;
                    }, new Pair<>(getContext(), mClickedAlarm));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onDuplicateAlarm() {
        mIsPendingOperation = true;
        Utils.runInBackground((Function<Context, Void>) input -> {
            Alarm alarm = mClickedAlarm.copy();
            alarm.id = 0;
            alarm.snoozedCount = 0;
            alarm.snoozedToTime = null;
            alarm.skippedTimeFlag = 0;
            alarm.skippedAlarmTime = null;
            AppDb.getInstance(input).alarmDao().insert(alarm);
            return null;
        }, input -> {
            mIsPendingOperation = false;
            onNeedRefresh.apply(null);
            return null;
        }, getContext());
    }

    private void onSkipNextAlarm() {
        mIsPendingOperation = true;
        Utils.runInBackground(input -> {
            if (mClickedAlarm.weekDayFlags == Weekday.NO_REPEAT) {
                if (mClickedAlarm.skippedTimeFlag == 0
                        || mClickedAlarm.skippedAlarmTime < System.currentTimeMillis()) {
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
                if (mClickedAlarm.skippedTimeFlag == 0
                        || mClickedAlarm.skippedAlarmTime < System.currentTimeMillis()) {
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
            AnalyticsTrackers.getInstance(input).logAlarmSkipped(mClickedAlarm);
            return new Pair<>(input, alarmDao.getAll());
        }, input -> {
            mIsPendingOperation = false;
            Utils.scheduleAndDeletePrevious(input.first, input.second);
            notifyDataSetChanged();
            return null;
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
                Intent intent = new Intent(getContext(), AlarmRingBroadcastReceiver.class);
                intent.putExtra(AlarmRingBroadcastReceiver.ARG_ALARM, mClickedAlarm);
                intent.putExtra(AlarmRingBroadcastReceiver.ARG_IS_PREVIEW, true);
                getContext().sendBroadcast(intent);
                break;
            case R.id.mnuSkipNextAlarm:
                onSkipNextAlarm();
                break;
        }
        return false;
    }
}
