package com.example.a16704.weather

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class TestActivity : AppCompatActivity() {
    fun onClick_Test(view: View) {
        Toast.makeText(this, "hello", Toast.LENGTH_LONG).show()
        Thread() {
            var url = URL("https://geekori.com/api/china/")
            var conn = url.openConnection() as HttpURLConnection
            //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
            conn.setRequestMethod("GET")
            //HttpURLConnection默认也支持从服务端读取结果流，所以下面的setDoInput也可以省略
            conn.setDoInput(true)
            //用setRequestProperty方法设置一个自定义的请求头:action，由于后端判断

            //禁用网络缓存
            conn.setUseCaches(false)

            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
            conn.connect()
            //调用getInputStream方法后，服务端才会收到请求，并阻塞式地接收服务端返回的数据
            val content = conn.getInputStream()
            //将InputStream转换成byte数组,getBytesByInputStream会关闭输入流

            var responseBody = getBytesByInputStream(content)
            var str = kotlin.text.String(responseBody, Charset.forName("utf-8"))
            var provinces = Utility.handleProvinceResponse(str)

            Log.d("aaa", provinces.get(0).provinceName)
        }.start()

    }

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

    fun onClick_Test1(view: View) {
        HttpUtil.sendOkHttpRequest("https://www.baidu.com", object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("Test", e!!.message)
            }

            override fun onResponse(call: Call?, response: Response?) {
                fun onResponse(call: Call, response: Response) {
                    val responseText = response.body()!!.string()
                    Log.d("Test", responseText)
/*                    var result = false
                    if ("province" == type) {
                        result = Utility.handleProvinceResponse(responseText)
                    } else if ("city" == type) {
                        result = Utility.handleCityResponse(responseText, selectedProvince!!.getId())
                    } else if ("county" == type) {
                        result = Utility.handleCountyResponse(responseText, selectedCity!!.getId())
                    }
                    if (result) {
                        activity.runOnUiThread {
                            closeProgressDialog()
                            if ("province" == type) {
                                queryProvinces()
                            } else if ("city" == type) {
                                queryCities()
                            } else if ("county" == type) {
                                queryCounties()
                            }
                        }
                    }*/
                }


            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}
