package com.binggege.scoop.listener

import android.util.Log
import android.view.Choreographer
import android.view.View

/**
 * Created by moviesguo on 2018/4/13.
 */
class FPSFrameCallBack(view: View,mask:View):Choreographer.FrameCallback {

    val TAG = "FPS"
    var x = view.scrollX
    var y = view.scrollY
    var view = view
    var mask = mask

    /**
     *     每一帧刷新时会回调，这样做出来的效果会有一些滞后
     *
     */
    override fun doFrame(frameTimeNanos: Long) {
        var location:IntArray? = kotlin.IntArray(2)
        view.getLocationOnScreen(location)
        mask.x = location!![0].toFloat()

        mask.y =location!![1].toFloat()
        x = view.scrollX
        x = view.scrollY

        //继续注册下一帧回调
        Choreographer.getInstance().postFrameCallback(this)
    }
}