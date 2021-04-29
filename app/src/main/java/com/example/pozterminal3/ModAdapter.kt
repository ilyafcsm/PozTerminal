package com.example.pozterminal3

import android.graphics.Color
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ModAdapter(val num: Int, val items: MutableList<String>, private val clickListener: (String) -> Unit): RecyclerView.Adapter<ModHolder>() {

    //var items = mutableListOf<String>()
    var currMod: String = "1"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.modrecview, parent, false)

        return ModHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(position: Int): String? {
        return items[position]
    }

    override fun onBindViewHolder(holder: ModHolder, position: Int) {
        val itm = items[position]
        holder.modText.text = itm.toString()

        if (holder.modText.text.isNullOrEmpty()) {
            holder.modText.visibility = View.GONE
            holder.itemView.layoutParams.height = holder.itemView.layoutParams.height - 450
        }

        if (num == 0) {
            holder.itemView.layoutParams.height = holder.itemView.layoutParams.height - 90
        }
      //  holder.itemView.set
        holder.itemView.setOnClickListener {

            this.notifyDataSetChanged()

            currMod = itm

            getItem(position)?.let { it1 -> clickListener(it1) }
        }

        if (itm == currMod){
            holder.itemView.setBackgroundColor(Color.GRAY)
        }else{
           holder.itemView.setBackgroundColor(Color.WHITE)}
    }

}