package kilanny.muslimalarm.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.adapters.SelectTuneAdapter;
import kilanny.muslimalarm.data.Tune;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectRingtuneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectRingtuneFragment extends Fragment {

    public static final int TYPE_TUNES = 1;
    public static final int TYPE_LOUD_TUNES = 2;
    public static final int TYPE_FILE_TUNES = 3;

    private static final String ARG_TYPE = "type";

    private int mType;
    private String mSelectedTune;

    public SelectRingtuneFragment() {
        // Required empty public constructor
    }

    public String getSelectedTune() {
        return mSelectedTune;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectRingtuneFragment.
     */
    public static SelectRingtuneFragment newInstance(int type) {
        SelectRingtuneFragment fragment = new SelectRingtuneFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_ringtune, container, false);
        SelectTuneAdapter adapter = null;
        switch (mType) {
            case TYPE_TUNES:
                adapter = new SelectTuneAdapter(getContext(), Tune.getTunes());
                break;
            case TYPE_LOUD_TUNES:
                break;
            case TYPE_FILE_TUNES:
                break;
        }
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        return view;
    }
}
