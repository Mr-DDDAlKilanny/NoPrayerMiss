package kilanny.muslimalarm.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.adapters.SelectRingtuneSectionsPagerAdapter;
import kilanny.muslimalarm.adapters.SelectTuneAdapter;
import kilanny.muslimalarm.fragments.NeedPermissionFragment;

public class SelectRingtuneActivity extends AppCompatActivity
        implements NeedPermissionFragment.OnFragmentInteractionListener,
        ViewPager.OnPageChangeListener {

    private static final int REQUEST_PERMISSION = 0;

    private SelectRingtuneSectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ringtune);
        boolean p = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            p = false;
        }
        mSectionsPagerAdapter = new SelectRingtuneSectionsPagerAdapter(this,
                getSupportFragmentManager(), p);
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);
        setTitle(R.string.select_alarm_tune);

        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onAskPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SelectTuneAdapter.stopPlayback();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mSectionsPagerAdapter = new SelectRingtuneSectionsPagerAdapter(this,
                    getSupportFragmentManager(), true);
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onAskPermission() {
        if (!mSectionsPagerAdapter.isHavingPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        SelectTuneAdapter.stopPlayback();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}