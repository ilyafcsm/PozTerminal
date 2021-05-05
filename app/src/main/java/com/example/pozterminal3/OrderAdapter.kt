package com.example.pozterminal3

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


data class OrderItemData(
    val name: String? = null,
    val amount: Double? = null,
    val addtime: com.google.firebase.Timestamp? = null,
    val price: Double? = null,
    val sum: Double? = null,
    val comm: String? = null,
    val povar: String? = null,
    val status: String? = null
)

data class OrderData(
    val number: String? = null,
    val status: String? = null,
    val table: String? = null,
    val opentime: com.google.firebase.Timestamp? = null,
    val items: Map<String,Map<String, OrderItemData>>? = null,
    val sum: Double? = null
)

sealed class RecyclerItem{
    data class OrderItem(val name:String, val kod:String, val amount:Double, val price: Double, val sum: Double, val comm: String, val povar: String, val status: String): RecyclerItem()
    data class OrderGuest(var name: String, var sum: String): RecyclerItem()
}

class OrderAdapter(orderId:String, private val clickListener: (String) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {




    private val TYPE_GUEST = 0
    private val TYPE_ITEM = 1

    private val TAG = "OrderAdapter"

    var items = mutableListOf<RecyclerItem>()

    val db = Firebase.firestore

    var currGuest: String = "1"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when(viewType){
        TYPE_GUEST -> GuestHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.guest_holder,
                parent,
                false
            )
        )
        TYPE_ITEM -> OrderHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.order_holder,
                parent,
                false
            )
        )
        else -> OrderHolder(
            LayoutInflater.from(
                parent.context
                ).inflate(
                R.layout.order_holder,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int) = when(items[position]){
        is RecyclerItem.OrderItem -> TYPE_ITEM
        is RecyclerItem.OrderGuest -> TYPE_GUEST
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = items[holder.adapterPosition]){
            is RecyclerItem.OrderItem -> {

                (holder as OrderHolder).itemText.text = (item as RecyclerItem.OrderItem).name
                (holder as OrderHolder).textSumOrd.text = (item as RecyclerItem.OrderItem).amount.toInt().toString() + " x " + (item as RecyclerItem.OrderItem).price.toInt().toString() +  "р" + " = " + (item as RecyclerItem.OrderItem).sum.toInt().toString() + "р"
                (holder as OrderHolder).textComm.text = (item as RecyclerItem.OrderItem).comm

                (holder as OrderHolder).itemText.setTextColor(Color.BLACK)
                (holder as OrderHolder).textSumOrd.setTextColor(Color.BLACK)
                (holder as OrderHolder).textComm.setTextColor(Color.BLACK)

                if (item.status == "finish") {
                    (holder as OrderHolder).itemView.setBackgroundColor(Color.GREEN)
                }
                else{
                    (holder as OrderHolder).itemView.setBackgroundColor(Color.WHITE)
                }
                if ((holder as OrderHolder).textComm.text.isNullOrEmpty()) {
                    (holder as OrderHolder).textComm.visibility = View.GONE
                    (holder as OrderHolder).itemView.layoutParams.height = 145
                }


            }
            is RecyclerItem.OrderGuest -> {

                val cg = (item as RecyclerItem.OrderGuest).name
                var resultSum = (item as RecyclerItem.OrderGuest).sum.toString()
                var resultSum1 = resultSum.removeSurrounding(
                    " (", // prefix
                    ")" // suffix
                )
                (holder as GuestHolder).guestText.text = "Гость "+ (item as RecyclerItem.OrderGuest).name
                (holder as GuestHolder).sumText.text = resultSum1 + "р"
                (holder as GuestHolder).itemView.setOnClickListener{ currGuest = cg
                    clickListener(currGuest)

                    this.notifyDataSetChanged()
                   // this.notifyItemRangeChanged(position, holder.)
                }
                if (cg == currGuest){
                    (holder as GuestHolder).itemView.setBackgroundColor(Color.DKGRAY)
                    (holder as GuestHolder).guestText.setTextColor(Color.WHITE)
                    (holder as GuestHolder).sumText.setTextColor(Color.WHITE)
                }else{
                    (holder as GuestHolder).itemView.setBackgroundColor(Color.LTGRAY)
                    (holder as GuestHolder).guestText.setTextColor(Color.BLACK)
                    (holder as GuestHolder).sumText.setTextColor(Color.BLACK)
                }
            }
        }
    }



}