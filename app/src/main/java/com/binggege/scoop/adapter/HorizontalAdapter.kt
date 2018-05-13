package com.binggege.scoop.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.binggege.scoop.R

/**
 * Created by moviesguo on 2018/4/10.
 */
class HorizontalAdapter (context: Context, data:List<String>): RecyclerView.Adapter<TextViewHolder>() {

    val context = context
    val data = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_horizontal, parent, false)
        view.setOnClickListener {
            Toast.makeText(it!!.context, "click", Toast.LENGTH_LONG).show()
        }
        return TextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder?.tv?.text = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

}