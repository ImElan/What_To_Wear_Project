package app.android.WhatToWear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity
{

    private ProgressDialog mLoginDialog;

    private FirebaseAuth mFireAuth;
    private DatabaseReference mUserDatabase;
    private Button LoginButton;
    private EditText EmailText;
    private EditText PasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFireAuth=FirebaseAuth.getInstance();
        mUserDatabase= FirebaseDatabase.getInstance().getReference();
        mUserDatabase.keepSynced(true);
        mLoginDialog=new ProgressDialog(this);

        EmailText = findViewById(R.id.email_id);
        PasswordText = findViewById(R.id.password_id);

        TextView SignUp = findViewById(R.id.sign_up_id);
        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RegisterIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(RegisterIntent);
            }
        });

        LoginButton = findViewById(R.id.sign_in);
        LoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String email = EmailText.getText().toString();
                String password = PasswordText.getText().toString();
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                {
                    mLoginDialog.setTitle("Logging in");
                    mLoginDialog.setMessage("Please wait while Logging in...!");
                    mLoginDialog.setCanceledOnTouchOutside(false);
                    mLoginDialog.show();
                    LoginMethod(email,password);
                }
                else {
                    Toast.makeText(LoginActivity.this,"Please Fill out All the required details..!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void LoginMethod(String email, String password)
    {
        mFireAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    mLoginDialog.dismiss();
                    Intent Main_intent = new Intent(LoginActivity.this,MainActivity.class);
                    Main_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(Main_intent);
                    finish();
                }
                else
                {
                    mLoginDialog.hide();
                    Toast.makeText(LoginActivity.this,"Cannot Sign in.Please check your email and password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
