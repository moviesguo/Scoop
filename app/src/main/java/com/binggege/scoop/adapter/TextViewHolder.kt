package com.binggege.scoop.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.binggege.scoop.R

/**
 * Created by moviesguo on 2018/4/10.
 */
class TextViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView){
    val tv = itemView?.findViewById<TextView>(R.id.tv_item)
}