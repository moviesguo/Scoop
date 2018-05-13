package com.binggege.scoop

import android.app.Activity
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import java.lang.reflect.Field
import java.util.*

/**
 * Created by congshengjie on 2018/4/29.
 */
object ScoopComponentsInfoHelper {

    fun getViewIdString(res: Resources, id: Int): String {
        try {
            val type = res.getResourceTypeName(id)
            val name = res.getResourceEntryName(id)
            return "R.$type.$name"
        } catch (e: Resources.NotFoundException) {

        }
        return if (id <= 0) "NO_ID" else "RUNTIME_ID / $id"
    }

    internal fun getComponents(activity: AppCompatActivity): ScoopResult {
        //拿到Activity自己的名字
        val component = NonUIComponent(NonUIComponent.TYPE_ACTIVITY, activity)

        val nonUIComponents = mutableListOf<ScoopComponentInfo>()
        val uiComponents = mutableListOf<ScoopUIComponent>()

        //拿当前Activity的布局ID
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        var idString: String? = null
        content.forEach {
            val idInt = it.getTag(R.id.scoop_tag_key_layout)
            if (idInt != null) {
                idString = getViewIdString(activity.resources, idInt as Int)
                return@forEach
            }
        }
        idString.whenNotNull {
            nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_ID, it))
        }

        //生成自己的组件类型
        nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_TYPE, "Activity"))

        //拿presenter
        val fields = mutableListOf<Field>()
        var clz: Class<*> = activity::class.java
        while (clz != Activity::class.java) {
            fields.addAll(clz.declaredFields)
            clz = clz.superclass
        }
        fields.forEach {
            if (!it.name.toLowerCase().contains("presenter")) return@forEach
            nonUIComponents.add(NonUIComponent(NonUIComponent.TYPE_PRESENTER, it.run {
                isAccessible = true
                get(activity)
            }))
        }

        //拿Fragments
        val fragmentViews = mutableListOf<View>()
        activity.supportFragmentManager.fragments.filterNotNull().forEach {
            it.view.whenNotNull { fragmentViews.add(it) }
            uiComponents.add(FragmentComponent(it))
        }

        //拿RecyclerView 不包含Fragment中的RecyclerView
        val stack = Stack<View>().apply { push(activity.findViewById(android.R.id.content)) }
        while (stack.isNotEmpty()) {
            val view = stack.pop()!!

            if (fragmentViews.contains(view)) continue

            if (view is RecyclerView) {
                uiComponents.add(RecyclerViewComponent(view))
                continue
            }
            (view as? ViewGroup)?.apply {
                for (index in 0 until childCount) {
                    stack.push(getChildAt(index))
                }
            }
        }

        return ScoopResult(component, uiComponents, nonUIComponents)
    }

    internal fun getComponents(fragment: Fragment): ScoopResult {
        //拿Fragment自己的名字
        val component = NonUIComponent(NonUIComponent.TYPE_FRAGMENT, fragment)

        val nonUIComponents = mutableListOf<ScoopComponentInfo>()
        val uiComponents = mutableListOf<ScoopUIComponent>()

        //拿当前Fragment的布局ID
        fragment.view?.getTag(R.id.scoop_tag_key_layout)?.let{ it as? Int}.whenNotNull {
            nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_ID,
                    getViewIdString(fragment.resources, it)))
        }

        //生成自己的组件类型
        nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_TYPE, "Fragment"))

        //拿presenter
        val fields = mutableListOf<Field>()
        var clz: Class<*> = fragment::class.java
        while (clz != Fragment::class.java) {
            fields.addAll(clz.declaredFields)
            clz = clz.superclass
        }
        fields.forEach {
            if (!it.name.toLowerCase().contains("presenter")) return@forEach
            nonUIComponents.add(NonUIComponent(NonUIComponent.TYPE_PRESENTER, it.apply {
                isAccessible = true
                get(fragment)
            }))
        }
        //拿子Fragment
        val childFragmentViews = mutableListOf<View>()
        fragment.childFragmentManager.fragments.filterNotNull().forEach {
            it.view.whenNotNull { childFragmentViews.add(it) }
            uiComponents.add(FragmentComponent(it))
        }

        //拿recyclerView 不包含子Fragment中的RecyclerView
        val stack = Stack<View>().apply { push(fragment.view) }
        while (stack.isNotEmpty()) {
            val view = stack.pop()!!

            if (childFragmentViews.contains(view)) continue

            if (view is RecyclerView) {
                uiComponents.add(RecyclerViewComponent(view))
                continue
            }
            (view as? ViewGroup)?.apply {
                for (index in 0 until childCount) {
                    stack.push(getChildAt(index))
                }
            }
        }
        return ScoopResult(component, uiComponents, nonUIComponents)
    }

    internal fun getComponents(recyclerView: RecyclerView): ScoopResult {
        //拿recyclerView自己
        val component = NonUIComponent(NonUIComponent.TYPE_RECYCLER_VIEW, recyclerView)

        val nonUIComponents = mutableListOf<ScoopComponentInfo>()
        val uiComponents = mutableListOf<ScoopUIComponent>()

        //拿recyclerView的id
        nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_ID,
                getViewIdString(recyclerView.context.resources, recyclerView.id)))

        //生成自己的组件类型
        nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_TYPE, "RecyclerView"))

        //拿adapter
        nonUIComponents.add(NonUIComponent(NonUIComponent.TYPE_ADAPTER, recyclerView.adapter))
        //拿viewHolder
        for (index in 0 until recyclerView.childCount) {
            val view = recyclerView.getChildAt(index)
            val viewHolder = recyclerView.getChildViewHolder(view)
            uiComponents.add(ViewHolderComponent(viewHolder, recyclerView))
        }
        return ScoopResult(component, uiComponents, nonUIComponents)
    }

    internal fun getComponents(viewHolder: RecyclerView.ViewHolder): ScoopResult {
        //拿viewHolder自己
        val component = NonUIComponent(NonUIComponent.TYPE_VIEW_HOLDER, viewHolder)

        val nonUIComponents = mutableListOf<ScoopComponentInfo>()
        val uiComponents = mutableListOf<ScoopUIComponent>()

        //拿当前ViewHolder的布局ID
        viewHolder.itemView?.getTag(R.id.scoop_tag_key_layout)?.let{ it as? Int}.whenNotNull {
            nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_ID,
                    getViewIdString(viewHolder.itemView.resources, it)))
        }

        //生成自己的组件类型
        nonUIComponents.add(ScoopPropertyInfo(ScoopPropertyInfo.TYPE_TYPE, "ViewHolder"))

        //拿presenter
        val fields = mutableListOf<Field>()
        var clz: Class<*> = viewHolder::class.java
        while (clz != RecyclerView.ViewHolder::class.java) {
            fields.addAll(clz.declaredFields)
            clz = clz.superclass
        }
        fields.forEach {
            if (!it.name.toLowerCase().contains("presenter")) return@forEach
            nonUIComponents.add(NonUIComponent(NonUIComponent.TYPE_PRESENTER, it.apply {
                isAccessible = true
                get(viewHolder)
            }))
        }
        //拿recyclerView
        val stack = Stack<View>().apply { push(viewHolder.itemView) }
        while (stack.isNotEmpty()) {
            val view = stack.pop()!!
            if (view is RecyclerView) {
                uiComponents.add(RecyclerViewComponent(view))
                continue
            }
            (view as? ViewGroup)?.apply {
                for (index in 0 until childCount) {
                    stack.push(getChildAt(index))
                }
            }
        }
        return ScoopResult(component, uiComponents, nonUIComponents)
    }
}

