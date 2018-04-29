package com.binggege.scoop

import android.graphics.Rect
import android.nfc.Tag
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.*
import com.binggege.scoop.adapter.ListBottomSheetDialogAdapter
import com.binggege.scoop.listener.FPSFrameCallBack
import com.binggege.scoop.listener.OnDialogItemClickListener
import java.time.LocalDate
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * Created by moviesguo on 2018/4/2.
 */
object ScoopHelper{

    private val TAG = "ScoopHelper"

    private lateinit var mDialog:BottomSheetDialog
    private lateinit var rv: RecyclerView           //BottomSheetDialog的列表

    private lateinit var currentActivity:AppCompatActivity  //当前界面的activity

    private var fpsFrameCallBack:FPSFrameCallBack? = null //刷新监听

    private var maskLayout:FrameLayout? = null      //蒙版view

    private lateinit var hierarchyView:ViewGroup     //当前层级的rootView
    private var currentParent:ViewGroup? = null

    /**
     *
     *    3指点击事件（理想状态下）会依次触发action的值
     *
     *      1.      0           0x00(ACTION_DOWN)  第1指落下
     *      2.     261          0x05(ACTION_POINTER_DOWN)|0x0100(ACTION_POINTER_2_DOWN)  第2指落下
     *      3.     517          0x05(ACTION_POINTER_DOWN) |0x0200(ACTION_POINTER_3_DOWN)  第3指落下
     *      4.     518          0x06(ACTION_POINTER_UP) | 0x0200(ACTION_POINTER_3_UP)   第3指抬起
     *      5.     262          0x06(ACTION_POINTER_UP) |0x0100(ACTION_POINTER_2_DOWN)  第2指抬起
     *      6.      1           0x01(ACTION_UP)    第1指抬起
     *
     *      其中第四步的518并不是次次出现，代替518出现的是  6  0x06(ACTION_POINTER_UP)
     *      所以这里3值点击取517为判断条件
     *
     *      不知道为什么不建议使用ACTION_POINTER_3_DOWN
     *
     *
     *      !!!这个不行的：所以这里判断条件修改为ACTION_POINTER_DOWN，并判断当触摸点为3个时拦截点击事件
     *
     */
    fun handleEvent(activity: AppCompatActivity, ev: MotionEvent?):Boolean {

        when (ev?.action) {
            MotionEvent.ACTION_POINTER_3_DOWN -> {
                init(activity)
                getAssembly()
                return true
            }
            MotionEvent.ACTION_MOVE->{
                if(hierarchyView!=null) {
                    var rect = Rect()
                    var location = IntArray(2)
                    hierarchyView.getGlobalVisibleRect(rect)
                    hierarchyView.getLocationOnScreen(location)
                    Log.d(TAG,"""hierarchyView:
                              left: ${rect.left} right: ${rect.right}
                              top: ${rect.top} bottom: ${rect.bottom}
                              transX: ${hierarchyView.translationX} transY: ${hierarchyView.translationY}
                               x: ${location[0]} y: ${location[1]}""")
                    maskLayout?.getGlobalVisibleRect(rect)
                    maskLayout?.getLocationOnScreen(location)
                    Log.d(TAG,"""maskLayout:
                              left: ${rect.left} right: ${rect.right}
                              top: ${rect.top} bottom: ${rect.bottom}
                              transX: ${maskLayout?.translationX} transY: ${maskLayout?.translationY}
                               x: ${location[0]} y: ${location[1]}""")

                }

                return true
            }
        }
        return true
    }

    /**
     *获取视图中的所有Fragment以及RecyclerView并展示
     *
     */
    private fun getAssembly() {

        //初始化assembys数据
        var assemblys: ArrayList<Any> = ArrayList()

        //将所有fragment加进来
        currentActivity.supportFragmentManager.fragments.forEach {
            assemblys.add(it)
        }

        //找到该层下的RecyclerView并加进来
        assemblys.addAll(getViews(hierarchyView))
        showDialog(assemblys)

    }

