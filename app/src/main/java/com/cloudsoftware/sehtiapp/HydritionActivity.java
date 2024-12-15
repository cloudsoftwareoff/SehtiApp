package com.cloudsoftware.sehtiapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class HydritionActivity extends AppCompatActivity {
    SharedPreferences localdb ;
    SharedPreferences.Editor editor ;
    int water_taken=0;
    private   ProgressBar progressBar;
    int max=100;
    private  TextView water_intake;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydrition);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        FirebaseAuth Mauth=FirebaseAuth.getInstance();
        uid=Mauth.getCurrentUser().getUid();


        progressBar=findViewById(R.id.progressBar);
        LinearLayout coffe=findViewById(R.id.coffee);
        LinearLayout juice=findViewById(R.id.juice);
        LinearLayout tea=findViewById(R.id.tea);
        LinearLayout glass=findViewById(R.id.glass);
        LinearLayout bottle=findViewById(R.id.bottle);
       water_intake=findViewById(R.id.water_intake);


        localdb= getSharedPreferences(uid+"MyPreferences", Context.MODE_PRIVATE);
        editor= localdb.edit();

        progressBar.setOnClickListener(view -> {
            updateGoal();
        });


        if(!localdb.contains("water_goal")) {
           //
            updateGoal();
        }else{
            if(localdb.contains("water_taken")){
                water_taken=Integer.parseInt(localdb.getString("water_taken",""));
                progressBar.setProgress(water_taken);
                updateUI(water_taken);

            }

                max=1000*Integer.parseInt(localdb.getString("water_goal",""));
                progressBar.setMax(max);


                coffe.setOnClickListener(view -> {
                     updateUI(200);
                });
            tea.setOnClickListener(view -> {
                updateUI(100);
            });
            juice.setOnClickListener(view -> {
                updateUI(400);
            });
            glass.setOnClickListener(view -> {
                updateUI(250);
            });
            bottle.setOnClickListener(view -> {
                updateUI(500);
            });




        }

    }
    public void updateUI(int progress){
        progressBar.setProgress(progress);

        water_taken+=progress;
        editor.putString("water_taken", String.valueOf(progress));
        editor.apply();
        water_intake.setText(String.valueOf(water_taken)+" mL");


    }
    public void updateGoal(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("objectif de consommation");
        builder.setMessage("Entrez votre objectif de consommation d'eau quotidienne en L");

        // Set up the input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the positive button
        builder.setPositiveButton("Fixer l'objectif", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String goalText = input.getText().toString();

                editor.putString("water_goal", goalText);
                editor.apply();

                max=1000*Integer.parseInt(goalText);
                progressBar.setMax(max);

                Toast.makeText(HydritionActivity.this, "Objectif enregistré", Toast.LENGTH_SHORT).show();
            }
        });


        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}