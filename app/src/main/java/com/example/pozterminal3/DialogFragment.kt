package com.example.pozterminal3

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.R.id

import android.content.ContentUris





open class DialogFragment(private val clickListener: (String) -> Unit) : DialogFragment() {

    private val zoneNames = arrayOf("1", "2", "3", "4", "5", "6")
    private val tableNames = arrayOf("1","2","3","4","5","6","7","8","9","10")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val adapter = ArrayAdapter<String>(
                activity!!.applicationContext,
                R.layout.table_dialog,
                R.id.zone_text,
                //android.R.layout.simple_list_item_1,
                zoneNames
            )

            val list: ListAdapter = adapter
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val builder = AlertDialog.Builder(it)
            //var viewNew = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
            builder.setTitle("Выберите зону")
            builder.setAdapter(list) { dialog, which ->

                    //Show.longToast("Successful")
                    val builder1 = AlertDialog.Builder(it)
                    val doneNames = arrayOf(""+zoneNames[which]+".1",""+zoneNames[which]+".2",""+zoneNames[which]+".3",""+zoneNames[which]+".4", ""+zoneNames[which]+".5",""+zoneNames[which]+".6",""+zoneNames[which]+".7",""+zoneNames[which]+".8",""+zoneNames[which]+".9",""+zoneNames[which]+".10")
                    //val doneNames = arrayOf("1","2","3")

                val adapter1 = ArrayAdapter<String>(
                    activity!!.applicationContext,
                    R.layout.table_dialog,
                    R.id.zone_text,
                    //android.R.layout.simple_list_item_1,
                    doneNames
                )
                val list1 = adapter1

                    builder1.setTitle("Выберите стол")
                        .setAdapter(list1
                        ) { dialog1, item ->
                            //Show.longToast("Выбранный стол:  ${tableNames[item]}")
                            clickListener(doneNames[item])
                            dialog.dismiss()
                        }
                        .setNegativeButton("Отмена") {
                                dialog1, id ->
                        }
                        .setPositiveButton("Назад") {
                                dialog1, id ->
                            dialog1.dismiss()
                            //builder.create().show()
                        }
                        
                    builder1.create().show()
                }
                .setNegativeButton("Отмена") {
                       dialog, id ->
               }

            builder.create()
            //builder.show()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}