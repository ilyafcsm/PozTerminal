package com.example.pozterminal3

import android.app.AlertDialog
import android.app.ListActivity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import androidx.core.text.toSpanned
import androidx.recyclerview.widget.RecyclerView
import com.example.pozterminal3.Show.Companion.init
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase


class DialogActivity : ListActivity() {

    private val zoneNames = arrayOf("1", "2", "3", "4", "5", "6")


    //MyNewAdapter myNewAdapter1 = new MyNewAdapter(this, null);
    var recyclerView: RecyclerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val adapter = ArrayAdapter<String>(
            this!!.applicationContext,
            R.layout.table_dialog1,
            R.id.zone_text1,
            //android.R.layout.simple_list_item_1,
            zoneNames
        )
        title = "Выберите зону"
        listAdapter = adapter
    }


    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        val itemsel = l!!.getItemAtPosition(position).toString()

        val db = Firebase.firestore

        var time: com.google.firebase.Timestamp? = com.google.firebase.Timestamp.now()

        var number: String = "0"

        val x = db.collection("cfg").document("cfgdata")
        db.runTransaction { transaction ->
            val snapshot = transaction.get(x)
            //val ng = (snapshot.getDouble("guests")!! + 1).toString()
            val n = snapshot.getDouble("number")!!
            //Log.d(TAG,"${n}")
            transaction.update(x, "number", n + 1)
            n + 1
            //transaction
            //transaction.update(x,mapOf("items.Гость $ng" to mutableListOf<String>()))
        }.addOnSuccessListener { result ->

            number = result.toInt().toString()
        }.addOnFailureListener { e ->
            Show.longToast("Fail")
        }

        val builder1 = AlertDialog.Builder(this)
        val doneNames = arrayOf(
            SpannableStringBuilder("" + itemsel + ".1"),
            SpannableStringBuilder("" + itemsel + ".2"),
            SpannableStringBuilder("" + itemsel + ".3"),
            SpannableStringBuilder("" + itemsel + ".4"),
            SpannableStringBuilder("" + itemsel + ".5"),
            SpannableStringBuilder("" + itemsel + ".6"),
            SpannableStringBuilder("" + itemsel + ".7"),
            SpannableStringBuilder("" + itemsel + ".8"),
            SpannableStringBuilder("" + itemsel + ".9"),
            SpannableStringBuilder("" + itemsel + ".10")
        )
        //val doneNames = arrayOf("1","2","3")
        var db1 = Firebase.firestore
        var colOrdersRef = db1.collection("test")

            var colorSpanRed = ForegroundColorSpan(Color.RED)
            var colorSpanGreen = ForegroundColorSpan(Color.GREEN)
            colOrdersRef.whereEqualTo("status", "open")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        for (i in doneNames) {
                            for (j in snapshot) {
                                if (j.data.get("table") == i.toString()) {
                                    i.setSpan(
                                        colorSpanRed,
                                        0,
                                        i.length,
                                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                                    )
                                    i.setSpan(
                                        StyleSpan(Typeface.BOLD),
                                        0,
                                        i.length,
                                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                                    )
                                    //Show.longToast(i.toString())
                                }
                                // var spa = i.toSpanned()
                            }
                        }
                    }
                }

        val adapter1 = ArrayAdapter<SpannableStringBuilder>(
            this!!.applicationContext,
            R.layout.table_dialog,
            R.id.zone_text,
            //android.R.layout.simple_list_item_1,
            doneNames
        )

        val list1 = adapter1
        var sum = 0.0


        db.runTransaction { transaction ->
            val snapshot = transaction.get(x)
            //val ng = (snapshot.getDouble("guests")!! + 1).toString()
            val pSum = snapshot.getDouble("sum")!!
            //Log.d(TAG,"${n}")

            transaction
            //transaction.update(x,mapOf("items.Гость $ng" to mutableListOf<String>()))
        }.addOnSuccessListener { result ->

            sum = result.get(x).get("sum") as Double
        }.addOnFailureListener { e ->
        }

        builder1.setTitle("Выберите стол")
            .setAdapter(
                list1
            ) { dialog1, item ->
                //Show.longToast("Выбранный стол:  ${tableNames[item]}")
                val data = hashMapOf(
                    "status" to "open",
                    "number" to number,
                    "table" to doneNames[item].toString(),
                    "opentime" to time,
                    "items" to hashMapOf("1" to hashMapOf<String, OrderItemData>()),
                    "sum" to sum
                )
                //var StrId: String = "44444444"

                val docRef = db.collection("test")
                    .document()
                docRef.set(data)
                    .addOnSuccessListener { }
                    .addOnFailureListener { Show.longToast("NoSuccess") }
                var strId = docRef.id.toString()

                val intent1 =
                    Intent(this, Order::class.java)

                //intent.putExtra("orderId", StrId)
                intent1.putExtra("orderId", strId)
                startActivity(intent1)
            }
            .setNegativeButton("Отмена") { dialog1, id ->
            }

        builder1.create().show()
    }

}