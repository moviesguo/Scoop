package com.binggege.scoop

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import org.jetbrains.anko.*
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.viewPager


/**
 * Created by congshengjie on 2018/5/3.
 */
internal class MainDialogHelper(val result: ScoopResult, val context: Context) {

    fun createMainDialog(itemClick: (ScoopUIComponent) -> Unit,
                         quickPreviewClick: () -> Unit): ScoopBottomDialog {
        val dialog = ScoopBottomDialog(context)
        var viewpager: ViewPager? = null
        val contentView = context.UI {
            verticalLayout {
                lparams(matchParent, wrapContent)
                relativeLayout {
                    backgroundColor = Color.WHITE

                    textView {
                        var drawable = ContextCompat.getDrawable(context, R.drawable.img_weng_water_mark_logo)!!
                        drawable.setColorFilter(0x474747.opaque, PorterDuff.Mode.SRC_IN)
                        drawable.setBounds(0, 0, dip(18), dip(16))
                        compoundDrawablePadding = dip(8)
                        setCompoundDrawables(drawable, null, null, null)
                        text = result.component.simpleComponentName
                        textSize = 18f
                        gravity = Gravity.CENTER_VERTICAL
                        textColor = 0x474747.opaque
                    }.lparams {
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                        centerInParent()
                    }

                    imageView {
                        ViewCompat.setElevation(this, dip(8).toFloat())
                        padding = dip(16)
                        imageResource = R.drawable.v8_ic_navi_back
                        background = Scoop.instance?.selectableItemBackgroundBorderless
                    }.lparams {
                        width = dip(48)
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                        gravity = Gravity.LEFT
                    }.setOnClickListener {
                        handleBackPress()
                    }

                    setOnTouchListener { _, event ->
                        val cancelable = event.action != MotionEvent.ACTION_UP
                        dialog.setCancelable(cancelable)
                        true
                    }
                }.lparams(ViewGroup.LayoutParams.MATCH_PARENT, dip(48))

                val tabLayout = tabLayout {
                    tabMode = TabLayout.MODE_SCROLLABLE
                    setSelectedTabIndicatorColor(0xffdb26.opaque)
                }.lparams(matchParent, dip(32)) {
                    leftMargin = dip(16)
                }

                view {
                    background = 0xe0e0e0.opaque.toDrawable()
                }.lparams(matchParent, 1) {
                    leftMargin = dip(16)
                }
                var maxSize = Math.max(result.uiComponents.size, result.nonUIComponents.size)
                var pagerHeight = maxSize.let { if (it > 5) 6 else it + 1 } * dip(48)
                if (maxSize < 5) pagerHeight += dip(8)
                viewpager = viewPager {
                    setOnTouchListener { v, event ->
                        dialog.setCancelable(false)
                        false
                    }
                }.lparams(matchParent, pagerHeight)
                tabLayout.setupWithViewPager(viewpager)
            }
        }.view

        viewpager!!.adapter = object : PagerAdapter() {
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) =
                    container.removeView(`object` as View)
            override fun isViewFromObject(view: View, `object`: Any) = view === `object`
            override fun getCount() = 2
            override fun instantiateItem(container: ViewGroup, position: Int) = context.UI {
                verticalLayout {
                    val maxSize = Math.max(result.uiComponents.size, result.nonUIComponents.size)
                    val measureHeight = maxSize.let { if (it > 5) 5 else it } * dip(48)
                    recyclerView {
                        layoutManager = LinearLayoutManager(context)
                        val components = if (position == 0) result.uiComponents else result.nonUIComponents
                        adapter = createRecyclerViewAdapter(components, itemClick)
                    }.lparams(matchParent, measureHeight)
                    textView {
                        text = "快速预览"
                        backgroundColor = 0xF9D94E.opaque
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            foreground = Scoop.instance?.selectableItemBackground
                        }
                        textSize = 15.5f
                        textColor = 0x474747.opaque
                        gravity = Gravity.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                        setOnClickListener {
                            quickPreviewClick()
                        }
                        if (result.uiComponents.size <= 0) {
                            textColor = 0x22474747
                            isEnabled = false
                        }
                    }.lparams(matchParent, dip(48)) {
                        if (maxSize < 5) topMargin = dip(8)
                    }.apply {
                        if (position == 1) isVisible = false
                    }
                }
                container.addView(view)
            }.view

            override fun getPageTitle(position: Int): CharSequence? {
                if (position == 0) {
                    return "视图"
                } else if (position == 1) {
                    return "属性"
                }
                return ""
            }
        }
        contentView.backgroundColor = Color.WHITE
        dialog.setContentView(contentView)
        dialog.setCancelable(false)
        dialog.setOnCancelListener {
            Scoop.getDialogStack()?.apply {
                if (size() <= 1) {
                    popAndDismiss()
                } else {
                    val backButton = ScoopBackButtonDialog(context as Activity)
                    pushAndShow(backButton)
                }
            }
        }
        return dialog
    }

    private fun handleBackPress() {
        Scoop.getDialogStack()?.apply {
            popAndDismiss(afterOldDialogDismiss = {
                var temp: ScoopBottomDialog? = peek() as? ScoopBottomDialog
                while (temp != null) {
                    val oldDialog = it as ScoopBottomDialog
                    if (temp.representativeComponent ===
                            oldDialog.representativeComponent) {
                        pop()
                    } else {
                        break
                    }
                    temp = peek() as? ScoopBottomDialog
                }
            })
        }
    }

    private fun createRecyclerViewAdapter(components: List<ScoopComponentInfo>,
                                          onItemClick: (ScoopUIComponent) -> Unit)  = object : RecyclerView.Adapter<ScoopComponentViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolderByType(viewType)

        override fun getItemCount() = components.size

        override fun getItemViewType(position: Int) = components[position].getTypeId()

        override fun onBindViewHolder(holder: ScoopComponentViewHolder, position: Int) {
            holder.onBindView(components[position], onItemClick)
        }
    }

    private fun putAwayDialog(componentInfo: ScoopUIComponent) {
        val dialog = ScoopBackButtonDialog(context as Activity)
        dialog.fromComponent = componentInfo
        Scoop.getDialogStack()?.pushAndShow(dialog)
    }

    fun createViewHolderByType(type: Int): ScoopComponentViewHolder {
        return when (type) {
            0, -1 -> ScoopComponentViewHolder(ScoopNonUIDelegate())
            1 -> ScoopComponentViewHolder(ScoopVHDelegate())
            2 -> ScoopComponentViewHolder(ScoopFMDelegate())
            3 -> ScoopComponentViewHolder(ScoopRVDelegate())
            else -> ScoopComponentViewHolder(ScoopComponentViewHolderDelegate())
        }
    }

    internal inner class ScoopComponentViewHolder(private val delegate: ScoopComponentViewHolderDelegate) :
            RecyclerView.ViewHolder(delegate.onCreateView(context)) {
        init {
            delegate.itemView = itemView
        }

        fun onBindView(component: ScoopComponentInfo, onItemClick: (ScoopUIComponent) -> Unit) {
            delegate.onBindView(component, onItemClick)
            itemView.setOnClickListener {
                if (component is ScoopUIComponent) {
                    onItemClick(component)
                    putAwayDialog(component)
                }
            }
        }
    }
}