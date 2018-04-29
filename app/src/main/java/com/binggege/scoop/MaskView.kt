package com.binggege.scoop

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

/**
 * Created by congshengjie on 2018/4/29.
 */
class MaskView : View {

    private var mTargetView: View? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}