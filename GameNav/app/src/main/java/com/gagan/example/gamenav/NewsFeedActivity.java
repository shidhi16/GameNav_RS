package com.gagan.example.gamenav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView.Adapter adapter;

    private String country = "ca"; // news country
    private String category = "sports"; // headlines

    private List<Article> articles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        // add toolbar layout to activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Highlights - " + category.toUpperCase());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyckerView);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        loadNewsJson();

    } // onCreate

    public void loadNewsJson(){
        NewsApiInterface newsApiInterface = NewsApiClient.getApiNewsClient().create(NewsApiInterface.class);
        Call<NewsApiClass> call;
        call = newsApiInterface.getNews(country, category, getResources().getString(R.string.newsKey));

        call.enqueue(new Callback<NewsApiClass>() {
            @Override
            public void onResponse(Call<NewsApiClass> call, Response<NewsApiClass> response) {
                if(response.isSuccessful() && response.body().getArticle() != null){
                    if(articles.isEmpty() == false){
                        articles.clear();
                    }
                    articles = response.body().getArticle();
                    //articles.addAll(response.body().getArticle());
                    //adapter.notifyDataSetChanged();
                    adapter = new CustomAdapter(articles);
                    recyclerView.setAdapter(adapter);

                    //Log.e("Articles", articles.get(0).getTitle());
                }
            }

            @Override
            public void onFailure(Call<NewsApiClass> call, Throwable t) {

            }
        });
    } // loadNewsJson
}
