package com.example.a16704.weather

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager

//描述省信息的数据类
data class Province(var id:Int = 0, var provinceName:String, var provinceCode:String)
//描述城市的数据类
data class City(var id:Int = 0, var cityName:String, var cityCode:String, var provinceCode:String)
//描述县区的数据类
data class County(var id:Int = 0, var countyName:String, var countyCode:String, var cityCode:String)
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getString("weather", null) != null){
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
