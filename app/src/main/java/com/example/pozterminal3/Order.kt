package com.example.pozterminal3

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import kotlinx.android.synthetic.main.menu_holder.view.*
import kotlinx.android.synthetic.main.order_holder.view.*
import org.json.JSONException
import org.json.JSONObject
import ru.evotor.framework.core.IntegrationException
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.core.action.event.receipt.changes.position.SetExtra
import ru.evotor.framework.receipt.ExtraKey
import ru.evotor.framework.receipt.Position
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class Order : AppCompatActivity() {
    private val TAG = "Order XXXXXXX"

    var currGeust = "1"


    lateinit var orderId:String

    var db = Firebase.firestore

    lateinit var orderRef: DocumentReference

    private lateinit var orderRecview: RecyclerView
    private lateinit var menuRecview: RecyclerView
    private lateinit var searchRecview: SearchView
    private lateinit var chosentable: TextView
   // private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    //private lateinit var addItemBtn: Button

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var content: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        var button1 = findViewById<Button>(R.id.chosentable)

        var itemsNeed: MutableList<MenuItem>? = null




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


            val x = orderRef //db.collection("test").document(orderId)
            val m = db.collection("menu5").document(it.id.toString())

            db.runTransaction{transaction ->
                val msnap = transaction.get(m)
                val xsmap = transaction.get(x)

                //val ng = (snapshot.getDouble("guests")!! + 1).toString()
                if (it.amount!!.toInt() > 0) {

                    var counterValue: Int
                    if (xsmap?.get("items.${currGeust}.${it.id}.amount") == null)
                        counterValue = 0
                    else
                        counterValue = xsmap?.get("items.${currGeust}.${it.id}.amount").toString().toDouble().toInt()

                    transaction.update(m,"amount",FieldValue.increment(-1))
                    transaction.update(x,"items.${currGeust}.${it.id}.amount",FieldValue.increment(+1),
                            "items.${currGeust}.${it.id}.name",it.name,
                        "items.${currGeust}.${it.id}.price",it.price,
                        "items.${currGeust}.${it.id}.sum", it.price?.times(counterValue +1),
                        "items.${currGeust}.${it?.id}.comm", "")

                    if (xsmap?.get("items.${currGeust}.${it.id}") == null) {
                        transaction.update(x,"items.${currGeust}.${it.id}.addtime", com.google.firebase.Timestamp.now())
                    }

                    transaction.update(x, "sum", FieldValue.increment(it.price!!))

                    orderRecview.adapter

                    transaction
            }}.addOnSuccessListener { result ->
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


        menuRecview.setHasFixedSize(true)



        searchRecview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
// do something on text submit
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
// do something when text changes
                var itemsNew = itemsNeed?.filter { it.name?.toLowerCase()?.contains(newText!!)!! } as MutableList<MenuItem>
                (menuRecview.adapter as MenuAdapter).items = itemsNew
                (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
                return true
            }
        })

        //Log.d(TAG,"2")

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
                (orderRecview.adapter as OrderAdapter).items  =
                    ((snapshot.toObject<OrderData>()?.items?.toSortedMap()?.flatMap { (guest, itm) -> mutableListOf<RecyclerItem>(
                        RecyclerItem.OrderGuest(guest, " (${itm.toList().fold(0.0, { a, (c, b) -> a + b.sum!!})})")
                    ) + ((itm.toList().sortedBy { (a,b) -> b.addtime }.map { (kod, itm2) ->
                        RecyclerItem.OrderItem(
                            itm2.name!!,
                            kod,
                            itm2.amount!!,
                            itm2.price!!,
                            itm2.sum!!,
                            itm2.comm!!
                        )
                    } as MutableList<RecyclerItem>?)!!)}) as MutableList<RecyclerItem>?)!! //map{RecyclerItem.OrderItem(it)}}) as MutableList<RecyclerItem>
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

        //Log.d(TAG,"3")

        menuRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "listen:error", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                //Log.d(TAG,"10")
                (menuRecview.adapter as MenuAdapter).items =  snapshot.documents!!.map{
                    MenuItem(
                        it.id,
                        it.getString("name"),
                        it.getDouble("amount"),
                        it.getDouble("price"),
                        it.get("modifor") as HashMap<Any, String>?
                    )
                } as MutableList<MenuItem>

                itemsNeed = snapshot.documents!!.map{
                    MenuItem(
                        it.id,
                        it.getString("name"),
                        it.getDouble("amount"),
                        it.getDouble("price"),
                        it.get("modifor") as HashMap<Any, String>?
                    )
                } as MutableList<MenuItem>
                //myOrders = snapshot.documents!!.map{mapOf("orderId" to it.id, "number" to it.get("number").toString())} as MutableList<Map<String,String>> //.data?.get("items") as MutableList<String>
                (menuRecview.adapter as MenuAdapter).notifyDataSetChanged()
                //Log.d(TAG,"11")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

        //Log.d(TAG,"4")


        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        content = findViewById<ConstraintLayout>(R.id.orderContent)
        drawerLayout.setScrimColor(Color.TRANSPARENT)


        //Log.d(TAG,"5")

        // Initialize the action bar drawer toggle instance
        val drawerToggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        ){
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

        //Log.d(TAG,"6")\


        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.DOWN or ItemTouchHelper.UP) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {


                    //Remove swiped item from list and notify the RecyclerView
                    var position = viewHolder.adapterPosition
                    //Show.longToast(""+position)

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
                            var needSum =
                                ((orderRecview.adapter as OrderAdapter).items[position] as RecyclerItem.OrderItem).sum

                            ////////////////////////////
                            val updates = hashMapOf<String, Any>(
                                "items.${needGuest}.${needPos}" to FieldValue.delete(),
                                "sum" to FieldValue.increment(-needSum)
                            )

                            x.update(updates).addOnCompleteListener {
                                (orderRecview.adapter as OrderAdapter).notifyDataSetChanged()
                            }

                        }

                        is RecyclerItem.OrderGuest -> {

                            val numbersForFun: MutableList<Int> = mutableListOf(1, 2, 3)


                            var needPos =
                                ((orderRecview.adapter as OrderAdapter).items[position] as RecyclerItem.OrderGuest).name
                            var needSum =
                                ((orderRecview.adapter as OrderAdapter).items[position] as RecyclerItem.OrderGuest).sum

                            var resultSum = needSum.removeSurrounding(
                                " (", // prefix
                                ")" // suffix
                            )

                            var resSum = resultSum.toDouble()


                            ////////////////////////////
                            val updates = hashMapOf<String, Any>(
                                "items.${needPos}" to FieldValue.delete(),
                                "sum" to FieldValue.increment(-resSum)
                            )

                            x.update(updates).addOnCompleteListener {
                                (orderRecview.adapter as OrderAdapter).notifyDataSetChanged()
                            }
                        }
                    }


                ////////////////////////////////////


