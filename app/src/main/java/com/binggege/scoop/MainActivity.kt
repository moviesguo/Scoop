package com.binggege.scoop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.view.MotionEvent
import com.binggege.scoop.adapter.HorizontalAdapter
import com.binggege.scoop.adapter.VerticalAdapter
import com.binggege.scoop.adapter.ViewPagerFragmentAdapter
import com.binggege.scoop.widget.FirstFragment
import com.binggege.scoop.widget.SecondFragment
import com.binggege.scoop.widget.TestFragment
import com.binggege.scoop.widget.ThirdFragment
import kotlinx.android.synthetic.main.activity_main.*

open class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    val fragments = listOf<Fragment>(TestFragment(),FirstFragment(),SecondFragment(),ThirdFragment())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //this is a zhushi
        initView()
    }

    private fun initView() {
        vp_main.adapter = ViewPagerFragmentAdapter(supportFragmentManager,fragments)
        vp_main.pageMargin = 8
        var data = listOf<String>("item","item","item"
                ,"item","item","item","item")
        rv_horizontal.adapter = HorizontalAdapter(this,data)
        rv_horizontal.layoutManager = LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false)
        rv_vertical.adapter = VerticalAdapter(this,data)
        rv_vertical.layoutManager = LinearLayoutManager(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!Scoop.handleTouchEvent(this, ev)) {
            return super.dispatchTouchEvent(ev)
        }
        return true
    }


}

