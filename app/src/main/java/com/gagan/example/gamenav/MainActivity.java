package com.gagan.example.gamenav;

// weather api key: 3451adb292f1b318e5c1bff378594fa7
// weather URL: api.openweathermap.org/data/2.5/weather?zip=L5N7Z4,CA&appid=3451adb292f1b318e5c1bff378594fa7
// example URL: api.openweathermap.org/data/2.5/weather?zip=L6R3E5,canada&appid=3451adb292f1b318e5c1bff378594fa7

// by city name
// http://api.openweathermap.org/data/2.5/weather?q=brampton&appid=3451adb292f1b318e5c1bff378594fa7&units=metric

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public SQLiteDatabase db;

    DrawerLayout drawer;
    private MapView mapView;
    private GoogleMap gmap;
    private Button findButton, locateMe;
    private EditText postalCode;
    private String postalCodeValue = "";
    Handler handler;

    String postalCodeRegex = "^(?!.*[DFIOQU])[A-VXY][0-9][A-Z]?[0-9][A-Z][0-9]$";

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude = 0, longitude = 0;
    private Marker markerName = null;
    private Marker dummyMarker1 = null;
    private Marker dummyMarker2 = null;
    private Marker dummyMarker3 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(); // for weather thread

        // add toolbar layout to activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // for navigation drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        findButton = findViewById(R.id.findButton);
        findButton.setEnabled(false); // enable only when postal code is valid
        locateMe = findViewById(R.id.locateButton); // locate users location
        postalCode = findViewById(R.id.postalCodeField);
        Pattern pattern = Pattern.compile(postalCodeRegex);

        locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCurrentLocation();
            }
        });

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationFromPostCode(postalCodeValue);
            }
        });

        postalCode.addTextChangedListener(new TextWatcher() {
            @Override
            //                              CharSequence s, int start, int count, int after
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            //                        CharSequence s, int start, int before, int count
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                //showToast(charSequence.toString());
                // 6 digit postal code
                if (charSequence.length() > 5) {
                    Matcher matcher = pattern.matcher(charSequence.toString().toUpperCase());
                    if (matcher.matches()) {
                        postalCodeValue = charSequence.toString();
                        findButton.setEnabled(true);
                    } else {
                        findButton.setEnabled(false);
                    }

                } else {
                    findButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        weatherButton = findViewById(R.id.findButton);
//        cityNameField = findViewById(R.id.postalCodeField);
//
//        weatherButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String cityName = cityNameField.getText().toString();
//                if(!cityName.isEmpty()){
//                    //System.out.println(cityName);
//                    //OpenWeatherAPI.getJSON(MainActivity.this, cityName);
//                    updateWeatherData(cityName);
//                }
//            }
//        });

        MyDatabase dbHelper = new MyDatabase(this);
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM UserData WHERE USER_ID =" + 1, null);
            if (cursor != null) {
                showToast("User Data Available");
            } else {
                showToast("User Data not Available");
            }
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        } catch (SQLException e) {
            showToast("Database Unavailable");
        }

        setupLocationManager();

    } // onCreate ends

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_profile:
                showToast("Loading Profile Page");
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_interests:
                showToast("Loading Interests Page");
                intent = new Intent(this, InterestsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_news_feed:
                intent = new Intent(this, NewsFeedActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // the execution of networking related code should be in a Thread
    // otherwise an exception will be generated
//    private void updateWeatherData(final String city){
//        boolean waitForData = true;
//        new Thread(){
//            public void run(){
//                final JSONObject json = OpenWeatherAPI.getJSON(MainActivity.this, city);
//                if(json == null){
//                    handler.post(new Runnable(){
//                        public void run(){
//                            showToast("Invalid Response");
//                        }
//                    });
//                } else {
//                    handler.post(new Runnable(){
//                        public void run(){
//                            startWeatherActivity(json);
//                        }
//                    });
//                }
//            }
//        }.start();
//    }

//    void startWeatherActivity(JSONObject json){
//        Intent intent = new Intent(this, WeatherUpdateActivity.class);
//        intent.putExtra("json", json.toString());
//        startActivity(intent);
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        setupLocationManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        setupLocationManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;

        //gmap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        //gmap.setOnMyLocationClickListener(onMyLocationClickListener);
        enableMyLocationIfPermitted();

        gmap.setMinZoomPreference(2);
        //gmap.setIndoorEnabled(true);
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (gmap != null) {
            gmap.setMyLocationEnabled(true);
        }
    }

    private void showDefaultLocation() {
        Toast.makeText(this, "Location permission not granted, " +
                        "showing default location",
                Toast.LENGTH_SHORT).show();
        LatLng defaultLocation = new LatLng(43.666310, -79.733933);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocationIfPermitted();
                } else {
                    showDefaultLocation();
                }
                return;
            }
        }
    }

