package com.binggege.scoop

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT

/**
 * Created by congshengjie on 2018/4/29.
 */
class Scoop(val context: AppCompatActivity) {

    private var isOpeningScoopDialog = false

    internal var maskView: ScoopMaskView? = null

    private var downX = 0
    private var downY = 0
    private var isHandlingMaskViewClick = false

    private var dialogStack: ScoopDialogStack = ScoopDialogStack().apply {
        onStackEmpty = {
            Scoop.destroy()
        }
    }

    internal val selectableItemBackground: Drawable
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            val attr = intArrayOf(android.R.attr.selectableItemBackground)
            val typedArray = context.theme.obtainStyledAttributes(typedValue.resourceId, attr)
            return typedArray.getDrawable(0)
        }

    internal val selectableItemBackgroundBorderless: Drawable
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return selectableItemBackground
            }
            val typeValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typeValue, true)
            val attr = intArrayOf(android.R.attr.selectableItemBackgroundBorderless)
            val typeArray = context.theme.obtainStyledAttributes(typeValue.resourceId, attr)
            return typeArray.getDrawable(0)
        }

    companion object {
        internal var instance: Scoop? = null

        @JvmStatic
        fun handleTouchEvent(context: AppCompatActivity, ev: MotionEvent): Boolean {
            //如果scoop对象存在直接交由scoop处理
            if (isALive()) return instance!!.handleTouchEventInner(context, ev)

            //否则尝试唤起scoop来处理
            if (ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                if (ev.pointerCount >= 3) {
                    getInstance(context).apply {
                        onCreate()
                        isOpeningScoopDialog = true
                    }
                    return true
                }
            }

            return false
        }

        @JvmStatic
        private fun getInstance(context: AppCompatActivity) = instance ?: Scoop(context)
                .also { instance = it }

        @JvmStatic
        fun destroy() {
            instance?.also { it.onDestroy() }
            instance = null
        }

        fun isALive() = instance != null

        internal fun getDialogStack(): ScoopDialogStack? {
            return instance?.dialogStack
        }

        internal fun openHardwareAccelerate(context: Activity) {
            context.window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            context.window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        internal fun closeHardwareAccelerated(context: Activity) {
            context.window.decorView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    fun onCreate() {
        if (maskView == null) {
            showMainDialog(context)
        }
    }

    fun handleTouchEventInner(context: AppCompatActivity, ev: MotionEvent): Boolean {
        //如果点到返回箭头了 就先让他处理
        dialogStack.peek()?.let { it as? ScoopBackButtonDialog }?.also {
            if (it.handleTouchEvent(ev)) {
                return true
            }
        }

        //尝试拦截MaskView的点击事件
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            if (maskView != null) {
                val maskView = maskView!!
                downX = ev.rawX.toInt()
                downY = ev.rawY.toInt()
                if (maskView.targetArea.contains(downX, downY)) {
                    isHandlingMaskViewClick = true
                }
            }
        }
        //尝试在已经打开scoop的情况下拦截三指点击事件
        else if (ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            if (ev.pointerCount >= 3) {
                onCreate()
                isOpeningScoopDialog = true
                isHandlingMaskViewClick = false
                return true
            }
        }
        //松手时要处理三指唤出及MaskView的点击
        else if (ev.actionMasked == MotionEvent.ACTION_UP) {
            if (isOpeningScoopDialog) {
                isOpeningScoopDialog = false
                return true
            }
            //down的时候给他赋的值 说明此时MaskView已经非空了
            if (isHandlingMaskViewClick) {
                val dx = ev.rawX - downX
                val dy = ev.rawY - downY
                val dis = Math.sqrt((dx * dx + dy * dy).toDouble())


                if (dis <= ViewConfiguration.get(context).scaledTouchSlop) {
                    maskView!!.doClickAction()
                    ev.action = MotionEvent.ACTION_CANCEL
                }
            }
            isHandlingMaskViewClick = false
        }

        //如果滑动过多就不能算maskView的点击（讲道理所有的点击都要做这样的处理 但就只处理maskview好了）
        if (isHandlingMaskViewClick && ev.action == MotionEvent.ACTION_MOVE) {
            val dx = ev.rawX - downX
            val dy = ev.rawY - downY
            val dis = Math.sqrt((dx * dx + dy * dy).toDouble())
            if (dis > ViewConfiguration.get(context).scaledTouchSlop) {
                isHandlingMaskViewClick = false
            }
        }

        //拦截掉所有三指时候的非up事件
        if (isOpeningScoopDialog) return true


        return false
    }

    private fun getOrCreateMaskView(): ScoopMaskView {
        if (maskView == null) {
            closeHardwareAccelerated(context)
            val decorView = context.window.decorView as ViewGroup
            val maskView = ScoopMaskView(context)
            decorView.addView(maskView, ViewGroup.LayoutParams(MATCH_PARENT,
                    MATCH_PARENT))
            this.maskView = maskView
        }
        return maskView!!
    }

    private fun showMainDialog(context: AppCompatActivity) {
        val result = ScoopComponentsInfoHelper.getComponents(context)
        val mainDialog = MainDialogHelper(result, context).createMainDialog(itemClick = {
            getOrCreateMaskView().markComponent(it)
        }, quickPreviewClick = {
            showQuickPreviewDialog(result.uiComponents)
        })
        dialogStack.pushAndShow(mainDialog)
    }

    private fun showQuickPreviewDialog(components: List<ScoopUIComponent>) {
        val previewDialog = QuickPreviewDialog(context)
        previewDialog.onLeftButtonClick = {
            dialogStack.popAndDismiss()
        }
        previewDialog.onPageSelected = { position ->
            getOrCreateMaskView().markComponent(components[position])
        }
        previewDialog.setData(components)
        dialogStack.pushAndShow(previewDialog)
    }

    private fun onDestroy() {
        openHardwareAccelerate(context)
        if (maskView != null) {
            (maskView!!.parent as ViewGroup).removeView(maskView)
            maskView = null
        }
        dialogStack.destroy()
    }
}