    //尝试给需要标记的View外部套上FrameLayout，ViewPager这部分出现了问题，他奶奶的
    fun replaceParent(any: Any) {

        var fragment = any as Fragment
        var view = fragment.view
        var parent = view?.parent as ViewGroup
        val resources = currentActivity.resources
        maskLayout = FrameLayout(currentActivity)
        maskLayout!!.layoutParams = view.layoutParams
        maskLayout!!.setBackgroundColor(resources.getColor(R.color.colorDimGrey))
        maskLayout!!.background.alpha = 100
        parent.removeAllViews()
        parent.addView(maskLayout,0)

//        var view:View? = null
//        var text = any::class.java.simpleName
//
//        val resources = currentActivity.resources
//
//        if (any is View) view = any
//        if (any is Fragment) view =any.view
//
//        var parent = view?.parent as ViewGroup
//        parent.removeView(view)
//        var v = LinearLayout(currentActivity)
//        v.layoutParams = view.layoutParams
//        iteratorRoot(parent)
//        maskLayout = FrameLayout(currentActivity)
//        maskLayout!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT)
//        maskLayout!!.setBackgroundColor(resources.getColor(R.color.colorDimGrey))
//        maskLayout!!.background.alpha = 100
//        view.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
//        maskLayout?.addView(view)
//        val tv = TextView(currentActivity)
//        tv.gravity = Gravity.CENTER
//        tv.textSize = 36F
//        tv.text = text
//        tv.setTextColor(resources.getColor(R.color.colorWhite))
//        maskLayout!!.addView(tv)
//        v.addView(maskLayout)
//        parent.addView(v)
//        mDialog.dismiss()
    }

    fun iteratorRoot(view: View) {

        if (view is ViewGroup) {
            Log.d(TAG, "view: ${view::class.java.simpleName}")
            var childCount = view.childCount
            var i = 0
            while (i < childCount) {
                iteratorRoot(view.getChildAt(i))
                i++
            }
        }

    }

    /**
     * 用于标记选中状态的蒙版
     */
    fun markAssembly(any: Any) {

        
        Log.d(TAG,"hierarchyView: ${hierarchyView::class.java.simpleName}")
        var view:View? = null
        var text = any::class.java.simpleName

        //获取最外层FrameLayout 就是我们setContentView的父View
        val viewGroup = currentActivity.window.decorView as ViewGroup

        //移出上一个maskLayout
        if(maskLayout!=null) viewGroup.removeView(maskLayout)

        if (any is ViewGroup) view = any
        else if (any is Fragment) view = any.view
        hierarchyView = view as ViewGroup
        currentParent = view?.parent as ViewGroup

        val resources = currentActivity.resources
        Log.d(TAG,"${view.hashCode()}")
        //构建蒙版View
        maskLayout = FrameLayout(currentActivity)
        maskLayout!!.setBackgroundColor(resources.getColor(R.color.colorDimGrey))
        maskLayout!!.background.alpha = 100

//
        var rect = Rect()
        view.getGlobalVisibleRect(rect)

        maskLayout?.left = rect.left
        maskLayout?.right = rect.right
        maskLayout?.top = rect.top
        maskLayout?.bottom = rect.bottom

        //蒙版的TexiView
        val tv = TextView(currentActivity)
        tv.gravity = Gravity.CENTER
        tv.textSize = 36F
        tv.text = text
        tv.setTextColor(resources.getColor(R.color.colorWhite))
        maskLayout!!.addView(tv)

        Log.d(TAG,"x: ${view!!.x} y:${view.y} height:${view.height} width:${view.width} scrollX: ${view.scrollX} scrollY: ${view.scrollY}")

//        locationView(maskLayout!!, view.parent as ViewGroup, viewGroup.hashCode())

        val layoutParams = FrameLayout.LayoutParams(view.width,view.height)

        //再将maskBoundaryLayout加入viewTree
        viewGroup.addView(maskLayout,layoutParams)

        //注册屏幕每帧刷新完成回调
        if (fpsFrameCallBack != null) {
            Choreographer.getInstance().removeFrameCallback(fpsFrameCallBack)
        }
        fpsFrameCallBack = FPSFrameCallBack(view!!, maskLayout!!)
        Choreographer.getInstance().postFrameCallback(fpsFrameCallBack)
        mDialog.dismiss()
    }



