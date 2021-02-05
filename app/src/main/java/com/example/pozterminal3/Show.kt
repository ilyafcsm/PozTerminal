package com.example.pozterminal3

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import java.lang.ref.WeakReference


class Show private constructor(context: Context) {
    private val ref: WeakReference<Context?>

    companion object {
        private var instance: Show? = null
        fun init(context: Context) {
            instance = Show(context)
        }

        fun shortToast(@StringRes id: Int) {
            if (instance == null || instance!!.ref.get() == null) return
            Toast.makeText(instance!!.ref.get(), id, Toast.LENGTH_SHORT).show()
        }

        fun longToast(@StringRes id: Int) {
            if (instance == null || instance!!.ref.get() == null) return
            Toast.makeText(instance!!.ref.get(), id, Toast.LENGTH_LONG).show()
        }

        fun shortToast(text: String?) {
            if (instance == null || instance!!.ref.get() == null) return
            Toast.makeText(instance!!.ref.get(), text, Toast.LENGTH_SHORT).show()
        }

        fun longToast(text: String?) {
            if (instance == null || instance!!.ref.get() == null) return
            Toast.makeText(instance!!.ref.get(), text, Toast.LENGTH_LONG).show()
        }
    }

    init {
        ref = WeakReference(context)
    }
}