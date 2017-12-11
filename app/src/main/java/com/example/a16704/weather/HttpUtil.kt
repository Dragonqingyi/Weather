package com.example.a16704.weather

import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by 16704 on 2017/12/11.
 */
object HttpUtil {
    fun sendOkHttpRequest(address:String, callback:okhttp3.Callback){
        val client = OkHttpClient()
        val request = Request.Builder().url(address).build()
        client.newCall(request).enqueue(callback)
    }
}