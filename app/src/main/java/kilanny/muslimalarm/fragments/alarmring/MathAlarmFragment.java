package kilanny.muslimalarm.fragments.alarmring;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.util.MathExpressionManager;
import kilanny.muslimalarm.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MathAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MathAlarmFragment extends ShowAlarmFragment implements View.OnClickListener {

    private static final String ARG_NUM_PROBLEMS = "numProblems";
    private static final String ARG_LEVEL = "level";
    private static final String ARG_IS_SILENT = "isSilent";

    private int mNumProblems;
    private int mLevel;
    private boolean mIsSilent;
    private int mSolved = 0;
    private int mAnswer;
    private TextView mTxtUserInput, mTxtProblemProgress, mTxtMathProblem;
    private ShowAlarmFragment.FragmentInteractionListener mListener;
    private Timer mWrongAnswerTimer;
    private AppCompatImageButton mBtnAccept;

    public MathAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MathAlarmFragment.
     */
    public static MathAlarmFragment newInstance(int numProblems, int level, boolean isSilent) {
        MathAlarmFragment fragment = new MathAlarmFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NUM_PROBLEMS, numProblems);
        args.putInt(ARG_LEVEL, level);
        args.putBoolean(ARG_IS_SILENT, isSilent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNumProblems = getArguments().getInt(ARG_NUM_PROBLEMS);
            mLevel = getArguments().getInt(ARG_LEVEL);
            mIsSilent = getArguments().getBoolean(ARG_IS_SILENT);
        }
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        try {
            mListener = (ShowAlarmFragment.FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_math_alarm, container, false);
        view.findViewById(R.id.btnZero).setOnClickListener(this);
        view.findViewById(R.id.btnOne).setOnClickListener(this);
        view.findViewById(R.id.btnTwo).setOnClickListener(this);
        view.findViewById(R.id.btnThree).setOnClickListener(this);
        view.findViewById(R.id.btnFour).setOnClickListener(this);
        view.findViewById(R.id.btnFive).setOnClickListener(this);
        view.findViewById(R.id.btnSix).setOnClickListener(this);
        view.findViewById(R.id.btnSeven).setOnClickListener(this);
        view.findViewById(R.id.btnEight).setOnClickListener(this);
        view.findViewById(R.id.btnNine).setOnClickListener(this);
        view.findViewById(R.id.btnClear).setOnClickListener(this);
        ImageView imageView = view.findViewById(R.id.imgIsSilent);
        imageView.setImageResource(mIsSilent ? android.R.drawable.ic_lock_silent_mode
                : android.R.drawable.ic_lock_silent_mode_off);
        imageView.setBackgroundResource(mIsSilent ? android.R.color.holo_green_light :
                android.R.color.holo_red_dark);
        mBtnAccept = view.findViewById(R.id.btnAccept);
        mBtnAccept.setOnClickListener(this);
        mTxtUserInput = view.findViewById(R.id.txtUserInput);
        mTxtProblemProgress = view.findViewById(R.id.txtProblemProgress);
        mTxtMathProblem = view.findViewById(R.id.txtMathProblem);
        generateProblem();
        updateProgress();
        return view;
    }

    private void updateProgress() {
        mTxtProblemProgress.setText(String.format(Locale.ENGLISH,
                "%d / %d", mSolved + 1, mNumProblems));
    }

    private void generateProblem() {
        String expr = MathExpressionManager.generateExpression(mLevel);
        mAnswer = MathExpressionManager.solveExpression(expr);
        StringBuilder s = new StringBuilder(expr);
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == '+' || s.charAt(i) == 'x') {
                s.insert(i + 1, ' ');
                s.insert(i, ' ');
                ++i;
            }
        }
        mTxtMathProblem.setText(s);
    }

    private void onAccept(Context context) {
        String txt = mTxtUserInput.getText().toString();
        if (txt.equals("؟")) {
            wrongAnswer(context);
            return;
        }
        int u = Integer.parseInt(txt);
        if (u == mAnswer) {
            ++mSolved;
            if (mSolved < mNumProblems) {
                updateProgress();
                generateProblem();
                mTxtUserInput.setText("؟");
            } else {
                mListener.onDismissed(true);
            }
        } else {
            wrongAnswer(context);
        }
    }

    private void wrongAnswer(final Context context) {
        mBtnAccept.setImageDrawable(
                context.getResources().getDrawable(R.drawable.iconfinder_error_32686));
        if (mWrongAnswerTimer != null)
            mWrongAnswerTimer.cancel();
        mWrongAnswerTimer = new Timer();
        mWrongAnswerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mWrongAnswerTimer = null;
                new Handler(context.getMainLooper()).post(() -> mBtnAccept.setImageResource(
                        R.drawable.outline_done_outline_24));
            }
        }, 2000);
        mTxtUserInput.setText("؟");

        Utils.vibrateFor(context, 500);
    }

    @Override
    public void onClick(View view) {
        mListener.onResetSleepTimeout();
        if (view.getId() == R.id.btnAccept) {
            onAccept(view.getContext());
            return;
        }

        StringBuilder txt = new StringBuilder(mTxtUserInput.getText().toString());
        if (txt.length() > 6)
            return;
        if (txt.toString().equals("؟"))
            txt.delete(0, txt.length());
        switch (view.getId()) {
            case R.id.btnZero:
            case R.id.btnOne:
            case R.id.btnTwo:
            case R.id.btnThree:
            case R.id.btnFour:
            case R.id.btnFive:
            case R.id.btnSix:
            case R.id.btnSeven:
            case R.id.btnEight:
            case R.id.btnNine:
                txt.append(((AppCompatButton) view).getText().toString());
                break;
            case R.id.btnClear:
                if (txt.length() > 0)
                    txt.deleteCharAt(txt.length() - 1);
                break;
        }
        while (txt.length() > 0 && txt.charAt(0) == '0')
            txt.deleteCharAt(0);
        if (txt.length() == 0)
            txt.append('؟');
        mTxtUserInput.setText(txt);
    }
}
