package com.binggege.scoop.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by moviesguo on 2018/4/9.
 */
class ViewPagerFragmentAdapter(fm: FragmentManager,fragments:List<Fragment>) : FragmentPagerAdapter(fm) {

    var fragments = fragments

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}