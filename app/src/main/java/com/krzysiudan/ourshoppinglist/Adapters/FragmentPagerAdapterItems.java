package com.krzysiudan.ourshoppinglist.Adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.krzysiudan.ourshoppinglist.Fragments.FragmentBoughtItems;
import com.krzysiudan.ourshoppinglist.Fragments.FragmentPlannedItems;
import com.krzysiudan.ourshoppinglist.R;

public class FragmentPagerAdapterItems extends FragmentPagerAdapter {

    final int PAGE_COUNT =2;
    private String tabTitles[] = new String[] {"Planowane","Kupione"};
    private String mMotherListName;
    private Context mContext;

    public FragmentPagerAdapterItems(FragmentManager fm, Context context, String MotherListName) {
        super(fm);
        mContext = context;
        mMotherListName=MotherListName;
    }



    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return FragmentPlannedItems.newInstance(mMotherListName);
            case 1: return FragmentBoughtItems.newInstance(mMotherListName);
            default: return FragmentPlannedItems.newInstance(mMotherListName);
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

}