//                db.runTransaction{ transaction ->
//                    val snapshot = transaction.get(x)
//                    val n = (snapshot.get("items") as Map<String, Any>).size
//                    transaction.update(x, "items.${n?.rem(position)}", mapOf<String, String>())
//                    (orderRecview.adapter as OrderAdapter).items.removeAt(position)
//                    (orderRecview.adapter as OrderAdapter).notifyItemRemoved(position)
//                    (orderRecview.adapter as OrderAdapter).notifyDataSetChanged()
//                    transaction
//                }.addOnSuccessListener { result ->
//                    val p = (orderRecview.adapter as OrderAdapter).items.size
//                    Show.longToast(""+p+"err")
//
//
//                }.addOnFailureListener { e ->
//                    Log.w(TAG, "Transaction failure.", e)
//                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(orderRecview)

    }

    fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("HH:mm")
            val netDate = Date(s.toULong().toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun print(view: View){

        val itemsNeed = (menuRecview.adapter as MenuAdapter).items

        var needName1 = view.itemTextView.text!!.toString()
        var needName = needName1.toLowerCase()

        val item = itemsNeed?.filter { it.name?.toLowerCase()?.contains(needName)!! }.get(0) as MenuItem
        ///////////////////////////////////
        val d = Dialog(this@Order)
        d.setContentView(R.layout.timer_dialog)
        val b1 = d.findViewById(R.id.button1) as Button
        val b2 = d.findViewById(R.id.button2) as Button
       // val modtext = d.findViewById(R.id.modiforName) as TextView
        val t2 = d.findViewById(R.id.textView2) as TextView
        val tspin = d.findViewById(R.id.textViewSpinner) as TextView
        val np = d.findViewById(R.id.numberPicker1) as NumberPicker
        val dcomm = d.findViewById(R.id.editcomm) as EditText

        var needItem: String = ""

        var modifors = mutableListOf<String>()

        if (item.modifor.isNullOrEmpty()) {

            }
        else{
            modifors = item.modifor!!.values.toMutableList()
            val recMod = d.findViewById(R.id.recMod) as RecyclerView
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recMod.layoutManager = layoutManager
            recMod.adapter = ModAdapter(modifors) {
                needItem = it
        }
        }
        //val adapterRec: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modifors!!)

        //recMod

        //val modifors = item.modifor!!.values.toMutableList()

//        val adapterSpin: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modifors!!)
//        adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        adapterSpin.notifyDataSetChanged()
//        spin.adapter = adapterSpin
//        spin.setSelection(0)

        t2.text = needName1
        tspin.text = "Выберите модификатор:"
        np.maxValue = 20
        np.minValue = 2
        np.wrapSelectorWheel = false
        b1.setOnClickListener {
            var settingValue = np.value
            val x1 = orderRef //db.collection("test").document(orderId)
            val m1 = db.collection("menu5").document(item?.id.toString())
            val textts = mutableListOf<String>("11", "12", "13", "14", "15", "16")
            //Show.longToast(""+ modifors.size)

            db.runTransaction{transaction1 ->
                val msnap1 = transaction1.get(m1)
                val xsmap1 = transaction1.get(x1)

                //val ng = (snapshot.getDouble("guests")!! + 1).toString()
                if (item?.amount!!.toInt() >= settingValue) {

                    var counterValue1: Int
                    if (xsmap1?.get("items.${currGeust}.${item?.id}.amount") == null)
                        counterValue1 = 0
                    else
                        counterValue1 = xsmap1?.get("items.${currGeust}.${item?.id}.amount").toString().toDouble().toInt()

                    transaction1.update(m1,"amount",FieldValue.increment(-settingValue.toDouble()))
                    transaction1.update(x1,"items.${currGeust}.${item?.id}.amount",FieldValue.increment(settingValue.toDouble()),
                        "items.${currGeust}.${item?.id}.name",item?.name,
                        "items.${currGeust}.${item?.id}.price",item?.price,
                        "items.${currGeust}.${item?.id}.sum", item?.price!!.times(counterValue1 + settingValue))

                    if (dcomm.text.toString() !== "") {
                        transaction1.update(x1, "items.${currGeust}.${item?.id}.comm", dcomm.text.toString() + System.getProperty("line.separator") + needItem)
                    }
                    else{
                        transaction1.update(x1,"items.${currGeust}.${item?.id}.comm", needItem)
                    }

                    if (xsmap1?.get("items.${currGeust}.${item?.id}") == null) {
                        transaction1.update(x1,"items.${currGeust}.${item?.id}.addtime", com.google.firebase.Timestamp.now())
                    }

                    transaction1.update(x1, "sum", FieldValue.increment(item?.price!!.times(settingValue)))

                    orderRecview.adapter

                    transaction1
                }}.addOnSuccessListener { result ->
                Log.d(TAG, "Transaction success")
                orderRecview.adapter?.notifyDataSetChanged()
                menuRecview.adapter?.notifyDataSetChanged()
            }.addOnFailureListener { e ->
                Log.w(TAG, "Transaction failure.", e)
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

    fun print1(view: View){

        val itemsNeed = (menuRecview.adapter as MenuAdapter).items

        var needName1 = view.itemText.text!!.toString()
        val needName2 =  needName1.substringBeforeLast(" (")
        var needName = needName2.toLowerCase()

        val item = itemsNeed?.filter { it.name?.toLowerCase()?.contains(needName)!! }.get(0) as MenuItem
        ///////////////////////////////////
        val d = Dialog(this@Order)
        //d.setTitle("${view.itemText.text}")
        d.setContentView(R.layout.timer_dialog)
        val b1 = d.findViewById(R.id.button1) as Button
        val b2 = d.findViewById(R.id.button2) as Button
        //val spin = d.findViewById(R.id.spinner12) as Spinner
        val t2 = d.findViewById(R.id.textView2) as TextView
        val tspin = d.findViewById(R.id.textViewSpinner) as TextView
        val np = d.findViewById(R.id.numberPicker1) as NumberPicker
        val comm = d.findViewById(R.id.editcomm) as EditText

        var needItem: String = ""

        var modifors = mutableListOf<String>()
        modifors = item.modifor!!.values.toMutableList()

        if (modifors !== null) {
            val recMod = d.findViewById(R.id.recMod) as RecyclerView
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recMod.layoutManager = layoutManager
            recMod.adapter = ModAdapter(modifors) {
                needItem = it
            }
        }

        t2.text = needName2
        tspin.text = "Выберите модификатор:"
        np.maxValue = 25
        np.minValue = 1
        np.wrapSelectorWheel = false
        b1.setOnClickListener {
            var settingValue = np.value
            val x1 = orderRef //db.collection("test").document(orderId)
            val m1 = db.collection("menu5").document(item?.id.toString())

            db.runTransaction{transaction1 ->
                val msnap1 = transaction1.get(m1)
                val xsmap1 = transaction1.get(x1)

                //val ng = (snapshot.getDouble("guests")!! + 1).toString()
                if (item?.amount!!.toInt() >= settingValue) {

                    var counterValue1: Int
                    if (xsmap1?.get("items.${currGeust}.${item?.id}.amount") == null)
                        counterValue1 = 0
                    else
                        counterValue1 = xsmap1?.get("items.${currGeust}.${item?.id}.amount").toString().toDouble().toInt()

                    transaction1.update(m1,"amount",FieldValue.increment(-settingValue.toDouble()))
                    transaction1.update(x1,"items.${currGeust}.${item?.id}.amount",FieldValue.increment(settingValue.toDouble()),
                        "items.${currGeust}.${item?.id}.name",item?.name,
                        "items.${currGeust}.${item?.id}.price",item?.price,
                        "items.${currGeust}.${item?.id}.sum", item?.price!!.times(counterValue1 + settingValue))

                    if (comm.text.toString() !== "") {
                        transaction1.update(x1, "items.${currGeust}.${item?.id}.comm", comm.text.toString() + System.getProperty("line.separator") + needItem)
                    }
                    else{
                        transaction1.update(x1,"items.${currGeust}.${item?.id}.comm", needItem)
                    }

                    if (xsmap1?.get("items.${currGeust}.${item?.id}") == null) {
                        transaction1.update(x1,"items.${currGeust}.${item?.id}.addtime", com.google.firebase.Timestamp.now())
                    }

                    transaction1.update(x1, "sum", FieldValue.increment(item?.price!!.times(settingValue)))

                    orderRecview.adapter

                    transaction1
                }}.addOnSuccessListener { result ->
                Log.d(TAG, "Transaction success")
                orderRecview.adapter?.notifyDataSetChanged()
                menuRecview.adapter?.notifyDataSetChanged()
            }.addOnFailureListener { e ->
                Log.w(TAG, "Transaction failure.", e)
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

    fun addGuest(view: View){
        val x = orderRef
        db.runTransaction{ transaction ->
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
                }
                else
                {}
            }, 680)


        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }
    }

    fun choosetable(view: View){
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

//                // OR
//                // String returnedResult = data.getDataString();
//            }

        }
        }
    }


//    fun onActivityResult(requestCode1: Int, resultCode1: Int, data1: Intent) {
//        super.onActivityResult(requestCode1, resultCode1, data1)
//        if (requestCode1 == request_Code1) {
//            if (resultCode1 == RESULT_OK) {
//                val returnedResult = data1.data.toString()
//                // OR
//                // String returnedResult = data.getDataString();
//            }
//        }
//    }




    fun openReceipt() {
        //Ben = CartHelper.getCart()
        // Adad = recyclerView.getAdapter()
        //SizeInt = CartHelper.getCartItems().size()
        var i: Int
        var pr: Int
        var co: Int
        pr = 20
        co = 4
        val price: BigDecimal? = pr.toBigDecimal()
        val count: BigDecimal? = co.toBigDecimal()
        val set: MutableSet<ExtraKey> = HashSet()
        set.add(ExtraKey(null, null, "Тест Зубочистки"))
        //Создание списка товаров чека
        val positionAddList: MutableList<PositionAdd> =
            ArrayList()
        i = 0
        // while (i < SizeInt) {
        //Cardd = CartHelper.getCartItems().get(i)
        //Дополнительное поле для позиции. В списке наименований расположено под количеством и выделяется синим цветом
        positionAddList.add(
            PositionAdd(
                Position.Builder.newInstance( //UUID позиции
                    UUID.randomUUID().toString(),  //UUID товара
                    null,  //Наименование
                    //Cardd.getProduct().getName()
                    "Чебурек",  //Наименование единицы измерения
                    "шт",  //Точность единицы измерения
                    0,  //Цена без скидок
                    // new BigDecimal(200)
                    //CartHelper.getCart().getTotalPrice()
                    count!!,  //Количество
                    price!! //Добавление цены с учетом скидки на позицию. Итог = price - priceWithDiscountPosition
                ).setPriceWithDiscountPosition(price)
                    .setExtraKeys(set).build()
            )
        )
        i++

        //Дополнительные поля в чеке для использования в приложении
        val `object` = JSONObject()
        try {
            `object`.put("extra", "Номер стола 4")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val extra = SetExtra(`object`)

        //Открытие чека продажи. Передаются: список наименований, дополнительные поля для приложения
        OpenSellReceiptCommand(positionAddList, extra).process(
            this
        ) { future ->
            try {
                val result = future.result
                if (result.type == IntegrationManagerFuture.Result.Type.OK) {
                    startActivity(Intent("evotor.intent.action.payment.SELL"))
                    val intent = Intent(this, MyPrintableService::class.java)
                    this?.startService(intent)
                }
            } catch (e: IntegrationException) {
                e.printStackTrace()
            }
        }
    }
 }