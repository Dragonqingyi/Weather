package com.example.a16704.weather

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by 16704 on 2017/12/11.
 */
object DataSupport {
    //从InputStream对象读取数据，并转换为ByteArray
    private fun getBytesByInputStream(content: InputStream): ByteArray {
        var bytes: ByteArray? = null
        val bis = BufferedInputStream(content)
        val baos = ByteArrayOutputStream()
        val bos = BufferedOutputStream(baos)
        val buffer = ByteArray(1024 * 8)
        var length = 0
        try {
            while (true) {
                length = bis.read(buffer)
                if (length < 0)
                    break
                bos.write(buffer, 0, length)
            }
            bos.flush()
            bytes = baos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                bis.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bytes!!
    }


    //从服务端获取数据，并以字符串形式返回获取的数据
    private fun getServerContent(urlStr: String): String {
        var url = URL(urlStr)
        var conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.useCaches = false

        val content = conn.inputStream
        var responseBody = getBytesByInputStream(content)
        var str = kotlin.text.String(responseBody, Charset.forName("utf-8"))
        return str
    }

    //获取省列表
    fun getProvinces(provinces: (List<Province>) -> Unit) {
        Thread() {
            var content = getServerContent("https://geekori.com/api/china")
            var provinces = Utility.handleProvinceResponse(content)
            provinces(provinces)
        }.start()
    }

    //根据省获取城市列表
    fun getCities(provinceCode:String, cities:(List<City>)->Unit){
        Thread(){
            var content = getServerContent("https://geekori.com/api/china/$provinceCode")
            var cities = Utility.handleCityResponse(content, provinceCode)
            cities(cities)
        }.start()
    }

    //根据市获取县区列表
    fun getCounties(provinceCode: String, cityCode:String, counties:(List<County>)->Unit){
        Thread(){
            var content = getServerContent("https://geekori.com/api/china/$provinceCode/$cityCode")
            var counties = Utility.handleCountyResponse(content, cityCode)
            counties(counties)
        }.start()
    }
}