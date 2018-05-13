package com.binggege.scoop

import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.support.v4.view.ViewCompat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.jetbrains.anko.*

/**
 * Created by congshengjie on 2018/5/9.
 */
internal class ScoopBackButtonDialog(val activity: Activity) : ScoopDialogStack.ScoopDialogInterface {

    var fromComponent: ScoopUIComponent? = null

    override fun getUIComponent(): ScoopUIComponent? {
        return fromComponent
    }

    private val id = View.generateViewId()
    private val rect = Rect()

    private val contentView: View = activity.UI {
        frameLayout {
            this@frameLayout.id = this@ScoopBackButtonDialog.id
            layoutParams = FrameLayout.LayoutParams(dip(48), dip(48)).apply {
                gravity = Gravity.BOTTOM
            }
            foreground = Scoop.instance?.selectableItemBackground
            isClickable = true
            backgroundColor = Color.WHITE
            imageView {
                ViewCompat.setElevation(this, dip(8).toFloat())
                padding = dip(16)
                imageResource = R.drawable.v8_ic_navi_back
                rotation = 90f
            }.lparams(matchParent, matchParent)
        }
    }.view

    override fun show() {
        val decorView = activity.window.decorView as ViewGroup
        decorView.addView(contentView)
        contentView.translationY = activity.dip(48).toFloat()
        contentView.animate().translationY(0f)
    }

    override fun dismiss() {
        (activity.window.findViewById<View>(id)?.parent as? ViewGroup)?.removeView(contentView)
    }

    override fun destroy() {
        dismiss()
    }

    private var shouldHandleTouchEvent = false
    fun handleTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                shouldHandleTouchEvent = false
                if (contentView.getGlobalVisibleRect(rect)
                        && rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    shouldHandleTouchEvent = true
                }
            }
        }
        if (shouldHandleTouchEvent) {
            contentView.dispatchTouchEvent(ev)
            if (ev.action == MotionEvent.ACTION_UP && contentView.getGlobalVisibleRect(rect)
                    && rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                onButtonClick()
            }
            return true
        }
        return false
    }

    private fun onButtonClick() {
        Scoop.getDialogStack()?.popAndDismiss()
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener) {
        //do nothing
    }
}
