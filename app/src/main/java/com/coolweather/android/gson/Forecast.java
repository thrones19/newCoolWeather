package com.coolweather.android.gson;

import android.view.Window;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sunshine on 2017/3/20.
 */

public class Forecast {
    public String date;
    public Wind wind;
    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;
        @SerializedName("code_d")
        public String infoUrl;

    }
    public  class Wind{
        public String sc;
        public String dir;
    }
}
