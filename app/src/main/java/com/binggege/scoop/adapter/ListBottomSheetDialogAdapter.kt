package com.binggege.scoop.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.binggege.scoop.listener.OnDialogItemClickListener
import com.binggege.scoop.R
import com.binggege.scoop.adapter.ListBottomSheetDialogAdapter.*
/**
 * Created by moviesguo on 2018/4/3.
 */
class ListBottomSheetDialogAdapter(context: Context, data: ArrayList<Any>,itemClickListener: OnDialogItemClickListener) : RecyclerView.Adapter<ListBottomSheetDialogHolder>() {

    private var context:Context = context
    private var data: ArrayList<Any> = data
    private var itemClickListener: OnDialogItemClickListener = itemClickListener


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListBottomSheetDialogHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dialog_bottom_sheet,parent,false)
        val holder = ListBottomSheetDialogHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ListBottomSheetDialogHolder, position: Int) {
        val item = data.get(position)
        holder?.tv?.text = item::class.java.simpleName
        holder?.itemView?.setOnClickListener({
            itemClickListener.onClick(item)
        })
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ListBottomSheetDialogHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val tv = itemView?.findViewById<TextView>(R.id.tv_item)
    }


}