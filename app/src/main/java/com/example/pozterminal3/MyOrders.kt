package com.example.pozterminal3

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.security.Timestamp


class MyOrders : AppCompatActivity() {

    private lateinit var myOrdersRecview: RecyclerView

    val TAG = "MYORDERS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        myOrdersRecview = findViewById(R.id.my_orders_recview)
        myOrdersRecview.layoutManager = LinearLayoutManager(this)
        myOrdersRecview.adapter = MyOrdersAdapter {
            Log.d("order click", it)
            val intent =
                Intent(this@MyOrders, Order::class.java)
            intent.putExtra("orderId", it)
            startActivity(intent)
        }
        myOrdersRecview.setHasFixedSize(true)

    }


    fun add(view: View) {

        val intent2 = Intent(this, DialogActivity::class.java)
        startActivity(intent2)
    }
}