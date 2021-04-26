package com.example.pozterminal3

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModHolder(moditem: View): RecyclerView.ViewHolder(moditem) {
    val modText = moditem.findViewById<TextView>(R.id.modiforName) as TextView
}