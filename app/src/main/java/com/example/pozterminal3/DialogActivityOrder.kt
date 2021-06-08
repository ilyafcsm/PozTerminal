package com.example.pozterminal3

import android.app.AlertDialog
import android.app.ListActivity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class DialogActivityOrder : ListActivity() {

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

        builder1.setTitle("Выберите стол")
            .setAdapter(list1
            ) { dialog1, item ->
                val intent2 = Intent()
                val text = doneNames[item]
                intent2.setData(Uri.parse(text.toString()));
                setResult(RESULT_OK, intent2)
                finish();
            }
            .setNegativeButton("Отмена") {
                    dialog1, id ->
            }

        builder1.create().show()
    }

}