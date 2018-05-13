package com.binggege.scoop

import android.content.Context
import android.widget.Toast

/**
 * Created by congshengjie on 2018/4/29.
 */
fun <T : Context> T.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}