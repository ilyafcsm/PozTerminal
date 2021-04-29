package com.example.pozterminal3

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

sealed class RecyclerItemGroup {
    data class MenuItem(
        val id: String?,
        val name: String?,
        val amount: Double?,
        val price: Double?,
        val modifor: HashMap<Any, String>?,
        val povar: String? = null,
        val descr: String? = null,
        val group: String? = null
    ): RecyclerItemGroup()

    data class MenuGroup(
        val name: String?,
        val group: String?
    ): RecyclerItemGroup()
}


class MenuAdapter(private val clickListener: (RecyclerItemGroup) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_GROUP = 0
    private val TYPE_ITEM = 1

    private val TAG = "MenuAdapter"

    var items = mutableListOf<RecyclerItemGroup>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_GROUP -> GroupHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.group_holder,
                parent,
                false
            )
        )
        TYPE_ITEM -> MenuHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.menu_holder,
                parent,
                false
            )
        )
        else -> MenuHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.menu_holder,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is RecyclerItemGroup.MenuItem -> TYPE_ITEM
        is RecyclerItemGroup.MenuGroup -> TYPE_GROUP
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val itm = items[position]) {

            is RecyclerItemGroup.MenuItem -> {
                (holder as MenuHolder).itemText.text = itm.name
                (holder as MenuHolder).itemTextValues.text =
                    itm.amount?.toInt().toString() + "шт, " + itm.price?.toInt().toString() + "р"
                (holder as MenuHolder).itemTextValues.setOnClickListener { clickListener(itm) }
            }

            is RecyclerItemGroup.MenuGroup -> {
                (holder as GroupHolder).groupText.text = itm.group
                (holder as GroupHolder).groupText.setOnClickListener { clickListener(itm) }
            }
        }
    }

}