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
class FPSFrameCallBack(view: ViewGroup,mask:View,maskBoundary:View):Choreographer.FrameCallback {

    val TAG = "FPS"


    var view = view     //需要标记的view
    var maskView = mask     //蒙版view
    var maskBoundary = maskBoundary


    //maskView 当前所处位置的边界
    var boundaryLeft = 1
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
            val scrollX = location!![0].toFloat() - maskBoundary.x
            val scrollY = location!![1].toFloat() - maskBoundary.y

            maskView.x = scrollX
            maskView.y = scrollY

            //ANR
//            var x = 0F
//            var y = 0F
//            parents.forEach {
//
//                x+=it.scrollX
//                y += it.scrollY
//            }
//
//            x+=view.x
//            y+=view.y
//
//            maskBoundary.x = x
//            maskBoundary.y = y

            //根据边界改变maskBoundary的尺寸和位置
//            val parent = view.parent.parent.parent as ViewGroup
//            if (maskBoundary.y < parent.y) {
//                var layoutParams = maskBoundary.layoutParams
//                layoutParams.height = (maskBoundary.y - parent.scrollY).toInt()
//                maskBoundary.layoutParams = layoutParams
//            } else if (b){
//                maskBoundary.y -= parent.scrollY.toFloat()
//                b = false
//            }
//            maskBoundary.x += parent.scrollX


        }
        //继续注册下一帧回调
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun locationBoundary(boundary: View, view: ViewGroup) {
        var x = 0F
        var y = 0F
        var parent = view.parent as ViewGroup
        while (view.parent!=null){
            x += (parent.x - parent.scrollX)
            y += (parent.y - parent.scrollY)
        }

        x += view.x
        y += view.y
        if(x!=maskBoundary.x) maskBoundary.x = x
        if(y!=maskBoundary.y) maskBoundary.y = y
    }

}