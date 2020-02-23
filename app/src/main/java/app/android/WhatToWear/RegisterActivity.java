package app.android.WhatToWear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ProgressDialog mRegProgress;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText NameText;
    private EditText PasswordText;
    private EditText EmailText;
    private EditText PhoneText;
    private Button SignUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mRegProgress = new ProgressDialog(this);

        NameText = findViewById(R.id.name_id);
        EmailText = findViewById(R.id.email_id);
        PhoneText = findViewById(R.id.phone_id);
        PasswordText = findViewById(R.id.password_id);
        SignUpButton = findViewById(R.id.sign_up_button);

        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = EmailText.getText().toString();
                String name = NameText.getText().toString();
                String password = PasswordText.getText().toString();
                String phone = PhoneText.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password))
                {
                    mRegProgress.show();
                    mRegProgress.setTitle("Registering Your Account");
                    mRegProgress.setMessage("Please wait while we create you an account..!");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    RegisterUser(name,email, password,phone);
                }
                else {
                    Toast.makeText(RegisterActivity.this,"Please Fill out All the required details..!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void RegisterUser(final String name, final String email, String password, final String phone)
    {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser current_User= FirebaseAuth.getInstance().getCurrentUser();
                    String user_id = current_User.getUid();
                    String Device_token = FirebaseInstanceId.getInstance().getToken();
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                    mDatabase.keepSynced(true);
                    HashMap<String,String > userMap = new HashMap<>();
                    userMap.put("name",name);
                    userMap.put("email",email);
                    userMap.put("phone",phone);
                    userMap.put("device_token",Device_token);
                    userMap.put("location","empty");
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mRegProgress.dismiss();
                                Intent Register_Intent = new Intent(RegisterActivity.this,MainActivity.class);
                                Register_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(Register_Intent);
                                finish();
                            }
                        }
                    });
                }
                else
                {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"You have some problem with Signing up. Please Try Again Later",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
