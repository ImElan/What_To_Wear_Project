package app.android.WhatToWear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private Boolean mLocationPermissionsGranted = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private ProgressDialog mProgress;

    private RecyclerView mRecycler;
    private WardRobeAdapter wardRobeAdapter;
    private List<WardRobeClass> wardRobeList;

    // FIREBASE
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String user_id;

    private String Locationlatitude;
    private String Locationlongitude;
    private String main = "";
    private String description = "";
    private String temperature = "";
    private String humidity = "";
    private String name = "";
    private String windSpeed = "";

    private String keyToFilter = "";

    private TextView LocationText;
    private TextView ClimateText;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("SEQUENCE_CHECK","On create");

        Toolbar mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("What To Wear");

        LocationText = findViewById(R.id.location_id);
        ClimateText = findViewById(R.id.climate_id);

        mProgress = new ProgressDialog(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        wardRobeList = new ArrayList<>();
        mRecycler = findViewById(R.id.main_recycler);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
    }

    private void getLocationPermission() {
        Log.d("SEQUENCE_CHECK", "getLocationPermission: getting location permissions");
        String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                getDeviceLocation();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("SEQUENCE_CHECK", "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){

                            mProgress.setTitle("Determining the Clothes based on your location");
                            mProgress.setMessage("Please wait while we search through your wardrobe ..!");
                            mProgress.setCanceledOnTouchOutside(false);
                            mProgress.show();

                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            FirebaseUser current_User= FirebaseAuth.getInstance().getCurrentUser();
                            user_id = current_User.getUid();
                            Log.d(TAG, "onComplete: "+currentLocation);
                            if(currentLocation != null)
                            {
                                Log.d(TAG, "UPDATE METHOD FIRST CALL: ");
                                UpdateClothesBasedOnLocation(currentLocation.getLatitude(),currentLocation.getLongitude());
                                mDatabase.child("Users").child(user_id).child("location").setValue("Filled");
                            }
                            else {
//                                mProgress.dismiss();
                                Log.d("SEQUENCE_CHECK", "onComplete: ");
                                onResume();
                            }

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void UpdateClothesBasedOnLocation(double latitude, double longitude) {
        Log.d(TAG, "UpdateClothesBasedOnLocation: ");
        HashMap<String,Double> locationMap = new HashMap<>();
        locationMap.put("latitude",latitude);
        locationMap.put("longitude",longitude);
        Log.d("LAT_LNG","Latitude:"+latitude+" Longitude:"+longitude);
        mDatabase.child("Users").child(user_id).child("LatitudeAndLongitude").setValue(locationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("LAT_LNG","Successfully Updated");
                mDatabase.child("Users").child(user_id).child("LatitudeAndLongitude").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Locationlatitude = dataSnapshot.child("latitude").getValue().toString();
                        Locationlongitude = dataSnapshot.child("longitude").getValue().toString();
                        Log.d("LAT_LNG_CHECK","Latitude:"+Locationlatitude+" Longitude:"+Locationlongitude);

                        getWeatherData(Locationlatitude,Locationlongitude);

                        HashMap<String,String> climateMap = new HashMap<>();
                        climateMap.put("main",main);
                        climateMap.put("description",description);
                        climateMap.put("humidity",humidity);
                        climateMap.put("windSpeed",windSpeed);
                        climateMap.put("temperature",temperature);
                        climateMap.put("place",name);

                        mDatabase.child("Climate").child(user_id).setValue(climateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // FETCH AND DISPLAY THE CLOTHES BASED ON CLIMATE
                                mDatabase.child("Climate").child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String climate = dataSnapshot.child("main").getValue().toString();
                                        climate = climate.toLowerCase();
                                        switch (climate)
                                        {
                                            case "rain" :
                                                keyToFilter = "Rainy";
                                                break;
                                            case "clouds" :
                                                keyToFilter = "Cloudy";
                                                break;
                                            case "clear" :
                                                keyToFilter = "Clear Sky";
                                                break;
                                            default:
                                                keyToFilter = "Sunny";
                                                break;
                                        }
                                        LocationText.setText(name);
                                        ClimateText.setText(keyToFilter);
                                        Log.d("FOUND_CLIMATE",climate);
                                        mDatabase.child("WardRobe").child(user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                wardRobeList.clear();
                                                Log.d("FOUND_CLIMATE","Preparing to display the list....");

                                                for(DataSnapshot ds : dataSnapshot.getChildren())
                                                {
                                                    // TO DISPLAY ALL
                                                                           /* WardRobeClass singleDress = ds.getValue(WardRobeClass.class);
                                                                            wardRobeList.add(singleDress);
                                                                            wardRobeAdapter = new WardRobeAdapter(MainActivity.this,wardRobeList);
                                                                            mRecycler.setAdapter(wardRobeAdapter); */

                                                    // FILTERING BASED ON CLIMATE
                                                    WardRobeClass singleDress = ds.getValue(WardRobeClass.class);
                                                    String currentClimate = singleDress.getClimate();
                                                    if(currentClimate.equals(keyToFilter))
                                                    {
                                                        wardRobeList.add(singleDress);
                                                        wardRobeAdapter = new WardRobeAdapter(MainActivity.this,wardRobeList);
                                                        mRecycler.setAdapter(wardRobeAdapter);
                                                    }

                                                }
                                                mProgress.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                        //Log.d("RETRIEVED_DATA",main);
                        //Log.d("RETRIEVED_DATA",description);
                        //Log.d("RETRIEVED_DATA",temperature);
                        //Log.d("RETRIEVED_DATA",humidity);
                        //Log.d("RETRIEVED_DATA",windSpeed);
                        //Log.d("RETRIEVED_DATA",name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void CheckForLocationChange()
    {
        mDatabase.child("Users").child(user_id).child("LatitudeAndLongitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgress.setTitle("Determining the Clothes based on your location");
                mProgress.setMessage("Please wait while we search through your wardrobe ..!");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                Locationlatitude = dataSnapshot.child("latitude").getValue().toString();
                Locationlongitude = dataSnapshot.child("longitude").getValue().toString();
                Log.d("LAT_LNG_CHECK","Latitude:"+Locationlatitude+" Longitude:"+Locationlongitude);

                getWeatherData(Locationlatitude,Locationlongitude);

                Map climateMap = new HashMap<>();
                climateMap.put("main",main);
                climateMap.put("description",description);
                climateMap.put("humidity",humidity);
                climateMap.put("windSpeed",windSpeed);
                climateMap.put("temperature",temperature);
                climateMap.put("place",name);

                mDatabase.child("Climate").child(user_id).updateChildren(climateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // FETCH AND DISPLAY THE CLOTHES BASED ON CLIMATE
                        mDatabase.child("Climate").child(user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String climate = dataSnapshot.child("main").getValue().toString();
                                climate = climate.toLowerCase();
                                switch (climate)
                                {
                                    case "rain" :
                                        keyToFilter = "Rainy";
                                        break;
                                    case "clouds" :
                                        keyToFilter = "Cloudy";
                                        break;
                                    case "clear" :
                                        keyToFilter = "Clear Sky";
                                        break;
                                    default:
                                        keyToFilter = "Sunny";
                                        break;
                                }
                                LocationText.setText(name);
                                ClimateText.setText(keyToFilter);
                                Log.d("FOUND_CLIMATE",climate);
                                mDatabase.child("WardRobe").child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        wardRobeList.clear();
                                        Log.d("FOUND_CLIMATE","Preparing to display the list....");

                                        for(DataSnapshot ds : dataSnapshot.getChildren())
                                        {
                                            // TO DISPLAY ALL
                                                                           /* WardRobeClass singleDress = ds.getValue(WardRobeClass.class);
                                                                            wardRobeList.add(singleDress);
                                                                            wardRobeAdapter = new WardRobeAdapter(MainActivity.this,wardRobeList);
                                                                            mRecycler.setAdapter(wardRobeAdapter); */

                                            // FILTERING BASED ON CLIMATE
                                            WardRobeClass singleDress = ds.getValue(WardRobeClass.class);
                                            String currentClimate = singleDress.getClimate();
                                            if(currentClimate.equals(keyToFilter))
                                            {
                                                wardRobeList.add(singleDress);
                                                wardRobeAdapter = new WardRobeAdapter(MainActivity.this,wardRobeList);
                                                mRecycler.setAdapter(wardRobeAdapter);
                                            }

                                        }
                                        mProgress.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                //Log.d("RETRIEVED_DATA",main);
                //Log.d("RETRIEVED_DATA",description);
                //Log.d("RETRIEVED_DATA",temperature);
                //Log.d("RETRIEVED_DATA",humidity);
                //Log.d("RETRIEVED_DATA",windSpeed);
                //Log.d("RETRIEVED_DATA",name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getWeatherData(String latitude,String longitude)
    {
        String content;
        Weather weather = new Weather();
        try {
            content = weather.execute("https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=438e0785a36d0ea3ee9f736003f1b382").get();
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            JSONArray jsonArray = new JSONArray(weatherData);
            JSONObject weatherPart = jsonArray.getJSONObject(0);
            main = weatherPart.getString("main");
            description = weatherPart.getString("description");
            JSONObject mainData = jsonObject.getJSONObject("main");
            temperature = mainData.getString("temp");
            humidity = mainData.getString("humidity");
            name = jsonObject.getString("name");
            JSONObject windData = jsonObject.getJSONObject("wind");
            windSpeed = windData.getString("speed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();
        Log.d("SEQUENCE_CHECK","On start");
        if(mCurrentUser == null)
        {
            Intent LoginIntent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(LoginIntent);
            finish();
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id)
        {
            case R.id.location_id:
                if(isServicesOK())
                {
                    Intent MapIntent = new Intent(MainActivity.this,MapActivity.class);
                    startActivity(MapIntent);
                    break;
                }
            case R.id.wardrobe_id:
                    Intent WardRobeIntent = new Intent(MainActivity.this,WardRobeActivity.class);
                    startActivity(WardRobeIntent);
                    break;
            case R.id.weather_id:
                Intent WeatherIntent = new Intent(MainActivity.this,WeatherIntent.class);
                startActivity(WeatherIntent);
                break;
            case R.id.sign_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Sign Out")
                        .setMessage("Are You Sure ?")
                        .setNegativeButton("Cancel",null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Intent LoginIntent = new Intent(MainActivity.this,LoginActivity.class);
                                startActivity(LoginIntent);
                                finish();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    static class Weather extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... address) {
            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                //retrieve data from url
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                //Retrieve data and return it as String
                int data = isr.read();
                String content = "";
                char ch;
                while (data != -1){
                    ch = (char) data;
                    content = content + ch;
                    data = isr.read();
                }
                return content;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, Please Enable it.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SEQUENCE_CHECK", "onActivityResult: called.");
        Log.d("SEQUENCE_CHECK",mLocationPermissionsGranted+"");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionsGranted){
                    getDeviceLocation();
                }
                else {
                    getLocationPermission();
                }
            }
        }

    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Method Call");
        Log.d("SEQUENCE_CHECK","On resume");
        if(checkMapServices()){
            if(mLocationPermissionsGranted){
                getDeviceLocation();
            }
            else{
                getLocationPermission();
            }
        }
    }

//    private void PermissionCheck()
//    {
//
//    }

}
