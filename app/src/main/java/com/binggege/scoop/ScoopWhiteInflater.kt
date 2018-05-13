package com.binggege.scoop

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View

/**
 * Created by congshengjie on 2018/5/8.
 */
class ScoopWhiteInflater :LayoutInflater{

    companion object {
        private val sClassPrefixList = arrayOf("android.widget.", "android.webkit.", "android.app.")
    }


    constructor(context: Context) : super(context)
    constructor(layoutInflater: LayoutInflater, context: Context) : super(layoutInflater, context)

    override fun cloneInContext(newContext: Context?) = ScoopWhiteInflater(this, context)

    @Throws(ClassNotFoundException::class)
    override fun onCreateView(name: String, attrs: AttributeSet): View {
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