package kilanny.muslimalarm.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.Tune;

public class SelectTuneAdapter extends ArrayAdapter<Tune> {

    private static MediaPlayer mediaPlayer;

    private final Tune[] mItems;
    private Tune mSelectedTune;

    public SelectTuneAdapter(@NonNull Context context, Tune[] items) {
        super(context, R.layout.tune_list_item, items);
        mItems = items;
    }

    public Tune getSelectedTune() {
        return mSelectedTune;
    }

    public void setSelectedTune(Tune selectedTune) {
        mSelectedTune = selectedTune;
        for (int i = 0; i < getCount(); ++i) {
            mItems[i].selected = mItems[i].equals(selectedTune);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Tune getItem(int position) {
        return mItems[position];
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
        txt.setText(item.nameResId);
        AppCompatRadioButton radioButtonSel = rowView.findViewById(R.id.radioSelected);
        radioButtonSel.setChecked(item.selected);
        //radioButtonSel.setEnabled(false);
        radioButtonSel.setClickable(false);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedTune(item);
            }
        });
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
                    playTune(getContext(), item.rawResId);
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

    private static void playTune(Context context, int tuneRawResId) {
        stopPlayback();
        mediaPlayer = MediaPlayer.create(context, tuneRawResId);
        mediaPlayer.start();
    }

    public static void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
