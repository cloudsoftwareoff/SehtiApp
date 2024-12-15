package com.cloudsoftware.sehtiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private Intent intent;
    private String uid,name,birth,weight,height,gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        String mode="new";
        if(getIntent().hasExtra("mode")){
            mode="update";}
        Button submit = findViewById(R.id.submit);
        EditText birth_edit=findViewById(R.id.birth_edit);
        EditText edit_name=findViewById(R.id.name_edit);
        EditText weight_edit=findViewById(R.id.weight_edit);
        EditText height_edit = findViewById(R.id.height_edit);

        RadioButton male = findViewById(R.id.radioButtonMale);
        RadioButton female= findViewById(R.id.radioButtonFemale);
        RadioButton nothing = findViewById(R.id.nothing);
        RadioButton sugar= findViewById(R.id.sugar);
        RadioButton tension = findViewById(R.id.tension);



        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Toast.makeText(SignupActivity.this, mode, Toast.LENGTH_SHORT).show();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (currentUser != null) {
            // User is already logged in
            uid=currentUser.getUid();
            final boolean[] datafound = {false};

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            String finalMode = mode;
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.hasChild("id")) {
                        if(finalMode.equals("update")){
                            datafound[0] =false;
                            edit_name.setText(snapshot.child("name").getValue(String.class));
                            birth_edit.setText(snapshot.child("birthday").getValue(String.class));
                            height_edit.setText(snapshot.child("height").getValue(String.class));
                            weight_edit.setText(snapshot.child("weight").getValue(String.class));
                            if(snapshot.child("gender").getValue(String.class).equals("female")){
                                female.setChecked(true);
                            }else {
                                male.setChecked(true);
                            }


                        }else{
                            if(!finalMode.equals("update")){
                     intent= new Intent(SignupActivity.this,HomeActivity.class);
                     startActivity(intent);
                     finish();
                     }}

                    }

                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                    builder.setTitle("Error")
                            .setMessage(error.getMessage())
                            .setPositiveButton("OK", null)
                            .show();

                }
            });




        submit.setOnClickListener(view -> {

            name=edit_name.getText().toString();
            birth=birth_edit.getText().toString().trim();
            weight=weight_edit.getText().toString().trim();
            height=height_edit.getText().toString().trim();

            if(birth.equals("") || birth.length()!= 4){
                birth_edit.setError("Saisir un anné valide");
                return;
            }
            if(edit_name.getText().toString().equals("")){
                edit_name.setError("Enter votre nom");
                return;
            }

            if(weight_edit.getText().toString().equals("")){
                weight_edit.setError("Type your weight");
                return;
            }
            if(height_edit.getText().toString().equals("")){
                height_edit.setError("Type your height");
                return;
            }

            if (male.isChecked()) {
                gender="male";
            } else if (female.isChecked()) {
                gender="female";
            } else {
                Toast.makeText(SignupActivity.this, "Select a gender", Toast.LENGTH_SHORT).show();
                return;
            }
            String illness="0";
            if (sugar.isChecked()) {
               illness="sugar";
            } else if (tension.isChecked()) {
               illness="tension";
            } else {
                illness="none";
            }
            HashMap<String, Object> data = new HashMap<>();
            data.put("id",uid);
            data.put("name",name);
            data.put("gender",gender);
            data.put("weight",weight);
            data.put("height",height);
            data.put("birthday",birth);
            data.put("illness",illness);
            // Update
            userRef.updateChildren(data)
                    .addOnSuccessListener(aVoid -> {
                        // Data updated successfully
                        Toast.makeText(SignupActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        intent=new Intent(this,HomeActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, e.toString(), Toast.LENGTH_SHORT).show());


        });

        } else {
            // User is not logged in
        intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        }

    }
}