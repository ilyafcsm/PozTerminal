package com.example.pozterminal3


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private lateinit var text_name: EditText
private lateinit var text_pass: EditText

const val TAG = "PinLockView"

private var mPinLockView: PinLockView? = null
private var mIndicatorDots: IndicatorDots? = null

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        mPinLockView = findViewById<PinLockView>(R.id.pin_lock_view)
        mIndicatorDots = findViewById<IndicatorDots>(R.id.indicator_dots)

        val db = Firebase.firestore
        val waitersRef = db.collection("cfg")
        var pincod: String = ""

        mPinLockView!!.attachIndicatorDots(mIndicatorDots)
        mPinLockView!!.setPinLength(4)
        mPinLockView!!.setTextColor(ContextCompat.getColor(this, R.color.text_black))

        val mPinLockListener: PinLockListener = object : PinLockListener {
            override fun onComplete(pin: String) {
                waitersRef.addSnapshotListener { value, error ->
                    if (value!!.documents[1].data!!.keys.filter { it.contains(pin.toLowerCase()) }[0].toString() !== "null") {
                        pincod =
                            value!!.documents[1].data!!.keys.filter { it.contains(pin.toLowerCase()) }[0].toString()
                    } else {
                        pincod = "6666"
                    }
                    //Show.longToast("Верно")
                    runOnUiThread(object : Runnable {
                        override fun run() {


                            //Show.longToast(value!!.documents[1].data!!.get(pincod).toString())
                            mPinLockView!!.postDelayed( Runnable { val intent = Intent(applicationContext, MyOrders::class.java)
                                intent.putExtra(
                                    "waiter",
                                    value!!.documents[1].data!!.get(pincod).toString()

                                )
                                startActivity(intent)
                                finish()}, 1000)
                        }
                    }
                    )
                }
            }

            override fun onEmpty() {
                //Show.longToast("Неверный пароль")
            }

            override fun onPinChange(pinLength: Int, intermediatePin: String) {
            }
        }

        mIndicatorDots!!.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION)

        mPinLockView!!.setPinLockListener(mPinLockListener)


    }

}