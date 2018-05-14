package com.binggege.scoop

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import org.jetbrains.anko.sp

/**
 * Created by congshengjie on 2018/4/29.
 */
internal class ScoopMaskView : View {

    companion object {
        private const val TEXT_SIZE_BIG_SP = 32
        private const val TEXT_SIZE_NORMAL_SP = 24
        private const val TEXT_SIZE_SMALL_SP = 16

        private const val RADIO = 0.80f

        private const val SUB_TEXT_SIZE_BIG_SP = 32 * RADIO
        private const val SUB_TEXT_SIZE_NORMAL_SP = 24 * RADIO
        private const val SUB_TEXT_SIZE_SMALL_SP = 16 * RADIO
    }

    private var targetComponent: ScoopUIComponent? = null
    internal val targetArea = Rect()
    internal val offset = Point()

    private val bgPaint = Paint().apply { color = Color.parseColor("#88000000") }
    private val textLayout = ContentTextLayout()

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        drawIndicator(canvas)

        //非合适情况下不绘制
        if (targetComponent == null || !targetComponent!!.getViewBounds(targetArea, offset) ||
                !targetComponent!!.view.isAttachedToWindow) {
            resetTargetArea()
            return
        }

        //绘制深色半透明背景
        canvas.drawRect(targetArea, bgPaint)

        //绘制文字
        textLayout.drawComponentContent(canvas, targetComponent as ScoopUIComponent)
    }

    private fun resetTargetArea() {
        targetArea.set(-1, -1, -1, -1)
    }

    private fun drawIndicator(canvas: Canvas) {
        val r = (Math.random() * 255).toInt()
        val g = (Math.random() * 255).toInt()
        val b = (Math.random() * 255).toInt()
        val color = Color.argb(10, r, g, b)
        canvas.drawColor(color)
    }

    fun doClickAction() {
        targetComponent.whenNotNull {
            showMainDialog(it)
        }
    }

    private fun showQuickPreviewDialog(components: List<ScoopUIComponent>) {
        val previewDialog = QuickPreviewDialog(context as Activity)
        previewDialog.onLeftButtonClick = {
            Scoop.getDialogStack()?.popAndDismiss()
        }
        previewDialog.onPageSelected = { position ->
            markComponent(components[position])
        }
        previewDialog.setData(components)
        Scoop.getDialogStack()?.pushAndShow(previewDialog)
    }

    private fun showMainDialog(component: ScoopUIComponent) {
        val result = when (component) {
            is FragmentComponent -> ScoopComponentsInfoHelper.getComponents(component.fragment)
            is ViewHolderComponent -> ScoopComponentsInfoHelper.getComponents(component.viewHolder)
            is RecyclerViewComponent -> ScoopComponentsInfoHelper.getComponents(component.recyclerView)
        }
        val mainDialog = MainDialogHelper(result, context).createMainDialog(itemClick = {
            markComponent(it)
        }, quickPreviewClick = {
            showQuickPreviewDialog(result.uiComponents)
        })
        mainDialog.representativeComponent = component
        Scoop.getDialogStack()?.apply {
            val backButton = peek()
            if (backButton is ScoopBackButtonDialog) {
                pop()
                backButton.dismiss()
            }
            pushAndShow(mainDialog)
        }
    }

    fun markComponent(component: ScoopUIComponent?) {
        targetComponent = component
        invalidate()
    }

    private inner class ContentTextLayout {

        private val nameTextPaint = TextPaint().apply {
            color = Color.WHITE
            flags = flags.or(Paint.ANTI_ALIAS_FLAG)
            textSize = sp(TEXT_SIZE_BIG_SP).toFloat()
        }
        private var nameLayout = StaticLayout("", nameTextPaint, 0,
                Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)

        private val idTextPaint = TextPaint().apply {
            color = Color.WHITE
            flags = flags.or(Paint.ANTI_ALIAS_FLAG)
            typeface = Typeface.SANS_SERIF
            textSize = sp(SUB_TEXT_SIZE_BIG_SP).toFloat()
        }
        private var idLayout = StaticLayout("", idTextPaint, 0,
                Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)

        fun drawComponentContent(canvas: Canvas, component: ScoopUIComponent) {
            //生成提示文字
            val componentName = component.toNonUIComponent().simpleComponentName
            val viewId = component.viewId

            //首先使用上次的字号大小做尝试
            nameLayout = createOrGetLayout(nameLayout, componentName,
                    component.viewWidth, nameTextPaint.textSize)
            idLayout = createOrGetLayout(idLayout, viewId,
                    component.viewWidth, idTextPaint.textSize)
            var totalHeight = nameLayout.height + idLayout.height

            //如果上次的字号太大 那就重新尝试三种字号做适配(low就low吧 下个版本再说。。
            if (totalHeight > component.viewHeight) {
                totalHeight = getBigSizeLayouts(component.viewWidth, componentName, viewId)
                if (totalHeight > component.viewHeight) {
                    totalHeight = getNormalSizeLayouts(component.viewWidth, componentName, viewId)
                    if (totalHeight > component.viewHeight) {
                        totalHeight = getSmallSizeLayouts(component.viewWidth, componentName, viewId)
                    }
                }
            }

            //将他们在屏幕内居中绘制
            canvas.save()
            canvas.clipRect(targetArea)
            val translateX = offset.x
            val translateY = offset.y + (component.viewHeight - totalHeight) / 2
            canvas.translate(translateX.toFloat(), translateY.toFloat())
            nameLayout.draw(canvas)
            canvas.translate(0f, nameLayout.height.toFloat())
            idLayout.draw(canvas)
            canvas.restore()
        }

        private fun getSmallSizeLayouts(width: Int, componentName: String, viewId: String): Int {
            nameLayout = createOrGetLayout(nameLayout, componentName,
                    width, sp(TEXT_SIZE_SMALL_SP).toFloat())
            idLayout = createOrGetLayout(idLayout, viewId,
                    width, sp(SUB_TEXT_SIZE_SMALL_SP).toFloat())
            return nameLayout.height + idLayout.height
        }

        private fun getNormalSizeLayouts(width: Int, componentName: String, viewId: String): Int {
            nameLayout = createOrGetLayout(nameLayout, componentName,
                    width, sp(TEXT_SIZE_NORMAL_SP).toFloat())
            idLayout = createOrGetLayout(idLayout, viewId,
                    width, sp(SUB_TEXT_SIZE_NORMAL_SP).toFloat())
            return nameLayout.height + idLayout.height
        }

        private fun getBigSizeLayouts(width: Int, componentName: String, viewId: String): Int {
            nameLayout = createOrGetLayout(nameLayout, componentName,
                    width, sp(TEXT_SIZE_BIG_SP).toFloat())
            idLayout = createOrGetLayout(idLayout, viewId,
                    width, sp(SUB_TEXT_SIZE_BIG_SP).toFloat())
            return nameLayout.height + idLayout.height
        }

        private fun createOrGetLayout(layout: StaticLayout, info: String, widthSpace: Int,
                                      textSizePx: Float): StaticLayout {
            if (layout.text != info || layout.width != widthSpace ||
                    layout.paint.textSize != textSizePx) {
                layout.paint.textSize = textSizePx
                return StaticLayout(info, layout.paint, widthSpace, Layout.Alignment.ALIGN_CENTER,
                        1f, 0f, false)
            }
            return layout
        }
    }
}