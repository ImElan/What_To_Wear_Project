package app.android.WhatToWear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WardRobeActivity extends AppCompatActivity
{
    private FirebaseUser mUser;
    private DatabaseReference mWardrobe;
    private RecyclerView mRecycler;


    private FirebaseRecyclerAdapter<WardRobeClass,WardRobeHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ward_robe);


        mUser= FirebaseAuth.getInstance().getCurrentUser();
        String user_id = mUser.getUid();
        mWardrobe = FirebaseDatabase.getInstance().getReference().child("WardRobe").child(user_id);
        mWardrobe.keepSynced(true);

        Toolbar mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Your Wardrobe");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecycler = findViewById(R.id.wardrobe_recycler_id);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<WardRobeClass, WardRobeHolder>
                    (
                        WardRobeClass.class,
                        R.layout.single_dress_layout,
                        WardRobeHolder.class,
                        mWardrobe
                    )
        {
            @Override
            protected void populateViewHolder(final WardRobeHolder wardRobeHolder, WardRobeClass wardRobeClass, final int position) {
                    wardRobeHolder.setName(wardRobeClass.getName());
                    wardRobeHolder.setType(wardRobeClass.getType());
                    wardRobeHolder.setCategory(wardRobeClass.getCategory());
                    wardRobeHolder.setClimate(wardRobeClass.getClimate());
                    wardRobeHolder.setImage(wardRobeClass.getImage_url(),WardRobeActivity.this);

                    wardRobeHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String key = getRef(position).getKey();
//                            Toast.makeText(WardRobeActivity.this,key,Toast.LENGTH_SHORT).show();
                            Intent IndividualDressIntent = new Intent(WardRobeActivity.this,EditAndDeleteDressActivity.class);
                            IndividualDressIntent.putExtra("key",key);
                            startActivity(IndividualDressIntent);
                        }
                    });

                    wardRobeHolder.itemView.findViewById(R.id.item_delete_id).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String key = getRef(position).getKey();
                            AlertDialog.Builder builder = new AlertDialog.Builder(WardRobeActivity.this);
                            builder.setTitle("Delete Item")
                                    .setMessage("Are You Sure ?")
                                    .setNegativeButton("Cancel",null)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mWardrobe.child(key).removeValue();
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        }
                    });
            }

        };

        mRecycler.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.wardrobe_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.add_new_id)
        {
            Intent newDressIntent = new Intent(WardRobeActivity.this,DressActivity.class);
            startActivity(newDressIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent MainIntent = new Intent(WardRobeActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
