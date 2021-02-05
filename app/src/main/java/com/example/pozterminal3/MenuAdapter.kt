package com.example.pozterminal3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


data class MenuItem(
    val id: String?,
    val name: String?,
    val amount: Double?,
    val price: Double?
)


class MenuAdapter(private val clickListener: (MenuItem) -> Unit): RecyclerView.Adapter<MenuHolder>() {
    private val TAG = "MenuAdapter"

    var items = mutableListOf<MenuItem>()

    val db = Firebase.firestore
    //var menuRef = db.collection("menu").document("mainMenu")

//    init{
//        menuRef.addSnapshotListener { snapshot, e ->
//            if (e != null) {
//                Log.w(TAG, "listen:error", e)
//                return@addSnapshotListener
//            }
//
//            if (snapshot != null && snapshot.exists()) {
//                items = snapshot.data?.get("items") as MutableList<String> //.data?.get("items") as MutableList<String>
//                //items = (snapshot.data?.get("items") as Map<String,Any>).entries.map{}
//                this.notifyDataSetChanged()
//            } else {
//                Log.d(TAG, "Current data: null")
//            }
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_holder, parent, false)

        return MenuHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        val itm = items[position]
        holder.itemText.text = itm.name + " (${itm.amount})"
        holder.itemText.setOnClickListener { clickListener(itm) }
    }

}