package app.android.WhatToWear;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;

public class WeatherIntent extends AppCompatActivity
{
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseRef;

    private TextView mainText;
    private TextView humidityText;
    private TextView windSpeedText;
    private TextView PlaceText;
    private TextView TemperatureText;
    private TextView DescriptionText;
    private LinearLayout mLinearLayout;

    private String main;
    private String description;
    private String temperature;
    private String humidity;
    private String name;
    private String windSpeed;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_intent);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = mUser.getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("Climate").child(user_id).keepSynced(true);

        mainText = findViewById(R.id.main);
        humidityText = findViewById(R.id.humidity);
        TemperatureText = findViewById(R.id.temp);
        windSpeedText = findViewById(R.id.wind_speed);
        DescriptionText = findViewById(R.id.description);
        PlaceText = findViewById(R.id.place);

        mLinearLayout = findViewById(R.id.main_layout);

        mDatabaseRef.child("Climate").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                main = dataSnapshot.child("main").getValue().toString();
                description = dataSnapshot.child("description").getValue().toString();
                temperature = dataSnapshot.child("temperature").getValue().toString();
                humidity = dataSnapshot.child("humidity").getValue().toString();
                name = dataSnapshot.child("place").getValue().toString();
                windSpeed = dataSnapshot.child("windSpeed").getValue().toString();

                main = main.toLowerCase();
                switch (main)
                {
                    case "clouds" :
                        mLinearLayout.setBackground(getDrawable(R.drawable.cloudy));
                        break;
                    case "rain" :
                        mLinearLayout.setBackground(getDrawable(R.drawable.rainy));
                        break;
                    case "clear":
                        mLinearLayout.setBackground(getDrawable(R.drawable.clear));
                        break;
                    default:
                        mLinearLayout.setBackground(getDrawable(R.drawable.sunny));
                        break;
                }
                if(main.length() > 0)
                {
                    main = main.substring(0,1).toUpperCase() + main.substring(1);
                }
                mainText.setText(main);
                humidityText.setText(humidity);
                DescriptionText.setText(description);
                windSpeedText.setText(windSpeed);
                PlaceText.setText(name);

                if(!temperature.isEmpty())
                {
                    Double Doubletemperature = Double.parseDouble(temperature) - 273.15;

                    DecimalFormat precision = new DecimalFormat("0.000");
                    Doubletemperature = Double.parseDouble(precision.format(Doubletemperature));

                    TemperatureText.setText(""+Doubletemperature);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

}
