package com.dans.apps.bitsa.ui;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dans.apps.bitsa.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


public class ProjectsFragment extends BaseFragmentChildrenFragments {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    void setViews() {
        adapter.addFragment(new BitsaProjectsFragment(), getResources().getString(R.string.bitsa));
        adapter.addFragment(new SeniorYearProjectsFragment(), getResources().getString(R.string.senior_year_projects));
        adapter.addFragment(new StudentProjectsFragment(),getResources().getString(R.string.students));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

}
