package com.example.pozterminal3

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var groupText = itemView.findViewById<TextView>(R.id.itemTextViewGroup)
}