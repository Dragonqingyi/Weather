package com.example.a16704.weather

import android.app.Fragment
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

/**
 * Created by 16704 on 2017/12/11.
 */
class ChooseAreaFragment : Fragment() {
    private var titleText: TextView? = null
    private var backButton: Button? = null
    private var listView: ListView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var hander = MyHander()
    private val dataList = ArrayList<String>()
    private var provinceList: List<Province>? = null
    private var cityList: List<City>? = null
    private var countyList: List<County>? = null
    private var selectedProvince: Province? = null
    private var selectedCity: City? = null
    private var currentLevel: Int = 0

    companion object {
        val LEVEL_PROVINCE = 0
        val LEVEL_CITY = 1
        val LEVEL_COUNTY = 2
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.choose_area, container, false)

        titleText = view.findViewById(R.id.title_text) as TextView
        backButton = view.findViewById(R.id.back_button) as Button
        listView = view.findViewById(R.id.list_view) as ListView
        adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, dataList)
        listView!!.adapter = adapter
        return view
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            //选择省
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList!![position]
                queryCities()
            }
            //选择市
            else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList!![position]
                queryCounties()
            }
            //选择县区
            else if (currentLevel == LEVEL_COUNTY) {
                val countyName = countyList!![position].countyName
                //选择县区后，如果Fragment处于打开状态，则隐藏，然后显示当前县区的天气
                if (activity is MainActivity) {
                    val intent = Intent(activity, WeatherActivity::class.java)
                    intent.putExtra("name", countyName)
                    startActivity(intent)
                    activity.finish()
                } else if (activity is WeatherActivity) {
                    val activity = activity as WeatherActivity
                    activity.drawerLayout?.closeDrawers()
                    activity.swipeRefresh?.isRefreshing = true
                    activity.requestWeather(countyName)
                }
            }
        }

        //回退按钮的单击事件
        backButton!!.setOnClickListener {
            //当前处于区县级，回退到市级
            if (currentLevel == LEVEL_COUNTY) {
                queryCities()
            }
            //当前处于市级，回退到省级
            else if (currentLevel == LEVEL_CITY) {
                queryProvinces()
            }
        }
        //默认显示省列表
        queryProvinces()
    }

    //用于更新ListView组件
    class MyHander : Handler() {
        override fun handleMessage(msg: Message?) {
            var activity = msg?.obj as ChooseAreaFragment
            when (msg?.arg1) {
            //在listview中显示省列表
                ChooseAreaFragment.LEVEL_PROVINCE -> {
                    if (activity.provinceList!!.isNotEmpty()) {
                        activity.dataList.clear()
                        for (province in activity.provinceList!!) {
                            activity.dataList.add(province.provinceName)
                        }
                        activity.adapter!!.notifyDataSetChanged()
                        activity.listView!!.setSelection(0)
                        activity.currentLevel = LEVEL_PROVINCE
                    }
                }
            //在listview中显示市列表
                ChooseAreaFragment.LEVEL_CITY -> {
                    if (activity.cityList!!.isNotEmpty()) {
                        activity.dataList.clear()
                        for (city in activity.cityList!!) {
                            activity.dataList.add(city.cityName)
                        }
                        activity.adapter!!.notifyDataSetChanged()
                        activity.listView!!.setSelection(0)
                        activity.currentLevel = LEVEL_CITY
                    }
                }
            //在listview中显示区县列表
                ChooseAreaFragment.LEVEL_COUNTY -> {
                    if (activity.countyList!!.isNotEmpty()) {
                        activity.dataList.clear()
                        for (county in activity.countyList!!) {
                            activity.dataList.add(county.countyName)
                        }
                        activity.adapter!!.notifyDataSetChanged()
                        activity.listView!!.setSelection(0)
                        activity.currentLevel = LEVEL_COUNTY
                    }
                }
            }
        }
    }

    //查询所有的省
    private fun queryProvinces() {
        titleText!!.text = "中国"
        backButton!!.visibility = View.GONE
        DataSupport.getProvinces {
            provinceList = it
            var msg = Message()
            msg.obj = this
            msg.arg1 = LEVEL_PROVINCE
            hander.sendMessage(msg)
        }
    }

    //根据省查询城市
    private fun queryCities() {
        titleText!!.text = selectedProvince!!.provinceName
        backButton!!.visibility = View.VISIBLE
        DataSupport.getCities(selectedProvince!!.provinceCode) {
            cityList = it
            var msg = Message()
            msg.obj = this
            msg.arg1 = LEVEL_CITY
            hander.sendMessage(msg)
        }
    }

    //根据选择的城市查询县区
    private fun queryCounties() {
        titleText!!.text = selectedCity!!.cityName
        backButton!!.visibility = View.VISIBLE
        DataSupport.getCounties(selectedProvince!!.provinceCode, selectedCity!!.cityCode) {
            countyList = it
            var msg = Message()
            msg.obj = this
            msg.arg1 = LEVEL_COUNTY
            hander.sendMessage(msg)
        }
    }
}