package com.cloudsoftware.sehtiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthActivity extends AppCompatActivity {
    private boolean newuser;
    private ProgressDialog progressDialog;

    String email,password;
    private EditText emailtxt,passtxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Button actionbtn = findViewById(R.id.auth_action);
        emailtxt=findViewById(R.id.email_edit);
        passtxt=findViewById(R.id.password_edit);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        if (action.equals("signup")) {
            newuser = true;
            actionbtn.setText("Créer un compte");
        }
        else newuser = false;

        // Athentication
        actionbtn.setOnClickListener(view -> {
            email= emailtxt.getText().toString().trim();
            password=passtxt.getText().toString().trim();
            if(email.equals("")){
                emailtxt.setError(" ");
                return;
            }

            if(password.equals(" ")) {
            passtxt.setError(" ");
            return;
            }

            // Sign up
            if(newuser){
                progressDialog = new ProgressDialog(AuthActivity.this);
                progressDialog.setMessage("Signing up...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Sign up success,

                                progressDialog.dismiss();
                                Intent  intent1 = new Intent(AuthActivity.this,SignupActivity.class);
                                startActivity(intent1);
                                finish();

                            } else {
                                progressDialog.dismiss();
                                String errorMessage = task.getException().getMessage();
                                AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
                                builder.setTitle("Error")
                                        .setMessage(errorMessage)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }


                            });


            }
            // Login
            else {
                progressDialog = new ProgressDialog(AuthActivity.this);
                progressDialog.setMessage("Loging in ...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Sign up success,
                                FirebaseUser user = mAuth.getCurrentUser();
                                progressDialog.dismiss();
                             Intent  intent1 = new Intent(this,SignupActivity.class);
                               startActivity(intent1);
                               finish();
                            } else {
                                progressDialog.dismiss();
                                String errorMessage = task.getException().getMessage();
                                AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
                                builder.setTitle("Error")
                                        .setMessage(errorMessage)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });

            }

        });
    }
}