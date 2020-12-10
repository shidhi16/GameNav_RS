package com.gagan.example.gamenav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherUpdateActivity extends AppCompatActivity {

    private TextView cityName, temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_update);
        cityName = findViewById(R.id.cityNameLabel);
        temperature = findViewById(R.id.temperatureLabel);

        // add toolbar layout to activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Weather");

        try {
            JSONObject jsonObject = new JSONObject(getIntent().getStringExtra("json"));
            JSONObject main = jsonObject.getJSONObject("main");
            cityName.setText(jsonObject.getString("name"));
            temperature.setText(String.valueOf(main.getDouble("temp")) + " C");

            //System.out.println(jsonObject.getString("country"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}