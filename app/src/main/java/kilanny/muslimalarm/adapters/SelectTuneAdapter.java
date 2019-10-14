package kilanny.muslimalarm.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import java.io.IOException;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Tune;

public class SelectTuneAdapter extends ArrayAdapter<Tune>
        implements CompoundButton.OnCheckedChangeListener {

    private static MediaPlayer mediaPlayer;
    private static Ringtone ringtone;

    private String mSelectedTune;
    private boolean mIsListeningForChange = true;

    public SelectTuneAdapter(@NonNull Context context, Tune[] items) {
        super(context, R.layout.tune_list_item, items);
    }

    public String getSelectedTune() {
        return mSelectedTune;
    }

    public void setSelectedTune(String mSelectedTune) {
        this.mSelectedTune = mSelectedTune;
        for (int i = 0; i < getCount(); ++i) {
            Tune tune = getItem(i);
            tune.selected = tune.path.equals(mSelectedTune);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView;
        if (convertView == null)
            rowView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.tune_list_item, parent, false);
        else
            rowView = convertView;
        AppCompatTextView txt = rowView.findViewById(R.id.txtTuneName);
        final Tune item = getItem(position);
        txt.setText(item.name);
        final AppCompatRadioButton radioButton = rowView.findViewById(R.id.radioSelected);
        radioButton.setChecked(item.selected);
        radioButton.setOnCheckedChangeListener(this);
        radioButton.setTag(item.path);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("onClick", "Will select " + item.name);
                radioButton.setChecked(true);
            }
        });
        //Log.v("createView", "Item: " + item.path + ", selected: " + mSelectedTune);
        final AppCompatImageButton btn = rowView.findViewById(R.id.btnPreviewTune);
        btn.setImageDrawable(getContext().getResources().getDrawable(item.playing ?
                android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.playing) {
                    stopPlayback();
                    for (int i = 0; i < getCount(); ++i)
                        getItem(i).playing = false;
                } else {
                    playTune(getContext(), item.path);
                    for (int i = 0; i < position; ++i)
                        getItem(i).playing = false;
                    item.playing = true;
                    for (int i = position + 1; i < getCount(); ++i)
                        getItem(i).playing = false;
                }
                notifyDataSetChanged();
            }
        });
        return rowView;
    }

    private static void playTune(Context context, String tune) {
        stopPlayback();
        Log.v("playTune", tune);
        if (tune.startsWith("content://")) {
            ringtone = RingtoneManager.getRingtone(context, Uri.parse(tune));
            ringtone.play();
        } else {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(tune);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, context.getString(R.string.failled_to_play_file, tune),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (ringtone != null) {
            if (ringtone.isPlaying())
                ringtone.stop();
            ringtone = null;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!mIsListeningForChange || !b)
            return;
        mIsListeningForChange = false;
        mSelectedTune = (String) compoundButton.getTag();
        Log.v("onCheckedChange", "Firing event for " + mSelectedTune);
        for (int i = 0; i < getCount(); ++i) {
            Tune item = getItem(i);
            item.selected = item.path.equals(mSelectedTune);
        }
        notifyDataSetChanged();
        mIsListeningForChange = true;
    }
}