internal data class ScoopResult(val component: NonUIComponent,
                                val uiComponents: MutableList<ScoopUIComponent>,
                                val nonUIComponents: List<ScoopComponentInfo>)

sealed class ScoopComponentInfo {
    abstract fun getDescription(): String
    abstract fun getTypeId(): Int
}

internal class ScoopPropertyInfo(val type: Int, val value: String) : ScoopComponentInfo() {
    companion object {
        const val TYPE_ID = 0;
        const val TYPE_TYPE = 1
    }

    override fun getDescription(): String {
        return "${getTypeName(type)}->$value"
    }

    override fun getTypeId(): Int {
        return -1
    }

    fun getTypeName(type: Int) = when (type) {
        TYPE_ID -> "id"
        TYPE_TYPE -> "type"
        else -> "property"
    }
}

internal class NonUIComponent(val type: Int, private val component: Any) : ScoopComponentInfo() {

    val simpleComponentName:String
        get() {
            var name = component::class.java.simpleName
            if (name.isEmpty()) {
                val className = component::class.java
                val packageName = className.`package`.name
                name = className.name.replace("$packageName.", "")
            }
            return name
        }

    companion object {
        const val TYPE_PRESENTER = 0
        const val TYPE_ADAPTER = 1
        const val TYPE_ACTIVITY = 2
        const val TYPE_FRAGMENT = 3
        const val TYPE_RECYCLER_VIEW = 4
        const val TYPE_VIEW_HOLDER = 5
    }

