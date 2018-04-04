package com.binggege.scoop.presenter

import android.support.v7.app.AppCompatActivity
import android.view.View

/**
 * Created by moviesguo on 2018/4/3.
 */
interface IDialogPresenter {

    /**
     * 获取Activity中的Fagment，以及recyclerView以及adapter
     *
     * 以后要改成自己设置需要获取的对象类型
     */
    fun getAssembly(activity: AppCompatActivity)

}