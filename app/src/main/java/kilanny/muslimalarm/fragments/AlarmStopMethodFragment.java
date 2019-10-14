package kilanny.muslimalarm.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kilanny.muslimalarm.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlarmStopMethodFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlarmStopMethodFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmStopMethodFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private int imageDrawableId;
    private String title;
    private boolean selected;
    private AppCompatImageButton img;
    private RelativeLayout backgroundLayout;

    public AlarmStopMethodFragment() {
        // Required empty public constructor
    }

    public int getImageDrawableId() {
        return imageDrawableId;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AlarmStopMethodFragment.
     */
    public static AlarmStopMethodFragment newInstance(int imageDrawableId, String title,
                                                      boolean selected) {
        AlarmStopMethodFragment fragment = new AlarmStopMethodFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("image_drawable_id", imageDrawableId);
        bundle.putString("title", title);
        bundle.putBoolean("selected", selected);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setSelected(boolean selected) {
        int color = img.getContext().getResources().getColor(
                selected ? R.color.colorPrimary : android.R.color.white);
        img.setBackgroundColor(color);
        backgroundLayout.setBackgroundColor(color);
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageDrawableId = getArguments().getInt("image_drawable_id");
            title = getArguments().getString("title");
            selected = getArguments().getBoolean("selected");
        }
    }

    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs,
                          Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        if (imageDrawableId == 0) {
            TypedArray ta = context.obtainStyledAttributes(attrs,
                    R.styleable.AlarmStopMethodFragment_MembersInjector);
            if (ta.hasValue(R.styleable.AlarmStopMethodFragment_MembersInjector_imageDrawableId)) {
                imageDrawableId = ta.getInt(
                        R.styleable.AlarmStopMethodFragment_MembersInjector_imageDrawableId,
                        0);
            }
            ta.recycle();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm_stop_method, container, false);
        img = view.findViewById(R.id.btnAlarmLogo);
        backgroundLayout = view.findViewById(R.id.layoutBackground);
        Context context = view.getContext();
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 21)
            drawable = context.getDrawable(imageDrawableId);
        else
            drawable = container.getResources().getDrawable(imageDrawableId);
        img.setImageDrawable(drawable);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onFragmentLogoButtonClick(AlarmStopMethodFragment.this);
            }
        });
        view.findViewById(R.id.bottomLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onFragmentPreviewClick(AlarmStopMethodFragment.this);
            }
        });
        TextView textView = view.findViewById(R.id.txtTitle);
        textView.setText(title);
        setSelected(selected);
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
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentPreviewClick(AlarmStopMethodFragment sender);
        void onFragmentLogoButtonClick(AlarmStopMethodFragment sender);
    }
}
