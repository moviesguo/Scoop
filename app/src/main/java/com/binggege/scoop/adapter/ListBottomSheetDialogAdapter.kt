package com.binggege.scoop.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.binggege.scoop.OnDialogItemClickListener
import com.binggege.scoop.R
import com.binggege.scoop.adapter.ListBottomSheetDialogAdapter.*
/**
 * Created by moviesguo on 2018/4/3.
 */
class ListBottomSheetDialogAdapter(context: Context, data: ArrayList<View>,itemClickListener: OnDialogItemClickListener) : RecyclerView.Adapter<ListBottomSheetDialogHolder>() {

    private var context:Context = context
    private var data: ArrayList<View> = data
    private var itemClickListener:OnDialogItemClickListener = itemClickListener


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ListBottomSheetDialogHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dialog_bottom_sheet,parent,false)
        val holder = ListBottomSheetDialogHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ListBottomSheetDialogHolder?, position: Int) {
        holder?.tv?.text = data.get(position).javaClass.simpleName
        holder?.itemView?.setOnClickListener({
            itemClickListener.onClick(data.get(position))
        })

    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ListBottomSheetDialogHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val tv = itemView?.findViewById<TextView>(R.id.tv_item)
    }

}