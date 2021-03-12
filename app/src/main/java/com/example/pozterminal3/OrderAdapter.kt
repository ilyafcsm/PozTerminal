package com.example.pozterminal3

import android.graphics.Color
import android.view.LayoutInflater
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
    val comm: String? = null
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
    data class OrderItem(val name:String, val kod:String, val amount:Double, val price: Double, val sum: Double, val comm: String): RecyclerItem()
    data class OrderGuest(var name: String, var sum: String): RecyclerItem()
}

class OrderAdapter(orderId:String, private val clickListener: (String) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {




    private val TYPE_GUEST = 0
    private val TYPE_ITEM = 1

    private val TAG = "OrderAdapter"

    var items = mutableListOf<RecyclerItem>()

    val db = Firebase.firestore

    var currGuest: String = "1"

    //var orderRef = db.collection("test").document(orderId)

//    init{
//        orderRef.addSnapshotListener { snapshot, e ->
//            if (e != null) {
//                Log.w(TAG, "listen:error", e)
//                return@addSnapshotListener
//            }
//
//            if (snapshot != null && snapshot.exists()) {
//                //items = snapshot.data?.get("items") as MutableList<String> //.data?.get("items") as MutableList<String>
//                items = (snapshot.toObject<OrderData>()?.items?.toSortedMap()?.flatMap { (guest, itm) -> mutableListOf<RecyclerItem>(RecyclerItem.OrderGuest(guest)) + itm.map{RecyclerItem.OrderItem(it)}}) as MutableList<RecyclerItem>
//
//                this.notifyDataSetChanged()
//            } else {
//                Log.d(TAG, "Current data: null")
//            }
//        }
//    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.order_holder, parent, false)
//
//        return OrderHolder(view)
//    }

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
            LayoutInflater.from(parent.context).inflate(
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

                (holder as OrderHolder).itemText.text = (item as RecyclerItem.OrderItem).name + " (${(item as RecyclerItem.OrderItem).amount.toInt().toString()}, ${(item as RecyclerItem.OrderItem).price.toInt().toString()}, ${(item as RecyclerItem.OrderItem).sum.toInt().toString()})"
                (holder as OrderHolder).textComm.text = (item as RecyclerItem.OrderItem).comm
            }
            is RecyclerItem.OrderGuest -> {

                val cg = (item as RecyclerItem.OrderGuest).name
                (holder as GuestHolder).guestText.text = (item as RecyclerItem.OrderGuest).name + (item as RecyclerItem.OrderGuest).sum
                (holder as GuestHolder).itemView.setOnClickListener{ currGuest = cg
                    clickListener(currGuest)

                    this.notifyDataSetChanged()
                   // this.notifyItemRangeChanged(position, holder.)
                }
                if (cg == currGuest){
                    (holder as GuestHolder).itemView.setBackgroundColor(Color.BLUE)
                }else{
                    (holder as GuestHolder).itemView.setBackgroundColor(Color.YELLOW)}
            }
        }
    }



}