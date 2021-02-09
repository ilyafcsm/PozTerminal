package com.example.pozterminal3

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class MyOrdersAdapter( private val clickListener: (String) -> Unit): RecyclerView.Adapter<MyOrdersHolder>() {
    private val TAG = "MyOrdersAdapter"

    //var myOrders = mutableListOf<String>()

    var myOrders = mutableListOf<Map<String,String>>()
    val db = Firebase.firestore
    var colOrdersRef = db.collection("test")
    var query = colOrdersRef.whereEqualTo("status", "open")
    init{
        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "listen:error", e)
                //Show.longToast("Error")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                myOrders = snapshot.documents!!.map{mapOf("orderId" to it.id, "number" to it.get("number").toString(), "table" to it.get("table").toString(), "opentime" to getDateTime(
                    it.getTimestamp("opentime")?.toDate()?.time.toString()), "sum" to it.get("sum").toString())} as MutableList<Map<String,String>> //.data?.get("items") as MutableList<String>
                //Show.longToast("Second ${snapshot.documents.size} ")
                this.notifyDataSetChanged()
            } else {0
                //Show.longToast("Third")
                Log.d(TAG, "Current data: null")
            }
        }
    }

    fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("HH:mm")
            val netDate = Date(s.toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrdersHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_orders_holder, parent, false)

        return MyOrdersHolder(view)
    }

    override fun getItemCount(): Int {
        return myOrders.size
    }



    override fun onBindViewHolder(holder: MyOrdersHolder, position: Int) {
        val item = myOrders[position]
        holder.myOrderText.text = "№" + item["number"]
        holder.tableText.text = "Стол " + item["table"]
        //Show.longToast(holder.tableText.text.toString())
        holder.timeText.text = "Открыт в " + item["opentime"].toString()

        //holder.myOrderText.setOnClickListener { clickListener(item["orderId"].toString()) }
        holder.itemView.setOnClickListener{clickListener(item["orderId"].toString())}
    }

}