package com.example.pozterminal3

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyOrdersHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val myOrderText = itemView.findViewById<TextView>(R.id.my_order_text) as TextView
    val tableText = itemView.findViewById<TextView>(R.id.table_text) as TextView
    val timeText = itemView.findViewById<TextView>(R.id.time_text) as TextView
}