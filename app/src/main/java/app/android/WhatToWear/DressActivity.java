package app.android.WhatToWear;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class DressActivity extends AppCompatActivity
{
    private FirebaseUser mUser;
    private StorageReference mStorage;
    private StorageReference Filepath;
    private DatabaseReference mDatabase;


    private static final int GALLERY_REQUEST_CODE=123;
    private ProgressDialog mProgress;


    private Spinner CategorySpinner;
    private Spinner ClimateSpinner;
    private Spinner TypeSpinner;
    private ArrayAdapter<CharSequence> category_adapter;
    private ArrayAdapter<CharSequence> climate_adapter;
    private ArrayAdapter<CharSequence> type_adapter;
    private ImageView ClothesImage;
    private TextInputLayout NameText;
    private Button AddItemButton;
    private CircleImageView circleImageView;

    private String imageUrl = "default";
    private String Category;
    private String CategoryPosition = "0";
    private String ClimatePosition ="0";
    private String TypesPosition = "0";
    private String Type;
    private String Climate;
    private String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dress);

        Toolbar mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Add a New Item To Your Wardrobe");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUser= FirebaseAuth.getInstance().getCurrentUser();
        String user_id = mUser.getUid();

        mStorage= FirebaseStorage.getInstance().getReference();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("WardRobe").child(user_id);
        mDatabase.keepSynced(true);


        CategorySpinner = findViewById(R.id.category_spinner);
        ClimateSpinner = findViewById(R.id.climate_spinner);
        TypeSpinner = findViewById(R.id.type_spinner);

        ClothesImage = findViewById(R.id.image_button);
        NameText = findViewById(R.id.name_EditText);
        AddItemButton = findViewById(R.id.add_item_button);
        circleImageView = findViewById(R.id.image_id);

        type_adapter = ArrayAdapter.createFromResource(DressActivity.this,R.array.types,android.R.layout.simple_spinner_item);
        type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        TypeSpinner.setAdapter(type_adapter);

        category_adapter = ArrayAdapter.createFromResource(DressActivity.this,R.array.categories,android.R.layout.simple_spinner_item);
        category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CategorySpinner.setAdapter(category_adapter);


        climate_adapter = ArrayAdapter.createFromResource(DressActivity.this,R.array.climate,android.R.layout.simple_spinner_item);
        climate_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ClimateSpinner.setAdapter(climate_adapter);

        ClothesImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(DressActivity.this);
            }
        });

        CategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryPosition = Integer.toString(position);
                Category = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        TypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypesPosition = Integer.toString(position);
                Type = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ClimateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ClimatePosition = Integer.toString(position);
                Climate = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Name = NameText.getEditText().getText().toString();
                if(!TextUtils.isEmpty(Name) || Name != null)
                {
                    mProgress=new ProgressDialog(DressActivity.this);
                    mProgress.setTitle("Adding New Item to WardRobe");
                    mProgress.setMessage("Please Wait....");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    HashMap<String,String> clothesMap = new HashMap<>();
                    clothesMap.put("name",Name);
                    clothesMap.put("image_url",imageUrl);
                    clothesMap.put("CategoryPosition",CategoryPosition);
                    clothesMap.put("category",Category);
                    clothesMap.put("type",Type);
                    clothesMap.put("TypesPosition",TypesPosition);
                    clothesMap.put("ClimatePosition",ClimatePosition);
                    clothesMap.put("climate",Climate);
                    Log.d("DOWNLOAD_URL",clothesMap.toString());
                    mDatabase.push().setValue(clothesMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mProgress.dismiss();
                                Intent Register_Intent = new Intent(DressActivity.this,WardRobeActivity.class);
                                Register_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(Register_Intent);
                                finish();
                            }
                        }
                    });


                }
                else {
                    Toast.makeText(DressActivity.this,"Please Give It a Name..",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mProgress=new ProgressDialog(DressActivity.this);
                mProgress.setTitle("Selecting The Image");
                mProgress.setMessage("Please Wait....");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                Uri resultUri = result.getUri();
                Name = NameText.getEditText().getText().toString();

                if(Name.equals(""))
                {
                    String word1 = Double.toString(Math.floor(Math.random()) * 255 + 1);
                    String word2 = Double.toString(Math.floor(Math.random()) * 255 + 1);
                    Name = word1 + word2;
                }

                Filepath = mStorage.child("clothes_images").child(Name+".jpg");

                Filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        imageUrl = uri.toString();

                                        Picasso.with(DressActivity.this)
                                                .load(imageUrl)
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .placeholder(R.drawable.hoodie)
                                                .into(circleImageView, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(DressActivity.this)
                                                                .load(imageUrl)
                                                                .placeholder(R.drawable.hoodie)
                                                                .into(circleImageView);
                                                    }
                                                });
                                        mProgress.dismiss();
                                        Log.d("DOWNLOAD_URL",imageUrl);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }

    }
}
