package com.example.pozterminal3

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_order.*
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
    private lateinit var chosentable: TextView
   // private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    //private lateinit var addItemBtn: Button

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var content: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        var button1 = findViewById<Button>(R.id.chosentable)




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
        menuRecview.layoutManager = LinearLayoutManager(this)
        menuRecview.adapter = MenuAdapter() {
            val x = orderRef //db.collection("test").document(orderId)
            val m = db.collection("menu5").document(it.id.toString())

            m.update("amount", FieldValue.increment(-1))
            x.update(
                "items.${currGeust}.${it.id}.amount",
                1,
                "items.${currGeust}.${it.id}.name",
                it.name
            )
            orderRecview.adapter?.notifyDataSetChanged()
            menuRecview.adapter?.notifyDataSetChanged()

//            db.runTransaction{transaction ->
//                val msnap = transaction.get(m)
//                val xsmap = transaction.get(x)
//
//                //val ng = (snapshot.getDouble("guests")!! + 1).toString()
//                transaction.update(m,"amount",FieldValue.increment(-1))
//                transaction.update(x,"items.${currGeust}.${it.id}.amount",1,"items.${currGeust}.${it.id}.name",it.name)
//                //transaction.update(x,"items.${currGeust}.${it.id}.name",it.name)
//
//                transaction
//            }.addOnSuccessListener { result ->
//                Log.d(TAG, "Transaction success")
//                orderRecview.adapter?.notifyDataSetChanged()
//                menuRecview.adapter?.notifyDataSetChanged()
//            }.addOnFailureListener { e ->
//                Log.w(TAG, "Transaction failure.", e)
//            }
            //orderRecview.adapter?.notifyDataSetChanged()

        }


        menuRecview.setHasFixedSize(true)

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
                        RecyclerItem.OrderGuest(guest)
                    ) + ((itm.entries.map { (kod, itm2) ->
                        RecyclerItem.OrderItem(
                            itm2.name!!,
                            kod,
                            itm2.amount!!
                        )
                    } as MutableList<RecyclerItem>?)!!)}) as MutableList<RecyclerItem>?)!! //map{RecyclerItem.OrderItem(it)}}) as MutableList<RecyclerItem>
                (orderRecview.adapter as OrderAdapter).notifyDataSetChanged()
                //Log.d(TAG,"9")
                supportActionBar?.title = "Заказ " + snapshot.getString("number")
                button1.text = snapshot.getString("table")
                //timeField = snapshot.getTimestamp("time")
                var time1 = snapshot.getTimestamp("opentime")!!.toDate().time.toString()
                var ptime = getDateTime(time1)
                supportActionBar?.subtitle = ptime

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
                        it.getDouble("price")
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

        //Log.d(TAG,"6")

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

    fun addGuest(view: View){
        val x = orderRef
        db.runTransaction{ transaction ->
            val snapshot = transaction.get(x)
            val n = (snapshot.get("items") as Map<String, Any>).size
            transaction.update(x, "items.${n?.plus(1)}", mapOf<String, String>())
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
            }, 650)


        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }
    }

    fun print(view: View){
        openReceipt()
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