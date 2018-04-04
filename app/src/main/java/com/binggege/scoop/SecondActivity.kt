package com.binggege.scoop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.binggege.scoop.widget.TestFragment

class SecondActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        initView()
    }

    fun initView() {
        val fragment = TestFragment()
        var transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_second,fragment).commit()
    }
}
