package com.gagan.example.gamenav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class InterestsActivity extends AppCompatActivity {

    public SQLiteDatabase db;
    MyDatabase dbHelper;
    private boolean userProfileDataAvailabeInDb = false;

    private Spinner gamePlatform;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button saveInterests;
    private TextView infoBox;

    private String gameGenre = "ARCADE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        // add toolbar layout to activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gaming Interests");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gamePlatform = findViewById(R.id.gamePlatformSpinner);
        radioGroup = findViewById(R.id.gameGenereRadioGroup);
        infoBox = findViewById(R.id.infoTextView);

        saveInterests = findViewById(R.id.saveInterestData);
        saveInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInterestsData();
            }
        });

        dbHelper = new MyDatabase(this);
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT GAME_PLATFORM, GAME_GENRE FROM UserData", null);
            int count = cursor.getCount();
            if (count > 0) {
                userProfileDataAvailabeInDb = true;   // that means update available data
                cursor.moveToFirst();
                String plt = cursor.getString(cursor.getColumnIndex("GAME_PLATFORM"));
                String gnr = cursor.getString(cursor.getColumnIndex("GAME_GENRE"));
                if((plt != null) && (gnr != null)){
                    updateInfoBox(plt, gnr);
                }
                else{
                    infoBox.setText("Update User Interest for Profile");
                }

            }
            else{
                userProfileDataAvailabeInDb = false;   // that means insert data
                infoBox.setText("Data is not available in Database");
                //showToast("User Profile is not set");
            }
            if(cursor != null){
                cursor.close();
            }
            if(db != null) {
                db.close();
            }
        }
        catch (SQLException e){
            showToast("Database Unavailable");
        }
    }

    public void onRadioButtonCheck(View view){
        int radioId = radioGroup.getCheckedRadioButtonId();
        if(radioId != -1) {
            radioButton = findViewById(radioId);
            gameGenre = radioButton.getText().toString();
            //showToast(gameGenre);
        }
    }

    private void saveInterestsData(){
        ContentValues contentValues = new ContentValues();
        String platform = String.valueOf(gamePlatform.getSelectedItem());
        //String genre = String.valueOf(gameGenre.getSelectedItem());
        String genre = gameGenre;
        showToast(platform + " - " + genre);
        updateInfoBox(platform, genre);
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            contentValues.put("USER_ID", 1); // user ID
            contentValues.put("GAME_PLATFORM", platform);
            contentValues.put("GAME_GENRE", genre);
            if (userProfileDataAvailabeInDb == false) {  // insert data
                db.insert("UserData", null, contentValues);
            } else { // update data
                db.update("UserData", contentValues, "USER_ID = " + 1, null);
            }
            db.close();
        }
        catch (SQLException e){

        }
    }

    void updateInfoBox(String platform, String genre){
        infoBox.setText("Interests\n\n" +
                "Game Platform: " + platform + "\n" +
                "Game Genre     : " + genre );
    }
    void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}