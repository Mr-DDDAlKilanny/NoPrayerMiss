package kilanny.muslimalarm.data;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import java.util.ArrayList;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.util.Utils;

public class AlarmHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

    public final Alarm mAlarm;
    private final AppCompatCheckBox mCheckboxEnabled;
    private final AppCompatTextView mtxtPrayerName, mtxtDays, mtxtTime;
    private final AppCompatImageView mIcon;
    private final AppCompatImageButton mbtnDots;

    private static final int[] weekdays = {
            R.string.friday,
            R.string.saturday,
            R.string.sunday,
            R.string.monday,
            R.string.tuesday,
            R.string.wednesday,
            R.string.thursday
    };
    private static final int[] times = {
            R.string.fajr,
            R.string.sun,
            R.string.zuhr,
            R.string.asr,
            R.string.maghrib,
            R.string.ishaa
    };

    public AlarmHolder(@NonNull View itemView, Alarm alarm) {
        super(itemView);
        mAlarm = alarm;
        mCheckboxEnabled = itemView.findViewById(R.id.chkAlarmEnabled);
        mtxtPrayerName = itemView.findViewById(R.id.prayerName);
        mtxtDays = itemView.findViewById(R.id.alarmDays);
        mtxtTime = itemView.findViewById(R.id.alarmTime);
        mIcon = itemView.findViewById(R.id.alarmTypeIcon);
        mbtnDots = itemView.findViewById(R.id.imgDots);
    }

    public void bind() {
        mCheckboxEnabled.setChecked(mAlarm.enabled);

        // time name
        String t;
        //TODO: display time with hours
        if (mAlarm.timeAlarmDiffMinutes > 0)
            t = itemView.getContext().getString(R.string.beforeTimeBy, -mAlarm.timeAlarmDiffMinutes);
        else
            t = itemView.getContext().getString(R.string.afterTimeBy, mAlarm.timeAlarmDiffMinutes);
        if (mAlarm.timeFlags == 63)
            mtxtPrayerName.setText(itemView.getContext().getString(R.string.all_times) + " " + t);
        else if (mAlarm.timeFlags == 47) // all prayers
            mtxtPrayerName.setText(itemView.getContext().getString(R.string.all_prayers) + " " + t);
        else {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < times.length; ++i) {
                int f = 1 << i;
                if ((mAlarm.timeFlags & f) != 0)
                    b.append(itemView.getContext().getString(times[i])).append(',');
            }
            b.delete(b.length() - 1, 1);
            mtxtPrayerName.setText(b.toString() + " " + t);
        }

        // left time
        if (mAlarm.enabled) {
            DateTime next = new DateTime(Utils.getNextAlarmDate(itemView.getContext(), mAlarm));
            DateTime now = new DateTime();
            DateTime diff = next.minus(now.getMillis());
            if (diff.dayOfMonth().get() > 1)
                mtxtTime.setText(itemView.getContext().getString(R.string.afterNDays, diff.dayOfMonth().get()));
            else if (diff.hourOfDay().get() > 1)
                mtxtTime.setText(itemView.getContext().getString(R.string.afterNHours,
                        Integer.toString(diff.hourOfDay().get())));
            else {
                int total = diff.hourOfDay().get() * 60 + diff.minuteOfDay().get();
                mtxtTime.setText(itemView.getContext().getString(R.string.afterNMinutes,
                        Integer.toString(total)));
            }
        }
        // days
        if (mAlarm.weekDayFlags == 0)
            mtxtDays.setText(itemView.getContext().getString(R.string.one_time));
        else if (mAlarm.weekDayFlags == 127)
            mtxtDays.setText(itemView.getContext().getString(R.string.everyday));
        else {
            StringBuilder b = new StringBuilder();
            ArrayList<Integer> not = new ArrayList<>();
            for (int i = 0; i < weekdays.length; ++i) {
                int f = 1 << i;
                if ((mAlarm.weekDayFlags & f) == 0)
                    not.add(i);
            }
            if (not.size() >= 3) {
                for (int i = 0; i < weekdays.length; ++i) {
                    int f = 1 << i;
                    if ((mAlarm.weekDayFlags & f) != 0)
                        b.append(itemView.getContext().getString(weekdays[i])).append(',');
                }
                b.delete(b.length() - 1, 1);
            } else if (not.size() == 2) {
                b.append(itemView.getContext().getString(R.string.allDaysExpect2Days,
                        itemView.getContext().getString(weekdays[not.get(0)]),
                        itemView.getContext().getString(weekdays[not.get(1)])));
            } else {
                b.append(itemView.getContext().getString(R.string.allDaysExpect1Day,
                        itemView.getContext().getString(weekdays[not.get(0)])));
            }
            mtxtDays.setText(b.toString());
        }

        // icon
        switch (mAlarm.dismissAlarmType) {
            case Alarm.DISMISS_ALARM_DEFAULT:
                mIcon.setImageResource(R.drawable.clock);
                break;
            case Alarm.DISMISS_ALARM_SHAKE:
                mIcon.setImageResource(R.drawable.shake);
                break;
            case Alarm.DISMISS_ALARM_MATH:
                mIcon.setImageResource(R.drawable.math);
                break;
            case Alarm.DISMISS_ALARM_BARCODE:
                mIcon.setImageResource(R.drawable.barcode);
                break;
        }

        // menu
        mbtnDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate menu
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_list_item_alarm, popup.getMenu());
                popup.setOnMenuItemClickListener(AlarmHolder.this);
                popup.show();
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuDeleteAlarm:
                break;
            case R.id.mnuSkipNextAlarm:
                break;
            case R.id.mnuPreviewAlarm:
                break;
            case R.id.mnuDuplicateAlarm:
                break;
        }
        return false;
    }
}
