package com.binggege.scoop

import android.content.Context
import android.support.design.widget.BottomSheetDialog

/**
 * Created by congshengjie on 2018/5/3.
 */
internal class ScoopBottomDialog(context: Context) : BottomSheetDialog(context),
        ScoopDialogStack.ScoopDialogInterface {

    var representativeComponent: ScoopUIComponent? = null

    override fun getUIComponent(): ScoopUIComponent? {
        return representativeComponent
    }

    override fun destroy() {
        dismiss()
    }
}