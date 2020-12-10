package com.gagan.example.gamenav;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

// API for city name:
// http://api.openweathermap.org/data/2.5/weather?q=brampton&appid=3451adb292f1b318e5c1bff378594fa7&units=metric

public class OpenWeatherAPI {

    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=3451adb292f1b318e5c1bff378594fa7";


    public static JSONObject getJSON(Context context, String city){

        try {
            //System.out.println("Open Weather 1");
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();
            //System.out.println(url);
           // System.out.println(city);
            connection.addRequestProperty("x-api-key",
                    context.getString(R.string.appId));
            //System.out.println("Open Weather 3");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            //System.out.println("Open Weather 4");
            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if(data.getInt("cod") != 200){

                return null;
            }
            //System.out.println(data.getString("temp"));
            return data;
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }
}
