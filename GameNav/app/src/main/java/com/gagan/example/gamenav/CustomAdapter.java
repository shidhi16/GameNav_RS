package com.gagan.example.gamenav;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder>  {
    private List<Article> articleList;
    private Context context;

    public String title, dateTime, source, description;

    CustomAdapter(List<Article> articles){
        this.articleList = articles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.layout_news_card, parent, false);
        return new MyViewHolder(cv);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        TextView sourceText = cardView.findViewById(R.id.sourceTextView);
        TextView dateTimeText = cardView.findViewById(R.id.dateTimeTextView);
        TextView titleText = cardView.findViewById(R.id.newsTitleTextView);
        TextView descText = cardView.findViewById(R.id.newsDescpTextView);

        Article model = articleList.get(position);
        //Log.i("DateTime", model.getPublisedAt());

        sourceText.setText("Source: " + model.getSource().getName());
        dateTimeText.setText(Utils.DateFormat(model.getPublishedAt()));
        titleText.setText("News:\n" + model.getTitle());
        descText.setText("Details:\n" + model.getDescription());
    }

    @Override
    public int getItemCount() {
        // to limiting the recycler list to 10
//        if(articleList.size() > 10){
//            return 10;
//        }
        return articleList.size();
    }

    // Inner class for View Holder
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        MyViewHolder(CardView cv){
            super(cv);
            cardView = cv;
        }
    }

}
