package com.example.pozterminal3

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuestHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var guestText = itemView.findViewById<TextView>(R.id.guestTextView)
    var sumText = itemView.findViewById<TextView>(R.id.textViewSum)
}