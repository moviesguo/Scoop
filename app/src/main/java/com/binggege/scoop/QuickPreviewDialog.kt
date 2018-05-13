package com.binggege.scoop

import android.app.Activity
import android.content.DialogInterface
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.support.v4.viewPager

/**
 * Created by congshengjie on 2018/5/3.
 */
internal class QuickPreviewDialog(val activity: Activity) : ScoopDialogStack.ScoopDialogInterface {

    var id: Int = View.generateViewId()
    private val contentView: View
    private var viewPager: ViewPager? = null
    private var onCancel: () -> Unit = {}

    var onLeftButtonClick: ((View) -> Unit)? = null
    var onPageSelected: ((Int) -> Unit)? = null

    private var currentUIComponent: ScoopUIComponent? = null
    private var components: List<ScoopUIComponent>? = null

    init {
        contentView = activity.UI {
            val layoutHeight = dip(48)
            frameLayout {
                this@frameLayout.id = this@QuickPreviewDialog.id
                lparams(width = ViewGroup.LayoutParams.MATCH_PARENT, height = layoutHeight) {
                    gravity = Gravity.BOTTOM
                }
                imageView {
                    ViewCompat.setElevation(this, dip(8).toFloat())
                    padding = dip(16)
                    imageResource = R.drawable.v8_ic_navi_back
                }.lparams {
                    width = layoutHeight
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                    gravity = Gravity.LEFT
                }.setOnClickListener { onLeftButtonClick?.invoke(it) }

                viewPager = viewPager {
                    onPageChangeListener {
                        onPageSelected { position ->
                            currentUIComponent = components?.get(position)
                            onPageSelected?.invoke(position)
                        }
                    }
                }.lparams {
                    leftMargin = layoutHeight
                    rightMargin = layoutHeight
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }

                imageView {
                    padding = dip(15)
                    imageResource = R.drawable.v8_ic_guide_success
                }.lparams {
                    width = layoutHeight
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                    gravity = Gravity.RIGHT
                }.setOnClickListener {
                    val dialog = ScoopBackButtonDialog(context as Activity)
                    dialog.fromComponent = currentUIComponent
                    Scoop.getDialogStack()?.apply {
                        pushAndShow(dialog)
                    }
                }

                repeat(3) {
                    view {
                        backgroundResource = R.drawable.guide_mdd_top_bar_shadow
                    }.lparams(matchParent, dip(16)) {
                        gravity = Gravity.TOP
                    }
                }
            }
        }.view
        contentView.backgroundColor = 0xF9D94E.opaque
    }

    fun setData(components: List<ScoopUIComponent>) {
        this.components = components
        viewPager?.adapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View, `object`: Any) = `object` == view
            override fun getCount() = components.size

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val data = components[position]
                val delegate: ScoopComponentViewHolderDelegate = createDelegateByData(data)
                val view = delegate.onCreateView(activity)
                delegate.itemView = view!!
                delegate.onBindView(data) {}
                view.setOnClickListener {
                    Scoop.instance?.maskView?.doClickAction()
                }
                container.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT))
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }
        }
    }

    private fun createDelegateByData(data: ScoopUIComponent): ScoopComponentViewHolderDelegate {
        return when (data) {
            is ViewHolderComponent -> ScoopVHDelegate()
            is FragmentComponent -> ScoopFMDelegate()
            is RecyclerViewComponent -> ScoopRVDelegate()
        }
    }

    override fun show() {
        val decorView = activity.window.decorView as ViewGroup
        decorView.addView(contentView, 0)
        val targets = mutableListOf<View>()
        for (index in 0 until decorView.childCount) {
            val child = decorView.getChildAt(index)
            if (child is ScoopMaskView || child.id == id) continue
            targets.add(child)
        }

        targets.take(targets.size - 1).forEach {
            it.animate().setDuration(200).translationY(-contentView.context.dip(48).toFloat())
        }
        targets.last().animate().setDuration(200)
                .translationY(-contentView.context.dip(48).toFloat())
                .withEndAction {
                    if (currentUIComponent != null) {
                        onPageSelected?.invoke(components?.indexOf(currentUIComponent!!) ?: 0)
                    } else {
                        onPageSelected?.invoke(0)
                    }
                }
    }

    override fun dismiss() {
        val decorView = activity.window.decorView as ViewGroup
        val targets = mutableListOf<View>()
        decorView.forEach { targets.add(it) }
        targets.take(targets.size - 1).forEach {
            it.animate().translationY(0f).start()
        }
        targets.last().animate().translationY(0f).withEndAction {
            (activity.window.findViewById<View>(id)?.parent as? ViewGroup)?.removeView(contentView)
        }
    }

    override fun destroy() {
        val decorView = activity.window.decorView as ViewGroup
        decorView.forEach {
            it.translationY = 0f
        }
        (activity.window.findViewById<View>(id)?.parent as? ViewGroup)?.removeView(contentView)
    }

    override fun getUIComponent(): ScoopUIComponent? {
        return currentUIComponent
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener) {
        onCancel = {
            listener.onCancel(null)
        }
    }
}