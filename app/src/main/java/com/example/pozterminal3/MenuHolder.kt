package com.example.pozterminal3

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MenuHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val itemText = itemView.findViewById<TextView>(R.id.itemTextView) as TextView
    val itemTextValues = itemView.findViewById<TextView>(R.id.textView) as TextView
}