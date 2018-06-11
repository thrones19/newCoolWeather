package com.coolweather.android.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by sunshine on 2017/3/20.
 */

public class HttpUtil {
    private static OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(6, TimeUnit.SECONDS)
            .connectTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .build();

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