    /**
     * @maskLayout 蒙版View
     * @view  需要定位的View
     * @hashCode 最外层FrameLayout的hashCode
     *
     * 测量需要定位的View在最外层FrameLayout中的位置
     * 以x举例，每个view的getX()返回的是自己相对与父View的位置，所以这里不断的向上累加的x就是自己相对于FrameLayout的位置
     * 除此之外还要考虑存在ScrollView这类可滑动的view，这里直接减去了parent的scrollX/Y
     */
    private fun locationView(maskLayout: ViewGroup, view: View,hashCode:Int) {
        var x = 0F
        var y = 0F
        var parent = view.parent as ViewGroup
        Log.d(TAG,"${parent::class.java.simpleName} x: ${parent.x} y: ${parent.y} scrollX: ${parent.scrollX} scrollY: ${parent.scrollY}")
        while (parent.hashCode() != hashCode) {
            //存在ScrollView或者可滑动View（还没测试）的情况下，有时会自动往上滑动
            x += (parent.x - parent.scrollX)
            y += (parent.y - parent.scrollY)
            parent = parent.parent as ViewGroup
            Log.d(TAG, "${parent::class.java.simpleName} x: ${parent.x} y: ${parent.y} scrollX: ${parent.scrollX} scrollY: ${parent.scrollY}")
        }

        Log.d(TAG,"locationX: $x locationY: $y")
        x += view.x
        y += view.y
        maskLayout.x = x
        maskLayout.y = y
    }

    /**
     * 初始化dialog，activity以及本次遍历的hierarchyView
     *  hierarchyView是在第一次遍历时的RootView也就是DecorView
     *  当我们选中一个RecyclerView/Fragment后，hierarchyView就是继续去遍历的RootView/RecyclerView
     *
     */
    fun init(activity: AppCompatActivity) {
        currentActivity = activity

        //这个frameLayout是我们自己布局的父布局,他的childAt(0)就是我们自己写的布局
        var frameLayout = activity.window.decorView.findViewById<FrameLayout>(android.R.id.content)
        hierarchyView = frameLayout.getChildAt(0) as ViewGroup

        //初始化dialog，recyclerView，recyclerView的
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_bootom_sheet, null)
        rv = view.findViewById<RecyclerView>(R.id.rv_dialog)
        rv.layoutManager = LinearLayoutManager(activity)
        mDialog = BottomSheetDialog(currentActivity)
        mDialog.setContentView(view)
    }

    //dialog列表初始化
    fun showDialog(data: ArrayList<Any>) {
        if (data.size==0) {
            showToast("不存在Fragment和RecyclerView")
            return
        }
        rv.adapter = ListBottomSheetDialogAdapter(currentActivity, data, DialogItemClickListener())
        mDialog.show()
    }

    /**
     * 遍历decorView，获取所有的同一层级的RecyclerView并返回
     * 因为RecyclerView是ViewGroup,所以这里不对类型是View的对象进行处理
     */
    private fun getViews(view: View) :ArrayList<Any>{
        var list = ArrayList<Any>()
        var fragments = currentActivity.supportFragmentManager.fragments
        fragments.forEach {
            if (it.view?.hashCode()==view.hashCode()) return  list
        }
        if(view is RecyclerView) {          //如果是RecyclerView，则不继续遍历子view，直接加入到list并返回
            list.add(view)
            return list
        }
        if (view is ViewGroup) {
            val childCount = view.childCount
            var i = 0
            while (i < childCount) {
                list.addAll(getViews(view.getChildAt(i)))       //遍历子view
                i++;
            }
        }
        return list
    }

    //吐司快捷方法
    private fun showToast(text: String) {
        if (currentActivity != null) {
            Toast.makeText(currentActivity,text, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 当跳转另一个activity的时候，释放之前的所有变量，防止内存泄漏
     */
    fun onDestroy() {

    }

    //Assembly列表的item点击操作
    class DialogItemClickListener : OnDialogItemClickListener {
        override fun onClick(any: Any) {
            markAssembly(any)
        }
    }


}