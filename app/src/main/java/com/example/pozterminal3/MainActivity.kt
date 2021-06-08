package com.example.pozterminal3


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.*
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


private lateinit var text_name: EditText
private lateinit var text_pass: EditText

private var mPinLockView: PinLockView? = null
private var mIndicatorDots: IndicatorDots? = null

class MainActivity : AppCompatActivity() {
    private val handler = Handler()
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
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        val canVibrate: Boolean = vibrator.hasVibrator()
                        val milliseconds = 500L

                        if (canVibrate) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                // API 26
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        milliseconds,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            } else {
                                // This method was deprecated in API level 26
                                vibrator.vibrate(milliseconds)
                            }
                        }
                        mPinLockView!!.resetPinLockView()
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