package com.binggege.scoop.presenter

import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.binggege.scoop.view.IDialogView

/**
 * Created by moviesguo on 2018/4/3.
 */
class DialogPresenterIMPL:IDialogPresenter{

    val TAG = "presenter"


    var mDialogView:IDialogView? = null

    constructor(mDialogView: IDialogView){
        this.mDialogView = mDialogView
    }

    override fun getAssembly(activity: AppCompatActivity) {
        var decorView = activity.window.decorView
        val dialog = mDialogView
        dialog?.let { dialog.showDialog(getViews(decorView)) }
    }



    /**
     * 找到所有的view和ViewGroup
     *
     */
    private fun getViews(view: View) :ArrayList<View>{
        var list = ArrayList<View>()
        if (view is ViewGroup) {
            val childCount = view.childCount
            var i = 0
            list.add(view)
            while (i < childCount) {
                list.addAll(getViews(view.getChildAt(i)))
                i++;
            }
        } else {

            list.add(view)
            return list
        }
        return list
    }



}