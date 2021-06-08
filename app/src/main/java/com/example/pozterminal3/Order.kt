package com.example.pozterminal3

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.group_holder.view.*
import kotlinx.android.synthetic.main.menu_holder.view.*
import kotlinx.android.synthetic.main.order_holder.view.*
import java.text.SimpleDateFormat
import java.util.*

class Order : AppCompatActivity() {
    private val TAG = "Order XXXXXXX"

    var currGeust = "1"


    lateinit var orderId: String
    lateinit var waiter: String

    var db = Firebase.firestore

    lateinit var orderRef: DocumentReference

    private lateinit var orderRecview: RecyclerView
    private lateinit var menuRecview: RecyclerView
    private lateinit var searchRecview: SearchView
    private lateinit var chosentable: TextView


    private lateinit var drawerLayout: DrawerLayout
    private lateinit var content: ConstraintLayout
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        var button1 = findViewById<Button>(R.id.chosentable)

        var itemsNeed: MutableList<RecyclerItemGroup.MenuItem>? = null


        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage("Нет интернета")

        val dialog: AlertDialog = builder.create()

        handler.post(object : Runnable {
            override fun run() {
                if (hasConnection(applicationContext) == false) {
                    dialog.show()
                }
                else{
                    dialog.dismiss()
                }
                handler.postDelayed(this, 1000)
            }
        })




        orderId = intent.getStringExtra("orderId") as String
        orderRef = db.collection("test").document(orderId)
        var menuRef = db.collection("menu5").orderBy("name")

        orderRecview = findViewById(R.id.items_recview)
        orderRecview.layoutManager = LinearLayoutManager(this)
        orderRecview.adapter = OrderAdapter(orderId) {
            currGeust = it
            Log.d(TAG, currGeust)
        }
        orderRecview.setHasFixedSize(true)


        //Log.d(TAG,"1")

        menuRecview = findViewById(R.id.menu_recview)
        searchRecview = findViewById(R.id.search_recview)
        menuRecview.layoutManager = LinearLayoutManager(this)
        menuRecview.adapter = MenuAdapter() {


            when (it) {

                is RecyclerItemGroup.MenuItem -> {

                    val x = orderRef //db.collection("test").document(orderId)
                    val m = db.collection("menu5")
                        .document(it.id.toString())

                    db.runTransaction { transaction ->
                        val msnap = transaction.get(m)
                        val xsmap = transaction.get(x)

                        //val ng = (snapshot.getDouble("guests")!! + 1).toString()
                        if (it.amount!!.toInt() > 0) {

                            var counterValue: Int
                            if (xsmap?.get("items.${currGeust}.${it.id}.amount") == null)
                                counterValue = 0
                            else
                                counterValue =
                                    xsmap?.get("items.${currGeust}.${it.id}.amount").toString()
                                        .toDouble()
                                        .toInt()

                            transaction.update(m, "amount", FieldValue.increment(-1))
                            transaction.update(
                                x,
                                "items.${currGeust}.${it.id}.amount",
                                FieldValue.increment(+1),
                                "items.${currGeust}.${it.id}.name",
                                it.name,
                                "items.${currGeust}.${it.id}.povar",
                                it.povar,
                                "items.${currGeust}.${it.id}.status",
                                "gotovitsya",
                                "items.${currGeust}.${it.id}.price",
                                it.price,
                                "items.${currGeust}.${it.id}.sum",
                                it.price?.times(counterValue + 1),
                                "items.${currGeust}.${it?.id}.comm",
                                ""
                            )

                            if (xsmap?.get("items.${currGeust}.${it.id}") == null) {
                                transaction.update(
                                    x,
                                    "items.${currGeust}.${it.id}.addtime",
                                    com.google.firebase.Timestamp.now()
                                )
                            }

                            if (it.group !== null) {
                                transaction.update(
                                    x,
                                    "items.${currGeust}.${it.id}.group",
                                    it.group
                                )
                            }
                            else {
                                transaction.update(
                                    x,
                                    "items.${currGeust}.${it.id}.group",
                                    ""
                                )
                            }

                            transaction.update(x, "sum", FieldValue.increment(it.price!!))

                            orderRecview.adapter

                            transaction
                        }
                    }.addOnSuccessListener { result ->
                        Log.d(TAG, "Transaction success")
                        orderRecview.adapter?.notifyDataSetChanged()
                        menuRecview.adapter?.notifyDataSetChanged()
                    }.addOnFailureListener { e ->
                        Log.w(TAG, "Transaction failure.", e)
                        Show.longToast("Fail")
                    }
                    orderRecview.adapter?.notifyDataSetChanged()
                    menuRecview.adapter?.notifyDataSetChanged()

                }
                is RecyclerItemGroup.MenuGroup -> {
                    var needGroup = it.group.toString()
                    menuRef.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "listen:error", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            //Log.d(TAG,"10")
                            (menuRecview.adapter as MenuAdapter).items = snapshot.documents!!.map {
                                RecyclerItemGroup.MenuItem(
                                    it.id,
                                    it.getString("name"),
                                    it.getDouble("amount"),
                                    it.getDouble("price"),
                                    it.get("modifor") as HashMap<Any, String>?,
                                    it.getString("povar"),
                                    it.getString("descr"),
                                    it.getString("group")
                                )
                            }.filter { it.group == needGroup } as MutableList<RecyclerItemGroup>
                            (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
                            //Log.d(TAG,"11")
                        } else {
                            Log.d(TAG, "Current data: null")
                        }
                    }
            }

        }
            }


        menuRecview.setHasFixedSize(true)



        searchRecview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
