package com.example.pozterminal3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyOrders : AppCompatActivity() {

    private lateinit var myOrdersRecview: RecyclerView
    private lateinit var waiter: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

       waiter = intent.getStringExtra("waiter").toString()

        myOrdersRecview = findViewById(R.id.my_orders_recview)
        myOrdersRecview.layoutManager = LinearLayoutManager(this)
        myOrdersRecview.adapter = MyOrdersAdapter {
            Log.d("order click", it.toString())
            val intent =
                Intent(this@MyOrders, Order::class.java)
            intent.putExtra("orderId", it.toString())
           //intent.putExtra("waiter", waiter)
            startActivity(intent)
        }
        myOrdersRecview.setHasFixedSize(true)

    }


    fun add(view: View) {

        val intent2 = Intent(this@MyOrders, DialogActivity::class.java)
        intent2.putExtra("waiter", waiter)
        startActivity(intent2)

        myOrdersRecview.adapter!!.notifyDataSetChanged()
    }


    fun delete(view: View) {
        val db = Firebase.firestore
        val docs = db.collection("test")

        deleteCollection(docs, 100)

        myOrdersRecview.adapter!!.notifyDataSetChanged()


    }

    fun deleteCollection(collection: CollectionReference, batchSize: Int) {
        try {
            var deleted = 0
            collection
                .limit(batchSize.toLong())
                .get()
                .addOnCompleteListener {
                    for (document in it.result!!.documents) {
                        document.getReference().delete()
                        ++deleted
                    }
                    if (deleted >= batchSize) {
                        deleteCollection(collection, batchSize)
                    }
                    myOrdersRecview.adapter!!.notifyDataSetChanged()
                }
        } catch (e: Exception) {
            System.err.println("Error deleting collection : " + e.message)
        }
    }
}