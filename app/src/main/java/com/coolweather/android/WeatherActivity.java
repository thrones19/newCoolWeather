package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView pltyText;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sporText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化控件
        init();
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);

        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();

        }
        if (weatherString != null){
            //有缓存的直接解析天气数据
            Weather weather = Utility.hanleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeather(weather);

        }else {
            //无缓存时去服务器查询天气;weather_id是上一个activity传过来的
            mWeatherId = getIntent().getStringExtra("weather_id");
            //String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            inquireWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.
                OnRefreshListener(){
            @Override
            public void onRefresh() {
                inquireWeather(mWeatherId);
            }
        });


        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


    }
    /*
    *初始化控件
     */
    private  void init(){
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        degreeeText = (TextView) findViewById(R.id.degree_text);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        pltyText = (TextView) findViewById(R.id.qlty_text);


        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.wash_text);
        sporText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_refresh);
        navButton = (Button) findViewById(R.id.nav_button);


    }
    /*
    *根据天气id请求城市天气信息
     */
    public void inquireWeather(final  String weatherId) {
        /*
        我自己的key
         */
        //String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
          //      weatherId + "&key=2e7b9e3dbf0d4bd1a5472a0ef7406ce9 ";
        /*
        郭霖的付费 key
         */
       // String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
            //    weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9 ";
        /*
        2017年7月31日
        之前的key不能用了，更新成v5接口
        付费：https://api.heweather.com/v5/weather?city=yourcity&key=yourkey
        免费用户：https://free-api.heweather.com/v5/weather?city=yourcity&key=yourkey
        下面用的郭霖的付费接口
         */
      //  String weatherUrl = "https://api.heweather.com/v5/weather?city=" +
        //            weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9 ";
        /*
        郭霖的key不能用了   我重新申请了一个key=ed519ff11d4c44e29861cf8acb8803ec
         */
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                            weatherId + "&key=ed519ff11d4c44e29861cf8acb8803ec";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,
                                "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.hanleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            //储存服务器返回的数据
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeather(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                            Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }
        });
        loadBingPic();
    }
    /*
    *处理并展示Weather实体类中的数据
     */
    private void showWeather(Weather weather){


        String cityName = weather.basic.cityName;


        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String updateTime2 = "["+updateTime+"更新]";
        String weatherInfo = weather.now.more.info;

        String degree = weather.now.temperature+"℃";
        String infoNowUrl = weather.now.more.infoUrl;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime2);
        degreeeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            ImageView yubaoinfoView = (ImageView) view.findViewById(R.id.yubaoinfo_view);
            TextView  windText = (TextView) view.findViewById(R.id.wind_text) ;
            String wind = forecast.wind.dir + "\n" + forecast.wind.sc;
            windText.setText(wind);
            String infoUrladress = forecast.more.infoUrl;
            loadbackgroundPng(infoUrladress,yubaoinfoView);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);

            String wendu = forecast.temperature.max +"～" + forecast.temperature.min + "℃";
            maxText.setText(wendu);
            forecastLayout.addView(view);
        }
        /*
        aqi里面是城市的空气质量参数，因为有一些城市没有aqi数据，所以使用前要判断
         */
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            //String weatherPlty = weather.aqi.city.qlty;
            pltyText.setText(weather.aqi.city.qlty);
        }
        else {
            aqiText.setText("无");
            pm25Text.setText("无");
            //String weatherPlty = weather.aqi.city.qlty;
            pltyText.setText("无");
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carwash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动指数: " + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carwash);
        sporText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        //weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent );
    }
    /*
    加载天气图片
     */

    private  void loadbackgroundPng(String infoUrladress,ImageView imageView){
        String backgroundPngUrl = "https://cdn.heweather.com/cond_icon/" +
                infoUrladress + ".png";
        imageView.setColorFilter(Color.WHITE);
        Glide.with(WeatherActivity.this).load(backgroundPngUrl).into(imageView);
    }
    /*
    *加载必应每日一图
     */
    private void loadBingPic() {

        final String bingPic = "https://api.dujin.org/bing/1366.php";
        SharedPreferences.Editor editor = PreferenceManager.
                getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("bing_pic",bingPic);
        editor.apply();
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                          }
                      });
       /** String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }
        */
    }

}
