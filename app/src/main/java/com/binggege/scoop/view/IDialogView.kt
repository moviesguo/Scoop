package com.binggege.scoop.view

import android.graphics.Rect
import android.view.View

/**
 * Created by moviesguo on 2018/4/3.
 */
interface IDialogView {

    fun showDialog(data: ArrayList<View>)

    fun dismiss()

    /**
     * 这个方式简直蠢，presenter完全没有作用
     */
    fun markAssembly(view: View)

}