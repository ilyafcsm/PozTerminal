package com.example.pozterminal3

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var itemText = itemView.findViewById<TextView>(R.id.itemText)
    var textComm = itemView.findViewById<TextView>(R.id.textView3)
}