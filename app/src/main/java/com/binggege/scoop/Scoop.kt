package com.binggege.scoop

import android.app.Activity
import android.view.*

/**
 * Created by congshengjie on 2018/4/29.
 */
class Scoop(val context: Activity) {

    companion object {
        private var instance: Scoop? = null
        @JvmStatic
        fun handleTouchEvent(context: Activity, ev: MotionEvent): Boolean {
            if (ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN && instance == null) {
                val count = ev.pointerCount
                if (count >= 3) {
                    getInstance(context).onCreate()
                    return true
                }
            } else if (ev.actionMasked == MotionEvent.ACTION_POINTER_UP ||
                    ev.actionMasked == MotionEvent.ACTION_MOVE ||
                    ev.actionMasked == MotionEvent.ACTION_UP) {
                if (instance != null) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun getInstance(context: Activity): Scoop {
            if (instance == null) {
                instance = Scoop(context)
            }
            return instance!!
        }

        @JvmStatic
        fun destroy() {
            instance?.context?.also {
                openHardwareAccelerated(it)
            }
            instance = null
        }

        fun openHardwareAccelerated(context: Activity) {
            context.window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        fun closeHardwareAccelerated(context: Activity) {
            var contentView = context.findViewById<View>(android.R.id.content)!! as ViewGroup
            contentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    fun onCreate() {
        closeHardwareAccelerated(context)
        var contentView = context.findViewById<View>(android.R.id.content)!! as ViewGroup
        var maskView = MaskView(context)
        contentView.addView(maskView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
    }
}