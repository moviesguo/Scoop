package com.binggege.scoop

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_main.*

open class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //this is a zhushi
        btn_jump.setOnClickListener {
            var intent = Intent(this,SecondActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        ScoopHelper.getInstance().bind(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ScoopHelper.getInstance().handleEvent(this, ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        ScoopHelper.getInstance().unbind()
    }

}

