package com.binggege.scoop.listener

import android.support.v7.widget.ContentFrameLayout
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import com.binggege.scoop.ScoopHelper

/**
 * Created by moviesguo on 2018/4/13.
 */
class FPSFrameCallBack(view: ViewGroup,mask:View):Choreographer.FrameCallback {

    val TAG = "FPS"


    var view = view     //需要标记的view
    var maskView = mask     //蒙版view


    //maskView 当前所处位置的边界
    /**
     * 边界的范围由小到大，如果超出了边界之后，获取当前边界的parent再次更新边界
     */
    var boundaryLeft = view.x
    var boundaryRight = view.x + view.width
    var boundaryTop = view.y
    var boundaryBottom = view.y + view.height

    /**
     *     每一帧刷新时会回调，这样做出来的效果会有一些滞后
     */
    override fun doFrame(frameTimeNanos: Long) {
        Log.d(TAG, "$boundaryLeft")

        view.let {
            var location:IntArray? = kotlin.IntArray(2)
            view.getLocationOnScreen(location)
            maskView.x = location!![0].toFloat()
            maskView.y = location!![1].toFloat()

        }
        //继续注册下一帧回调
        Choreographer.getInstance().postFrameCallback(this)
    }


}