package com.cloudsoftware.sehtiapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class IMCActivity extends AppCompatActivity {

    SharedPreferences localdb ;

    SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imcactivity);

        ImageView image  = findViewById(R.id.image);
        TextView value = findViewById(R.id.imc_value);
        TextView tip = findViewById(R.id.imc_tip);

        FirebaseAuth Mauth=FirebaseAuth.getInstance();
       String uid=Mauth.getCurrentUser().getUid();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        localdb= getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor= localdb.edit();
        String gender=localdb.getString(uid+"gender","");
        boolean isFemale=localdb.getString(uid+"gender","female").equals("female");
        value.setText(localdb.getString(uid+"imc","0"));
        double imc=Double.parseDouble(localdb.getString(uid+"imc","0"));


        if(imc > 35){
            tip.setText("Extrêmement obèse");
            image.setImageResource(R.drawable.man_very_obese);
            if(isFemale)
                image.setImageResource(R.drawable.women_very_obese);
        }
        else if (imc >30){
            tip.setText("Obèse");
            image.setImageResource(R.drawable.man_obese);
            if(isFemale)
                image.setImageResource(R.drawable.women_obese);
        }
        else if(imc>25){
            tip.setText("surpoids ");
            image.setImageResource(R.drawable.man_overweight);
            if(isFemale)
                image.setImageResource(R.drawable.women_overweight);
        }
        else if(imc>18.5){
            tip.setText("Normale");
            image.setImageResource(R.drawable.man_normal);
            if(isFemale)
                image.setImageResource(R.drawable.women_normal);
        }else {
            tip.setText("insuffisance pondérale");
            image.setImageResource(R.drawable.man_underweight);
            if(isFemale)
                image.setImageResource(R.drawable.women_underweight);
        }










    }
}