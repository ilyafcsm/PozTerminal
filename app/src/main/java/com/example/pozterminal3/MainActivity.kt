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
import java.util.*


private lateinit var text_name: EditText
private lateinit var text_pass: EditText

private var mPinLockView: PinLockView? = null
private var mIndicatorDots: IndicatorDots? = null

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        mPinLockView = findViewById<PinLockView>(R.id.pin_lock_view)
        mIndicatorDots = findViewById<IndicatorDots>(R.id.indicator_dots)

        val db = Firebase.firestore
        val waitersRef = db.collection("cfg")
        var pincod: String = ""

        mPinLockView!!.attachIndicatorDots(mIndicatorDots)
        mPinLockView!!.pinLength = 4
        mPinLockView!!.textColor = ContextCompat.getColor(this, R.color.text_black)

        val mPinLockListener: PinLockListener = object : PinLockListener {
            override fun onComplete(pin: String) {
                waitersRef.addSnapshotListener { value, _ ->
                    if (value!!.documents[1].data!!.keys.filter { it.contains(pin.toLowerCase(Locale.ROOT)) }.isNotEmpty() ) {
                        pincod =
                            value.documents[1].data!!.keys.filter { it.contains(pin.toLowerCase(
                                Locale.ROOT)) }[0].toString()

                    } else {
                        pincod = "0000"
                    }
                    if (pincod !== "0000") {
                        runOnUiThread {
                            mPinLockView!!.postDelayed(Runnable {
                                val intent = Intent(applicationContext, MyOrders::class.java)
                                intent.putExtra(
                                    "waiter",
                                    value.documents[1].data!!.get(pincod).toString()
                                )
                                startActivity(intent)
                                finish()
                            }, 1000)
                        }
                    }

                    else {
                        Show.longToast("Неверный пароль!")
                    }
                }
            }

            override fun onEmpty() {
                //Show.longToast("Неверный пароль")
            }

            override fun onPinChange(pinLength: Int, intermediatePin: String) {
            }
        }

        mIndicatorDots!!.indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION

        mPinLockView!!.setPinLockListener(mPinLockListener)


    }

}