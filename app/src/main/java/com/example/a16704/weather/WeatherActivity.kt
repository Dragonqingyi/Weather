package com.example.a16704.weather

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

/**
 * Created by 16704 on 2017/12/11.
 */
class WeatherActivity : AppCompatActivity() {
    var drawerLayout: DrawerLayout? = null
    var swipeRefresh: SwipeRefreshLayout? = null
    private var weatherLayout: ScrollView? = null
    private var navButton: Button? = null
    private var titleCity: TextView? = null
    private var titleUpdateTime: TextView? = null
    private var degreeText: TextView? = null
    private var weatherInfoText: TextView? = null
    private var forecastLayout: LinearLayout? = null
    private var aqiText: TextView? = null
    private var pm25Text: TextView? = null
    private var comfortText: TextView? = null
    private var carWashText: TextView? = null
    private var sportText: TextView? = null
    private var bingPicImg: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT > 21) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }

        setContentView(R.layout.activity_weather)

        bingPicImg = findViewById(R.id.bing_pic_img)
        weatherLayout = findViewById(R.id.weather_layout)
        titleCity = findViewById(R.id.title_city)
        titleUpdateTime = findViewById(R.id.title_update_time)
        degreeText = findViewById(R.id.degree_text)
        weatherInfoText = findViewById(R.id.weather_info_text)
        forecastLayout = findViewById(R.id.forecast_layout)
        aqiText = findViewById(R.id.aqi_text)
        pm25Text = findViewById(R.id.pm25_text)
        comfortText = findViewById(R.id.comfort_text)
        carWashText = findViewById(R.id.car_wash_text)
        sportText = findViewById(R.id.sport_text)
        swipeRefresh = findViewById(R.id.swipe_refresh) as? SwipeRefreshLayout
        swipeRefresh?.setColorSchemeResources(R.color.colorPrimary)
        drawerLayout = findViewById(R.id.drawer_layout)
        navButton = findViewById(R.id.nav_button)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val weatherString = prefs.getString("weather", null)
        val weatherId: String?
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            val weather = Utility.handleWeatherResponse(weatherString)
            weatherId = weather?.basic?.weatherId
            showWeatherInfo(weather)
        } else {
            //无缓存时去服务器查询天气
            weatherId = intent.getStringExtra("weather_id")
            weatherLayout!!.visibility = View.INVISIBLE
            requestWeather(weatherId)
        }

        //设置刷新时的监听事件
        swipeRefresh!!.setOnRefreshListener { requestWeather(weatherId) }
        navButton!!.setOnClickListener { drawerLayout?.openDrawer(GravityCompat.START) }
        val bingPic = prefs.getString("bing_pic", null)
        if (bingPic != null) {
            //装载显示天气时的背景图
            Glide.with(this).load(bingPic).into(bingPicImg)
        } else {
            loadBingPic()
        }
    }

    //根据天气id请求城市天气信息
    fun requestWeather(id: String?) {
        val weatherUrl = "https://geekori.com/api/weather?id=$id"
        HttpUtil.sendOkHttpRequest(weatherUrl, object : okhttp3.Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body()!!.string()
                val weather = Utility.handleWeatherResponse(responseText)
                runOnUiThread {
                    if (weather != null && "ok" == weather!!.status) {
                        val editor = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
                        editor.putString("weather", responseText)
                        editor.apply()
                        showWeatherInfo(weather)
                    } else {
                        Toast.makeText(this@WeatherActivity, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    }
                    swipeRefresh?.isRefreshing = false
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@WeatherActivity, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    swipeRefresh?.isRefreshing = false
                }
            }
        })
        loadBingPic()
    }

    //显示背景图像
    private fun loadBingPic() {
        val requestBingPic = "https://geekori.com/api/background/pic"
        HttpUtil.sendOkHttpRequest(requestBingPic, object : okhttp3.Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val bingPic = response.body()!!.string()
                val editor = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
                editor.putString("bing_id", null)
                editor.apply()
                runOnUiThread {
                    Glide.with(this@WeatherActivity).load(bingPic).into(bingPicImg)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })
    }

    //处理并显示Weather实体类中的数据
    private fun showWeatherInfo(weather: Weather?) {
        val cityName = weather?.basic?.cityName
        val updateTime = weather?.basic?.update?.updateTime!!.split(" ")[1]
        val degree = weather?.now?.temperature + "℃"
        val weatherInfo = weather?.now?.more?.info
        titleCity!!.text = cityName
        titleUpdateTime!!.text = updateTime
        degreeText!!.text = degree
        weatherInfoText!!.text = weatherInfo
        forecastLayout!!.removeAllViews()
        for (forecast in weather.forecastList) {
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dataText = view.findViewById(R.id.date_text) as TextView
            val infoText = view.findViewById(R.id.info_text) as TextView
            val maxText = view.findViewById(R.id.max_text) as TextView
            val minText = view.findViewById(R.id.min_text) as TextView
            dataText.text = forecast.date
            infoText.text = forecast.more.info
            maxText.text = forecast.temperature.max
            minText.text = forecast.temperature.min
            forecastLayout!!.addView(view)
        }
        if (weather.aqi != null) {
            aqiText!!.text = weather.aqi.city.aqi
            pm25Text!!.text = weather.aqi.city.pm25
        }

        val comfort = "舒适度：" + weather.suggestion.comfort.info
        val carWash = "洗车指数：" + weather.suggestion.carWash.info
        val sport = "运动建议：" + weather.suggestion.sport.info
        comfortText!!.text = comfort
        carWashText!!.text = carWash
        sportText!!.text = sport
        weatherLayout!!.visibility = View.VISIBLE
    }
}