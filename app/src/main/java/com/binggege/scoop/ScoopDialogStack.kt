package com.binggege.scoop

import android.content.DialogInterface
import java.util.*

/**
 * Created by congshengjie on 2018/5/3.
 */
internal class ScoopDialogStack {

    private val stack = Stack<ScoopDialogInterface>()

    var onStackEmpty: () -> Unit = {}

    fun pushAndShow(dialog: ScoopDialogInterface) {
        if (stack.isNotEmpty()) {
            stack.peek().dismiss()
        }
        stack.push(dialog)
        dialog.show()
    }

    fun popAndDismiss() {
        popAndDismiss {}
    }

    fun popAndDismiss(afterOldDialogDismiss: (ScoopDialogInterface) -> Unit) {
        if (stack.isNotEmpty()) {
            val pop = stack.pop()
            pop.dismiss()
            afterOldDialogDismiss(pop)
        }
        if (stack.isNotEmpty()) {
            val dialogInterface = stack.peek()
            val uiComponent = dialogInterface.getUIComponent()
            Scoop.instance?.maskView?.markComponent(uiComponent)
            dialogInterface.show()
        }
        if (stack.isEmpty()) {
            onStackEmpty()
        }
    }

    fun peek(): ScoopDialogInterface? {
        if (stack.isNotEmpty()) {
            return stack.peek()
        }
        return null
    }

    fun pop(): ScoopDialogInterface? {
        if (stack.isNotEmpty()) {
            return stack.pop()
        }
        return null
    }

    fun size() = stack.size

    fun destroy() {
        while (stack.isNotEmpty()) {
            stack.pop().destroy()
        }
    }

    interface ScoopDialogInterface {
        fun show()
        fun dismiss()
        fun destroy()
        fun getUIComponent(): ScoopUIComponent?
        fun setOnCancelListener(listener: DialogInterface.OnCancelListener)
    }
}