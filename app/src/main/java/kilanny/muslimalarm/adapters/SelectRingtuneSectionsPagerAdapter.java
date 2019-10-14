package kilanny.muslimalarm.adapters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.fragments.NeedPermissionFragment;
import kilanny.muslimalarm.fragments.SelectRingtuneFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SelectRingtuneSectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_text_1,
            R.string.tab_text_2,
            R.string.tab_text_3
    };
    private final Context mContext;
    private final boolean mIsHavingPermission;

    public SelectRingtuneSectionsPagerAdapter(Context context, FragmentManager fm,
                                              boolean isHavingPermission) {
        super(fm);
        mContext = context;
        mIsHavingPermission = isHavingPermission;
    }

    public boolean isHavingPermission() {
        return mIsHavingPermission;
    }

    @Override
    public Fragment getItem(int position) {
        return mIsHavingPermission || position == 1 ?
                SelectRingtuneFragment.newInstance(position + 1)
                : NeedPermissionFragment.newInstance();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 3;
    }
}