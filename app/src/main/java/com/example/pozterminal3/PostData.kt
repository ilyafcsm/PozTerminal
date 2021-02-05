package com.example.pozterminal3

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class PostData {
    var client = OkHttpClient()

    @Throws(IOException::class)
    fun run(url: String?, callback: Callback) {
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(callback)
    }
}