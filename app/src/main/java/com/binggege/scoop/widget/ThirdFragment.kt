package com.binggege.scoop.widget

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.binggege.scoop.R
import com.binggege.scoop.ViewHolderComponent

/**
 * Created by moviesguo on 2018/4/9.
 */
class ThirdFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View? = null
        view = inflater.inflate(R.layout.fragment_second, container, false)
        view!!.setOnClickListener {
            Toast.makeText(it!!.context, "click", Toast.LENGTH_LONG).show()
        }
        view?.findViewById<TextView>(R.id.tv_second_fragment)?.text = "Third Fragment"
        view?.findViewById<RecyclerView>(R.id.rv_second_fragment)!!.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = object : RecyclerView.ViewHolder(View(context)) {}
                override fun getItemCount() = 0
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            }
        }
        return view
    }
}