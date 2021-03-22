package com.example.pozterminal3

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


data class MenuItem(
    val id: String?,
    val name: String?,
    val amount: Double?,
    val price: Double?,
    val modifor: HashMap<Any, String>?
)


class MenuAdapter(private val clickListener: (MenuItem) -> Unit): RecyclerView.Adapter<MenuHolder>() {
    private val TAG = "MenuAdapter"

    var items = mutableListOf<MenuItem>()


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
        holder.itemText.text = itm.name
        holder.itemTextValues.text = itm.amount?.toInt().toString() + "шт, " + itm.price?.toInt().toString() + "р"
        holder.itemTextValues.setOnClickListener { clickListener(itm) }
//        holder.itemText.setOnClickListener {  val builder = AlertDialog.Builder(Order().applicationContext)
//
//
//            builder.setTitle("${itm.name}")
//            builder.setMessage("Выберите количество")
//
//            builder.setPositiveButton("Добавить"){dialog, which ->
//
//            }
//
//            builder.setNegativeButton("Назад"){dialog,which ->
//                dialog.cancel()
//            }
//
//            val dialog: AlertDialog = builder.create()
//
//            dialog.show() }
    }

}