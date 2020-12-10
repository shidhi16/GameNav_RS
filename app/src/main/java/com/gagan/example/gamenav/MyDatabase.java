package com.gagan.example.gamenav;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
    * table will store the followings
    * 1. ID - primary key
    * 2. first name
    * 3. last name
    * 4. profile picture
    * 5. game platform
    * 6. game genre
    * 7. latitide
    * 8. longitude
 */
public class MyDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "gamenav_db";
    private static final int DB_VERSION = 1;

    MyDatabase(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // create the tables needed for the app
    @Override
    public void onCreate(SQLiteDatabase db) {
        // try with same profile pic name stored in
        // app data not in sql
        // USER_ID is req to get/update user data although there will be only 1 user
        db.execSQL("CREATE TABLE UserData ("
                + "_id INTEGER PRIMARY KEY, "
                + "USER_ID INTEGER,"
                + "FIRST_NAME TEXT, "
                + "LAST_NAME TEXT, "
                + "PROFILE_PIC TEXT,"
                + "GAME_PLATFORM TEXT,"
                + "GAME_GENRE TEXT,"
                + "LATITUDE REAL,"
                + "LONGITUDE REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    // user id is always 1 because the database is only for 1 user profile
    public void addUserToDb(SQLiteDatabase db,
                            int userId,
                            String firstName,
                            String lastName,
                            String gamePlatform,
                            String gameGenre,
                            float latitude,
                            float longitude){
        ContentValues contentValues = new ContentValues();
        contentValues.put("USER_ID", userId);
        contentValues.put("FIRST_NAME", firstName);
        contentValues.put("LAST_NAME", lastName);
        contentValues.put("GAME_PLATFORM", gamePlatform);
        contentValues.put("GAME_GENRE", gameGenre);
        contentValues.put("LATITUDE", latitude);
        contentValues.put("LONGITUDE", longitude);
        db.insert("UserData", null,contentValues);
    }

    // get all columns of user data by user id = 1
    public Cursor getUserDataByUserId(SQLiteDatabase db, int userId) {
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor res;
        res = db.rawQuery("SELECT * FROM UserData WHERE USER_ID =" + userId, null);
        //db.close();
        return res;
    }

    public void updateUserData(SQLiteDatabase db,
                               int userId,
                               String firstName,
                               String lastName,
                               String gamePlatform,
                               String gameGenre,
                               float latitude,
                               float longitude){
        ContentValues contentValues = new ContentValues();
        contentValues.put("USER_ID", userId);
        contentValues.put("FIRST_NAME", firstName);
        contentValues.put("LAST_NAME", lastName);
        contentValues.put("GAME_PLATFORM", gamePlatform);
        contentValues.put("GAME_GENRE", gameGenre);
        contentValues.put("LATITUDE", latitude);
        contentValues.put("LONGITUDE", longitude);
        db.update("UserData", contentValues, "USER_ID = " + userId, null);
    }
}