// do something on text submit
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
// do something when text changes
                var itemsNew = itemsNeed?.filter {
                    it.name?.toLowerCase()?.contains(newText!!)!!
                } as MutableList<RecyclerItemGroup>
                (menuRecview.adapter as MenuAdapter).items = itemsNew
                (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
                return true
            }
        })


        setSupportActionBar(toolbar)
        supportActionBar?.subtitle = "12:43"


        orderRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "listen:error", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                //Log.d(TAG,"8")
                //items = snapshot.data?.get("items") as MutableList<String> //.data?.get("items") as MutableList<String>
                (orderRecview.adapter as OrderAdapter).items =
                    ((snapshot.toObject<OrderData>()?.items?.toSortedMap()
                        ?.flatMap { (guest, itm) ->
                            mutableListOf<RecyclerItem>(
                                RecyclerItem.OrderGuest(
                                    guest,
                                    " (${itm.toList().fold(0.0, { a, (c, b) -> a + b.sum!! })})"
                                )
                            ) + ((itm.toList().sortedBy { (a, b) -> b.addtime }.map { (kod, itm2) ->
                                RecyclerItem.OrderItem(
                                    itm2.name!!,
                                    kod,
                                    itm2.amount!!,
                                    itm2.price!!,
                                    itm2.sum!!,
                                    itm2.comm!!,
                                    itm2.povar!!,
                                    itm2.status!!,
                                    itm2.group!!
                                )
                            } as MutableList<RecyclerItem>?)!!)
                        }) as MutableList<RecyclerItem>?)!! //map{RecyclerItem.OrderItem(it)}}) as MutableList<RecyclerItem>
                (orderRecview.adapter as OrderAdapter).notifyDataSetChanged()
                //Log.d(TAG,"9")
                supportActionBar?.title = "Заказ " + snapshot.getString("number")
                button1.text = snapshot.getString("table")
                //timeField = snapshot.getTimestamp("time")
                var time1 = snapshot.getTimestamp("opentime")!!.toDate().time.toString()
                var ptime = getDateTime(time1)
                supportActionBar?.subtitle = ptime + ",  ${snapshot.getDouble("sum")}р"

            } else {
                Log.d(TAG, "Current data: null")
            }
        }


        menuRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "listen:error", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                //Log.d(TAG,"10")
                (menuRecview.adapter as MenuAdapter).items = snapshot.documents!!.map {
                    RecyclerItemGroup.MenuGroup(
                        it.getString("name"),
                        it.getString("group")
                    )
                }.distinctBy{it.group} as MutableList<RecyclerItemGroup>

                itemsNeed = snapshot.documents!!.map {
                    RecyclerItemGroup.MenuItem(
                        it.id,
                        it.getString("name"),
                        it.getDouble("amount"),
                        it.getDouble("price"),
                        it.get("modifor") as HashMap<Any, String>?,
                        it.getString("povar"),
                        it.getString("descr"),
                        it.getString("group")
                    )
                } as MutableList<RecyclerItemGroup.MenuItem>
                (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
            } else {
                Log.d(TAG, "Current data: null")
            }
        }


        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        content = findViewById<ConstraintLayout>(R.id.orderContent)
        drawerLayout.setScrimColor(Color.TRANSPARENT)


        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        ) {
            private val scaleFactor = 6f
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val slideX = drawerView.width * slideOffset
                content.translationX = slideX
                content.setScaleX(1 - (slideOffset / scaleFactor))
                content.setScaleY(1 - (slideOffset / scaleFactor))
            }
        }
        drawerLayout.addDrawerListener(drawerToggle)


        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {

                var position = viewHolder.adapterPosition

                val updates1 = hashMapOf<String, Any>()
                val updates = hashMapOf<String, Any>()
                val x = orderRef

                when ((orderRecview.adapter as OrderAdapter).items[position]) {
                    is RecyclerItem.OrderItem -> {
                        var newPos = 0
                        loop@ for (i in position downTo 0 step 1)
                            when ((orderRecview.adapter as OrderAdapter).items[i]) {
                                is RecyclerItem.OrderGuest -> {
                                    break@loop
                                }
                                is RecyclerItem.OrderItem -> {
                                    newPos = i
                                }
                            }


                        var needGuest =
                            ((orderRecview.adapter as OrderAdapter).items[newPos - 1] as RecyclerItem.OrderGuest).name
                        var needPos =
                            ((orderRecview.adapter as OrderAdapter).items[position] as RecyclerItem.OrderItem).kod
                        var needName =
                            ((orderRecview.adapter as OrderAdapter).items[position] as RecyclerItem.OrderItem).name.toLowerCase()
                        var needSum =
                            ((orderRecview.adapter as OrderAdapter).items[position] as RecyclerItem.OrderItem).sum
                        var needAmount =
                            ((orderRecview.adapter as OrderAdapter).items[position] as RecyclerItem.OrderItem).amount

                        var itemsNew = itemsNeed?.filter {
                            it.name?.toLowerCase()?.contains(needName!!)!!
                        } as MutableList<RecyclerItemGroup>
                        var needFood = itemsNew[0]


                        val m = db.collection("menu5").document((needFood as RecyclerItemGroup.MenuItem).id.toString())


                        val updates = hashMapOf<String, Any>(
                            "items.${needGuest}.${needPos}" to FieldValue.delete(),
                            "sum" to FieldValue.increment(-needSum)
                        )

                        val updates1 = hashMapOf<String, Any>(
                            "amount" to FieldValue.increment(+needAmount)
                        )

                        m.update(updates1).addOnCompleteListener {
                            (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
                        }

                        x.update(updates).addOnCompleteListener {
                            (orderRecview.adapter as OrderAdapter).notifyDataSetChanged()
                        }

                    }

                    is RecyclerItem.OrderGuest -> {

                        val con = (orderRecview.adapter as OrderAdapter).items.size

                        loop@ for (sh in position..con - 1) {
                            when ((orderRecview.adapter as OrderAdapter).items[sh]) {
                                is RecyclerItem.OrderGuest -> {
                                    if (sh == position) {

                                        var needPos =
                                            ((orderRecview.adapter as OrderAdapter).items[sh] as RecyclerItem.OrderGuest).name
                                        var needSum =
                                            ((orderRecview.adapter as OrderAdapter).items[sh] as RecyclerItem.OrderGuest).sum

                                        var resultSum = needSum.removeSurrounding(
                                            " (", // prefix
                                            ")" // suffix
                                        )

                                        var resSum = resultSum.toDouble()

                                        updates.put("items.${needPos}", FieldValue.delete())
                                        updates.put("sum", FieldValue.increment(-resSum))
                                    } else {
                                        break@loop
                                    }
                                }
                                is RecyclerItem.OrderItem -> {

                                    var needName =
                                        ((orderRecview.adapter as OrderAdapter).items[sh] as RecyclerItem.OrderItem).name.toLowerCase()
                                    var needAmount =
                                        ((orderRecview.adapter as OrderAdapter).items[sh] as RecyclerItem.OrderItem).amount

                                    var itemsNew = itemsNeed?.filter {
                                        it.name?.toLowerCase()?.contains(needName!!)!!
                                    } as MutableList<RecyclerItemGroup>
                                    var needFood = itemsNew[0]
                                    val m = db.collection("menu5").document((needFood as RecyclerItemGroup.MenuItem).id.toString())

                                    updates1.put("amount", FieldValue.increment(+needAmount))
                                    m.update(updates1).addOnCompleteListener {
                                        (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
                                    }
                                }
                            }
                        }

                        x.update(updates).addOnCompleteListener {
                            (orderRecview.adapter as OrderAdapter).notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(orderRecview)

    }

    override fun onBackPressed() {
        val intent = Intent(applicationContext, MyOrders::class.java)
        startActivity(intent)
    }

    fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("HH:mm:ss")
            val netDate = Date(s.toULong().toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun print(view: View) {

        val itemsNeed = (menuRecview.adapter as MenuAdapter).items



        var needName1 = view.itemTextView.text!!.toString()
        var needName = view.itemTextView.text!!.toString().toLowerCase()

        val item =
            itemsNeed?.filter { (it as RecyclerItemGroup.MenuItem).name?.toLowerCase()?.contains(needName)!! }.get(0) as RecyclerItemGroup.MenuItem
        ///////////////////////////////////

        val descr = item.descr.toString()
        val d = Dialog(this@Order)
        d.setContentView(R.layout.timer_dialog)
        val b1 = d.findViewById(R.id.button1) as Button
        val b2 = d.findViewById(R.id.button2) as Button
        val t2 = d.findViewById(R.id.textView2) as TextView
        val tspin = d.findViewById(R.id.textViewSpinner) as TextView
        val np = d.findViewById(R.id.numberPicker1) as NumberPicker
        val dcomm = d.findViewById(R.id.editcomm) as EditText
        val descrText = d.findViewById<TextView>(R.id.textViewDescr)

        var needItem: String = ""

        var modifors = mutableListOf<String>()

        if (item.modifor.isNullOrEmpty()) {
            val recMod = d.findViewById(R.id.recMod) as RecyclerView
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recMod.layoutManager = layoutManager
            recMod.adapter = ModAdapter(0, modifors) {
                needItem = it
            }
            tspin.visibility = View.GONE

        } else {
            modifors = item.modifor!!.values.toMutableList()
            val recMod = d.findViewById(R.id.recMod) as RecyclerView
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recMod.layoutManager = layoutManager
            recMod.adapter = ModAdapter(1, modifors) {
                needItem = it
            }
        }

        if (descr.isNullOrEmpty()){}
        else {
            descrText.text = descr
        }
        t2.text = needName1
        tspin.text = "Выберите модификатор:"
        np.maxValue = 30
        np.minValue = 1
        np.wrapSelectorWheel = false
        b1.setOnClickListener {
            var settingValue = np.value
            val x1 = orderRef
            val m1 = db.collection("menu5").document(item?.id.toString())

            db.runTransaction { transaction1 ->
                val msnap1 = transaction1.get(m1)
                val xsmap1 = transaction1.get(x1)

                if (item?.amount!!.toInt() >= settingValue) {

                    var counterValue1: Int
                    if (xsmap1?.get("items.${currGeust}.${item?.id}.amount") == null)
                        counterValue1 = 0
                    else
                        counterValue1 =
                            xsmap1?.get("items.${currGeust}.${item?.id}.amount").toString()
                                .toDouble().toInt()

                    transaction1.update(
                        m1,
                        "amount",
                        FieldValue.increment(-settingValue.toDouble())
                    )
                    transaction1.update(
                        x1,
                        "items.${currGeust}.${item?.id}.amount",
                        FieldValue.increment(settingValue.toDouble()),
                        "items.${currGeust}.${item?.id}.name",
                        item?.name,
                        "items.${currGeust}.${item?.id}.povar",
                        item?.povar,
                        "items.${currGeust}.${item?.id}.status",
                        "gotovitsya",
                        "items.${currGeust}.${item?.id}.price",
                        item?.price,
                        "items.${currGeust}.${item?.id}.group",
                        item?.group,
                        "items.${currGeust}.${item?.id}.sum",
                        item?.price!!.times(counterValue1 + settingValue)
                    )

                    if (dcomm.text.isNullOrEmpty()) {
                        transaction1.update(x1, "items.${currGeust}.${item?.id}.comm", needItem)
                    } else {
                        if (needItem !== "") {
                            transaction1.update(
                                x1,
                                "items.${currGeust}.${item?.id}.comm",
                                "• " + dcomm.text.toString() + System.getProperty("line.separator") + "• " + needItem
                            )
                        } else {
                            transaction1.update(
                                x1,
                                "items.${currGeust}.${item?.id}.comm",
                                "• " + dcomm.text.toString()
                            )
                        }
                    }

                    if (xsmap1?.get("items.${currGeust}.${item?.id}") == null) {
                        transaction1.update(
                            x1,
                            "items.${currGeust}.${item?.id}.addtime",
                            com.google.firebase.Timestamp.now()
                        )
                    }

                    transaction1.update(
                        x1,
                        "sum",
                        FieldValue.increment(item?.price!!.times(settingValue))
                    )

                    orderRecview.adapter

                    transaction1
                }
            }.addOnSuccessListener { result ->
                orderRecview.adapter?.notifyDataSetChanged()
                menuRecview.adapter?.notifyDataSetChanged()
            }.addOnFailureListener { e ->
            }
            orderRecview.adapter?.notifyDataSetChanged()
            menuRecview.adapter?.notifyDataSetChanged()
            d.dismiss()
        }
        b2.setOnClickListener {
            d.dismiss()
        }
        d.show()
    }

    fun print1(view: View) {

        val namePos = (orderRecview.adapter as OrderAdapter).items.filter { when (it) { is RecyclerItem.OrderItem -> {it.name == view.itemText.text} is RecyclerItem.OrderGuest -> {1==0}}}

        val namePos1 = namePos.get(0) as RecyclerItem.OrderItem
        val nameGroup = namePos1.group
        var itemsNeed = (menuRecview.adapter as MenuAdapter).items
        val itemNeed = itemsNeed.filter { (it as RecyclerItemGroup.MenuGroup).group == nameGroup }.get(0) as RecyclerItemGroup.MenuGroup

        //menuRecview.forEachIndexed { index, view ->  if (view.textView.text == itemNeed.group) {view.performClick()} }

        menuRecview.forEach { if (it.itemTextViewGroup.text == itemNeed.group) {
            menuRecview.postDelayed(Runnable {
                it.itemTextViewGroup.performClick()
                val text = "Пора покормить кота!"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
            }, 700)
            menuRecview.adapter!!.notifyDataSetChanged()} }

        menuRecview.adapter!!.notifyDataSetChanged()

        itemsNeed = (menuRecview.adapter as MenuAdapter).items

        var needName1 = view.itemText.text!!.toString()
        val needName2 = needName1.substringBeforeLast(" (")
        var needName = needName2.toLowerCase()

        val item = itemsNeed?.filter { (it as RecyclerItemGroup.MenuItem).name?.toLowerCase()?.contains(needName)!! }.get(0) as RecyclerItemGroup.MenuItem
        val descr = (item as RecyclerItemGroup.MenuItem).descr
        val d = Dialog(this@Order)
        d.setContentView(R.layout.timer_dialog)
        val b1 = d.findViewById(R.id.button1) as Button
        val b2 = d.findViewById(R.id.button2) as Button
        val t2 = d.findViewById(R.id.textView2) as TextView
        val tspin = d.findViewById(R.id.textViewSpinner) as TextView
        val np = d.findViewById(R.id.numberPicker1) as NumberPicker
        val comm = d.findViewById(R.id.editcomm) as EditText
        val descrText = d.findViewById<TextView>(R.id.textViewDescr)

        var needItem: String = ""

        var modifors = mutableListOf<String>()

        if (modifors !== null) {
            modifors = item.modifor!!.values.toMutableList()
            val recMod = d.findViewById(R.id.recMod) as RecyclerView
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recMod.layoutManager = layoutManager
            recMod.adapter = ModAdapter(1, modifors) {
                needItem = it
            }
        }
        else{

            val recMod = d.findViewById(R.id.recMod) as RecyclerView
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recMod.layoutManager = layoutManager
            recMod.adapter = ModAdapter(0, modifors) {
                needItem = it
            }
            tspin.visibility = View.GONE
        }

        t2.text = needName2
        if (descr.isNullOrEmpty()){}
        else {
            descrText.text = descr
        }
        tspin.text = "Выберите модификатор:"
        np.maxValue = 25
        np.minValue = 1
        np.wrapSelectorWheel = false
        b1.setOnClickListener {
            var settingValue = np.value
            val x1 = orderRef
            val m1 = db.collection("menu5").document(item?.id.toString())

            db.runTransaction { transaction1 ->
                val msnap1 = transaction1.get(m1)
                val xsmap1 = transaction1.get(x1)

                if (item?.amount!!.toInt() >= settingValue) {

                    var counterValue1: Int
                    if (xsmap1?.get("items.${currGeust}.${item?.id}.amount") == null)
                        counterValue1 = 0
                    else
                        counterValue1 =
                            xsmap1?.get("items.${currGeust}.${item?.id}.amount").toString()
                                .toDouble().toInt()

                    transaction1.update(
                        m1,
                        "amount",
                        FieldValue.increment(-settingValue.toDouble())
                    )
                    transaction1.update(
                        x1,
                        "items.${currGeust}.${item?.id}.amount",
                        FieldValue.increment(settingValue.toDouble()),
                        "items.${currGeust}.${item?.id}.name",
                        item?.name,
                        "items.${currGeust}.${item?.id}.povar",
                        item?.povar,
                        "items.${currGeust}.${item?.id}.status",
                        "gotovitsya",
                        "items.${currGeust}.${item?.id}.price",
                        item?.price,
                        "items.${currGeust}.${item?.id}.group",
                        item?.group,
                        "items.${currGeust}.${item?.id}.sum",
                        item?.price!!.times(counterValue1 + settingValue)
                    )

                    if (comm.text.toString() !== "") {
                        transaction1.update(
                            x1,
                            "items.${currGeust}.${item?.id}.comm",
                            "• " + comm.text.toString() + System.getProperty("line.separator") + "• " + needItem
                        )
                    } else {
                        transaction1.update(x1, "items.${currGeust}.${item?.id}.comm", needItem)
                    }

                    if (xsmap1?.get("items.${currGeust}.${item?.id}") == null) {
                        transaction1.update(
                            x1,
                            "items.${currGeust}.${item?.id}.addtime",
                            com.google.firebase.Timestamp.now()
                        )
                    }

                    transaction1.update(
                        x1,
                        "sum",
                        FieldValue.increment(item?.price!!.times(settingValue))
                    )

                    orderRecview.adapter

                    transaction1
                }
            }.addOnSuccessListener { result ->
                orderRecview.adapter?.notifyDataSetChanged()
                menuRecview.adapter?.notifyDataSetChanged()
            }.addOnFailureListener { e ->
            }
            orderRecview.adapter?.notifyDataSetChanged()
            menuRecview.adapter?.notifyDataSetChanged()
            d.dismiss()
        }
        b2.setOnClickListener {
            d.dismiss()
        }
        d.show()

        /////////////////////////////////////
    }

    fun addGuest(view: View) {
        val x = orderRef
        db.runTransaction { transaction ->
            val snapshot = transaction.get(x)
            val n = (snapshot.get("items") as Map<String, Any>).keys
            var max = 0
            var thatkey: String
            n.forEach {
                var needInt = it.toInt()
                if (needInt > max)
                    max = needInt
            }
            transaction.update(x, "items.${max?.plus(1)}", mapOf<String, String>())
            transaction
        }.addOnSuccessListener { result ->
            val p = (orderRecview.adapter as OrderAdapter).items.size
            orderRecview.adapter?.notifyDataSetChanged()

            orderRecview.postDelayed(Runnable {
                if (orderRecview.findViewHolderForAdapterPosition(p) != null) {
                    orderRecview.findViewHolderForAdapterPosition(p)!!.itemView.performClick()
                } else {
                }
            }, 700)


        }.addOnFailureListener { e ->
        }
    }

    fun choosetable(view: View) {
        val intent1 = Intent(this, DialogActivityOrder::class.java)
        startActivityForResult(intent1, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                val returnedResult = data?.data.toString()
                var button1 = findViewById<Button>(R.id.chosentable)
                button1.text = returnedResult
                val db = Firebase.firestore
                val docRef = db.collection("test").document(orderId)
                docRef.update("table", returnedResult)
            }
        }
    }

    fun back(view: View) {
        var menuRef = db.collection("menu5").orderBy("name")
        menuRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "listen:error", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                //Log.d(TAG,"10")
                (menuRecview.adapter as MenuAdapter).items = snapshot.documents!!.map {
                    RecyclerItemGroup.MenuGroup(
                        it.getString("name"),
                        it.getString("group")
                    )
                }.distinctBy{it.group} as MutableList<RecyclerItemGroup>
                //myOrders = snapshot.documents!!.map{mapOf("orderId" to it.id, "number" to it.get("number").toString())} as MutableList<Map<String,String>> //.data?.get("items") as MutableList<String>
                (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
                //Log.d(TAG,"11")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }


    fun send(view: View) {

        val title = "Отправить на:"
        val button1String = "Кухню"
        val button2String = "Оплату"

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setPositiveButton(button1String
        ) { dialog, id ->
            val docRef = db.collection("test").document(orderId)
            docRef.update("status", "send")
            //docRef.update("waiter", waiter)
            dialog.dismiss()
        }
        builder.setNegativeButton(button2String
        ) { dialog, id ->
            val docRef = db.collection("test").document(orderId)
            docRef.update("status", "payed")
            //docRef.update("waiter", waiter)
            dialog.dismiss()
        }
        builder.setCancelable(true)

        builder.create().show()
    }

    fun hasConnection(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.activeNetworkInfo
        return if (wifiInfo != null && wifiInfo.isConnected) {
            true
        } else false
    }
}