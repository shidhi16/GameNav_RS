package com.gagan.example.gamenav;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsApiClient {

    private static final String BASE_URL = "https://newsapi.org/v2/";

    public static Retrofit retrofit;

    public static Retrofit getApiNewsClient(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    //.client(getUnsafeOkHttpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

//    public static OkHttpClient.Builder getUnsafeOkHttpClient{
//        return null;
//    }
}
