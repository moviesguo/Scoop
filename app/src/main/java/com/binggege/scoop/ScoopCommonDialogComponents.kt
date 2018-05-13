package com.binggege.scoop

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*

/**
 * Created by congshengjie on 2018/5/11.
 */
internal open class ScoopComponentViewHolderDelegate {

    companion object {
        fun createTypeText(typeName: String) = "$typeName  •  "
    }

    lateinit var itemView: View
    open fun onCreateView(context: Context): View? = null
    open fun onBindView(component: ScoopComponentInfo, onItemClick: (ScoopUIComponent) -> Unit) {}

    protected fun @AnkoViewDslMarker _LinearLayout.nameWithIdLayout(): NameWithIdLayoutContainer {
        var nameView: TextView? = null
        var idView: TextView? = null
        frameLayout {
            verticalPadding = dip(5)
            nameView = textView {
                textSize = 16f
                textColor = 0x474747.opaque
            }.lparams(matchParent, wrapContent) {
                gravity = Gravity.TOP
            }
            idView = textView {
                textSize = 11f
                textColor = 0xaa474747.toInt()
            }.lparams(matchParent, wrapContent) {
                gravity = Gravity.BOTTOM
            }
        }.lparams(wrapContent, matchParent)
        return NameWithIdLayoutContainer(nameView!!, idView!!)
    }

    protected fun @AnkoViewDslMarker _LinearLayout.typeLayout(typeName: String) = textView {
        textSize = 11f
        text = createTypeText(typeName)
        typeface = Typeface.DEFAULT_BOLD
        textColor = 0x474747.opaque
        gravity = Gravity.CENTER_VERTICAL
    }.lparams(height = matchParent)

    inner class NameWithIdLayoutContainer(val nameView: TextView, val idView: TextView) {
        operator fun component1(): TextView {
            return nameView
        }
        operator fun component2(): TextView {
            return idView
        }
    }
}

internal class ScoopNonUIDelegate : ScoopComponentViewHolderDelegate() {
    private lateinit var name: TextView
    private lateinit var value: TextView
    override fun onCreateView(context: Context) = context.UI {
        linearLayout {
            alpha = 0.72f
            background = Scoop.instance?.selectableItemBackground
            leftPadding = dip(16)
            orientation = LinearLayout.HORIZONTAL
            lparams(matchParent, dip(48))
            name = typeLayout("PROPERTY")
            value = textView {
                textSize = 16f
                textColor = 0x474747.opaque
                gravity = Gravity.CENTER_VERTICAL
            }.lparams(matchParent, matchParent)
        }
    }.view

    override fun onBindView(component: ScoopComponentInfo, onItemClick: (ScoopUIComponent) -> Unit) {
        if (component is NonUIComponent) {
            name.text = createTypeText(component.getTypeName(component.type).toUpperCase())
            value.text = component.simpleComponentName
        } else if (component is ScoopPropertyInfo) {
            name.text = createTypeText(component.getTypeName(component.type).toUpperCase())
            value.text = component.value
        }
    }
}

internal class ScoopVHDelegate : ScoopComponentViewHolderDelegate() {
    private lateinit var holderName: TextView
    private lateinit var holderLayoutId: TextView
    private lateinit var holderPosition: TextView

    override fun onCreateView(context: Context) = context.UI {
        linearLayout {
            background = Scoop.instance?.selectableItemBackground
            leftPadding = dip(16)
            orientation = LinearLayout.HORIZONTAL
            lparams(matchParent, dip(48))

            typeLayout("HOLDER")

            nameWithIdLayout().apply {
                holderName = nameView
                holderLayoutId = idView
            }

            holderPosition = textView {
                textSize = 13f
                textColor = 0x575757.opaque
                gravity = Gravity.RIGHT.or(Gravity.CENTER_VERTICAL)
                rightPadding = dip(16)
            }.lparams(matchParent, matchParent)
        }
    }.view

    override fun onBindView(component: ScoopComponentInfo, onItemClick: (ScoopUIComponent) -> Unit) {
        super.onBindView(component, onItemClick)
        val component = component as ViewHolderComponent
        holderName.text = component.toNonUIComponent().simpleComponentName
        holderLayoutId.text = component.viewId
        holderPosition.text = "(${component.adapterPosition})"
    }
}

internal class ScoopFMDelegate : ScoopComponentViewHolderDelegate() {
    private lateinit var fragmentName: TextView
    override fun onCreateView(context: Context) = context.UI {
        linearLayout {
            background = Scoop.instance?.selectableItemBackground
            leftPadding = dip(16)
            orientation = LinearLayout.HORIZONTAL
            lparams(matchParent, dip(48))
            typeLayout("FRAGMENT")
            fragmentName = textView {
                textSize = 16f
                textColor = 0x474747.opaque
                gravity = Gravity.CENTER_VERTICAL
            }.lparams(matchParent, matchParent)
        }
    }.view

    override fun onBindView(component: ScoopComponentInfo, onItemClick: (ScoopUIComponent) -> Unit) {
        super.onBindView(component, onItemClick)
        val component = component as FragmentComponent
        fragmentName.text = component.fragment::class.java.simpleName
        component.fragment.apply {
            if (isHidden || isDetached) {
                itemView.alpha = 0.4f
            } else {
                itemView.alpha = 1f
            }
        }
    }
}

internal class ScoopRVDelegate : ScoopComponentViewHolderDelegate() {
    private lateinit var recyclerViewName: TextView
    private lateinit var recyclerViewID: TextView

    override fun onCreateView(context: Context) = context.UI {
        linearLayout {
            background = Scoop.instance?.selectableItemBackground
            leftPadding = dip(16)
            orientation = LinearLayout.HORIZONTAL
            lparams(matchParent, dip(48))
            typeLayout("RECYCLER")
            nameWithIdLayout().apply {
                recyclerViewName = nameView
                recyclerViewID = idView
            }
        }
    }.view

    override fun onBindView(component: ScoopComponentInfo, onItemClick: (ScoopUIComponent) -> Unit) {
        super.onBindView(component, onItemClick)
        val component = component as RecyclerViewComponent
        recyclerViewName.text = component.simpleViewName
        recyclerViewID.text = component.viewId
    }
}