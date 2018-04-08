package com.binggege.scoop.widget

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binggege.scoop.R

/**
 * Created by moviesguo on 2018/4/4.
 */
class TestFragment: Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View? = null
        inflater?.let {
            view = inflater.inflate(R.layout.fragment_second, container, false)
        }
        view?.let {

        }
        Log.d("fragments","onCreate: ${view?.hashCode()}")
        return view
    }


}