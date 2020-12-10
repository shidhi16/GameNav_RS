package com.gagan.example.gamenav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    public SQLiteDatabase db;
    MyDatabase dbHelper;

    private boolean userProfileDataAvailabeInDb = false;
    String profileImageTag = "";  // this tag will be the path and name of the image to display

    private EditText firstName, lastName;
    private Button saveProfile;

    private ImageView imageView;
    private ImageButton cameraButton;

    private static final String TAG = "CapturePicture";
    private String pictureFilePath;
    static final int REQUEST_PICTURE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageView = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.imageButton);
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            cameraButton.setEnabled(false);
        }

        // add toolbar layout to activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstName = findViewById(R.id.firstNameField);
        lastName = findViewById(R.id.lastNameField);
        saveProfile = findViewById(R.id.saveProfileButton);
        //saveProfile.setEnabled(false); // disable save button
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfileData();
            }
        });

        imageView.setImageResource(R.drawable.gamenav_logo);
       // imageView.setTag("R.drawable.gamenav_logo");
       // Log.e("ImageTag", imageView.getTag().toString());


        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                    sendTakePictureIntent();
                }
            }
        });

        dbHelper = new MyDatabase(this);
        try{
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT FIRST_NAME, LAST_NAME, PROFILE_PIC FROM UserData", null);
            int count = cursor.getCount();
            if(count > 0){
                userProfileDataAvailabeInDb = true;   // that means update available data
                cursor.moveToFirst();
                //Log.e("UserId", cursor.getString(cursor.getColumnIndex("USER_ID")));
                //Log.e("F_Name", cursor.getString(cursor.getColumnIndex("FIRST_NAME")));
                //Log.e("L_Name", cursor.getString(cursor.getColumnIndex("LAST_NAME")));
//                Log.e("L_Name", cursor.getString(cursor.getColumnIndex("GAME_PLATFORM")));
//                Log.e("L_Name", cursor.getString(cursor.getColumnIndex("GAME_GENRE")));
//                Log.e("L_Name", cursor.getString(cursor.getColumnIndex("LATITUDE")));
                //Log.e("ProfPic", cursor.getString(cursor.getColumnIndex("PROFILE_PIC")));
                firstName.setText(cursor.getString(cursor.getColumnIndex("FIRST_NAME")));
                lastName.setText(cursor.getString(cursor.getColumnIndex("LAST_NAME")));
                profileImageTag = cursor.getString(cursor.getColumnIndex("PROFILE_PIC"));
                if(profileImageTag != null) {
                    File imgFile = new File(profileImageTag);
                    if (imgFile.exists()) {
                        //Log.e("URI: ", pictureFilePath);
                        imageView.setImageURI(Uri.fromFile(imgFile));
                        imageView.setTag(profileImageTag);
                        profileImageTag = imageView.getTag().toString(); // this will be saved in database to retrieve the image
                        //Log.e("ImageTag", imageView.getTag().toString());
                    }
                }
                showToast("User Data Available");
            }
            else{
                userProfileDataAvailabeInDb = false;   // that means insert data
                showToast("User Profile is not set");
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

    private void sendTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);

            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                showToast("Photo file can't be created, please try again");
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.gagan.example.gamenav",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }
    private File getPictureFile() throws IOException {
        String pictureFile = "profile_pic";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                //Log.e("URI: ", pictureFilePath);
                imageView.setImageURI(Uri.fromFile(imgFile));
                imageView.setTag(pictureFilePath);
                profileImageTag = imageView.getTag().toString(); // this will be saved in database to retrieve the image
                //Log.e("ImageTag", imageView.getTag().toString());
            }
        }
    }

    void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void saveProfileData(){
        ContentValues contentValues = new ContentValues();
        String firstNameValue = firstName.getText().toString();
        String lastNameValue = lastName.getText().toString();
        if((!firstNameValue.isEmpty()) && (!lastNameValue.isEmpty())){
            showToast(firstNameValue + " " + lastNameValue);
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                contentValues.put("USER_ID", 1); // user ID
                contentValues.put("FIRST_NAME", firstNameValue);
                contentValues.put("LAST_NAME", lastNameValue);
                contentValues.put("PROFILE_PIC", profileImageTag);
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
        else{
            showToast("Fields must not be empty.");
        }
    }
}