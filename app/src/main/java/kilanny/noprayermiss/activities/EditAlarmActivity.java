package kilanny.noprayermiss.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import kilanny.noprayermiss.R;
import kilanny.noprayermiss.dialogs.NumberPickerDialog;

public class EditAlarmActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    private static final int CHOOSE_ALARM_STOP_REQUEST = 1;

    private int selectedMinutes = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);

        findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: needs to be two pickers: hours & minutes
                NumberPickerDialog newFragment = new NumberPickerDialog(0, 600,
                        selectedMinutes, "", "");
                newFragment.setValueChangeListener(EditAlarmActivity.this);
                newFragment.show(getSupportFragmentManager(), "time picker");
            }
        });
        findViewById(R.id.firstCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(EditAlarmActivity.this,
                        ChooseAlarmStopMethodActivity.class), CHOOSE_ALARM_STOP_REQUEST);
            }
        });

        RadioGroup group = findViewById(R.id.radioGroupBeforeAfter);
        if (group.getCheckedRadioButtonId() == R.id.radioBefore) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_ALARM_STOP_REQUEST) {

        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        selectedMinutes = numberPicker.getValue();
    }
}
