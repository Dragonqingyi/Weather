package com.example.a16704.weather;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 16704 on 2017/12/11.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
