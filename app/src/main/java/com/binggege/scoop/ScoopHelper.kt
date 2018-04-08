package com.binggege.scoop

import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.binggege.scoop.view.DialogHelper
import kotlin.collections.ArrayList

/**
 * Created by moviesguo on 2018/4/2.
 */
object ScoopHelper{


    private lateinit var dialogHelper: DialogHelper
    private var fragmentManager:FragmentManager? = null

    /**
     *
     *    3指点击事件（理想状态下）会依次触发action的值
     *
     *      1.      0           0x00(ACTION_DOWN)  第1指落下
     *      2.     261          0x05(ACTION_POINTER_DOWN)|0x0100(ACTION_POINTER_2_DOWN)  第2指落下
     *      3.     517          0x05(ACTION_POINTER_DOWN) |0x0200(ACTION_POINTER_3_DOWN)  第3指落下
     *      4.     518          0x06(ACTION_POINTER_UP) | 0x0200(ACTION_POINTER_3_UP)   第3指抬起
     *      5.     262          0x06(ACTION_POINTER_UP) |0x0100(ACTION_POINTER_2_DOWN)  第2指抬起
     *      6.      1           0x01(ACTION_UP)    第1指抬起
     *
     *      其中第四步的518并不是次次出现，代替518出现的是  6  0x06(ACTION_POINTER_UP)
     *      所以这里3值点击取517为判断条件
     *
     *      不知道为什么不建议使用ACTION_POINTER_3_DOWN
     *      所以这里判断条件修改为ACTION_POINTER_DOWN，并判断当触摸点为3个时拦截点击事件
     *
     *      我TM在干吗！！！！！！
     *
     */
    fun handleEvent(activity: AppCompatActivity, ev: MotionEvent?):Boolean {
        //todo 抛出没有绑定activity异常
        dialogHelper = DialogHelper(activity)
        when (ev?.action) {
            MotionEvent.ACTION_POINTER_3_DOWN -> {
                getAssembly(activity)
                return true
            }
        }
        return false
    }

    private fun getAssembly(activity: AppCompatActivity) {
        var decorView = activity.window.decorView
        dialogHelper.showDialog(getViews(decorView))
    }

    /**
     * 找到所有的view和ViewGroup
     *
     */
    private fun getViews(view: View) :ArrayList<View>{
        var list = ArrayList<View>()
        if (view is ViewGroup) {
            val childCount = view.childCount
            var i = 0
            list.add(view)
            while (i < childCount) {
                list.addAll(getViews(view.getChildAt(i)))
                i++;
            }
        } else {
            if (view is RecyclerView) {
                list.add(view)
            }
            return list
        }
        return list
    }

    private fun getFragments() {
        val fragments = fragmentManager?.fragments
        fragments?.forEach({
        })
    }

}