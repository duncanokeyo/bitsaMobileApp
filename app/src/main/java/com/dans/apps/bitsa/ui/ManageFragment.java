package com.dans.apps.bitsa.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.model.User;


public class ManageFragment extends BaseFragmentChildrenFragments {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    void setViews() {
        adapter.addFragment(new GeneralFragment(),getResources().getString(R.string.general));
        adapter.addFragment(new StudentsFragment(), getResources().getString(R.string.student_plural));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onUserFetched(User currentUser) {
        if(currentUser.getType() == Constants.USER_TYPE.SUPER_ADMIN){
            adapter.addFragment(new ClubOfficialsFragment(),getResources().getString(R.string.club_officials));
            adapter.notifyDataSetChanged();
        }
    }
}