    override fun getDescription(): String {
        return "${getTypeName(type)}->$simpleComponentName"
    }

    override fun getTypeId() = 0

    fun <T> getComponent(): T = component as T

    fun getTypeName(type: Int): String {
        return when (type) {
            TYPE_ADAPTER -> "adapter"
            TYPE_PRESENTER -> "presenter"
            TYPE_ACTIVITY -> "activity"
            TYPE_FRAGMENT -> "fragment"
            TYPE_RECYCLER_VIEW -> "recyclerView"
            TYPE_VIEW_HOLDER -> "viewHolder"
            else -> "component"
        }
    }
}

internal sealed class ScoopUIComponent(val view: View) : ScoopComponentInfo() {
    val viewId: String by lazy {
        val layoutId = view.getTag(R.id.scoop_tag_key_layout)
        if (layoutId != null) {
            ScoopComponentsInfoHelper.getViewIdString(view.context.resources, layoutId as Int)
        } else {
            ScoopComponentsInfoHelper.getViewIdString(view.context.resources, view.id)
        }
    }
    val viewWidth get() = view.width
    val viewHeight get() = view.height

    val simpleViewName = view::class.java.simpleName!!

    fun getViewBounds(rect: Rect): Boolean {
        return view.getGlobalVisibleRect(rect)
    }

    fun getViewBounds(rect: Rect, offset: Point): Boolean {
        return view.getGlobalVisibleRect(rect, offset)
    }

    abstract fun toNonUIComponent(): NonUIComponent

}

internal class ViewHolderComponent(val viewHolder: RecyclerView.ViewHolder,
                                   val recyclerView: RecyclerView) : ScoopUIComponent(viewHolder.itemView) {
    var adapterPosition = viewHolder.adapterPosition

    override fun getTypeId() = 1

    override fun toNonUIComponent() = NonUIComponent(NonUIComponent.TYPE_VIEW_HOLDER, viewHolder)

    override fun getDescription() = "ViewHolder->${toNonUIComponent().simpleComponentName}($adapterPosition)"

}

internal class FragmentComponent(val fragment: Fragment) : ScoopUIComponent(fragment.view!!) {
    var stateStr: String = "visible"
        get() {
            return when {
                fragment.isHidden -> "hidden"
                fragment.isDetached -> "detached"
                else -> field
            }
        }

    override fun getTypeId() = 2

    override fun toNonUIComponent() = NonUIComponent(NonUIComponent.TYPE_FRAGMENT, fragment)

    override fun getDescription(): String = "Fragment->${fragment::class.java.simpleName}($stateStr)"
}

internal class RecyclerViewComponent(val recyclerView: RecyclerView) : ScoopUIComponent(recyclerView) {

    override fun getTypeId() = 3

    override fun toNonUIComponent() = NonUIComponent(NonUIComponent.TYPE_RECYCLER_VIEW, recyclerView)

    override fun getDescription(): String {
        val name = recyclerView::class.java.simpleName
        return "RecyclerView->$name($viewId)"
    }
}