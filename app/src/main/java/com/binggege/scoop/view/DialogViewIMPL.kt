
package com.binggege.scoop.view

import android.graphics.Color
import android.graphics.Rect
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.binggege.scoop.OnDialogItemClickListener
import com.binggege.scoop.R
import com.binggege.scoop.adapter.ListBottomSheetDialogAdapter
import com.binggege.scoop.presenter.IDialogPresenter

/**
 * Created by moviesguo on 2018/4/3.
 */
class DialogViewIMPL(activity: AppCompatActivity):IDialogView {

    private val TAG:String ="DialogView"

    private var activity:AppCompatActivity = activity
    private lateinit var dialog: BottomSheetDialog
    private lateinit var rv: RecyclerView

    init {
        initDialog()
    }

    fun initDialog() {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_bootom_sheet, null)
        rv = view.findViewById<RecyclerView>(R.id.rv_dialog)
        rv.layoutManager = LinearLayoutManager(activity)
        dialog = BottomSheetDialog(activity)
        dialog.setContentView(view)
    }

    override fun showDialog(data: ArrayList<View>) {
        rv?.adapter = ListBottomSheetDialogAdapter(activity, data,DialogItemClickListener())
        dialog.show()
    }

    override fun dismiss() {
        dialog.dismiss()
    }

    override fun markAssembly(view: View) {
        //用于标记选中状态的蒙版
        var maskLayout = FrameLayout(activity)
        maskLayout.setBackgroundColor(Color.parseColor("#303F9F"))
        var fragment = Fragment()

        maskLayout.x = view.x
        maskLayout.y = view.y

        val params = FrameLayout.LayoutParams(view.width,view.height)

        var frameLayout = activity.window.decorView.findViewById<FrameLayout>(android.R.id.content)
        var childAt = frameLayout.getChildAt(0) as ViewGroup
        childAt.addView(maskLayout, params)
        dismiss()
    }

    inner class DialogItemClickListener : OnDialogItemClickListener{

        override fun onClick(view: View) {
            markAssembly(view)
        }

    }

}