//    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
//            new GoogleMap.OnMyLocationButtonClickListener() {
//                @Override
//                public boolean onMyLocationButtonClick() {
//                    //gmap.setMinZoomPreference(15);
//                    return false;
//                }
//            };

//    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
//            new GoogleMap.OnMyLocationClickListener() {
//                @Override
//                public void onMyLocationClick(@NonNull Location location) {
//
//                    //gmap.setMinZoomPreference(12);
//
//                    CircleOptions circleOptions = new CircleOptions();
//                    circleOptions.center(new LatLng(location.getLatitude(),
//                            location.getLongitude()));
//
//                    circleOptions.radius(100);
//                    circleOptions.fillColor(Color.RED);
//                    circleOptions.strokeWidth(6);
//
//                    //gmap.addCircle(circleOptions);
//                }
//            };

    public void setupLocationManager(){
        locationListener = new MyLocListener();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        //criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        //criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            showToast("No Location Permission Granted");
        } else {
            locationManager.requestLocationUpdates(bestProvider, 5000, 0, locationListener);
        }
    }
    public class MyLocListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // save values of location to use in button click function
            // the values will be updated at set interval in requestLocationUpdates
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.e("Updated Lat", String.valueOf(latitude));
            Log.e("Updated Lon", String.valueOf(longitude));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    void updateCurrentLocation() {
        if(latitude != 0 && longitude != 0) {
            Log.e("Current Lat", String.valueOf(latitude));
            Log.e("Current Lon", String.valueOf(longitude));
            // Add a marker for current location
            // location manager will provide the current location
            LatLng marker = new LatLng(latitude, longitude);
            // the line is added to correct the map zoom issue
            // the zoom area was shifted from actual location
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker, 16);
            // this is to remove the previous marker for user's location
            // otherwise there will be multiple markers for one location
            if(markerName != null){
                markerName.remove();
            }
            markerName = gmap.addMarker(new MarkerOptions().position(marker).title("My Location"));
            //gmap.addMarker(new MarkerOptions().position(marker).title("My Location"));
            gmap.moveCamera(CameraUpdateFactory.newLatLng(marker));
            gmap.animateCamera(cameraUpdate, 2000, null);
        }
        else{
            //showToast("Current Location not accessed");
            //showDefaultLocation();
        }
    }

    public void locationFromPostCode(String postCode){
        double latitude_start, longitude_start;
        Location dummyLocation = new Location("service Provider");
        Geocoder geocoder1 = new Geocoder(this);
        try {
            List<Address> addresses1 = geocoder1.getFromLocationName(postCode, 1);
            if (addresses1 != null && !addresses1.isEmpty()) {
                Address address1 = addresses1.get(0);
                // get random location in radius (m)
                dummyLocation = getDummyLocation(address1.getLongitude(), address1.getLatitude(), 500);
                LatLng dummyUser1 = new LatLng(dummyLocation.getLatitude(), dummyLocation.getLongitude());
                // remove previously added marker
                if(dummyMarker1 != null){
                    dummyMarker1.remove();
                }
                dummyMarker1 = gmap.addMarker(new MarkerOptions().position(dummyUser1).title("User_1"));
                gmap.moveCamera(CameraUpdateFactory.newLatLng(dummyUser1));
                gmap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

                // get random location in radius (m)
                dummyLocation = getDummyLocation(address1.getLongitude(), address1.getLatitude(), 500);
                LatLng dummyUser2 = new LatLng(dummyLocation.getLatitude(), dummyLocation.getLongitude());
                // remove previously added marker
                if(dummyMarker2 != null){
                    dummyMarker2.remove();
                }
                dummyMarker2 = gmap.addMarker(new MarkerOptions().position(dummyUser2).title("User_2"));

                dummyLocation = getDummyLocation(address1.getLongitude(), address1.getLatitude(), 500);
                LatLng dummyUser3 = new LatLng(dummyLocation.getLatitude(), dummyLocation.getLongitude());
                // remove previously added marker
                if(dummyMarker3 != null){
                    dummyMarker3.remove();
                }
                dummyMarker3 = gmap.addMarker(new MarkerOptions().position(dummyUser3).title("User_3"));

            } else {
                // Display appropriate message when Geocoder services are not available
                Toast.makeText(this, "Unable to geocode zipcode", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // handle exception
        }
    }

    // to get dummy location for adding a marker for other users at address location
    // found by postal code
    public Location getDummyLocation(double x0, double y0, int radius) {
        Location dummyLocation = new Location("service Provider");
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);

        double foundLongitude = new_x + x0;
        double foundLatitude = y + y0;

        dummyLocation.setLatitude(foundLatitude);
        dummyLocation.setLongitude(foundLongitude);
        Log.e("Dummy Lat" , String.valueOf(dummyLocation.getLatitude()));
        Log.e("Dummy Long", String.valueOf(dummyLocation.getLongitude()));
        return dummyLocation;
    }
}