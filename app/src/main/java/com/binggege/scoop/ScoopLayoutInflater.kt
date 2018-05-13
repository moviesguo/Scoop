package com.binggege.scoop

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.xmlpull.v1.XmlPullParser

/**
 * Created by congshengjie on 2018/5/2.
 */
class ScoopLayoutInflater(private val orgLayoutInflater: LayoutInflater, context: Context) : LayoutInflater(orgLayoutInflater, context) {

    companion object {
        private val sClassPrefixList = arrayOf("android.widget.", "android.webkit.", "android.app.")
    }

    override fun inflate(resource: Int, root: ViewGroup?): View {
        return inflate(resource, root, root != null)
    }

    override fun inflate(parser: XmlPullParser?, root: ViewGroup?): View {
        return inflate(parser, root, root != null)
    }

    override fun inflate(parser: XmlPullParser?, root: ViewGroup?, attachToRoot: Boolean): View {
        return orgLayoutInflater.inflate(parser, root, attachToRoot)
    }

    override fun inflate(resource: Int, root: ViewGroup?, attachToRoot: Boolean): View {
        val res = context.resources
        val parser = res.getLayout(resource)
        parser.use {
            val view: View = inflate(it, root, attachToRoot)
            view.setTag(R.id.scoop_tag_key_layout, resource)
            return view
        }
    }

    override fun cloneInContext(newContext: Context?): LayoutInflater {
        return ScoopLayoutInflater(this, context)
    }

    override fun onCreateView(name: String?, attrs: AttributeSet?): View {
        for (prefix in sClassPrefixList) {
            try {
                val view = createView(name, prefix, attrs)
                if (view != null) {
                    return view
                }
            } catch (e: ClassNotFoundException) {
                // In this case we want to let the base class take a crack
                // at it.
            }
        }
        return super.onCreateView(name, attrs)
    }
}