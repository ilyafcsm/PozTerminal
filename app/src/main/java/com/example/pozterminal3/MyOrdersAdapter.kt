package com.example.pozterminal3

import android.graphics.Color
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Integer.parseInt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MyOrdersAdapter( private val clickListener: (String) -> Unit): RecyclerView.Adapter<MyOrdersHolder>() {
    private val TAG = "MyOrdersAdapter"
    private val handler = Handler()


    //var myOrders = mutableListOf<String>()

    var myOrders = mutableListOf<Map<String,String>>()
    val db = Firebase.firestore
    var colOrdersRef = db.collection("test")
    var query = colOrdersRef.whereIn("status", listOf("open","finish","send","payed"))
    var outinmills1: Long = 0
    var outinmills: Long = 0

    init{
        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "listen:error", e)
                //Show.longToast("Error")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                myOrders = snapshot.documents!!.map{mapOf("status" to it.get("status").toString(), "orderId" to it.id, "number" to it.get("number").toString(), "table" to it.get("table").toString(), "opentime" to getDateTime(
                    it.getTimestamp("opentime")?.toDate()?.time.toString()), "sum" to it.get("sum").toString(), "waiter" to it.get("waiter").toString())} as MutableList<Map<String,String>> //.data?.get("items") as MutableList<String>
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
            val sdf = SimpleDateFormat("HH:mm:ss")
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

        var opentime = item["opentime"].toString()

        val dateFormat = SimpleDateFormat("kk:mm")
        val dateFormatsec = SimpleDateFormat("kk:mm:ss")
        val dateFormat2 = SimpleDateFormat("mm")
        val dateFormat3 = SimpleDateFormat("kk")
        val dateformatseconds = SimpleDateFormat("ss")
        val date = dateFormatsec.parse(opentime).time

        handler.post(object : Runnable {
            override fun run() {
                val times = dateFormatsec.format(Calendar.getInstance().time)

                val needtimes = dateFormatsec.parse(times).time

                val outmininsec1 = (parseInt(dateFormat2.format(needtimes)) * 60)
                val outhourinsec1 = (parseInt(dateFormat3.format(needtimes)) * 60*60)
                val secs1 = parseInt(dateformatseconds.format(needtimes))
                outinmills1 = ((outhourinsec1 + outmininsec1 + secs1)*1000).toLong()

                val outmininsec = (parseInt(dateFormat2.format(date)) * 60)
                val outhourinsec = (parseInt(dateFormat3.format(date)) * 60*60)
                val secs = parseInt(dateformatseconds.format(date))
                outinmills = ((outhourinsec + outmininsec + secs)*1000).toLong()
                handler.postDelayed(this, 1000)
                updateTime(holder.openTime, outinmills1, outinmills.toLong())
                if (outinmills1 - outinmills >= 600000) {
                    holder.itemView.setBackgroundColor(Color.RED)
                }
            }
        })

        if (item["status"].toString() == "finish") {
            holder.itemView.setBackgroundColor(Color.GREEN)
        }
        else
        {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }


        holder.myOrderText.text = "№" + item["number"]
        holder.tableText.text = "Стол " + item["table"]
        holder.timeText.text = "Открыт в " + item["opentime"].toString()
        holder.status.text = item["status"].toString()

        var mapping = item["orderId"].toString()
        holder.itemView.setOnClickListener{clickListener(mapping)}
    }

    fun updateTime(mCounterTextView: TextView,currTime: Long,openTime: Long) {

                val needTime = currTime - openTime
                val minutesRemaining = needTime / 60000
                val secondsRemaining = (needTime % 60000) / 1000
                val minutes = appendZero(minutesRemaining)
                val seconds = appendZero(secondsRemaining)
                val timerText = "${minutes} : ${seconds}"
                mCounterTextView.text = timerText

    }

    private fun appendZero(time: Long): String {
        val timeString = time.toString()
        return if (time < 10) "0$timeString" else timeString
    }

}