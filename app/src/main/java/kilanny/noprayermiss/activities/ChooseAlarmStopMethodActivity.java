package kilanny.noprayermiss.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import kilanny.noprayermiss.R;
import kilanny.noprayermiss.fragments.AlarmStopMethodFragment;

public class ChooseAlarmStopMethodActivity extends AppCompatActivity
        implements AlarmStopMethodFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_alarm_stop_method);

        FragmentManager fragmentManager = getSupportFragmentManager();
        String[] t = {
                getString(R.string.Default),
                getString(R.string.phone_shaking),
                getString(R.string.barcode),
                getString(R.string.math_problems)
        };
        /*<div>Icons made by <a href="https://www.flaticon.com/authors/freepik"
        title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/"
        title="Flaticon">www.flaticon.com</a> is licensed by
        <a href="http://creativecommons.org/licenses/by/3.0/"
        title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>*/
        fragmentManager.beginTransaction()
                .replace(R.id.topLeftLayout,
                        AlarmStopMethodFragment.newInstance(R.drawable.alarm, t[0]))
                .replace(R.id.topRightLayout,
                        AlarmStopMethodFragment.newInstance(R.drawable.cell_phone_vibration, t[1]))
                .replace(R.id.bottomLeftLayout,
                        AlarmStopMethodFragment.newInstance(R.drawable.barcode, t[2]))
                .replace(R.id.bottomRightLayout,
                        AlarmStopMethodFragment.newInstance(R.drawable.math, t[3]))
                .commit();
        setTitle("Alarm Stop Method");
    }

    @Override
    public void onFragmentPreviewClick(AlarmStopMethodFragment sender) {
    }

    @Override
    public void onFragmentLogoButtonClick(AlarmStopMethodFragment sender) {
    }
}
