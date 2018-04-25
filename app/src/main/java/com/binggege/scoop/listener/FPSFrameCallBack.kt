package com.binggege.scoop.listener

import android.graphics.Rect
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout

/**
 * Created by moviesguo on 2018/4/13.
 */
class FPSFrameCallBack(view: ViewGroup,mask:View):Choreographer.FrameCallback {

    val TAG = "FPS"

    var view = view     //需要标记的view
    var maskView = mask     //蒙版view

    /**
     *     每一帧刷新时会回调，这样做出来的效果会有一些滞后
     */
    override fun doFrame(frameTimeNanos: Long) {

        var location:IntArray? = kotlin.IntArray(2)
        view.getLocationOnScreen(location)
        var rect= Rect()
        var visible = view.getGlobalVisibleRect(rect)
        Log.d(TAG, view.toString())
        var layoutParams = maskView.layoutParams as FrameLayout.LayoutParams
        if (!visible) {
            maskView.visibility = View.INVISIBLE
        } else {
            layoutParams.move(rect)
//            layoutParams.leftMargin = rect.left
//            layoutParams.rightMargin = rect.right
//            layoutParams.bottomMargin = rect.bottom
//            layoutParams.topMargin = rect.top
//            layoutParams.width = rect.right - rect.left
//            layoutParams.height = rect.bottom - rect.top
//            maskView.layoutParams = layoutParams
//            maskView.visibility = View.VISIBLE
        }


//            if (!visible) {
//                maskView.visibility = View.INVISIBLE
//            } else {
//                maskView.left = rect.left
//                maskView.right = rect.right
//                maskView.top = rect.top
//                maskView.bottom = rect.bottom
//
//                maskView.visibility = View.VISIBLE
//            }
        //继续注册下一帧回调
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun FrameLayout.LayoutParams.move(rect: Rect){
        this.leftMargin = rect.left
        this.rightMargin = rect.right
        this.bottomMargin = rect.bottom
        this.topMargin = rect.top
        this.width = rect.right - rect.left
        this.height = rect.bottom - rect.top
        maskView.layoutParams = this
        maskView.visibility = View.VISIBLE
    }


}