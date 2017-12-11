package com.example.a16704.weather;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by 16704 on 2017/12/11.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast>forecastList;
}

