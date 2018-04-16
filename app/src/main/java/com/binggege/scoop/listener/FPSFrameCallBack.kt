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
        view.let {

            var location:IntArray? = kotlin.IntArray(2)
            view.getLocationOnScreen(location)
            var rect= Rect()
            var globalVisibleRect = view.getGlobalVisibleRect(rect)
            Log.d(TAG, "left: ${rect.left} right: ${rect.right} top: ${rect.top} bottom: ${rect.bottom} globalVisibleRect :$globalVisibleRect")
            Log.d(TAG, "maskViewLeft: ${maskView.left} maskViewRight: ${maskView.right} maskViewTop: ${maskView.top} maskViewBottom: ${maskView.bottom} globalVisibleRect :$globalVisibleRect")
            if (!globalVisibleRect) {
                maskView.visibility = View.INVISIBLE
            } else {
                maskView.left = rect.left
                maskView.right = rect.right
                maskView.top = rect.top
                maskView.bottom = rect.bottom
                maskView.scrollX = (maskView.x - location!![0]).toInt()
                maskView.scrollY = (maskView.y - location[1]).toInt()
                maskView.visibility = View.VISIBLE
            }
        }
        //继续注册下一帧回调
        Choreographer.getInstance().postFrameCallback(this)
    }


}