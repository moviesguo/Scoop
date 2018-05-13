package com.binggege.scoop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.binggege.scoop.adapter.HorizontalAdapter
import com.binggege.scoop.adapter.VerticalAdapter
import com.binggege.scoop.adapter.ViewPagerFragmentAdapter
import com.binggege.scoop.widget.FirstFragment
import com.binggege.scoop.widget.SecondFragment
import com.binggege.scoop.widget.TestFragment
import com.binggege.scoop.widget.ThirdFragment
import kotlinx.android.synthetic.main.activity_main.view.*
import org.jetbrains.anko.backgroundColor

open class MainActivity : AppCompatActivity() {

    val containerId = View.generateViewId()
    var mainPresenter = MainPresenter()

    @SuppressLint("ValidFragment")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            id = containerId
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            backgroundColor = Color.WHITE
        })
        supportFragmentManager.beginTransaction().replace(containerId, Container()).commit()
    }

    class Container : Fragment() {
        val fragments = listOf<Fragment>(TestFragment(), FirstFragment(), SecondFragment(), ThirdFragment())

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.activity_main, container, false)
            initView(view as ViewGroup)
            return view
        }

        private fun initView(parent: ViewGroup) {
            parent.vp_main.adapter = ViewPagerFragmentAdapter(childFragmentManager, fragments)
            parent.vp_main.pageMargin = 8
            var data = listOf<String>("item", "item", "item",
                    "item", "item", "item", "item", "item",
                    "item", "item", "item", "item", "item")
            parent.rv_horizontal.adapter = HorizontalAdapter(context!!, data)
            parent.rv_horizontal.layoutManager = LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false)
            parent.rv_vertical.adapter = VerticalAdapter(context!!, data)
            parent.rv_vertical.layoutManager = LinearLayoutManager(context)
        }
    }

    fun injectScoopLayoutInflater() {
        //window中的inflater
//        var scoopInflater = ScoopLayoutInflater(window.layoutInflater, this)
//        var inflaterField: Field? = null
//        var clazz: Class<*> = window::class.java
//        while (inflaterField == null && clazz != Object::class.java) {
//            for (field in clazz.declaredFields) {
//                if (field.type == LayoutInflater::class.java) {
//                    inflaterField = field
//                    break
//                }
//            }
//            if (inflaterField != null) break
//            clazz = clazz.superclass
//        }
//        inflaterField?.isAccessible = true
//        inflaterField?.set(window, scoopInflater)

        //context中的inflater
//        scoopInflater = ScoopLayoutInflater(LayoutInflater.from(this), this)
//        clazz = Activity::class.java
//        inflaterField = null
//        while (inflaterField == null && clazz != Object::class.java) {
//            for (field in clazz.declaredFields) {
//                if (field.type == LayoutInflater::class.java) {
//                    inflaterField = field
//                    break
//                }
//            }
//            if (inflaterField != null) break
//            clazz = clazz.superclass
//        }
//        inflaterField?.isAccessible = true
//        inflaterField?.set(this, scoopInflater)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!Scoop.handleTouchEvent(this, ev)) {
            return super.dispatchTouchEvent(ev)
        }
        return true
    }

    var inflater: ScoopLayoutInflater? = null
    override fun getSystemService(name: String?): Any {
        if (Context.LAYOUT_INFLATER_SERVICE == name) {
            if (inflater == null) {
                inflater = ScoopLayoutInflater(super.getSystemService(name) as LayoutInflater, this)
            }
            return inflater!!
        }
        return super.getSystemService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        Scoop.destroy()
    }

    override fun onBackPressed() {
        if (Scoop.isALive()) {
            Scoop.destroy()
        } else {
            super.onBackPressed()
        }
    }

}

