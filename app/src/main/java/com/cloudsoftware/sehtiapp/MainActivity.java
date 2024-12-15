package com.cloudsoftware.sehtiapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Intent intent;
    private Button login,signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login=findViewById(R.id.login);
        signup=findViewById(R.id.signup);
        FirebaseUser currentUser =  FirebaseAuth.getInstance().getCurrentUser();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if(currentUser!=null) {
        intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        finish();
        }
        signup.setOnClickListener(view -> {

            intent = new Intent(this,AuthActivity.class);
            intent.putExtra("action","signup");
            startActivity(intent);
            finish();
        });


        login.setOnClickListener(view -> {

            intent = new Intent(this,AuthActivity.class);
            intent.putExtra("action","login");

            startActivity(intent);
        });


    }
}