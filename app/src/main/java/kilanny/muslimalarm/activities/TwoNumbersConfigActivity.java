package kilanny.muslimalarm.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.CharSequenceTransformation;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import kilanny.muslimalarm.R;

public class TwoNumbersConfigActivity extends AppCompatActivity {

    public static final String ARG_ACTIVITY_TITLE = "activityTitle";
    public static final String ARG_TITLE1 = "title1";
    public static final String ARG_TITLE2 = "title2";
    public static final String ARG_NUM1_FROM = "num1From";
    public static final String ARG_NUM1_TO = "num1To";
    public static final String ARG_NUM1_VALUE = "num1Value";
    public static final String ARG_NUM2_FROM = "num2From";
    public static final String ARG_NUM2_TO = "num2To";
    public static final String ARG_NUM2_VALUE = "num2Value";
    public static final String ARG_NUM2_SEEK_TICKS_COUNT = "seekCount";
    public static final String ARG_NUM2_LABELS = "num2Labels";

    public static final int RESULT_CODE_OK = 0;
    public static final int RESULT_CODE_CANCEL = 1;
    public static final String RESULT_NUM1 = "num1";
    public static final String RESULT_NUM2 = "num2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_numbers_config);

        TextView txtTitle1 = findViewById(R.id.txtTitle1);
        TextView txtTitle2 = findViewById(R.id.txtTitle2);
        final TextView txtLabel = findViewById(R.id.txtLabel);
        final NumberPicker numberPicker = findViewById(R.id.numberPicker);
        final IndicatorSeekBar seekBar = findViewById(R.id.seekBar1);
        Intent intent = getIntent();
        setTitle(intent.getStringExtra(ARG_ACTIVITY_TITLE));
        txtTitle1.setText(intent.getStringExtra(ARG_TITLE1));
        txtTitle2.setText(intent.getStringExtra(ARG_TITLE2));
        numberPicker.setMinValue(intent.getIntExtra(ARG_NUM1_FROM, 0));
        numberPicker.setMaxValue(intent.getIntExtra(ARG_NUM1_TO, 100));
        numberPicker.setValue(intent.getIntExtra(ARG_NUM1_VALUE, 50));
        seekBar.setMin(intent.getIntExtra(ARG_NUM2_FROM, 0));
        seekBar.setMax(intent.getIntExtra(ARG_NUM2_TO, 100));
        seekBar.setTickCount(intent.getIntExtra(ARG_NUM2_SEEK_TICKS_COUNT, 6));
        seekBar.setProgress(intent.getIntExtra(ARG_NUM2_VALUE, 70));
        final String[] labels = intent.getStringArrayExtra(ARG_NUM2_LABELS);
        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                if (labels != null)
                    txtLabel.setText(labels[seekParams.progress - 1]);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });
        txtLabel.setText(labels != null ? labels[seekBar.getProgress() - 1] : "");

        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CODE_CANCEL);
                finish();
            }
        });
        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent res = new Intent();
                res.putExtra(RESULT_NUM1, numberPicker.getValue());
                res.putExtra(RESULT_NUM2, seekBar.getProgress());
                setResult(RESULT_CODE_OK, res);
                finish();
            }
        });
    }
}
